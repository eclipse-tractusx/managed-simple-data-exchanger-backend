package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.tractusx.sde.agent.entity.SftpSchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SftpReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SftpReportMapper;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.model.SftpReportModel;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class SchedulerService implements Runnable {
    private SftpConfigModel sftpConfigModel;

    private CsvHandlerService csvHandlerService;

    private SftpReportMapper sftpReportMapper;

    private FTPClient ftpsClient;

    private FtpsService ftpsService;

    private ProcessReportRepository processReportRepository;

    private SftpReportRepository sftpReportRepository;

    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;

    private TaskScheduler taskScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MetadataProvider metadataProvider;

    private SubmodelOrchestartorService submodelOrchestartorService;

    @Autowired
    public SchedulerService(SftpConfigModel sftpConfigModel,
                            CsvHandlerService csvHandlerService,
                            SftpReportMapper sftpReportMapper,
                            FTPClient ftpsClient,
                            FtpsService ftpsService,
                            ProcessReportRepository processReportRepository,
                            SftpReportRepository sftpReportRepository,
                            SubmodelOrchestartorService submodelOrchestartorService,
                            MetadataProvider metadataProvider) {
        this.sftpConfigModel = sftpConfigModel;
        this.csvHandlerService = csvHandlerService;
        this.sftpReportMapper = sftpReportMapper;
        this.ftpsClient = ftpsClient;
        this.ftpsService = ftpsService;
        this.processReportRepository = processReportRepository;
        this.sftpReportRepository = sftpReportRepository;
        this.submodelOrchestartorService = submodelOrchestartorService;
        this.metadataProvider = metadataProvider;
    }

    @PostConstruct
    public void initialiseScheduler() {
        // 1. Read Scheduler config from the DB and then apply
        this.scheduleJob("0/20 * * * * *");
    }

    public void scheduleJob(String cronExpression) {
        // 1. Check if there is any previously scheduled job and stop that
        if(this.taskScheduler == null) {
            this.taskScheduler = new ConcurrentTaskScheduler();
        }
        if(this.scheduledFuture != null) {
            this.scheduledFuture.cancel(false);
        }
        this.scheduledFuture = this.taskScheduler.schedule(this, new CronTrigger(cronExpression));
    }


    @SneakyThrows
    @Override
    public void run() {
        // Connect to FTPS server
        // Process files
        // Disconnect FTPS server
        log.info("Scheduler started");
        String schedulerId = UUID.randomUUID().toString();
        try {
            if (ftpsService.connectFtpsClient()) {
                FTPFile[] files = ftpsClient.listFiles(sftpConfigModel.getToBeProcessedLocation());
                log.info("Number of files: " + files.length);
                for (FTPFile file : files) {
                    // 1. Send file to DFT backend
                    String filePath = sftpConfigModel.getToBeProcessedLocation() + "/" +file.getName();
                    log.info("File content type:" +Files.probeContentType(Paths.get(filePath)));
                    //MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), Files.probeContentType(Paths.get(filePath)),
                    //        new FileInputStream(new File(filePath)));
                    String processId = UUID.randomUUID().toString();
                    try {
                        //processId = csvHandlerService.storeFile(multipartFile);
                        log.info("File UUID: "+processId);
                        JsonNode jsonNode = metadataProvider.getMetadata();
                        SubmodelFileRequest submodelFileRequest = objectMapper.convertValue(metadataProvider.getMetadata(), SubmodelFileRequest.class);
                        submodelOrchestartorService.processSubmodelAutomationCsv(submodelFileRequest, processId);
                        // **********move file to inprogress directory and make entry in the DB
                        ftpsClient.rename(sftpConfigModel.getToBeProcessedLocation()+"/"+file.getName(),
                                sftpConfigModel.getInProgressLocation()+"/"+file.getName());
                        SftpReportModel reportModel = SftpReportModel.builder()
                                .schedulerId(schedulerId)
                                .processId(processId)
                                .fileName(file.getName())
                                .status(SftpReportStatusEnum.IN_PROGRESS)
                                .startDate(LocalDateTime.now())
                                .build();
                        saveSftpSchedulerReport(reportModel);
                    } catch(Exception e) {
                        log.info("Message: "+e.getMessage());
                        log.info("Exception occurred while doing the csv validation check for "+ file.getName());
                        // **********move file to failed directory and make entry in the DB
                        ftpsClient.rename(sftpConfigModel.getToBeProcessedLocation()+"/"+file.getName(),
                                sftpConfigModel.getFailedLocation()+"/"+file.getName());
                        SftpReportModel reportModel = SftpReportModel.builder()
                                .schedulerId(schedulerId)
                                .processId(processId)
                                .fileName(file.getName())
                                .status(SftpReportStatusEnum.FAILED)
                                .startDate(LocalDateTime.now())
                                .build();
                        saveSftpSchedulerReport(reportModel);
                    }
                }

                // Check for the file status of In-progress files
                checkStatusOfInprogressFiles();
            } else {
                log.info("FTP login failure");
                // Send notification of failure
            }

            //Thread.sleep(5000);
            log.info("Scheduler stopped");
        } catch(Exception e) {
            log.info("Exception in the scheduler: "+ e.getMessage());
        } finally {
            ftpsService.disconnectFtpClient();
        }
    }

    public void saveSftpSchedulerReport(SftpReportModel reportModel) {
        sftpReportRepository.save(sftpReportMapper.mapFrom(reportModel));
    }

    public void checkStatusOfInprogressFiles() throws IOException {
        List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findByStatus(
                SftpReportStatusEnum.IN_PROGRESS
        );
        if(!sftpReportList.isEmpty()) {
            for(SftpSchedulerReport sftpSchedulerReport : sftpReportList) {
                // get status from process report
                Optional<ProcessReportEntity> processReport = processReportRepository.findByProcessIdAndStatus(
                        sftpSchedulerReport.getProcessId(),
                        ProgressStatusEnum.COMPLETED
                );
                sftpSchedulerReport.setNumberOfSucceededItems(
                        processReport.get().getNumberOfSucceededItems() + processReport.get().getNumberOfUpdatedItems()
                );
                sftpSchedulerReport.setNumberOfFailedItems(processReport.get().getNumberOfFailedItems());
                if(processReport.get().getNumberOfItems() ==
                        (processReport.get().getNumberOfSucceededItems() + processReport.get().getNumberOfUpdatedItems())) {
                    // Move to success
                    ftpsClient.rename(sftpConfigModel.getInProgressLocation()+"/"+sftpSchedulerReport.getFileName(),
                            sftpConfigModel.getSuccessLocation()+"/"+sftpSchedulerReport.getFileName());
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.SUCCESS);
                    sftpReportRepository.save(sftpSchedulerReport);
                } else if(processReport.get().getNumberOfSucceededItems() > 0 ||
                        processReport.get().getNumberOfUpdatedItems() > 0) {
                    // Move to partial success
                    ftpsClient.rename(sftpConfigModel.getInProgressLocation()+"/"+sftpSchedulerReport.getFileName(),
                            sftpConfigModel.getPartialSuccessLocation()+"/"+sftpSchedulerReport.getFileName());
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.PARTIAL_SUCCESS);
                    sftpReportRepository.save(sftpSchedulerReport);
                } else {
                    // Move to failed
                    ftpsClient.rename(sftpConfigModel.getInProgressLocation()+"/"+sftpSchedulerReport.getFileName(),
                            sftpConfigModel.getFailedLocation()+"/"+sftpSchedulerReport.getFileName());
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.FAILED);
                    sftpReportRepository.save(sftpSchedulerReport);
                }
            }
        }
        //processReportRepository.findByProcessId("234");
    }
}
