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
public class SchedulerModel {

    @JsonProperty(value = "type")
    private SchedulerType type;

    @JsonProperty(value = "day")
    private String day;

    @JsonProperty(value = "bpn_numbers")
    private String time;
}
