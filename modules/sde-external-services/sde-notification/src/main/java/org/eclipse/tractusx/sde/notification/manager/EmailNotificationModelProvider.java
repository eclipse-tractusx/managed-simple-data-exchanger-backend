package org.eclipse.tractusx.sde.notification.manager;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.EmailNotificationModel;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("email")
@RequiredArgsConstructor
public class EmailNotificationModelProvider implements ConfigurationProvider<EmailNotificationModel> {
    @Value("${mail.to.address}")
    private String toEmail;

    @Value("${mail.cc.address}")
    private String ccEmail;

    private final ConfigService configService;

    @Override
    public EmailNotificationModel getConfiguration() {
        return configService.getConfigurationAsObject(EmailNotificationModel.class)
                .orElseGet(() -> {
                    var model = new EmailNotificationModel(toEmail, ccEmail);
                    saveConfig(model);
                    return model;
                });
    }

    @Override
    public void saveConfig(EmailNotificationModel config) {
        configService.saveConfiguration(config);
    }

    @Override
    public Class<EmailNotificationModel> getConfigClass() {
        return EmailNotificationModel.class;
    }
}
