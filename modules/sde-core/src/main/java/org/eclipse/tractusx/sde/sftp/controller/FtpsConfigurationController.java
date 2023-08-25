package org.eclipse.tractusx.sde.sftp.controller;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.sftp.service.FtpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class FtpsConfigurationController {


    private FtpsService ftpsService;

    @Autowired
    public FtpsConfigurationController(FtpsService ftpsService)  {
        this.ftpsService = ftpsService;
    }

    @PostMapping("/updateMetadata")
    public Object updateFtpsMetadata(@NotBlank @RequestBody Map<String, Object> metadataDto) throws IOException {
        ftpsService.updateMetadata(metadataDto);
        return "success";
    }

    @PostMapping("/updateFtpsConfig")
    public Object updateFtpsConfig(@NotBlank @RequestBody SftpConfigModel sftpConfigDto) throws IOException {
        ftpsService.updateFtpsConfig(sftpConfigDto);
        return "success";
    }



}
