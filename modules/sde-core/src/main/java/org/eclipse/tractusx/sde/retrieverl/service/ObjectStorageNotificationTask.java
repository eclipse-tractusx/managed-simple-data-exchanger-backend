package org.eclipse.tractusx.sde.retrieverl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.entity.SchedulerReport;
import org.eclipse.tractusx.sde.agent.repository.SchedulerReportRepository;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.utils.DateUtil;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.notification.manager.EmailNotificationModelProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.tractusx.sde.common.utils.TryUtils.tryRun;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageNotificationTask {

    private final SchedulerReportRepository sftpReportRepository;

    private final JobMaintenanceConfigService jobMaintenanceConfigService;
    private final EmailManager emailManager;
    private final EmailNotificationModelProvider emailNotificationModelProvider;

    private final ProcessReportRepository processReportRepository;

    private static final String TD_CLOSE = "</td>";
    private static final String TD = "<td>";

    @Async
    public void sendNotificationForProcessedFiles(String schedulerId) {
        if(jobMaintenanceConfigService.getConfiguration().getEmailNotification().booleanValue()) {
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
                        statusMsg = Optional.ofNullable(sftpSchedulerReport.getRemark()).orElse("");
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
        } else {
            log.warn("The notification is disable, so avoiding sent email notification");
        }
    }



    private void formatEmailContent(StringBuilder tableData, SchedulerReport sftpSchedulerReport) {
        Optional<ProcessReportEntity> processReport = processReportRepository
                .findByProcessId(sftpSchedulerReport.getProcessId());
        int numberOfSucceededItems = 0;
        int numberOfFailedItems = 0;
        String csvType = "";
        if (processReport.isPresent()) {
            numberOfSucceededItems = processReport.get().getNumberOfSucceededItems()
                    + processReport.get().getNumberOfUpdatedItems();
            csvType = processReport.get().getCsvType();
            numberOfFailedItems = processReport.get().getNumberOfFailedItems();
        }

        tableData.append("<tr>");
        String rowData = TD;
        rowData += sftpSchedulerReport.getProcessId() + TD_CLOSE;
        rowData += TD + sftpSchedulerReport.getFileName() + TD_CLOSE;
        rowData += TD + sftpSchedulerReport.getPolicyName() + TD_CLOSE;
        rowData += TD + csvType + TD_CLOSE;
        rowData += TD + DateUtil.formatter.format(sftpSchedulerReport.getStartDate()) + TD_CLOSE;
        rowData += TD + DateUtil.formatter.format(sftpSchedulerReport.getEndDate()) + TD_CLOSE;
        rowData += TD + sftpSchedulerReport.getStatus() + TD_CLOSE;
        rowData += TD + numberOfSucceededItems + TD_CLOSE;
        rowData += TD + numberOfFailedItems + TD_CLOSE;
        tableData.append(rowData);
        tableData.append("</tr>");

    }
}
