package service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface AnalyticsService extends BaseService{
	
	public ObjectNode getVendorSellerAnalytics(final String email) throws Exception;

}
