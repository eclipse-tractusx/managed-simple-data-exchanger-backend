package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.entity.SftpSchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SftpReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SftpReportMapper;
import org.eclipse.tractusx.sde.agent.model.SftpReportModel;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.eclipse.tractusx.sde.core.utils.TryUtils.IGNORE;
import static org.eclipse.tractusx.sde.core.utils.TryUtils.tryRun;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessRemoteCsv {

    private final CsvHandlerService csvHandlerService;
    private final MetadataProvider metadataProvider;
    private final RetrieverFactory retrieverFactory;
    private final SubmodelOrchestartorService submodelOrchestartorService;
    private final SftpReportRepository sftpReportRepository;
    private final ProcessReportRepository processReportRepository;
    private final SftpReportMapper sftpReportMapper;
    private final ObjectFactory<ProcessRemoteCsv> selfFactory;


    private final ObjectMapper objectMapper = new ObjectMapper();



    @SuppressWarnings({"CallToPrintStackTrace","ResultOfMethodCallIgnored"})
    public void process(TaskScheduler taskScheduler) throws JsonProcessingException {
        log.info("Scheduler started");
        var submodelFileRequest = objectMapper.convertValue(metadataProvider.getMetadata(), SubmodelFileRequest.class);
        var schedulerId = UUID.randomUUID().toString();
        boolean loginSuccess = false;
        try (var retriever = retrieverFactory.create()) {
            loginSuccess = true;
            var inProgress = StreamSupport.stream(retriever.spliterator(), false)
                    .filter(processId -> tryRun(
                            () -> retriever.setProgress(processId),
                            e -> {
                                log.info("Could not move remote file to the Progress folder {}", retriever.getFileName(processId));
                                Paths.get(csvHandlerService.getFilePath(processId)).toFile().delete();
                            })
                    ).filter(processId -> tryRun(
                            () -> submodelOrchestartorService.processSubmodelAutomationCsv(submodelFileRequest, processId),
                            e -> {
                                log.info("Could not submit CVS file for processing. {}", csvHandlerService.getFilePath(processId));
                                tryRun( () -> retriever.setFailed(processId),
                                        e1 -> log.info("Could not move file to the Failed folder {}", retriever.getFileName(processId))
                                );
                            })
                    ).peek(processId -> sftpReportRepository.save(
                            sftpReportMapper.mapFrom(
                                    SftpReportModel.builder()
                                            .schedulerId(schedulerId)
                                            .processId(processId)
                                            .fileName(retriever.getFileName(processId))
                                            .status(SftpReportStatusEnum.IN_PROGRESS)
                                            .startDate(LocalDateTime.now())
                                            .build()
                            ))
                    ).toList();
            taskScheduler.schedule(() -> checkStatusOfInprogressFiles(taskScheduler, retriever, inProgress), Instant.now().plus(Duration.ofSeconds(5)));
        } catch (Exception e) {
            if (!loginSuccess) {
                log.info("Possible wrong credentials");
            }
            e.printStackTrace();
        }
    }

    public void checkStatusOfInprogressFiles(TaskScheduler taskScheduler, RetrieverI retriever, List<String> inProgress) {
        if (processReportRepository.countByProcessIdInAndStatus(inProgress, ProgressStatusEnum.COMPLETED) != inProgress.size()) {
            taskScheduler.schedule(() -> checkStatusOfInprogressFiles(taskScheduler, retriever, inProgress), Instant.now().plus(Duration.ofSeconds(5)));
        } else {
            selfFactory.getObject().createDbReport(retriever, inProgress).forEach(Runnable::run);
            tryRun(retriever::close, IGNORE());
        }
    }

    /***
     * Method does not close passed retriever
     * @param retriever is a RetrieverI service
     * @param completed is a List of completed ProcessId
     * @return List of actions (side effect functions) which move remote files to the appropriate locations
     */
    @Transactional
    public List<Runnable> createDbReport(RetrieverI retriever, List<String> completed) {
        List<Runnable> remoteActions = new ArrayList<>();
        List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findByProcessIdIn(completed);
        var processReportMap = processReportRepository.findByProcessIdIn(completed).stream().collect(Collectors.toMap(ProcessReportEntity::getProcessId, Function.identity()));
        for (var sftpSchedulerReport : sftpReportList) {
            final var processId = sftpSchedulerReport.getProcessId();
            final var processReport = processReportMap.get(processId);
            final var numberOfSucceededItems = processReport.getNumberOfSucceededItems() + processReport.getNumberOfUpdatedItems();
            final var numberOfFailedItems = processReport.getNumberOfFailedItems();
            sftpSchedulerReport.setNumberOfSucceededItems(numberOfSucceededItems);
            sftpSchedulerReport.setNumberOfFailedItems(numberOfFailedItems);
            if (processReport.getNumberOfItems() == numberOfSucceededItems) {
                sftpSchedulerReport.setStatus(SftpReportStatusEnum.SUCCESS);
                remoteActions.add(() -> tryRun(
                        () -> retriever.setSuccess(processId),
                        e -> log.info("Could not move file {} to Success Folder", retriever.getFileName(processId))
                ));
            } else if (numberOfSucceededItems > 0) {
                sftpSchedulerReport.setStatus(SftpReportStatusEnum.PARTIAL_SUCCESS);
                remoteActions.add(() -> tryRun(
                        () -> retriever.setPartial(processId),
                        e -> log.info("Could not move file {} to Partial Success Folder", retriever.getFileName(processId))
                ));
            } else {
                sftpSchedulerReport.setStatus(SftpReportStatusEnum.FAILED);
                remoteActions.add(() -> tryRun(
                        () -> retriever.setFailed(processId),
                        e -> log.info("Could not move file {} to Failed Folder", retriever.getFileName(processId))
                ));
            }
        }
        return remoteActions;
    }

}
