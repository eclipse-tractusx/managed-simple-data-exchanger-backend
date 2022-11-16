package org.eclipse.tractusx.sde.submodels.pap.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "part_as_planned")
@Entity
@Data
public class PartAsPlannedEntity implements Serializable {

    @Id
    @Column(name = "uuid")
    private String uuid;
    
    @Column(name = "process_id")
    private String processId;
    
    @Column(name = "manufacturer_part_id")
    private String manufacturerPartId;
    
    @Column(name = "classification")
    private String classification;
    
    @Column(name = "name_at_manufacturer")
    private String nameAtManufacturer;
   
    @Column(name = "valid_from")
    private String validFrom;
    
    @Column(name = "valid_to")
    private String validTo;

    @Column(name = "shell_id")
    private String shellId;
    
    @Column(name = "contract_defination_id")
    private String contractDefinationId;
    
    @Column(name = "usage_policy_id")
    private String usagePolicyId;
    
    @Column(name = "access_policy_id")
    private String accessPolicyId;
    
    @Column(name = "asset_id")
    private String assetId;
    
    @Column(name = "deleted")
    private String deleted;
}