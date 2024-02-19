package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.ConfigParams;
import model.IdosIntegrationKey;
import model.IdosLeads;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IdosUtil;

import javax.inject.Inject;
import controllers.StaticController;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import views.html.apiIntegrationLink;

public class ApplicationServiceImpl implements ApplicationService {

	private final GenericDAO genericDAO = new GenericJpaDAO();
	private static final String INTERNAL_MSG = "A request for API Integration with IDOS has been made. Please find the details below.";
	private static final String EXTERNAL_MSG = "Your request for API Integration with IDOS has been made. Please find the details below.";
	private static final String API_INTEGRATION = "API Integration Request";
	protected static Logger log = Logger.getLogger("service");
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode generateKey(String org, String pName, String email,
			String url, String phone, String note) throws Exception {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode on = Json.newObject();
		EntityTransaction et = null;
		on.put("result", false);
		on.put("message", "Something went wrong.");
		try {
			String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
			if (null == org || "".equals(org) || "Organization".equalsIgnoreCase(org)) {
				on.put("message", "Enter Organization Name");
				on.put("field", "keyOrganization");
			} else if (null == pName || "".equals(pName) || "Product Name".equalsIgnoreCase(pName)) {
				on.put("message", "Enter Product Name");
				on.put("field", "keyProductName");
			} else if (null == email || "".equals(email) || "Email".equalsIgnoreCase(email)) {
				on.put("message", "Enter Email ID");
				on.put("field", "keyEmail");
			} else {
				if (!email.matches(emailPattern)) {
					on.put("message", "Enter Valid Email ID");
					on.put("field", "keyEmail");
				} else {
					// entityManager.em = jpaApi.em();
					StringBuilder query = new StringBuilder();
					query.append("SELECT obj FROM IdosIntegrationKey obj WHERE obj.email = '").append(email)
							.append("'");
					List<IdosIntegrationKey> integrationKeys = genericDAO.executeSimpleQueryWithLimit(query.toString(),
							entityManager, 1);
					IdosIntegrationKey integrationKey = null;
					String key = null;
					if (integrationKeys.isEmpty() && integrationKeys.size() == 0) {
						integrationKey = new IdosIntegrationKey();
						if ("".equals(phone) || "Contact Number".equalsIgnoreCase(phone)) {
							phone = null;
						}
						if ("".equals(url) || "Company URL".equalsIgnoreCase(url)) {
							url = null;
						}
						if ("".equals(note) || "Note".equalsIgnoreCase(note)) {
							note = null;
						}
						integrationKey.setPhoneNumber(phone);
						integrationKey.setCompanyUrl(url);
						integrationKey.setNote(note);
						integrationKey.setEmail(email);
						integrationKey.setOrgName(org);
						integrationKey.setProductName(pName);
						integrationKey.setPresentStatus(0);
						key = IdosUtil.getIntegrationKey();
						integrationKey.setAuthKey(key);
						et = entityManager.getTransaction();
						et.begin();
						genericDAO.saveOrUpdate(integrationKey, null, entityManager);
						et.commit();
						final String username = ConfigFactory.load().getString("smtp.user");
						String body = apiIntegrationLink.render(org, pName, email, url, phone, note, key, INTERNAL_MSG,
								true, ConfigParams.getInstance()).body();
						StaticController.mailTimer(body, username, StaticController.emailsession, "alerts@myidos.com",
								null, API_INTEGRATION);
						body = apiIntegrationLink.render(org, pName, email, url, phone, note, key, EXTERNAL_MSG, false,
								ConfigParams.getInstance()).body();
						StaticController.mailTimer(body, username, StaticController.emailsession, email, null,
								API_INTEGRATION);
						on.put("result", true);
						on.put("message1", "Please note your API key");
						on.put("message2", key);
						on.remove("message");
					} else {
						integrationKey = integrationKeys.get(0);
						key = integrationKey.getAuthKey();
						on.put("result", true);
						on.put("message1", "You have registered for the API integration.<br/>Key is");
						on.put("message2", key);
						on.remove("message");
					}
				}
			}
		} catch (Exception e) {
			if (null != et && et.isActive()) {
				et.rollback();
			}
			throw e;
		}
		return on;
	}

	@Override
	public String activateApiAccess(String email, String key) throws Exception {
		log.log(Level.FINE, ">>>> Start");
		String result = "Something went wrong.";
		EntityTransaction et = null;
		try {
			if (null != email && !"".equals(email) && null != key && !"".equals(key)) {
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("email", email);
				criterias.put("authKey", key);
				criterias.put("presentStatus", 1);
				// entityManager.em = jpaApi.em();
				IdosIntegrationKey iKey = genericDAO.getByCriteria(IdosIntegrationKey.class, criterias, entityManager);
				if (null != iKey) {
					iKey.setPresentStatus(1);
					et = entityManager.getTransaction();
					et.begin();
					genericDAO.saveOrUpdate(iKey, null, entityManager);
					et.commit();
					StringBuilder msg = new StringBuilder();
					msg.append("API Access has been granted for the Organization : ").append(iKey.getOrgName());
					msg.append(" registered with Email ID : ").append(iKey.getEmail());
					result = msg.toString();
				} else {
					result = "Organization with the email and key specified is not found.";
				}
			}
		} catch (Exception e) {
			if (null != et && et.isActive()) {
				et.rollback();
			}
			throw e;
		}
		return result;
	}

	@Override
	public boolean saveIdosLeads(String name, String email, String phone, int type) {
		log.log(Level.FINE, ">>>> Start");
		boolean result = Boolean.FALSE;

		IdosLeads leads = new IdosLeads();
		if (null == name || "".equals(name)) {
			leads.setName(name);
		} else {
			leads.setName(name);
		}
		leads.setEmail(email);
		leads.setSource("Web");
		leads.setPhone(phone);
		leads.setEnquiryType(type);
		// entityManager.manager = jpaApi.em();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		genericDAO.saveOrUpdate(leads, null, entityManager);
		transaction.commit();
		result = Boolean.TRUE;

		return result;
	}

}