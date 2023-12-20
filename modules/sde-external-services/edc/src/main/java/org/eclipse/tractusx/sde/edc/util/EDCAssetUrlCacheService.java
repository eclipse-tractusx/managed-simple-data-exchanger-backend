package org.eclipse.tractusx.sde.edc.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EDCAssetUrlCacheService {

	private static final Map<String, LocalDateTime> dDTRmap = new ConcurrentHashMap<>();
	private static final Map<String, LocalDateTime> pcfExchangeURLMap = new ConcurrentHashMap<>();

	private final ConsumerControlPanelService consumerControlPanelService;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	private final DDTRUrlCacheUtility dDTRUrlCacheUtility;
	private final PCFExchangeAssetUtils pcfExchangeAssetUtils;

	@SneakyThrows
	public EDRCachedByIdResponse verifyAndGetToken(String bpnNumber, QueryDataOfferModel queryDataOfferModel) {
		
		Map<UsagePolicyEnum, UsagePolicies> policies = queryDataOfferModel.getUsagePolicies();
		ActionRequest action = policyConstraintBuilderService.getUsagePolicyConstraints(policies);
		
		Offer offer = Offer.builder().assetId(queryDataOfferModel.getAssetId())
				.offerId(queryDataOfferModel.getOfferId())
				.policyId(queryDataOfferModel.getPolicyId())
				.build();
		try {
			EDRCachedResponse eDRCachedResponse = consumerControlPanelService.verifyOrCreateContractNegotiation(
					bpnNumber, Map.of(), queryDataOfferModel.getConnectorOfferUrl(), action, offer);

			if (eDRCachedResponse == null) {
				throw new ServiceException("Time out!! to get 'NEGOTIATED' EDC EDR status to lookup '"
						+ queryDataOfferModel.getAssetId() + "', The current status is null");
			} else if (!"NEGOTIATED".equalsIgnoreCase(eDRCachedResponse.getEdrState())) {
				throw new ServiceException(
						"Time out!! to get 'NEGOTIATED' EDC EDR status to lookup  '" + queryDataOfferModel.getAssetId()
								+ "', The current status is '" + eDRCachedResponse.getEdrState() + "'");
			} else
				return consumerControlPanelService
						.getAuthorizationTokenForDataDownload(eDRCachedResponse.getTransferProcessId());

		} catch (FeignException e) {
			log.error("FeignException Request : " + e.request());
			String errorMsg = "Unable to look up offer because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up offer because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return null;
	}

	public List<QueryDataOfferModel> getDDTRUrl(String bpnNumber) {

		LocalDateTime cacheExpTime = dDTRmap.get(bpnNumber);
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			dDTRUrlCacheUtility.removeDDTRUrlCache(bpnNumber);
			cacheExpTime = currDate.plusHours(12);
		}
		dDTRmap.put(bpnNumber, cacheExpTime);
		return dDTRUrlCacheUtility.getDDTRUrlDirect(bpnNumber);
	}

	public void clearDDTRUrlCache() {
		dDTRmap.clear();
		dDTRUrlCacheUtility.cleareDDTRUrlAllCache();
	}

	public List<QueryDataOfferModel> getPCFExchangeUrlFromTwin(String bpnNumber) {

		LocalDateTime cacheExpTime = pcfExchangeURLMap.get(bpnNumber);
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			pcfExchangeAssetUtils.removePCFExchangeCache(bpnNumber);
			cacheExpTime = currDate.plusHours(12);
		}
		pcfExchangeURLMap.put(bpnNumber, cacheExpTime);
		return pcfExchangeAssetUtils.getPCFExchangeUrl(bpnNumber);
	}

	public void clearPCFExchangeUrlCache() {
		pcfExchangeURLMap.clear();
		pcfExchangeAssetUtils.clearePCFExchangeAllCache();
	}

}
