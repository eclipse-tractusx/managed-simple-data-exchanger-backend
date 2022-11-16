package org.eclipse.tractusx.sde.submodels.pap.mapper;

import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlannedAspectResponse;
import org.eclipse.tractusx.sde.submodels.pap.model.PartTypeInformation;
import org.eclipse.tractusx.sde.submodels.pap.model.ValidityPeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Mapper(componentModel = "spring")
public abstract class PartAsPlannedMapper {

	@Mapping(target = "rowNumber", ignore = true)
	@Mapping(target = "subModelId", ignore = true)
	public abstract PartAsPlanned mapFrom( PartAsPlannedEntity partAsPlanned);

	public abstract PartAsPlannedEntity mapFrom(PartAsPlanned partAsPlanned);

	public PartAsPlanned mapFrom(JsonObject partAsPlanned) {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.setPrettyPrinting().create();
		return gson.fromJson(partAsPlanned, PartAsPlanned.class);
	}
	
	public PartAsPlannedEntity mapforEntity(JsonObject aspect) {
		return new Gson().fromJson(aspect, PartAsPlannedEntity.class);
	}

	public JsonObject mapFromEntity(PartAsPlannedEntity aspect) {
		return new Gson().toJsonTree(aspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(PartAsPlannedEntity entity) {

		if (entity == null) {
			return null;
		}

		PartTypeInformation partTypeInformation = PartTypeInformation.builder()
				.manufacturerPartId(entity.getManufacturerPartId())
				.classification(entity.getClassification())
				.nameAtManufacturer(entity.getNameAtManufacturer())
				.build();
		
		ValidityPeriod validityPeriod = ValidityPeriod.builder()
				.validFrom(entity.getValidFrom())
				.validTo(entity.getValidTo())
				.build();

		return new Gson().toJsonTree(PartAsPlannedAspectResponse.builder()
				.partTypeInformation(partTypeInformation)
				.validityPeriod(validityPeriod)
				.catenaXId(entity.getUuid())
				.build()).getAsJsonObject();
	}
}
