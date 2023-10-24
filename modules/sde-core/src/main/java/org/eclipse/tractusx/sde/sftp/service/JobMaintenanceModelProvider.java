package org.eclipse.tractusx.sde.sftp.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.JobMaintenanceModel;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobMaintenanceModelProvider implements ConfigurationProvider<JobMaintenanceModel> {

    final private ConfigService configService;

    @Override
    public JobMaintenanceModel getConfiguration() {
        return configService.getConfigurationAsObject(JobMaintenanceModel.class)
                .orElseGet(() -> {
                    var jmp = getDefaultJobMaintenanceModel();
                    saveConfig(jmp);
                    return jmp;
                });
    }

    @Override
    public void saveConfig(JobMaintenanceModel config) {
        configService.saveConfiguration(config);
    }

    private JobMaintenanceModel getDefaultJobMaintenanceModel() {
        return new JobMaintenanceModel(true, true);
    }
}
