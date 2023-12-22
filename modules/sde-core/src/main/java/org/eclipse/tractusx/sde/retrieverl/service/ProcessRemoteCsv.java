/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.retrieverl.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.entity.SchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SchedulerReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SchedulerReportMapper;
import org.eclipse.tractusx.sde.agent.model.ActiveStorageMedia;
import org.eclipse.tractusx.sde.agent.model.SchedulerReportModel;
import org.eclipse.tractusx.sde.agent.repository.SchedulerReportRepository;
import org.eclipse.tractusx.sde.common.ConfigurableFactory;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.eclipse.tractusx.sde.retrieverl.RetrieverI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.eclipse.tractusx.sde.common.utils.TryUtils.tryRun;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessRemoteCsv {
	private final CsvHandlerService csvHandlerService;
	private final PolicyProvider policyProvider;
	private final SubmodelOrchestartorService submodelOrchestartorService;
	private final SchedulerReportRepository sftpReportRepository;
	private final ProcessReportRepository processReportRepository;
	private final SchedulerReportMapper sftpReportMapper;
	private final ObjectFactory<ProcessRemoteCsv> selfFactory;
	private final JobMaintenanceConfigService jobMaintenanceConfigService;
	private final ActiveStorageMediaProvider activeStorageMediaProvider;

	private final AutoUploadNotificationTask notificationTask;
	private final ApplicationContext applicationContext;

	@SneakyThrows
	public String process(TaskScheduler taskScheduler, String schedulerUuid) {
		log.info("Scheduler started " + schedulerUuid);

		ActiveStorageMedia media =  activeStorageMediaProvider.getConfiguration();
		if(Optional.ofNullable(media).isEmpty())
			throw new ValidationException("No active storage media found");

		String activeStorageMedia = media.getName().toLowerCase();
		@SuppressWarnings("unchecked")
		var retrieverFactory = (ConfigurableFactory<RetrieverI>)applicationContext.getBean(activeStorageMedia);

		if(Optional.ofNullable(retrieverFactory).isEmpty())
			throw new ValidationException(activeStorageMedia + " not supported");

		String msg = null;
		SchedulerReport schedulerTrigger = sftpReportRepository.save(sftpReportMapper
				.mapFrom(SchedulerReportModel.builder().schedulerId(schedulerUuid).processId(schedulerUuid)
						.status(SchedulerReportStatusEnum.IN_PROGRESS).startDate(LocalDateTime.now()).build()));

		try {
			int statusVerifyRetry = 3;
			Callable<RetrieverI> collableThread = retrieverFactory::create;
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Future<RetrieverI> future = executorService.submit(collableThread);
			do {
				if (future.isDone()) {
					break;
				}
				Thread.sleep(3000);
				statusVerifyRetry--;
			} while (statusVerifyRetry > 0);

			if (future.isDone()) {
				msg = waitOrProcessRetrivel(taskScheduler, schedulerUuid, activeStorageMedia, schedulerTrigger, future);
			} else {
				taskScheduler.schedule(() -> waitOrProcessRetrivel(taskScheduler, schedulerUuid, activeStorageMedia,
						schedulerTrigger, future), Instant.now());
				msg = "The job '" + schedulerUuid
						+ "' trigger process taking longer time to complete, you will get email notification about process result";
				log.warn(msg);
			}
		} catch (ValidationException ve) {
			log.error("Process :" + ve.getMessage());
			updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.FAILED);
			notificationTask.sendNotificationForProcessedFiles(schedulerUuid);
			throw new ValidationException(ve.getMessage());
		} catch (Exception e) {
			log.error("Process :" + e.getMessage());
			msg = "Unable to complete trigger job, please reached to technical team.";
			updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.FAILED);
			notificationTask.sendNotificationForProcessedFiles(schedulerUuid);
			Thread.currentThread().interrupt();
			throw new ServiceException(msg + "- " + e.getMessage());
		}

		return msg;
	}

	@SneakyThrows
	private String waitOrProcessRetrivel(TaskScheduler taskScheduler, String schedulerUuid, String activeStorageMedia,
			SchedulerReport schedulerTrigger, Future<RetrieverI> future) {
		String msg = "";
		try {
			RetrieverI retriever = future.get();
			int size = retriever.size();
			if (size > 0) {
				retriverProcess(taskScheduler, schedulerUuid, retriever);
				msg = "Job trigged successfully, " + size + " files founds";
				log.info(msg);
				updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.SUCCESS);
			} else {
				msg = "No files found in '" + activeStorageMedia
						+ "' location for processing, scheduled job completed";
				log.info(msg);
				updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.SUCCESS);
				notificationTask.sendNotificationForProcessedFiles(schedulerUuid);
			}
		} catch (Throwable e) {
			if(e instanceof ExecutionException) {
				e = e.getCause();
			}
			log.error("WaitOrProcessRetrivel: " + e.getMessage());
			msg = "Unable to complete trigger job, please reach to technical team.";
			updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.FAILED);
			notificationTask.sendNotificationForProcessedFiles(schedulerUuid);
			Thread.currentThread().interrupt();
			if(e instanceof ValidationException)
				throw new ValidationException(e.getMessage());
			else
				throw new ServiceException(msg + "-" + e.getMessage());
		}

		return msg;
	}

	private void updateTrigger(SchedulerReport schedulerTrigger, String msg, SchedulerReportStatusEnum status) {
		schedulerTrigger.setRemark(msg);
		schedulerTrigger.setStatus(status);
		schedulerTrigger.setEndDate(LocalDateTime.now());
		sftpReportRepository.save(schedulerTrigger);
	}

	private void retriverProcess(TaskScheduler taskScheduler, String schedulerId, RetrieverI retriever) {

		var inProgressIdList = StreamSupport.stream(retriever.spliterator(), false).filter(processId -> tryRun(
				(TryUtils.ThrowableAction<IOException>) () -> retriever.setProgress(processId), e -> {
					log.info("Could not move remote file to the Progress folder {}", retriever.getFileName(processId));
					boolean flag = Paths.get(csvHandlerService.getFilePath(processId)).toFile().delete();
					if (flag)
						log.info("File deleted successfully");
				})).filter(processId -> tryRun(() -> {
					// need original file for identify Usage policy
					String originalFileName = retriever.getFileName(processId);
					var submodelFileRequest = policyProvider.getMatchingPolicyBasedOnFileName(originalFileName);
					retriever.setPolicyName(processId, submodelFileRequest.getPolicyName());
					submodelOrchestartorService.processSubmodelAutomationCsv(submodelFileRequest, processId);
				}, e -> {
					sftpReportRepository.save(sftpReportMapper.mapFrom(SchedulerReportModel.builder()
							.schedulerId(schedulerId).processId(processId).fileName(retriever.getFileName(processId))
							.policyName("Not found").status(SchedulerReportStatusEnum.FAILED)
							.endDate(LocalDateTime.now()).remark(e.getMessage())
							.startDate(LocalDateTime.now()).build()));
					log.info("Could not submit CVS file for processing. {}", csvHandlerService.getFilePath(processId));
					tryRun((TryUtils.ThrowableAction<IOException>) () -> retriever.setFailed(processId), e1 -> log
							.info("Could not move file to the Failed folder {}", retriever.getFileName(processId)));
				}))
				.peek(processId -> sftpReportRepository.save(sftpReportMapper.mapFrom(SchedulerReportModel.builder()
						.schedulerId(schedulerId).processId(processId).fileName(retriever.getFileName(processId))
						.policyName(retriever.getPolicyName(processId)).status(SchedulerReportStatusEnum.IN_PROGRESS)
						.startDate(LocalDateTime.now()).build())))
				.toList();
		if (!inProgressIdList.isEmpty()) {
			taskScheduler.schedule(() -> checkStatusOfInprogressFilesAndNotify(taskScheduler, retriever,
					inProgressIdList, schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
		} else {
			// In case of error this will send the notification. E.g. policy not present
			// Send this email after 5 seconds to finish 'update trigger' from caller method
			taskScheduler.schedule(() -> notificationTask.sendNotificationForProcessedFiles(schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
		}
	}

	public void checkStatusOfInprogressFilesAndNotify(TaskScheduler taskScheduler, RetrieverI retriever,
			List<String> inProgressIdList, String schedulerId) {
		if (processReportRepository.countByProcessIdInAndStatus(inProgressIdList,
				ProgressStatusEnum.COMPLETED) != inProgressIdList.size()) {
			taskScheduler.schedule(() -> checkStatusOfInprogressFilesAndNotify(taskScheduler, retriever,
					inProgressIdList, schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
		} else {
			selfFactory.getObject().createDbReport(retriever, inProgressIdList, schedulerId).forEach(Runnable::run);
			tryRun(retriever::close, TryUtils::IGNORE);
			notificationTask.sendNotificationForProcessedFiles(schedulerId);
		}
	}

	/***
	 * Method does not close passed retriever
	 * 
	 * @param retriever is a RetrieverI service
	 * @param completed is a List of completed ProcessId
	 * @return List of actions (side effect functions) which move remote files to
	 *         the appropriate locations
	 */
	@Transactional
	public List<Runnable> createDbReport(RetrieverI retriever, List<String> completed, String schedulerId) {
		List<Runnable> remoteActions = new ArrayList<>();
		if (!completed.isEmpty()) {
			List<SchedulerReport> sftpReportList = sftpReportRepository.findBySchedulerId(schedulerId);
			var processReportMap = processReportRepository.findByProcessIdIn(completed).stream()
					.collect(Collectors.toMap(ProcessReportEntity::getProcessId, Function.identity()));
			for (var sftpSchedulerReport : sftpReportList) {
				if (!schedulerId.equals(sftpSchedulerReport.getProcessId())) {
					final var processId = sftpSchedulerReport.getProcessId();
					final var processReport = processReportMap.get(processId);
					if(processReport != null) { // If process report is null then file is not processed e.g. In case Policy not present
						final var numberOfSucceededItems = processReport.getNumberOfSucceededItems()
								+ processReport.getNumberOfUpdatedItems();
						if (processReport.getNumberOfItems() == numberOfSucceededItems) {
							sftpSchedulerReport.setStatus(SchedulerReportStatusEnum.SUCCESS);
							remoteActions.add(() -> tryRun(
									(TryUtils.ThrowableAction<IOException>) () -> retriever.setSuccess(processId),
									e -> log.info("Could not move file {} to Success Folder",
											retriever.getFileName(processId))));
						} else if (numberOfSucceededItems > 0) {
							sftpSchedulerReport.setStatus(SchedulerReportStatusEnum.PARTIAL_SUCCESS);
							remoteActions.add(() -> tryRun(
									(TryUtils.ThrowableAction<IOException>) () -> retriever.setPartial(processId),
									e -> log.info("Could not move file {} to Partial Success Folder",
											retriever.getFileName(processId))));
						} else {
							sftpSchedulerReport.setStatus(SchedulerReportStatusEnum.FAILED);
							remoteActions.add(() -> tryRun(
									(TryUtils.ThrowableAction<IOException>) () -> retriever.setFailed(processId),
									e -> log.info("Could not move file {} to Failed Folder",
											retriever.getFileName(processId))));
						}
					}
					sftpSchedulerReport.setEndDate(LocalDateTime.now());
				}
			}
		}
		return remoteActions;
	}

}
