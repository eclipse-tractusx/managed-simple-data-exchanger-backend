package org.eclipse.tractusx.sde.sftp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.sftp.service.ConfigType;
import org.eclipse.tractusx.sde.sftp.service.FtpsService;
import org.eclipse.tractusx.sde.sftp.service.MetadataProvider;
import org.eclipse.tractusx.sde.sftp.service.RetrieverConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class FtpsConfigurationController {

    private MetadataProvider metadataProvider;
    private RetrieverConfigurationProvider retrieverConfigurationProvider;

    @Autowired
    public FtpsConfigurationController(MetadataProvider metadataProvider,
                                       RetrieverConfigurationProvider retrieverConfigurationProvider)  {
        this.metadataProvider = metadataProvider;
        this.retrieverConfigurationProvider = retrieverConfigurationProvider;
    }

    @PostMapping("/updateFtpsConfig1")
    public Object updateFtpsConfig(@NotBlank @RequestBody JsonNode config,
                                   @RequestParam("type") ConfigType type) throws IOException {
        if (type.equals(ConfigType.METADADA)) {
            metadataProvider.saveMetadata(config);
        } else if (type.equals(ConfigType.CLIENT)) {
            retrieverConfigurationProvider.saveRetrieverConfig(config);
        }
        return "success";
    }

}
