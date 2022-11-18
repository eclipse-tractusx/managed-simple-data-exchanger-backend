package org.eclipse.tractusx.sde.submodels.slbap.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "single_level_bom_as_planned")
@Entity
@Data
@IdClass(SingleLevelBoMAsPlannedPrimaryKey.class)
public class SingleLevelBoMAsPlannedEntity implements Serializable {
	
    @Id
    @Column(name = "parent_catenax_id")
    private String parentCatenaXId;
    
    @Column(name = "process_id")
    private String processId;
    
    @Id
    @Column(name = "child_catenax_id")
    private String childCatenaXId;
    @Column(name = "quantity_number")
    private Double quantityNumber;
    @Column(name = "measurement_unit_lexical_value")
    private String measurementUnitLexicalValue;
    @Column(name = "datatype_uri")
    private String dataTypeUri;
    @Column(name = "created_on")
	private String createdOn;
    @Column(name = "last_modified_on")
	private String lastModifiedOn;
    
    @Column(name = "shell_id")
    private String shellId;
    @Column(name = "usage_policy_id")
    private String usagePolicyId;
    @Column(name = "contract_defination_id")
    private String contractDefinationId;
    @Column(name = "asset_id")
    private String assetId;
    @Column(name = "access_policy_id")
    private String accessPolicyId;
    @Column(name = "deleted")
    private String deleted;
}

@Data
class SingleLevelBoMAsPlannedPrimaryKey implements Serializable {

    private String parentCatenaXId;
    private String childCatenaXId;
}
