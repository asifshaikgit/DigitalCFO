package service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ApplicationService {

	public ObjectNode generateKey(final String org, final String pName, final String email, 
			final String url, final String phone, final String note) throws Exception;
	public String activateApiAccess(String email, String key) throws Exception;
	public boolean saveIdosLeads(String name, String email, String phone, int type);
}
