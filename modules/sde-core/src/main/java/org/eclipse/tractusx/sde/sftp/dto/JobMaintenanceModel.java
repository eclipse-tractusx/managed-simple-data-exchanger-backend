package org.eclipse.tractusx.sde.sftp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobMaintenanceModel {

    @JsonProperty(value = "automatic_upload")
    private String automatic_upload;

    @JsonProperty(value = "email_notification")
    private String email_notification;

}
