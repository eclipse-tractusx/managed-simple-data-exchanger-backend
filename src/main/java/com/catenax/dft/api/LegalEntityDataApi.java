package com.catenax.dft.api;

import com.catenax.dft.model.legalEntity.LegalEntityData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "LegalEntityDataApi", url = "${partner.pool.hostname}")
public interface LegalEntityDataApi {
    @GetMapping(path = "/api/catena/legal-entities")
    ResponseEntity<LegalEntityData> fetchLegalEntityData(@RequestParam String name, @RequestParam Integer page, @RequestParam Integer size, @RequestHeader("Authorization") String bearerToken);

}

