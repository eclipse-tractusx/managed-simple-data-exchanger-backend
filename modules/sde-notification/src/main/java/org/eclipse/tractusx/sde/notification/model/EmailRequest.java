package org.eclipse.tractusx.sde.notification.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EmailRequest {

    private String toEmail;
    private String subject;
    private String templateFileName;
    private Map<String, Object> emailContent;
}
