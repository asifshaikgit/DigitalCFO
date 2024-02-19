package com.idos.dao;

import java.util.List;

import model.Organization;
import model.Vendor;

public interface OrgAnalyticsDAO extends BaseDAO{
	
	public List<Organization> getUniqueOrgContacted(final long id) throws Exception;
	public List<Vendor> getConverted(final String email, final String inParameterOrgIds) throws Exception;

}
