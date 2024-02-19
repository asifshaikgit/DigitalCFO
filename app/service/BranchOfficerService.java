package service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface BranchOfficerService extends BaseService{

	public ObjectNode getDetails(final String email);
}
