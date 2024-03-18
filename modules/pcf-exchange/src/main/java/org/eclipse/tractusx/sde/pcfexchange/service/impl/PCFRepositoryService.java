package org.eclipse.tractusx.sde.pcfexchange.service.impl;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.mapper.PcfExchangeMapper;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfRequestRepository;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PCFRepositoryService {

	private final PcfRequestRepository pcfRequestRepository;
	private final PcfExchangeMapper pcfMapper;

	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message,
			PCFTypeEnum type, PCFRequestStatusEnum status, String remark) {

		PcfRequestModel pojo = PcfRequestModel.builder().requestId(requestId).productId(productId).bpnNumber(bpnNumber)
				.status(status).message(message).type(type).requestedTime(Instant.now().getEpochSecond())
				.lastUpdatedTime(Instant.now().getEpochSecond()).remark(remark).build();

		PcfRequestEntity entity = pcfMapper.mapFrom(pojo);

		return pcfMapper.mapFrom(pcfRequestRepository.save(entity));
	}

	public void updatePCFPushStatus(PCFRequestStatusEnum status, String requestId, String sendNotificationStatus) {
		if (PCFRequestStatusEnum.APPROVED.equals(status) && StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.PUSHED);
		} else if (PCFRequestStatusEnum.PUSHING_UPDATED_DATA.equals(status)
				&& StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.PUSHED_UPDATED_DATA);
		} else if (PCFRequestStatusEnum.REQUESTED.equals(status) && StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.REQUESTED);
		} else if (PCFRequestStatusEnum.REJECTED.equals(status) && StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.REJECTED);
		} else if (PCFRequestStatusEnum.APPROVED.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_PUSH_DATA.equals(status)
				|| PCFRequestStatusEnum.PUSHING_UPDATED_DATA.equals(status)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.FAILED_TO_PUSH_DATA);
		} else if (PCFRequestStatusEnum.REJECTED.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_SEND_REJECT_NOTIFICATION.equals(status))
			savePcfStatus(requestId, PCFRequestStatusEnum.FAILED_TO_SEND_REJECT_NOTIFICATION);
		else {
			savePcfStatus(requestId, PCFRequestStatusEnum.FAILED);
		}
	}

	@SneakyThrows
	public PcfRequestEntity savePcfStatus(String requestId, PCFRequestStatusEnum status) {

		PcfRequestEntity pcfRequestEntity = pcfRequestRepository.getReferenceById(requestId);
		pcfRequestEntity.setLastUpdatedTime(Instant.now().getEpochSecond());
		pcfRequestEntity.setStatus(status);
		log.info("'" + pcfRequestEntity.getProductId() + "' pcf request saved in the database successfully as {}",
				status);
		pcfRequestRepository.save(pcfRequestEntity);
		return pcfRequestEntity;

	}

	public PagingResponse getPcfData(List<PCFRequestStatusEnum> status, PCFTypeEnum type, Integer page, Integer pageSize) {

		Page<PcfRequestEntity> result = null;
		if (status == null || status.isEmpty()) {
			result = pcfRequestRepository.findByType(PageRequest.of(page, pageSize), type);
		} else {
			result = pcfRequestRepository.findByTypeAndStatusIn(PageRequest.of(page, pageSize), type, status);
		}

		List<PcfRequestModel> requestList = result.stream().map(pcfMapper::mapFrom).toList();

		return PagingResponse.builder().items(requestList).pageSize(result.getSize()).page(result.getNumber())
				.totalItems(result.getTotalElements()).build();
	}

}
