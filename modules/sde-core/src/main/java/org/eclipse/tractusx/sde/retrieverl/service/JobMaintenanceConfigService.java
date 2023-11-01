package org.eclipse.tractusx.sde.retrieverl.service;

import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.JobMaintenanceModel;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service("jobmaintenance")
public class JobMaintenanceConfigService implements ConfigurationProvider<JobMaintenanceModel> {
    private final ConfigService configService;
    private final RetrieverScheduler retrieverScheduler;


    @Autowired
    public JobMaintenanceConfigService(ConfigService configService, @Lazy RetrieverScheduler retrieverScheduler) {
        this.configService = configService;
        this.retrieverScheduler = retrieverScheduler;
    }

    @Override
    public JobMaintenanceModel getConfiguration() {
        return configService.getConfigurationAsObject(JobMaintenanceModel.class)
                .orElseGet(() -> {
                    var jmp = getDefaultJobMaintenanceModel();
                    configService.saveConfiguration(jmp);
                    return jmp;
                });
    }

    @Override
    public void saveConfig(JobMaintenanceModel jmConfig) {
        configService.saveConfiguration(jmConfig);
        retrieverScheduler.reschedule();
    }

    @Override
    public Class<JobMaintenanceModel> getConfigClass() {
        return JobMaintenanceModel.class;
    }

    private JobMaintenanceModel getDefaultJobMaintenanceModel() {
        return new JobMaintenanceModel(true, true);
    }
}
