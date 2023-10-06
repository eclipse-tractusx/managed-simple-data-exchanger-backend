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

package org.eclipse.tractusx.sde.sftp.service;

import static org.eclipse.tractusx.sde.core.utils.TryUtils.IGNORE;
import static org.eclipse.tractusx.sde.core.utils.TryUtils.tryRun;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.tractusx.sde.agent.entity.SftpSchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SftpReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SftpReportMapper;
import org.eclipse.tractusx.sde.agent.model.SftpReportModel;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.utils.DateUtil;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.eclipse.tractusx.sde.sftp.dto.EmailNotificationModel;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
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
	private final RetrieverFactory retrieverFactory;
	private final SubmodelOrchestartorService submodelOrchestartorService;
	private final SftpReportRepository sftpReportRepository;
	private final ProcessReportRepository processReportRepository;
	private final SftpReportMapper sftpReportMapper;
	private final ObjectFactory<ProcessRemoteCsv> selfFactory;
	private final EmailManager emailManager;
	private final ConfigService configService;

	@Value("mail.to.address")
	private String toEmail;

	@Value("mail.cc.address")
	private String ccEmail;

	@SneakyThrows
	@SuppressWarnings({ "CallToPrintStackTrace", "ResultOfMethodCallIgnored" })
	@Retryable(retryFor = {
			ServiceException.class }, maxAttemptsExpression = "3", backoff = @Backoff(delayExpression = "5000"))
	public String process(TaskScheduler taskScheduler) {
		var schedulerId = UUID.randomUUID().toString();
		log.info("Scheduler started " + schedulerId);
		boolean loginSuccess = false;
		String msg = null;
		try (var retriever = retrieverFactory.create()) {
			loginSuccess = true;
			int size = retriever.size();
			if (size>0) {
				taskScheduler.schedule(() -> retriverProcess(taskScheduler, schedulerId, retriever), Instant.now());
				msg = "Job trigged successfully, "+size+" files founds";
				log.info(msg);
			} else {
				msg = "No files found in SFTP location for processing, scheduled job completed";
				log.info(msg);
			}
		} catch (Exception e) {
			if (!loginSuccess) {
				log.error("Possible wrong credentials : retry "
						+ (RetrySynchronizationManager.getContext().getRetryCount() + 1) + ": " + e.getMessage());
				throw new ServiceException("Login failed just doing retry");
			}
			log.error(e.getMessage());
		}
		return msg;
	}

	private void retriverProcess(TaskScheduler taskScheduler, String schedulerId, RetrieverI retriever) {
		
		var inProgressIdList = StreamSupport.stream(retriever.spliterator(), false)
				.filter(processId -> tryRun(() -> retriever.setProgress(processId), e -> {
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
					tryRun(() -> retriever.setFailed(processId), e1 -> log
							.info("Could not move file to the Failed folder {}", retriever.getFileName(processId)));
				})).peek(processId -> {

					sftpReportRepository.save(sftpReportMapper.mapFrom(SftpReportModel.builder()
							.schedulerId(schedulerId).processId(processId).fileName(retriever.getFileName(processId))
							.policyName(retriever.getPolicyName(processId)).status(SftpReportStatusEnum.IN_PROGRESS)
							.startDate(LocalDateTime.now()).build()));
				}).toList();
		if (!inProgressIdList.isEmpty()) {
			taskScheduler.schedule(() -> checkStatusOfInprogressFilesAndNotify(taskScheduler, retriever,
					inProgressIdList, schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
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
			tryRun(retriever::close, IGNORE());

			if (configService.getJobMaintenanceDetails().isEmailNotification()) {
				// EmailNotificationModel method call
				sendNotificationForProcessedFiles(schedulerId);
			} else {
				log.warn("The notification is disable, so avoiding sent email notification");
			}
		}
	}

	private void sendNotificationForProcessedFiles(String schedulerId) {
		List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findBySchedulerId(schedulerId);
		if (!sftpReportList.isEmpty()) {
			log.info("Send notification for scheduler: " + schedulerId);
			EmailNotificationModel emailNotification = configService.getNotificationDetails();

			Map<String, Object> emailContent = new HashMap<>();
			emailContent.put("toemail", emailNotification.getToEmail());
			emailContent.put("ccemail", emailNotification.getCcEmail());
			StringBuilder tableData = new StringBuilder();
			for (SftpSchedulerReport sftpSchedulerReport : sftpReportList) {
				Optional<ProcessReportEntity> processReport = processReportRepository
						.findByProcessId(sftpSchedulerReport.getProcessId());
				if (processReport.isPresent()) {
					final int numberOfSucceededItems = processReport.get().getNumberOfSucceededItems()
							+ processReport.get().getNumberOfUpdatedItems();
					tableData.append("<tr>");
					emailContent.put("schedulerTime", DateUtil.formatter.format(sftpSchedulerReport.getStartDate()));
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
				}
			}
			emailContent.put("fileTableData", tableData.toString());
			tryRun(() -> emailManager.sendEmail(emailContent, "Scheduler status", "scheduler_status.html"), se -> log
					.info("Exception occurred while sending email for scheduler id: " + schedulerId + "\n" + se));
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
			List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findBySchedulerId(schedulerId);
			var processReportMap = processReportRepository.findByProcessIdIn(completed).stream()
					.collect(Collectors.toMap(ProcessReportEntity::getProcessId, Function.identity()));
			for (var sftpSchedulerReport : sftpReportList) {
				final var processId = sftpSchedulerReport.getProcessId();
				final var processReport = processReportMap.get(processId);
				final var numberOfSucceededItems = processReport.getNumberOfSucceededItems()
						+ processReport.getNumberOfUpdatedItems();
				if (processReport.getNumberOfItems() == numberOfSucceededItems) {
					sftpSchedulerReport.setStatus(SftpReportStatusEnum.SUCCESS);
					remoteActions.add(() -> tryRun(() -> retriever.setSuccess(processId), e -> log
							.info("Could not move file {} to Success Folder", retriever.getFileName(processId))));
				} else if (numberOfSucceededItems > 0) {
					sftpSchedulerReport.setStatus(SftpReportStatusEnum.PARTIAL_SUCCESS);
					remoteActions.add(() -> tryRun(() -> retriever.setPartial(processId),
							e -> log.info("Could not move file {} to Partial Success Folder",
									retriever.getFileName(processId))));
				} else {
					sftpSchedulerReport.setStatus(SftpReportStatusEnum.FAILED);
					remoteActions.add(() -> tryRun(() -> retriever.setFailed(processId), e -> log
							.info("Could not move file {} to Failed Folder", retriever.getFileName(processId))));
				}
				sftpSchedulerReport.setEndDate(LocalDateTime.now());
			}
		}
		return remoteActions;
	}

}
