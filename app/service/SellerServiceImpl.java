package service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.dao.SellerDAO;
import com.idos.dao.SellerDAOImpl;

import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import controllers.StaticController;
import model.IdosRegisteredVendor;
import model.IdosRegisteredVendorPricings;
import play.libs.Json;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import play.mvc.Http.MultipartFormData;

public class SellerServiceImpl implements SellerService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode getVendorPriceLists(String email) {
		log.log(Level.FINE, "============ Start " + email);
		ObjectNode on = Json.newObject();
		if (null != email && !"".equals(email)) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("vendorEmail", email);
			criterias.put("presentStatus", 1);
			List<IdosRegisteredVendorPricings> pricings = genericDAO.findByCriteria(IdosRegisteredVendorPricings.class,
					criterias, entityManager);
			if (null != pricings && !pricings.isEmpty() && pricings.size() > 0) {
				ObjectNode row = null;
				ArrayNode an = on.putArray("pricings");
				for (IdosRegisteredVendorPricings price : pricings) {
					row = getPriceObjectNode(price);
					if (null != row) {
						an.add(row);
					}
				}
				on.put("result", true);
			}
		} else {
			on.put("result", false);
			on.put("message", "Vendor cannot be empty");
		}
		log.log(Level.FINE, "============ End " + on);
		return on;
	}

	@Override
	public ObjectNode getVendorPriceLists(IdosRegisteredVendor vendor) {
		log.log(Level.FINE, "============ Start");
		ObjectNode on = null;
		if (null != vendor && null != vendor.getVendorEmail() && !"".equals(vendor.getVendorEmail())) {
			on = getVendorPriceLists(vendor.getVendorEmail());
		} else {
			on = Json.newObject();
			on.put("result", false);
			on.put("message", "Vendor cannot be empty");
		}
		return on;
	}

	@Override
	public ObjectNode getVendorPriceDetails(String email, long id) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		if (null != email && !"".equals(email)) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("vendorEmail", email);
			criterias.put("id", id);
			criterias.put("presentStatus", 1);
			IdosRegisteredVendorPricings price = genericDAO.getByCriteria(IdosRegisteredVendorPricings.class, criterias,
					entityManager);
			result = getPriceObjectNode(price);
			if (null != result) {
				result.put("result", true);
			} else {
				result = Json.newObject();
				result.put("result", false);
				result.put("message", "Unable to find the Item. Please try agan later.");
			}
		} else {
			result.put("message", "Vendor cannot be empty");
		}
		return result;
	}

	private ObjectNode getPriceObjectNode(final IdosRegisteredVendorPricings price) {
		log.log(Level.FINE, "============ Start");
		ObjectNode row = null, subRow = null;
		ArrayNode inAn = null;
		if (null != price) {
			row = Json.newObject();
			row.put("id", price.getId());
			if (null != price.getVendorRetailerUnitPrice()) {
				row.put("retailerUnitPrice", price.getVendorRetailerUnitPrice());
			} else {
				row.put("retailerUnitPrice", "");
			}
			if (null != price.getVendorSpecialUnitPrice()) {
				row.put("specialUnitPrice", price.getVendorSpecialUnitPrice());
			} else {
				row.put("specialUnitPrice", "");
			}
			if (null != price.getVendorWholesaleUnitPrice()) {
				row.put("wholesaleUnitPrice", price.getVendorWholesaleUnitPrice());
			} else {
				row.put("wholesaleUnitPrice", "");
			}
			if (null != price.getVendorEmail()) {
				row.put("email", price.getVendorEmail());
			} else {
				row.put("email", "");
			}
			if (null != price.getModifiedAt()) {
				row.put("updated", StaticController.idosdf.format(price.getModifiedAt()));
			} else {
				row.put("updated", "");
			}
			if (null != price.getCreatedAt()) {
				row.put("created", StaticController.idosdf.format(price.getCreatedAt()));
			} else {
				row.put("created", "");
			}
			if (null != price.getVendorItemsDescription()) {
				row.put("description", price.getVendorItemsDescription());
			} else {
				row.put("description", "");
			}
			if (null != price.getVendorSpecialPriceRequirements()) {
				row.put("specialPriceRequirements", price.getVendorSpecialPriceRequirements());
			} else {
				row.put("specialPriceRequirements", "");
			}
			if (null != price.getVendorItems()) {
				row.put("item", price.getVendorItems());
			} else {
				row.put("item", "");
			}
			inAn = row.putArray("locations");
			if (null != price.getVendorLocations()) {
				String locs = price.getVendorLocations();
				String[] locArr = locs.split(",");
				if (locArr.length > 0) {
					for (String s : locArr) {
						if (null != s && !s.equals("")) {
							subRow = Json.newObject();
							subRow.put("location", s);
							inAn.add(subRow);
						}
					}
				}
			}

		}
		return row;
	}

	@Override
	public ObjectNode uploadSupplierItems(Request request, ObjectNode result, JsonNode json,
			MultipartFormData<File> body, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		result = SELLER_DAO.uploadSupplierItems(request, result, json, body, entityManager, entitytransaction);
		return result;
	}
}