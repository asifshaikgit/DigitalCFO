package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Branch;
import model.BranchSpecifics;
import model.Particulars;
import model.Specifics;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IdosConstants;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import controllers.StaticController;
import play.libs.Json;

public class BudgetServiceImpl implements BudgetService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BudgetServiceImpl() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Override
	public ObjectNode getBudgetDetails(final Users user) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode on = Json.newObject(), sr = null, ssr = null;
		ArrayNode an = on.putArray("details"), san = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		Particulars part = null;
		List<BranchSpecifics> specifics = Collections.emptyList();
		List<Branch> branches = user.getOrganization().getBranches();
		on.put("size", branches.size());
		if (null != branches && branches.size() > 0 && !branches.isEmpty()) {
			for (Branch branch : branches) {
				if (null != branch && null != branch.getId()) {
					sr = Json.newObject();
					san = sr.putArray("specifics");
					sr.put("isBranch", true);
					sr.put("id", branch.getId());
					if (null != branch.getName()) {
						sr.put("name", branch.getName());
					} else {
						sr.put("name", "");
					}
					if (null != branch.getBudgetAmountJan()) {
						sr.put("january", IdosConstants.decimalFormat.format(branch.getBudgetAmountJan()));
					} else {
						sr.put("january", "");
					}
					if (null != branch.getBudgetAmountFeb()) {
						sr.put("february", IdosConstants.decimalFormat.format(branch.getBudgetAmountFeb()));
					} else {
						sr.put("february", "");
					}
					if (null != branch.getBudgetAmountMar()) {
						sr.put("march", IdosConstants.decimalFormat.format(branch.getBudgetAmountMar()));
					} else {
						sr.put("march", "");
					}
					if (null != branch.getBudgetAmountApr()) {
						sr.put("april", IdosConstants.decimalFormat.format(branch.getBudgetAmountApr()));
					} else {
						sr.put("april", "");
					}
					if (null != branch.getBudgetAmountMay()) {
						sr.put("may", IdosConstants.decimalFormat.format(branch.getBudgetAmountMay()));
					} else {
						sr.put("may", "");
					}
					if (null != branch.getBudgetAmountJune()) {
						sr.put("june", IdosConstants.decimalFormat.format(branch.getBudgetAmountJune()));
					} else {
						sr.put("june", "");
					}
					if (null != branch.getBudgetAmountJuly()) {
						sr.put("july", IdosConstants.decimalFormat.format(branch.getBudgetAmountJuly()));
					} else {
						sr.put("july", "");
					}
					if (null != branch.getBudgetAmountAug()) {
						sr.put("august", IdosConstants.decimalFormat.format(branch.getBudgetAmountAug()));
					} else {
						sr.put("august", "");
					}
					if (null != branch.getBudgetAmountSep()) {
						sr.put("september", IdosConstants.decimalFormat.format(branch.getBudgetAmountSep()));
					} else {
						sr.put("september", "");
					}
					if (null != branch.getBudgetAmountOct()) {
						sr.put("october", IdosConstants.decimalFormat.format(branch.getBudgetAmountOct()));
					} else {
						sr.put("october", "");
					}
					if (null != branch.getBudgetAmountNov()) {
						sr.put("november", IdosConstants.decimalFormat.format(branch.getBudgetAmountNov()));
					} else {
						sr.put("november", "");
					}
					if (null != branch.getBudgetAmountDec()) {
						sr.put("december", IdosConstants.decimalFormat.format(branch.getBudgetAmountDec()));
					} else {
						sr.put("december", "");
					}
					if (null != branch.getBudgetTotal()) {
						sr.put("total", IdosConstants.decimalFormat.format(branch.getBudgetTotal()));
					} else {
						sr.put("total", "0.00");
					}
					if (null != branch.getBudgetDeductedAmountJan()) {
						sr.put("januaryDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountJan()));
					} else {
						sr.put("januaryDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountFeb()) {
						sr.put("februaryDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountFeb()));
					} else {
						sr.put("februaryDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountMar()) {
						sr.put("marchDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountMar()));
					} else {
						sr.put("marchDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountApr()) {
						sr.put("aprilDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountApr()));
					} else {
						sr.put("aprilDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountMay()) {
						sr.put("mayDeducted", IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountMay()));
					} else {
						sr.put("mayDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountJune()) {
						sr.put("juneDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountJune()));
					} else {
						sr.put("juneDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountJuly()) {
						sr.put("julyDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountJuly()));
					} else {
						sr.put("julyDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountAug()) {
						sr.put("augustDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountAug()));
					} else {
						sr.put("augustDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountSep()) {
						sr.put("septemberDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountSep()));
					} else {
						sr.put("septemberDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountOct()) {
						sr.put("octoberDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountOct()));
					} else {
						sr.put("octoberDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountNov()) {
						sr.put("novemberDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountNov()));
					} else {
						sr.put("novemberDeducted", "");
					}
					if (null != branch.getBudgetDeductedAmountDec()) {
						sr.put("decemberDeducted",
								IdosConstants.decimalFormat.format(branch.getBudgetDeductedAmountDec()));
					} else {
						sr.put("decemberDeducted", "");
					}
					if (null != branch.getBudgetDeductedTotal()) {
						sr.put("totalDeducted", IdosConstants.decimalFormat.format(branch.getBudgetDeductedTotal()));
					} else {
						sr.put("totalDeducted", "0.00");
					}
					criterias.clear();
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("accountCode", 2000000000000000000L);
					criterias.put("presentStatus", 1);
					part = genericDAO.getByCriteria(Particulars.class, criterias, entityManager);
					if (null != part) {
						criterias.clear();
						criterias.put("branch.id", branch.getId());
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("particular.id", part.getId());
						criterias.put("presentStatus", 1);
						specifics = genericDAO.findByCriteria(BranchSpecifics.class, criterias, entityManager);
						if (null != specifics && specifics.size() > 0) {
							sr.put("size", specifics.size());
							for (BranchSpecifics specific : specifics) {
								if (null != specific && null != specific.getId()) {
									String hasChildorNoQuery = "select obj from Specifics obj where obj.organization.id= ?1 and obj.parentSpecifics.id= ?2 and obj.presentStatus=1";
									ArrayList<Object> inparam1 = new ArrayList<Object>(3);
									inparam1.add(specific.getOrganization().getId());
									inparam1.add(specific.getSpecifics().getId());
									List<Specifics> hasChildorNo = genericDAO.queryWithParamsName(hasChildorNoQuery,
											entityManager, inparam1);
									if (hasChildorNo.size() == 0) {
										if (specific.getSpecifics().getParentSpecifics() != null) {
											ssr = Json.newObject();
											ssr.put("isBranch", false);
											ssr.put("id", specific.getId());
											if (null != specific.getSpecifics()
													&& null != specific.getSpecifics().getName()) {
												ssr.put("name", specific.getSpecifics().getName());
											} else {
												ssr.put("name", "");
											}
											if (null != specific.getBudgetAmountJan()) {
												ssr.put("january", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountJan()));
											} else {
												ssr.put("january", "");
											}
											if (null != specific.getBudgetAmountFeb()) {
												ssr.put("february", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountFeb()));
											} else {
												ssr.put("february", "");
											}
											if (null != specific.getBudgetAmountMar()) {
												ssr.put("march", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountMar()));
											} else {
												ssr.put("march", "");
											}
											if (null != specific.getBudgetAmountApr()) {
												ssr.put("april", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountApr()));
											} else {
												ssr.put("april", "");
											}
											if (null != specific.getBudgetAmountMay()) {
												ssr.put("may", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountMay()));
											} else {
												ssr.put("may", "");
											}
											if (null != specific.getBudgetAmountJune()) {
												ssr.put("june", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountJune()));
											} else {
												ssr.put("june", "");
											}
											if (null != specific.getBudgetAmountJuly()) {
												ssr.put("july", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountJuly()));
											} else {
												ssr.put("july", "");
											}
											if (null != specific.getBudgetAmountAug()) {
												ssr.put("august", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountAug()));
											} else {
												ssr.put("august", "");
											}
											if (null != specific.getBudgetAmountSep()) {
												ssr.put("september", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountSep()));
											} else {
												ssr.put("september", "");
											}
											if (null != specific.getBudgetAmountOct()) {
												ssr.put("october", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountOct()));
											} else {
												ssr.put("october", "");
											}
											if (null != specific.getBudgetAmountNov()) {
												ssr.put("november", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountNov()));
											} else {
												ssr.put("november", "");
											}
											if (null != specific.getBudgetAmountDec()) {
												ssr.put("december", IdosConstants.decimalFormat
														.format(specific.getBudgetAmountDec()));
											} else {
												ssr.put("december", "");
											}
											if (null != specific.getBudgetTotal()) {
												ssr.put("total",
														IdosConstants.decimalFormat.format(specific.getBudgetTotal()));
											} else {
												ssr.put("total", "0.00");
											}
											if (null != specific.getBudgetDeductedAmountJan()) {
												ssr.put("januaryDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountJan()));
											} else {
												ssr.put("januaryDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountFeb()) {
												ssr.put("februaryDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountFeb()));
											} else {
												ssr.put("februaryDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountMar()) {
												ssr.put("marchDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountMar()));
											} else {
												ssr.put("marchDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountApr()) {
												ssr.put("aprilDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountApr()));
											} else {
												ssr.put("aprilDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountMay()) {
												ssr.put("mayDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountMay()));
											} else {
												ssr.put("mayDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountJune()) {
												ssr.put("juneDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountJune()));
											} else {
												ssr.put("juneDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountJuly()) {
												ssr.put("julyDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountJuly()));
											} else {
												ssr.put("julyDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountAug()) {
												ssr.put("augustDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountAug()));
											} else {
												ssr.put("augustDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountSep()) {
												ssr.put("septemberDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountSep()));
											} else {
												ssr.put("septemberDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountOct()) {
												ssr.put("octoberDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountOct()));
											} else {
												ssr.put("octoberDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountNov()) {
												ssr.put("novemberDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountNov()));
											} else {
												ssr.put("novemberDeducted", "");
											}
											if (null != specific.getBudgetDeductedAmountDec()) {
												ssr.put("decemberDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedAmountDec()));
											} else {
												ssr.put("decemberDeducted", "");
											}
											if (null != specific.getBudgetDeductedTotal()) {
												ssr.put("totalDeducted", IdosConstants.decimalFormat
														.format(specific.getBudgetDeductedTotal()));
											} else {
												ssr.put("totalDeducted", "0.00");
											}
											san.add(ssr);
										}
									}
								}
							}
						}
					}
					an.add(sr);
				}
			}
		}
		on.put("result", true);
		return on;
	}

}
