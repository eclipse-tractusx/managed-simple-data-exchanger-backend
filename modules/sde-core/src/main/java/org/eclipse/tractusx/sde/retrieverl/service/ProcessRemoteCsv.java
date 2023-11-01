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

import static org.eclipse.tractusx.sde.common.utils.TryUtils.tryRun;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.tractusx.sde.agent.entity.SchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SchedulerReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SchedulerReportMapper;
import org.eclipse.tractusx.sde.agent.model.SchedulerReportModel;
import org.eclipse.tractusx.sde.agent.repository.SchedulerReportRepository;
import org.eclipse.tractusx.sde.common.ConfigurableFactory;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.utils.DateUtil;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.notification.manager.EmailNotificationModelProvider;
import org.eclipse.tractusx.sde.retrieverl.RetrieverI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessRemoteCsv {
	private static final String TD_CLOSE = "</td>";
	private static final String TD = "<td>";
	private final CsvHandlerService csvHandlerService;
	private final PolicyProvider policyProvider;
	private final SubmodelOrchestartorService submodelOrchestartorService;
	private final SchedulerReportRepository sftpReportRepository;
	private final ProcessReportRepository processReportRepository;
	private final SchedulerReportMapper sftpReportMapper;
	private final ObjectFactory<ProcessRemoteCsv> selfFactory;
	private final EmailManager emailManager;
	private final EmailNotificationModelProvider emailNotificationModelProvider;
	private final JobMaintenanceConfigService jobMaintenanceConfigService;
	private final ActiveStorageMediaProvider activeStorageMediaProvider;
	private final ApplicationContext applicationContext;

	@SneakyThrows
	public String process(TaskScheduler taskScheduler, String schedulerUuid) {
		log.info("Scheduler started " + schedulerUuid);

		String activeStorageMedia = activeStorageMediaProvider.getConfiguration().getName().toLowerCase();
		@SuppressWarnings("unchecked")
		var retrieverFactory = (ConfigurableFactory<RetrieverI>)applicationContext.getBean(activeStorageMedia);

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
		} catch (Exception e) {
			log.error("Process :" + e.getMessage());
			msg = "Unable to complete trigger job, please reached to technical team.";
			updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.FAILED);
			sendNotificationForProcessedFiles(schedulerUuid);
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
						+ "' storage location for processing, scheduled job completed";
				log.info(msg);
				updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.SUCCESS);
				sendEmailNotification(schedulerUuid);
			}
		} catch (Exception e) {
			log.error("WaitOrProcessRetrivel: " + e.getMessage());
			msg = "Unable to complete trigger job, please reach to technical team.";
			updateTrigger(schedulerTrigger, msg, SchedulerReportStatusEnum.FAILED);
			sendEmailNotification(schedulerUuid);
			Thread.currentThread().interrupt();
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

	private void sendEmailNotification(String schedulerUuid) {
		if (jobMaintenanceConfigService.getConfiguration().getEmailNotification().booleanValue()) {
			sendNotificationForProcessedFiles(schedulerUuid);
		} else {
			log.warn("The notification is disable, so avoiding sent email notification");
		}
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
		}
	}

	private void sendNotificationForProcessedFiles(String schedulerId) {
		List<SchedulerReport> sftpReportList = sftpReportRepository.findBySchedulerId(schedulerId);
		if (!sftpReportList.isEmpty()) {
			var emailNotification = emailNotificationModelProvider.getConfiguration();
			Map<String, Object> emailContent = new HashMap<>();
			emailContent.put("toemail", emailNotification.getToEmail());
			emailContent.put("ccemail", emailNotification.getCcEmail());

			String statusMsg = "";
			String startTime = "";

			log.info("Send notification for scheduler: " + schedulerId);
			StringBuilder tableData = new StringBuilder();

			if (sftpReportList.size() > 1)
				tableData.append("""
								<table border="1">
								<tr>
								<th>Process Id </th>
								<th>File name </td>
								<th>Policy</td>
								<th>CSV Type </th>
								<th>Processing start time </td>
								<th>Processing end time </th>
								<th>Processing Status </th>
								<th>Successful entries count </td>
								<th>Failed entries count</th>
							</tr>
						""");

			for (SchedulerReport sftpSchedulerReport : sftpReportList) {
				if (schedulerId.equals(sftpSchedulerReport.getProcessId())) {
					statusMsg = sftpSchedulerReport.getRemark();
					startTime = sftpSchedulerReport.getStartDate().toString();
				} else {
					formatEmailContent(tableData, sftpSchedulerReport);
				}
			}

			if (!tableData.isEmpty())
				tableData.append("</table>");

			emailContent.put("statusMsg", statusMsg);
			emailContent.put("schedulerTime", startTime);
			emailContent.put("content", tableData.toString());

			String subject = "SDE automatic file processing scheduler notification :: " + schedulerId + " :: "
					+ startTime;

			tryRun((TryUtils.ThrowableAction<ServiceException>) () -> emailManager.sendEmail(emailContent, subject,
					"scheduler_status.html"),
					se -> log.info(
							"Exception occurred while sending email for scheduler id: " + schedulerId + "\n" + se));
		} else {
			log.warn("No data found in automatic storage upload to send notification email");
		}
	}

	private void formatEmailContent(StringBuilder tableData, SchedulerReport sftpSchedulerReport) {
		Optional<ProcessReportEntity> processReport = processReportRepository
				.findByProcessId(sftpSchedulerReport.getProcessId());
		if (processReport.isPresent()) {
			final int numberOfSucceededItems = processReport.get().getNumberOfSucceededItems()
					+ processReport.get().getNumberOfUpdatedItems();
			tableData.append("<tr>");
			String rowData = TD;
			rowData += processReport.get().getProcessId() + TD_CLOSE;
			rowData += TD + sftpSchedulerReport.getFileName() + TD_CLOSE;
			rowData += TD + sftpSchedulerReport.getPolicyName() + TD_CLOSE;
			rowData += TD + processReport.get().getCsvType() + TD_CLOSE;
			rowData += TD + DateUtil.formatter.format(processReport.get().getStartDate()) + TD_CLOSE;
			rowData += TD + DateUtil.formatter.format(processReport.get().getEndDate()) + TD_CLOSE;
			rowData += TD + sftpSchedulerReport.getStatus() + TD_CLOSE;
			rowData += TD + numberOfSucceededItems + TD_CLOSE;
			rowData += TD + processReport.get().getNumberOfFailedItems() + TD_CLOSE;
			tableData.append(rowData);
			tableData.append("</tr>");
		} else {
			log.warn("No data found " + sftpSchedulerReport.getProcessId()
					+ " inprocess report to send notification email");
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
			sendEmailNotification(schedulerId);
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
					sftpSchedulerReport.setEndDate(LocalDateTime.now());
				}
			}
		}
		return remoteActions;
	}

}
