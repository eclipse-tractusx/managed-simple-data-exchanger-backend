package org.eclipse.tractusx.sde.submodels.slbap.mapper;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.slbap.model.ChildParts;
import org.eclipse.tractusx.sde.submodels.slbap.model.MeasurementUnit;
import org.eclipse.tractusx.sde.submodels.slbap.model.Quantity;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlannedAspectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class SingleLevelBoMAsPlannedMapper {
	
	ObjectMapper mapper=new ObjectMapper();
	
	@Mapping(target = "rowNumber", ignore = true)
	@Mapping(target = "subModelId", ignore = true)
	public abstract SingleLevelBoMAsPlanned mapFrom(SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity);

	public abstract SingleLevelBoMAsPlannedEntity mapFrom(SingleLevelBoMAsPlanned singleLevelBoMAsPlanned);

	@SneakyThrows
	public SingleLevelBoMAsPlanned mapFrom(ObjectNode singleLevelBoMAsPlanned) {
		return mapper.readValue(singleLevelBoMAsPlanned.toString(), SingleLevelBoMAsPlanned.class);
	}
	
	public SingleLevelBoMAsPlannedEntity mapforEntity(JsonObject singleLevelBoMAsPlanned) {
		return new Gson().fromJson(singleLevelBoMAsPlanned, SingleLevelBoMAsPlannedEntity.class);
	}

	public JsonObject mapFromEntity(SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity) {
		return new Gson().toJsonTree(singleLevelBoMAsPlannedEntity).getAsJsonObject();
	}

	public JsonObject mapToResponse(SingleLevelBoMAsPlannedEntity entity) {

		if (entity == null) {
			return null;
		}

		MeasurementUnit measurementUnit = MeasurementUnit.builder()
				.lexicalValue(null)
				.datatypeURI(null)
				.build();
				
		Quantity quantity = Quantity.builder()
				.quantityNumber(null)
				.measurementUnit(measurementUnit)
				.build();
				
		Set<ChildParts> childPartsSet =  new HashSet<>();
		ChildParts childParts =	ChildParts.builder()
				.quantity(quantity)
				.createdOn(null)
				.lastModifiedOn(null)
				.childCatenaXId(null)
				.build();
		
		childPartsSet.add(childParts);


		return new Gson().toJsonTree(SingleLevelBoMAsPlannedAspectResponse.builder()
				.childParts(childPartsSet)
				.catenaXId(null)
				.build()).getAsJsonObject();
	}

}
