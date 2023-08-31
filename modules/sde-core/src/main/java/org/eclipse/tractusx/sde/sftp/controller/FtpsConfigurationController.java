package org.eclipse.tractusx.sde.sftp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.sftp.service.ConfigType;
import org.eclipse.tractusx.sde.sftp.service.MetadataProvider;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactoryImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FtpsConfigurationController {

    private final MetadataProvider metadataProvider;
    private final SftpRetrieverFactoryImpl sftpRetrieverFactory;

    @PostMapping("/updateFtpsConfig")
    public Object updateFtpsConfig(@NotBlank @RequestBody JsonNode config,
                                   @RequestParam("type") ConfigType type) {
        if (type.equals(ConfigType.METADADA)) {
            metadataProvider.saveMetadata(config);
        } else if (type.equals(ConfigType.CLIENT)) {
            sftpRetrieverFactory.saveConfig(config);
        }
        return "success";
    }

}
