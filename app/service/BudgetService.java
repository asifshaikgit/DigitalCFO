package service;

import model.Users;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface BudgetService extends BaseService{
	
	public ObjectNode getBudgetDetails(final Users user);

}
