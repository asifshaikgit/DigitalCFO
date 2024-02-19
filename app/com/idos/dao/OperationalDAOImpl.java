package com.idos.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IdosConstants;
import model.Branch;
import model.BranchInsurance;
import model.Organization;
import model.OrganizationOperationalRemainders;
import model.StatutoryDetails;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

public class OperationalDAOImpl implements OperationalDAO {

	@Override
	public ObjectNode operationalAlertsDates(Users user, String monthFirstDate, String monthLastDate,
			EntityManager entityManager) {
		ObjectNode result = Json.newObject();
		try {
			ArrayNode branchWisePremiseAlertLiveDates = result.putArray("branchWisePremiseAlertLiveDatesData");
			ArrayNode branchWiseStatutoryAlertLiveDates = result.putArray("branchWiseStatutoryAlertLiveDatesData");
			ArrayNode branchWiseOperationalAlertLiveDates = result.putArray("branchWiseOperationalAlertLiveDatesData");
			ArrayNode branchWiseInsurenceAlertLiveDates = result.putArray("branchWiseInsurenceAlertLiveDatesData");
			Date startDt = IdosConstants.mysqldf.parse(monthFirstDate);
			Date endDt = IdosConstants.mysqldf.parse(monthLastDate);
			Organization orgn = user.getOrganization();
			List<Branch> branchList = orgn.getBranches();
			for (Branch bnch : branchList) {
				if (bnch.getAggreementValidTo() != null) {
					if ((bnch.getAggreementValidTo().compareTo(startDt) >= 0)
							&& (bnch.getAggreementValidTo().compareTo(endDt) <= 0)) {
						ObjectNode premiserow = Json.newObject();
						premiserow.put("title", bnch.getName() + " Validity End");
						premiserow.put("start", IdosConstants.mysqldf.format(bnch.getAggreementValidTo()));
						branchWisePremiseAlertLiveDates.add(premiserow);
					}
				}
				if (bnch.getRentRevisedDueOn() != null) {
					if ((bnch.getRentRevisedDueOn().compareTo(startDt)) >= 0
							&& (bnch.getRentRevisedDueOn().compareTo(endDt) <= 0)) {
						ObjectNode premiserow = Json.newObject();
						premiserow.put("title", bnch.getName() + " Rent Revision");
						premiserow.put("start", IdosConstants.mysqldf.format(bnch.getRentRevisedDueOn()));
						branchWisePremiseAlertLiveDates.add(premiserow);
					}
				}
				if (bnch.getAggreementValidFrom() != null) {
					if (bnch.getPeriodicityOfPayment() != null) {
						if (bnch.getPeriodicityOfPayment() == 1) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 7) > 0) {
									if (7 - (diffInDays - ((diffInDays / 7) * 7)) == 7) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 2) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 30) > 0) {
									if (30 - (diffInDays - ((diffInDays / 30) * 30)) == 30) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 3) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 120) > 0) {
									if (120 - (diffInDays - ((diffInDays / 120) * 120)) == 120) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 4) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 180) > 0) {
									if (180 - (diffInDays - ((diffInDays / 180) * 180)) == 180) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 5) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 365) > 0) {
									if (365 - (diffInDays - ((diffInDays / 365) * 365)) == 365) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 6) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 730) > 0) {
									if (730 - (diffInDays - ((diffInDays / 730) * 730)) == 730) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
						if (bnch.getPeriodicityOfPayment() == 7) {
							for (int i = 0; i < 42; i++) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(startDt);
								cal.add(Calendar.DATE, i);
								Date dateToCompare = cal.getTime();
								int diffInDays = (int) ((dateToCompare.getTime()
										- bnch.getAggreementValidFrom().getTime()) / (1000 * 60 * 60 * 24));
								if ((diffInDays / 1095) > 0) {
									if (1095 - (diffInDays - ((diffInDays / 1095) * 1095)) == 1095) {
										ObjectNode premiserow = Json.newObject();
										premiserow.put("title", bnch.getName() + " Rent Payment");
										premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
										branchWisePremiseAlertLiveDates.add(premiserow);
									}
								}
							}
						}
					}
				}
				List<StatutoryDetails> bnchStatDetails = bnch.getBranchStatutoryDetails();
				for (StatutoryDetails bnchStat : bnchStatDetails) {
					if (bnchStat.getValidTo() != null) {
						if ((bnchStat.getValidTo().compareTo(startDt) >= 0)
								&& (bnchStat.getValidTo().compareTo(endDt) <= 0)) {
							ObjectNode statutoryrow = Json.newObject();
							statutoryrow.put("title",
									bnch.getName() + ":" + bnchStat.getStatutoryDetails() + " Validity End");
							statutoryrow.put("start", IdosConstants.mysqldf.format(bnchStat.getValidTo()));
							branchWiseStatutoryAlertLiveDates.add(statutoryrow);
						}
					}
				}
				List<OrganizationOperationalRemainders> bnchOperationalRemainders = bnch.getBranchOperationAlerts();
				for (OrganizationOperationalRemainders bnchOperRem : bnchOperationalRemainders) {
					if (bnchOperRem.getDueOn() != null) {
						if (bnchOperRem.getRecurrences() != null) {
							if (bnchOperRem.getRecurrences() == 1) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if (bnchOperRem.getDueOn() != null && ((dateToCompare
											.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1))) {
										if ((diffInDays / 7) > 0) {
											if (7 - (diffInDays - ((diffInDays / 7) * 7)) == 7) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 2) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if (bnchOperRem != null && ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1))) {
										if ((diffInDays / 30) > 0) {
											if (30 - (diffInDays - ((diffInDays / 30) * 30)) == 30) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 3) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1)) {
										if ((diffInDays / 120) > 0) {
											if (120 - (diffInDays - ((diffInDays / 120) * 120)) == 120) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 4) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1)) {
										if ((diffInDays / 180) > 0) {
											if (180 - (diffInDays - ((diffInDays / 180) * 180)) == 180) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 5) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1)) {
										if ((diffInDays / 365) > 0) {
											if (365 - (diffInDays - ((diffInDays / 365) * 365)) == 365) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 6) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1)) {
										if ((diffInDays / 730) > 0) {
											if (730 - (diffInDays - ((diffInDays / 730) * 730)) == 730) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
							if (bnchOperRem.getRecurrences() == 7) {
								for (int i = 0; i < 42; i++) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startDt);
									cal.add(Calendar.DATE, i);
									Date dateToCompare = cal.getTime();
									int diffInDays = (int) ((dateToCompare.getTime() - bnchOperRem.getDueOn().getTime())
											/ (1000 * 60 * 60 * 24));
									if ((dateToCompare.compareTo(bnchOperRem.getDueOn()) == 0
											|| dateToCompare.compareTo(bnchOperRem.getDueOn()) == 1)
											&& (dateToCompare.compareTo(bnchOperRem.getValidTo()) == 0
													|| dateToCompare.compareTo(bnchOperRem.getValidTo()) == -1)) {
										if ((diffInDays / 1095) > 0) {
											if (1095 - (diffInDays - ((diffInDays / 1095) * 1095)) == 1095) {
												ObjectNode premiserow = Json.newObject();
												premiserow.put("title", bnch.getName() + ":"
														+ bnchOperRem.getRequiements() + " Validity End");
												premiserow.put("start", IdosConstants.mysqldf.format(dateToCompare));
												branchWiseOperationalAlertLiveDates.add(premiserow);
											}
										}
									}
								}
							}
						}
					}
				}
				List<BranchInsurance> bnchInsurenceList = bnch.getBranchInsurance();
				for (BranchInsurance bnchInsurence : bnchInsurenceList) {
					if (bnchInsurence.getPolicyValidTo() != null) {
						if (bnchInsurence.getPolicyValidTo().compareTo(startDt) >= 0
								&& bnchInsurence.getPolicyValidTo().compareTo(startDt) <= 0) {
							ObjectNode insurencerow = Json.newObject();
							insurencerow.put("title",
									bnch.getName() + ":" + bnchInsurence.getPolicyNumber() + " Validity End");
							insurencerow.put("start", IdosConstants.mysqldf.format(bnchInsurence.getPolicyValidTo()));
							branchWiseInsurenceAlertLiveDates.add(insurencerow);
						}
					}
					if (bnchInsurence.getPolicyValidFrom() != null) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(bnchInsurence.getPolicyValidFrom());
						cal.add(Calendar.DATE, 365);
						ObjectNode insurencerow = Json.newObject();
						insurencerow.put("title",
								bnch.getName() + ":" + bnchInsurence.getPolicyNumber() + " Premium Payment");
						insurencerow.put("start", IdosConstants.mysqldf.format(cal.getTime()));
						branchWiseInsurenceAlertLiveDates.add(insurencerow);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return result;
	}
}
