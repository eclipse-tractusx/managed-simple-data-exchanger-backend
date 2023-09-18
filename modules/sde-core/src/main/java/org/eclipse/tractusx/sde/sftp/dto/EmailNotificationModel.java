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
public class EmailNotificationModel {

    @JsonProperty(value = "from_email")
    private String from_email;

    @JsonProperty(value = "to_email")
    private String to_email;

    @JsonProperty(value = "cc_email")
    private String cc_email;
}
