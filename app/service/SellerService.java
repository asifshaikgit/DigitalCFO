package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.IdosRegisteredVendor;
import model.Users;
import java.io.File;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.mvc.Http.MultipartFormData;

public interface SellerService extends BaseService {
	ObjectNode getVendorPriceLists(final String email);

	ObjectNode getVendorPriceLists(final IdosRegisteredVendor vendor);

	ObjectNode getVendorPriceDetails(final String email, final long id);

	public ObjectNode uploadSupplierItems(Request request, ObjectNode result, JsonNode json,
			MultipartFormData<File> body, EntityManager entityManager, EntityTransaction entitytransaction);
}
