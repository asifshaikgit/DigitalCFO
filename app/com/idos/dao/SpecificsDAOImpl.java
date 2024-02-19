package com.idos.dao;

import com.idos.cache.OrganizationConfigCache;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;

import model.*;
import model.payroll.PayrollSetup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.util.*;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public class SpecificsDAOImpl implements SpecificsDAO {
    public void getChildCOA(JsonNode json, Users user, EntityManager entityManager, ObjectNode results)
            throws IDOSException {
        ArrayNode an = results.putArray("coaSpecfChildData");
        String coaActCode = json.findValue("coaAccountCode") == null ? null : json.findValue("coaAccountCode").asText();
        if (coaActCode == null) {
            return;
        }
        int coaIdentForDataValid = json.findValue("identForDataValid") == null ? 0
                : json.findValue("identForDataValid").asInt();
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add("%" + coaActCode + "/");
        //List<String> bankAccounts = new ArrayList<>();
        List<Specifics> specifics = genericDao.queryWithParamsName(COA_BY_ACCOUNT_CODE_JPQL, entityManager, inparams);
        if (specifics.size() > 0) {
            for (Specifics specf : specifics) {
                ObjectNode row = Json.newObject();
                row.put("id", specf.getId());
                String description = specf.getName();
                if (specf.getInvoiceItemDescription1() != null) {
                    description += " " + specf.getInvoiceItemDescription1();
                }
                if (specf.getInvoiceItemDescription2() != null) {
                    description += " " + specf.getInvoiceItemDescription2();
                }
                row.put("name", description);
                row.put("specfaccountCode", specf.getAccountCode().toString());
                row.put("topLevelAccountCode", specf.getParticularsId().getAccountCode().toString());
                row.put("identificationForDataValid", specf.getIdentificationForDataValid());
                row.put("isTxnAndOpBalPresent", isSpecificHasTxnsAndOpeningBal(specf, entityManager));
                row.put("iscoachild", 0);
                /*if(specf.getParentSpecifics() != null){
                    if(specf.getParentSpecifics().getIdentificationForDataValid() != null && (specf.getParentSpecifics().getIdentificationForDataValid().equals("4") || specf.getParentSpecifics().getIdentificationForDataValid().equals("5"))){
                        row.put("isbankAccount", 1);
                    }else{
                        row.put("isbankAccount", 0);
                    }
                }else{
                    row.put("isbankAccount", 0);
                }*/
                an.add(row);

                /*StringBuilder newquery = null;
                newquery = new StringBuilder("select obj from BranchBankAccountMapping obj where obj.specifics.id = ")
                .append(specf.getId())
                .append(" and obj.presentStatus=1");
                
                List<BranchBankAccountMapping> bankMapList = genericDao.executeSimpleQuery(newquery.toString(), entityManager);
                if (bankMapList.size() > 0) {
                    for (BranchBankAccountMapping bank : bankMapList) {
                        bankAccounts.add(bank.getBranchBankAccounts().getAccountNumber());
                    }
                }*/
            }
        }

        String vendcust = null;
        if (coaIdentForDataValid == 1 || coaIdentForDataValid == 6) {
            vendcust = " and obj.type = 2 and obj.presentStatus=1";
        } else if (coaIdentForDataValid == 2 || coaIdentForDataValid == 7) {
            vendcust = " and obj.type = 1 and obj.presentStatus=1";
        }
        StringBuilder newsbquery = null;
        if (coaIdentForDataValid == 1 || coaIdentForDataValid == 2 || coaIdentForDataValid == 6
                || coaIdentForDataValid == 7) {

            if (coaIdentForDataValid == 1 || coaIdentForDataValid == 2) {
                log.log(Level.FINE, "Fetching vendor for liabilities /customer for assets list");
                newsbquery = new StringBuilder("select obj from Vendor obj where obj.organization.id = ")
                        .append(user.getOrganization().getId()).append(vendcust);
                // .append(" and obj.id not in (select customer.vendorSpecific.id from
                // VendorSpecific customer where customer.organization.id = ")
                // .append(user.getOrganization().getId()).append(" and customer.advanceMoney !=
                // null)");
            }
            if (coaIdentForDataValid == 6 || coaIdentForDataValid == 7) {
                log.log(Level.FINE, "Fetching vendor for assets /customer list for liabilities");
                newsbquery = new StringBuilder("select obj from Vendor obj where obj.organization.id = ")
                        .append(user.getOrganization().getId()).append(vendcust)
                        .append(" and obj.id in (select customer.vendorSpecific.id from VendorSpecific customer where customer.organization.id = ")
                        .append(user.getOrganization().getId()).append(" and customer.advanceMoney != null)");
            }

            List<Vendor> vendorList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            if (vendorList.size() > 0) {
                for (Vendor vendor : vendorList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", vendor.getId());
                    row.put("name", vendor.getName());
                    row.put("specfaccountCode", vendor.getId());
                    row.put("topLevelAccountCode", coaActCode);
                    row.put("identificationForDataValid", "0");
                    row.put("iscoachild", 1);
                    an.add(row);
                }
            }
        } else if (coaIdentForDataValid == 3 || coaIdentForDataValid == 30) {
            log.log(Level.FINE, "Fetching cash list");
            newsbquery = new StringBuilder("select obj from BranchCashCount obj where obj.organization.id = ")
                    .append(user.getOrganization().getId()).append(" and obj.presentStatus=1");
            List<BranchCashCount> branchCashList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            if (branchCashList.size() > 0) {
                for (BranchCashCount branchCash : branchCashList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", branchCash.getId());
                    if (coaIdentForDataValid == 3) {
                        row.put("name", branchCash.getBranch().getName() + " - Cash");
                    } else {
                        row.put("name", branchCash.getBranch().getName() + " - Petty Cash");
                    }
                    row.put("specfaccountCode", branchCash.getId());
                    row.put("topLevelAccountCode", coaActCode);
                    row.put("identificationForDataValid", "0");
                    row.put("iscoachild", 1);
                    an.add(row);
                }
            }
        } else if (coaIdentForDataValid == 4 || coaIdentForDataValid == 5) {
            
            newsbquery = new StringBuilder("select obj from BranchBankAccounts obj where obj.organization.id = ")
                    .append(user.getOrganization().getId())
                    .append(" and obj.presentStatus=1 and (obj.openingBalance >= 0 or obj.openingBalance is null)");

            if (coaIdentForDataValid == 5) {
                newsbquery = new StringBuilder("select obj from BranchBankAccounts obj where obj.organization.id = ")
                        .append(user.getOrganization().getId())
                        .append(" and obj.presentStatus=1 and obj.openingBalance < 0");
            }

            List<BranchBankAccounts> bankList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            List<BranchBankAccounts> newBankList = null;
            if ("ORG".equals(
                    OrganizationConfigCache.getParamValue(user.getOrganization().getId(), "bank.account.type"))) {
                // Used in BHive
                newBankList = removeDuplicateBankNames(bankList);
            } else {
                newBankList = bankList;
            }
            if (newBankList.size() > 0) {
                /*for (int i = newBankList.size() - 1; i >= 0; i--) {
                    BranchBankAccounts bank = newBankList.get(i);
                    List<BranchBankAccountMapping> branchBankMap = BranchBankAccountMapping.findByBankId(entityManager, bank);
                    if(branchBankMap == null || branchBankMap.size() == 0){*/
                for (BranchBankAccounts bank : newBankList) {
                        //if (!bankAccounts.contains(bank.getAccountNumber())) {
                            ObjectNode row = Json.newObject();
                            row.put("id", bank.getId());
                            row.put("name", bank.getBankName());
                            row.put("specfaccountCode", bank.getAccountNumber());
                            row.put("topLevelAccountCode", coaActCode);
                            row.put("identificationForDataValid", "0");
                            row.put("iscoachild", 1);
                            an.add(row);
                        //}
                    //}
                }
                
            }
        } else if (coaIdentForDataValid == 12 || coaIdentForDataValid == 13) {
            newsbquery = new StringBuilder(
                    "select obj from Users obj where id in (select distinct createdBy from  ClaimTransaction claim")
                    .append(" where claim.transactionBranchOrganization.id = ").append(user.getOrganization().getId())
                    .append(" and obj.presentStatus=1 and claim.transactionStatus='Accounted'");
            if (coaIdentForDataValid == 12) {
                newsbquery.append(" and claim.transactionPurpose.id = 15)");
            } else if (coaIdentForDataValid == 13) {
                newsbquery.append(" and claim.transactionPurpose.id = 17)");
            }

            List<Users> travelAdvUserList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            if (travelAdvUserList.size() > 0) {
                for (Users userTmp : travelAdvUserList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", userTmp.getId());
                    row.put("name", userTmp.getFullName());
                    row.put("specfaccountCode", userTmp.getId());
                    row.put("topLevelAccountCode", coaActCode);
                    row.put("identificationForDataValid", "0");
                    row.put("iscoachild", 1);
                    an.add(row);
                }
            }
        } else if ((coaIdentForDataValid == 14 || coaIdentForDataValid == 15)
                || (coaIdentForDataValid >= 39 && coaIdentForDataValid <= 50)
                || (coaIdentForDataValid >= 53 && coaIdentForDataValid <= 56)) {
            getTaxCOAChilds(coaIdentForDataValid, entityManager, user, coaActCode, an);
        } else if (coaIdentForDataValid == 57) {
            getInterBranchAccounts(entityManager, user, coaActCode, an);
        } else if (coaIdentForDataValid == 58) {
            getPayrollIncomeTypes(entityManager, user, coaActCode, an, IdosConstants.PAYROLL_TYPE_EARNINGS); // Expense->paryoll
                                                                                                             // earnings
        } else if (coaIdentForDataValid == 59) {
            getPayrollIncomeTypes(entityManager, user, coaActCode, an, IdosConstants.PAYROLL_TYPE_DEDUCTIONS); // liablities->paryoll
                                                                                                               // deductions
        } else if (coaIdentForDataValid == 60 || coaIdentForDataValid == 61) {
            // //og.debug("Fetching Employee Advance list");
            List<Users> userList = Users.findAllActByOrg(entityManager, user.getOrganization().getId());
            if (userList != null && userList.size() > 0) {
                for (Users users : userList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", users.getId());
                    if (coaIdentForDataValid == 60) {
                        row.put("name", users.getFullName() + " - Advance");
                    } else {
                        row.put("name", users.getFullName() + " - Claim");
                    }
                    row.put("specfaccountCode", users.getId());
                    row.put("topLevelAccountCode", coaActCode);
                    row.put("identificationForDataValid", "0");
                    row.put("iscoachild", 1);
                    an.add(row);
                }
            }
        }
    }

    @Override
    public void getTaxCOAChilds(int coaIdentForDataValid, EntityManager entityManager, Users user, String coaActCode,
            ArrayNode an) {
        ArrayList inparamList = new ArrayList(2);
        inparamList.add(user.getOrganization().getId());
        if (coaIdentForDataValid == 14) {
            inparamList.add(new Integer(IdosConstants.INPUT_TAX));
        } else if (coaIdentForDataValid == 39) {
            inparamList.add(new Integer(IdosConstants.INPUT_SGST));
        } else if (coaIdentForDataValid == 40) {
            inparamList.add(new Integer(IdosConstants.INPUT_CGST));
        } else if (coaIdentForDataValid == 41) {
            inparamList.add(new Integer(IdosConstants.INPUT_IGST));
        } else if (coaIdentForDataValid == 42) {
            inparamList.add(new Integer(IdosConstants.INPUT_CESS));
        } else if (coaIdentForDataValid == 53) {
            inparamList.add(new Integer(IdosConstants.RCM_SGST_IN));
        } else if (coaIdentForDataValid == 54) {
            inparamList.add(new Integer(IdosConstants.RCM_CGST_IN));
        } else if (coaIdentForDataValid == 55) {
            inparamList.add(new Integer(IdosConstants.RCM_IGST_IN));
        } else if (coaIdentForDataValid == 56) {
            inparamList.add(new Integer(IdosConstants.RCM_CESS_IN));
        } else if (coaIdentForDataValid == 15) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_TAX));
        } else if (coaIdentForDataValid == 43) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_SGST));
        } else if (coaIdentForDataValid == 44) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_CGST));
        } else if (coaIdentForDataValid == 45) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_IGST));
        } else if (coaIdentForDataValid == 46) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_CESS));
        } else if (coaIdentForDataValid == 47) {
            inparamList.add(new Integer(IdosConstants.RCM_SGST_OUTPUT));
        } else if (coaIdentForDataValid == 48) {
            inparamList.add(new Integer(IdosConstants.RCM_CGST_OUTPUT));
        } else if (coaIdentForDataValid == 49) {
            inparamList.add(new Integer(IdosConstants.RCM_IGST_OUTPUT));
        } else if (coaIdentForDataValid == 50) {
            inparamList.add(new Integer(IdosConstants.RCM_CESS_OUTPUT));
        }

        List<BranchTaxes> taxDetailList = genericDao.queryWithParams(TAX_JQL, entityManager, inparamList);
        for (BranchTaxes taxDetail : taxDetailList) {
            ObjectNode row = Json.newObject();
            row.put("id", taxDetail.getId());
            String taxName = taxDetail.getTaxName();
            if (user.getOrganization().getGstCountryCode() == null
                    || "".equals(user.getOrganization().getGstCountryCode())) {
                taxName = taxName + "-" + taxDetail.getBranch().getName();
            }
            row.put("name", taxName);
            row.put("specfaccountCode", taxDetail.getId());
            row.put("topLevelAccountCode", coaActCode);
            row.put("identificationForDataValid", "0");
            row.put("iscoachild", 1);
            row.put("rate", taxDetail.getTaxRate());
            an.add(row);
        }
    }

    private void getInterBranchAccounts(EntityManager em, Users user, String coaActCode, ArrayNode an) {
        Query query = em.createQuery(COA_57_JPQL);
        query.setParameter(1, user.getOrganization().getId());
        List<Object[]> branchList = query.getResultList();
        for (Object[] branch : branchList) {
            ObjectNode row = Json.newObject();
            Long id = (Long) branch[4];
            row.put("id", id);
            String name = (String) branch[2] + "-" + (String) branch[3];
            row.put("name", name);
            row.put("specfaccountCode", id);
            row.put("topLevelAccountCode", coaActCode);
            row.put("identificationForDataValid", "0");
            row.put("iscoachild", 1);
            an.add(row);
        }
    }

    @Override
    public boolean getInterBranchAccountsWithHead(EntityManager em, Users user, ArrayNode an) {
        boolean retValue = false;
        Query query = em.createQuery(COA_57_JPQL);
        query.setParameter(1, user.getOrganization().getId());
        List<Object[]> branchList = query.getResultList();
        if (branchList.size() > 0) {
            retValue = true;
        }
        for (Object[] branch : branchList) {
            ObjectNode row = Json.newObject();
            String id = IdosConstants.HEAD_INTR_BRANCH + String.valueOf(branch[0]) + "-" + String.valueOf(branch[1]);
            row.put("id", id);
            row.put("interbranchid", IdosConstants.HEAD_INTR_BRANCH + (Long) branch[4]);
            String name = (String) branch[2] + "-" + (String) branch[3];
            row.put("name", name);
            row.put("headType", IdosConstants.HEAD_INTR_BRANCH);
            an.add(row);
        }
        return retValue;
    }

    @Override
    public boolean getInterBranchAccountsWithHead4Branch(EntityManager em, Users user, ArrayNode an, long branchId) {
        boolean retValue = false;
        Query query = em.createQuery(COA_57_BRANCH_JPQL);
        query.setParameter(1, user.getOrganization().getId());
        query.setParameter(2, branchId);
        List<Object[]> branchList = query.getResultList();
        if (branchList.size() > 0) {
            retValue = true;
        }
        for (Object[] branch : branchList) {
            ObjectNode row = Json.newObject();
            String id = IdosConstants.HEAD_INTR_BRANCH + String.valueOf(branch[0]) + "-" + String.valueOf(branch[1]);
            row.put("id", id);
            row.put("interbranchid", IdosConstants.HEAD_INTR_BRANCH + (Long) branch[4]);
            String name = (String) branch[2] + "-" + (String) branch[3];
            row.put("name", name);
            row.put("headType", IdosConstants.HEAD_INTR_BRANCH);
            an.add(row);
        }
        return retValue;
    }

    @Override
    public boolean getInterBranchWithIdHead(EntityManager em, Users user, ArrayNode an) {
        boolean retValue = false;
        Query query = em.createQuery(COA_57_JPQL);
        query.setParameter(1, user.getOrganization().getId());
        List<Object[]> branchList = query.getResultList();
        if (branchList.size() > 0) {
            retValue = true;
        }
        for (Object[] branch : branchList) {
            ObjectNode row = Json.newObject();
            String id = IdosConstants.HEAD_INTR_BRANCH + (Long) branch[4];
            row.put("id", id);
            String name = (String) branch[2] + "-" + (String) branch[3];
            row.put("name", name);
            row.put("headType", IdosConstants.HEAD_INTR_BRANCH);
            an.add(row);
        }
        return retValue;
    }

    @Override
    public boolean getInterBranchMappingAccountsWithHead(EntityManager em, Users user, ArrayNode an) {
        boolean retValue = false;
        Query query = em.createQuery(COA_57_JPQL);
        query.setParameter(1, user.getOrganization().getId());
        List<Object[]> branchList = query.getResultList();
        if (branchList.size() > 0) {
            retValue = true;
        }
        for (Object[] branch : branchList) {
            ObjectNode row = Json.newObject();
            String id = IdosConstants.HEAD_INTR_BRANCH + String.valueOf(branch[4]);
            row.put("id", id);
            String name = (String) branch[2] + "-" + (String) branch[3];
            row.put("name", name);
            row.put("headType", IdosConstants.HEAD_INTR_BRANCH);
            an.add(row);
        }
        return retValue;
    }

    private void getPayrollIncomeTypes(EntityManager em, Users user, String coaActCode, ArrayNode an, int payrollType) {
        // earning list
        Map criterias = new HashMap();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("payrollType", payrollType); // Earning/Deductions list
        criterias.put("presentStatus", 1);
        List<PayrollSetup> payrollSetupList = genericDao.findByCriteria(PayrollSetup.class, criterias, em);
        if (payrollSetupList != null && payrollSetupList.size() > 0) {
            for (PayrollSetup payrollItem : payrollSetupList) {
                ObjectNode row = Json.newObject();
                row.put("id", payrollItem.getId());
                row.put("name", payrollItem.getPayrollHeadName());
                row.put("specfaccountCode", payrollItem.getId());
                row.put("topLevelAccountCode", coaActCode);
                row.put("identificationForDataValid", "0");
                row.put("iscoachild", 1);
                an.add(row);
            }
        }
    }

    @Override
    public void getTaxCOAChildsForBranch(int coaIdentForDataValid, EntityManager entityManager, Users user,
            String coaActCode, Long branchId, ArrayNode an) {
        ArrayList inparamList = new ArrayList(3);
        inparamList.add(user.getOrganization().getId());
        inparamList.add(branchId);
        if (coaIdentForDataValid == 14) {
            inparamList.add(new Integer(IdosConstants.INPUT_TAX));
        } else if (coaIdentForDataValid == 39) {
            inparamList.add(new Integer(IdosConstants.INPUT_SGST));
        } else if (coaIdentForDataValid == 40) {
            inparamList.add(new Integer(IdosConstants.INPUT_CGST));
        } else if (coaIdentForDataValid == 41) {
            inparamList.add(new Integer(IdosConstants.INPUT_IGST));
        } else if (coaIdentForDataValid == 42) {
            inparamList.add(new Integer(IdosConstants.INPUT_CESS));
        } else if (coaIdentForDataValid == 53) {
            inparamList.add(new Integer(IdosConstants.RCM_SGST_IN));
        } else if (coaIdentForDataValid == 54) {
            inparamList.add(new Integer(IdosConstants.RCM_CGST_IN));
        } else if (coaIdentForDataValid == 55) {
            inparamList.add(new Integer(IdosConstants.RCM_IGST_IN));
        } else if (coaIdentForDataValid == 56) {
            inparamList.add(new Integer(IdosConstants.RCM_CESS_IN));
        } else if (coaIdentForDataValid == 15) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_TAX));
        } else if (coaIdentForDataValid == 43) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_SGST));
        } else if (coaIdentForDataValid == 44) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_CGST));
        } else if (coaIdentForDataValid == 45) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_IGST));
        } else if (coaIdentForDataValid == 46) {
            inparamList.add(new Integer(IdosConstants.OUTPUT_CESS));
        } else if (coaIdentForDataValid == 47) {
            inparamList.add(new Integer(IdosConstants.RCM_SGST_OUTPUT));
        } else if (coaIdentForDataValid == 48) {
            inparamList.add(new Integer(IdosConstants.RCM_CGST_OUTPUT));
        } else if (coaIdentForDataValid == 49) {
            inparamList.add(new Integer(IdosConstants.RCM_IGST_OUTPUT));
        } else if (coaIdentForDataValid == 50) {
            inparamList.add(new Integer(IdosConstants.RCM_CESS_OUTPUT));
        }

        List<BranchTaxes> taxDetailList = genericDao.queryWithParams(TAX_BRANCH_JQL, entityManager, inparamList);
        for (BranchTaxes taxDetail : taxDetailList) {
            ObjectNode row = Json.newObject();
            row.put("id", taxDetail.getId());
            String taxName = taxDetail.getTaxName();
            if (user.getOrganization().getGstCountryCode() == null
                    || "".equals(user.getOrganization().getGstCountryCode())) {
                taxName = taxName + "-" + taxDetail.getBranch().getName();
            }
            row.put("name", taxName);
            row.put("specfaccountCode", taxDetail.getId());
            row.put("topLevelAccountCode", coaActCode);
            row.put("identificationForDataValid", "0");
            row.put("iscoachild", 1);
            row.put("rate", taxDetail.getTaxRate());
            an.add(row);
        }
    }

    @Override
    public Boolean isSpecificHasTxnsAndOpeningBal(Specifics specifics, EntityManager entityManager)
            throws IDOSException {
        Boolean flag = false;
        if (specifics.getTotalOpeningBalance() != null && specifics.getTotalOpeningBalance() != 0.0) {
            flag = true;
        }
        if (!flag) {
            List<Transaction> transList = Transaction.findByOrgSpecific(entityManager,
                    specifics.getOrganization().getId(), specifics.getId());
            if (transList != null && transList.size() > 0) {
                flag = true;
            }
        }
        if (!flag) {
            List<TransactionItems> transItemList = TransactionItems.findByOrgSpecific(entityManager,
                    specifics.getOrganization().getId(), specifics.getId());
            if (transItemList != null && transItemList.size() > 0) {
                flag = true;
            }
        }
        if (!flag) {
            List<ProvisionJournalEntryDetail> pjeList = ProvisionJournalEntryDetail.findByOrgSpecific(entityManager,
                    specifics.getOrganization().getId(), specifics.getId());
            if (pjeList != null && pjeList.size() > 0) {
                flag = true;
            }
        }
        return flag;
    }

    /** used in Bhive */
    private static List<BranchBankAccounts> removeDuplicateBankNames(List<BranchBankAccounts> list) {
        Map<String, BranchBankAccounts> map = new HashMap<>();
        ArrayList<BranchBankAccounts> uniqueBanks = new ArrayList<>();
        for (BranchBankAccounts branchBankAccounts : list) {
            if (branchBankAccounts.getAccountNumber() != null && !branchBankAccounts.getAccountNumber().equals("")
                    && branchBankAccounts.getBankName() != null && !branchBankAccounts.getBankName().equals("")) {
                if (!map.containsKey(branchBankAccounts.getAccountNumber())) {
                    map.put(branchBankAccounts.getAccountNumber(), branchBankAccounts);
                }
            }
        }
        Iterator itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry mapElement = (Map.Entry) itr.next();
            BranchBankAccounts bankAccounts = (BranchBankAccounts) mapElement.getValue();
            uniqueBanks.add(bankAccounts);
        }
        return uniqueBanks;
    }
}
