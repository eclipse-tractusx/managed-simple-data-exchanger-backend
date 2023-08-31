package org.eclipse.tractusx.sde.sftp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class RetrieverScheduler {
    private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
    private final ProcessRemoteCsv processRemoteCsv;

    private ScheduledFuture<?> cronFuture = null;

    public synchronized void schedule(String cronExpression) {
        if (cronFuture != null) {
            cronFuture.cancel(false);
        }
        cronFuture = taskScheduler.schedule(() -> processRemoteCsv.process(taskScheduler), new CronTrigger(cronExpression));
    }

    public void fire() {
        taskScheduler.schedule(() -> processRemoteCsv.process(taskScheduler), Instant.now());
    }
}
