package service;

import com.idos.dao.AuditDAO;
import com.idos.dao.GenericDAO;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.*;

/**
 * Created by Sunil Namdev on 14-08-2016.
 */
public class BranchCashServiceImpl implements BranchCashService {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public boolean saveBranchCash(JsonNode json, Branch newBranch, Users usr, Organization org,
            String branchrentRevisionDueOn, String premiseType, EntityManager entityManager, GenericDAO genericDAO,
            AuditDAO auditDAO, Session emailsession, String ipAddress) throws Exception {

        Map<Object, Object> criterias = new HashMap<Object, Object>();

        String keyDepositOpeningBalance = json.findPath("keyDepositOpeningBalance") == null ? null
                : json.findPath("keyDepositOpeningBalance").asText().trim();
        String pettyCashOpeningBalance = json.findPath("pettyCashOpeningBalance") == null ? null
                : json.findPath("pettyCashOpeningBalance").asText().trim();

        // start branch safe deposit box logic
        BranchDepositBoxKey newBranchDepBoxKey = null;
        String keyDepositHiddenId = json.findPath("keyDepHidIds") == null ? null
                : json.findPath("keyDepHidIds").asText().trim();
        String keyDepositName = json.findPath("keyDepName") == null ? null
                : json.findPath("keyDepName").asText().trim();
        // String keyDepositOpeningBalance = json.findPath("keyDepositOpeningBalance")
        // == null ? null : json.findPath("keyDepositOpeningBalance").asText().trim();
        String keyDepositPhNoCtryCd = json.findPath("kPhNoCtryCd") == null ? null
                : json.findPath("kPhNoCtryCd").asText().trim();
        String keyDepositPhNo = json.findPath("keyDepPhNo") == null ? null
                : json.findPath("keyDepPhNo").asText().trim();
        String keyDepositEmail = json.findPath("keyDepEmail") == null ? null
                : json.findPath("keyDepEmail").asText().trim();
        String cashierName = json.findPath("cashName") == null ? null : json.findPath("cashName").asText().trim();
        String cashierPhNoCtryCd = json.findPath("cashierCtryCode") == null ? null
                : json.findPath("cashierCtryCode").asText().trim();
        String cashierPhNo = json.findPath("cashierNo") == null ? null : json.findPath("cashierNo").asText().trim();
        String cashierMail = json.findPath("cashierMail") == null ? null : json.findPath("cashierMail").asText().trim();
        String cashierKL = json.findPath("cashierKL") == null ? null : json.findPath("cashierKL").asText().trim();
        String cashierPettyTxnApprReqd = json.findPath("cashierPettyTxnApprReqd") == null ? null
                : json.findPath("cashierPettyTxnApprReqd").asText().trim();
        String cashierPettyTxnApprAmtLimit = json.findPath("cashierPettyTxnApprAmtLimit") == null ? null
                : json.findPath("cashierPettyTxnApprAmtLimit").asText().trim();
        // String pettyCashOpeningBalance = json.findPath("pettyCashOpeningBalance") ==
        // null ? null : json.findPath("pettyCashOpeningBalance").asText().trim();
        if (keyDepositHiddenId != null && !keyDepositHiddenId.equals("")) {
            newBranchDepBoxKey = BranchDepositBoxKey.findById(IdosUtil.convertStringToLong(keyDepositHiddenId));
        } else {
            newBranchDepBoxKey = new BranchDepositBoxKey();
        }

        if (keyDepositName != null) {
            newBranchDepBoxKey.setName(keyDepositName);
        } else {
            newBranchDepBoxKey.setName(null);
        }

        if (keyDepositPhNoCtryCd != null) {
            newBranchDepBoxKey.setCountryPhnCode(keyDepositPhNoCtryCd);
        } else {
            newBranchDepBoxKey.setCountryPhnCode(null);
        }
        if (keyDepositPhNo != null) {
            newBranchDepBoxKey.setPhoneNumber(keyDepositPhNo);
        } else {
            newBranchDepBoxKey.setPhoneNumber(null);
        }
        if (keyDepositEmail != null) {
            newBranchDepBoxKey.setEmail(keyDepositEmail);
        } else {
            newBranchDepBoxKey.setEmail(null);
        }
        newBranchDepBoxKey.setOrganization(org);
        newBranchDepBoxKey.setBranch(newBranch);

        // bnchdepboxcrud.save(user, newBranchDepBoxKey, entityManager);

        if (keyDepositHiddenId != null && !keyDepositHiddenId.equals("")) {
            // creates cashier user
            if (cashierMail != null && !cashierMail.equals("")) {
                newBranchDepBoxKey.setCashierName(cashierName);
                newBranchDepBoxKey.setCashierPhnNoCountryCode(cashierPhNoCtryCd);
                newBranchDepBoxKey.setCashierPhnNo(cashierPhNo);
                newBranchDepBoxKey.setCashierEmail(cashierMail);
                newBranchDepBoxKey.setCashierKnowledgeLibrary(cashierKL);
                genericDAO.saveOrUpdate(newBranchDepBoxKey, usr, entityManager);

                criterias.clear();
                /*
                 * criterias.put("email", cashierMail);
                 * criterias.put("organization.id", org.getId());
                 * Users newCashierDuringUpdate = genericDAO.getByCriteria(Users.class,
                 * criterias, entityManager);
                 * 
                 * criterias.clear();
                 * criterias.put("branchSafeDepositBox.id", Long.parseLong(keyDepositHiddenId));
                 * Users branchCashierUser = genericDAO.getByCriteria(Users.class, criterias,
                 * entityManager);
                 * 
                 * criterias.clear();
                 * criterias.put("id", 8L);
                 * Role role = genericDAO.getByCriteria(Role.class, criterias, entityManager);
                 * 
                 * if (branchCashierUser != null && newCashierDuringUpdate != null) {
                 * branchCashierUser.setFullName(cashierName);
                 * branchCashierUser.setEmail(cashierMail);
                 * branchCashierUser.setOrganization(org);
                 * branchCashierUser.setBranch(newBranch);
                 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
                 * auditDAO.
                 * saveAuditLogs("updated cashier user for the branch cash/safe deposit box",
                 * usr, branchCashierUser.getId(), Users.class, ipAddress, json.toString(),
                 * entityManager);
                 * } else {
                 * if (branchCashierUser != null) {
                 * branchCashierUser.setBranchSafeDepositBox(null);
                 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
                 * }
                 * branchCashierUser = new Users();
                 * branchCashierUser.setFullName(cashierName);
                 * branchCashierUser.setEmail(cashierMail);
                 * branchCashierUser.setOrganization(org);
                 * branchCashierUser.setBranch(newBranch);
                 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
                 * String password = PasswordUtil.gen(10);
                 * String body = userAccountCreation.render(branchCashierUser.getEmail(),
                 * password,ConfigParams.getInstance()).body();
                 * final String username =
                 * Play.application().configuration().getString("smtp.user");
                 * Session session = emailsession;
                 * String subject = "Successfully Created Cashier Users for Organization " +
                 * org.getName();
                 * StaticController.mailTimer1(body, username, session,
                 * branchCashierUser.getEmail(), null, subject);
                 * }
                 * criterias.clear();
                 * criterias.put("branchSafeDepositBox.id", Long.parseLong(keyDepositHiddenId));
                 * UsersRoles branchCashierUserRoles =
                 * genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
                 * if (branchCashierUserRoles != null) {
                 * branchCashierUserRoles.setRole(role);
                 * branchCashierUserRoles.setUser(branchCashierUser);
                 * branchCashierUserRoles.setOrganization(org);
                 * branchCashierUserRoles.setBranch(newBranch);
                 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
                 * auditDAO.
                 * saveAuditLogs("updated cashier user for the branch cash/safe deposit box",
                 * usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
                 * json.toString(), entityManager);
                 * } else if (branchCashierUserRoles == null) {
                 * branchCashierUserRoles = new UsersRoles();
                 * branchCashierUserRoles.setRole(role);
                 * branchCashierUserRoles.setUser(branchCashierUser);
                 * branchCashierUserRoles.setOrganization(org);
                 * branchCashierUserRoles.setBranch(newBranch);
                 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
                 * auditDAO.
                 * saveAuditLogs("Created cashier user roles for the branch cash/safe deposit box"
                 * , usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
                 * json.toString(), entityManager);
                 * }
                 */
            }
        } else {
            // creates cashier user!
            if (cashierMail != null && !cashierMail.equals("")) {
                newBranchDepBoxKey.setCashierName(cashierName);
                newBranchDepBoxKey.setCashierPhnNoCountryCode(cashierPhNoCtryCd);
                newBranchDepBoxKey.setCashierPhnNo(cashierPhNo);
                newBranchDepBoxKey.setCashierEmail(cashierMail);
                newBranchDepBoxKey.setCashierKnowledgeLibrary(cashierKL);
                genericDAO.saveOrUpdate(newBranchDepBoxKey, usr, entityManager);

                Users branchCashierUser = new Users();
                UsersRoles branchCashierUserRoles = new UsersRoles();
                criterias.clear();
                /*
                 * criterias.put("email", cashierMail);
                 * criterias.put("organization.id", org.getId());
                 * Users existingusr = genericDAO.getByCriteria(Users.class, criterias,
                 * entityManager);
                 * if (existingusr != null) {
                 * criterias.clear();
                 * criterias.put("role.id", 8L);
                 * criterias.put("user.id", existingusr.getId());
                 * UsersRoles existingUserRoles = genericDAO.getByCriteria(UsersRoles.class,
                 * criterias, entityManager);
                 * branchCashierUser = existingusr;
                 * branchCashierUserRoles = existingUserRoles;
                 * }
                 * if (branchCashierUserRoles == null) {
                 * branchCashierUserRoles = new UsersRoles();
                 * }
                 * branchCashierUser.setFullName(cashierName);
                 * branchCashierUser.setEmail(cashierMail);
                 * branchCashierUser.setOrganization(org);
                 * branchCashierUser.setBranch(newBranch);
                 * String password = PasswordUtil.gen(10);
                 * branchCashierUser.setPassword(PasswordUtil.encrypt(password));
                 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
                 * auditDAO.saveAuditLogs("created cashier user for the branch safe/deposit box"
                 * , usr, branchCashierUser.getId(), Users.class, ipAddress, json.toString(),
                 * entityManager);
                 * criterias.clear();
                 * criterias.put("id", 8L);
                 * Role role = genericDAO.getByCriteria(Role.class, criterias, entityManager);
                 * branchCashierUserRoles.setRole(role);
                 * branchCashierUserRoles.setUser(branchCashierUser);
                 * branchCashierUserRoles.setOrganization(org);
                 * branchCashierUserRoles.setBranch(newBranch);
                 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
                 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
                 * auditDAO.
                 * saveAuditLogs("created cashier user role for the branch safe/deposit box",
                 * usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
                 * json.toString(), entityManager);
                 * String body = userAccountCreation.render(branchCashierUser.getEmail(),
                 * password,ConfigParams.getInstance()).body();
                 * final String username =
                 * Play.application().configuration().getString("smtp.user");
                 * Session session = emailsession;
                 * String subject = "Successfully Created Cashier Users for Organization " +
                 * org.getName();
                 * StaticController.mailTimer1(body, username, session,
                 * branchCashierUser.getEmail(), null, subject);
                 */
            }
        }
        if (cashierPettyTxnApprReqd != null && !cashierPettyTxnApprReqd.equals("")) {
            newBranchDepBoxKey.setPettyCashTxnApprovalRequired(IdosUtil.convertStringToInt(cashierPettyTxnApprReqd));
            if (cashierPettyTxnApprReqd.equals("1")) {
                if (cashierPettyTxnApprAmtLimit != null && !cashierPettyTxnApprAmtLimit.equals("")) {
                    newBranchDepBoxKey
                            .setApprovalAmountLimit(IdosUtil.convertStringToDouble(cashierPettyTxnApprAmtLimit));
                }
            }
        }
        boolean isUpdatableCash = true;
        boolean isUpdatablePettyCash = true;
        String bnchId = json.findPath("branchId") != null ? json.findPath("branchId").asText().trim() : null;
        if (bnchId == null || "".equals(bnchId)) { // newly addded branch
            BranchCashCount bnchCashCount = new BranchCashCount();
            bnchCashCount.setBranch(newBranch);
            bnchCashCount.setOrganization(org);
            bnchCashCount.setDate(Calendar.getInstance().getTime());
            if (keyDepositOpeningBalance != null && !keyDepositOpeningBalance.equals("")) {
                bnchCashCount.setResultantCash(IdosUtil.convertStringToDouble(keyDepositOpeningBalance));
            }
            if (pettyCashOpeningBalance != null && !pettyCashOpeningBalance.equals("")) {
                bnchCashCount.setResultantPettyCash(IdosUtil.convertStringToDouble(pettyCashOpeningBalance));
            }
            genericDAO.saveOrUpdate(bnchCashCount, usr, entityManager);
            auditDAO.saveAuditLogs("Added Branch Cash Count", usr, bnchCashCount.getId(), UsersRoles.class, ipAddress,
                    json.toString(), entityManager);
        } else {

            String newsbquery = "select obj from BranchCashCount obj WHERE obj.branch.id='" + newBranch.getId()
                    + "' AND obj.organization.id='" + newBranch.getOrganization().getId()
                    + "' and obj.presentStatus=1 ORDER BY obj.date desc";
            List<BranchCashCount> branchCashCountList = genericDAO.executeSimpleQueryWithLimit(newsbquery,
                    entityManager, 1);
            if (branchCashCountList.size() > 0) {
                boolean updateFlag = false;
                // if(newBranchDepBoxKey != null && newBranchDepBoxKey.getOpeningBalance() ==
                // null) {

                if (newBranchDepBoxKey.getId() != null) {
                    isUpdatableCash = !BranchDepositBoxKey.isCashOrPettyCashInvolve(usr.getOrganization().getId(),
                            usr.getBranch().getId(), "Cash", newBranchDepBoxKey.getId(), genericDAO, entityManager);
                }

                if (newBranchDepBoxKey != null && isUpdatableCash) {
                    if (keyDepositOpeningBalance != null && !keyDepositOpeningBalance.equals("")) {
                        if (branchCashCountList.get(0).getResultantCash() != null) {
                            Double resultantCashTmp = branchCashCountList.get(0).getResultantCash();
                            if (newBranchDepBoxKey.getOpeningBalance() != null) {
                                resultantCashTmp = resultantCashTmp - newBranchDepBoxKey.getOpeningBalance();
                            }
                            resultantCashTmp = resultantCashTmp
                                    + IdosUtil.convertStringToDouble(keyDepositOpeningBalance);
                            branchCashCountList.get(0).setResultantCash(resultantCashTmp);
                        } else {
                            branchCashCountList.get(0)
                                    .setResultantCash(IdosUtil.convertStringToDouble(keyDepositOpeningBalance));
                        }
                        updateFlag = true;
                    }
                }
                // if(newBranchDepBoxKey != null &&
                // newBranchDepBoxKey.getPettyCashOpeningBalance() == null) {

                if (newBranchDepBoxKey.getId() != null) {
                    isUpdatablePettyCash = !BranchDepositBoxKey.isCashOrPettyCashInvolve(usr.getOrganization().getId(),
                            newBranchDepBoxKey.getBranch().getId(), "PettyCash", newBranchDepBoxKey.getId(), genericDAO,
                            entityManager);
                }

                if (newBranchDepBoxKey != null && isUpdatablePettyCash) {
                    if (pettyCashOpeningBalance != null && !pettyCashOpeningBalance.equals("")) {
                        if (branchCashCountList.get(0).getResultantPettyCash() != null) {
                            Double resultantPettyCashTmp = branchCashCountList.get(0).getResultantPettyCash();
                            if (newBranchDepBoxKey.getPettyCashOpeningBalance() != null) {
                                resultantPettyCashTmp = resultantPettyCashTmp
                                        - newBranchDepBoxKey.getPettyCashOpeningBalance();
                            }
                            resultantPettyCashTmp = resultantPettyCashTmp
                                    + IdosUtil.convertStringToDouble(pettyCashOpeningBalance);
                            branchCashCountList.get(0).setResultantPettyCash(resultantPettyCashTmp);
                        } else {
                            branchCashCountList.get(0)
                                    .setResultantPettyCash(IdosUtil.convertStringToDouble(pettyCashOpeningBalance));
                        }
                        updateFlag = true;
                    }
                }
                if (updateFlag) {
                    genericDAO.saveOrUpdate(branchCashCountList.get(0), usr, entityManager);
                    auditDAO.saveAuditLogs("updated Branch Cash Count", usr, branchCashCountList.get(0).getId(),
                            UsersRoles.class, ipAddress, json.toString(), entityManager);
                }
            }
        }

        if (pettyCashOpeningBalance != null && !pettyCashOpeningBalance.equals("")) {
            // if (newBranchDepBoxKey.getPettyCashOpeningBalance() == null){ // only once
            // opening balance is allowed to set.
            if (isUpdatablePettyCash) { // only once opening balance is allowed to set.
                newBranchDepBoxKey.setPettyCashOpeningBalance(IdosUtil.convertStringToDouble(pettyCashOpeningBalance));
            }
        } else {
            newBranchDepBoxKey.setPettyCashOpeningBalance(null);
        }

        if (keyDepositOpeningBalance != null && !"".equals(keyDepositOpeningBalance)) {
            // if (newBranchDepBoxKey.getOpeningBalance() == null) { // only once opening
            // balance is allowed to set.
            if (isUpdatableCash) {
                newBranchDepBoxKey.setOpeningBalance(IdosUtil.convertStringToDouble(keyDepositOpeningBalance));
            }
        } else {
            newBranchDepBoxKey.setOpeningBalance(null);
        }

        genericDAO.saveOrUpdate(newBranchDepBoxKey, usr, entityManager);
        auditDAO.saveAuditLogs("updated/added Branch Cash Deposite", usr, newBranchDepBoxKey.getId(), UsersRoles.class,
                ipAddress, json.toString(), entityManager);
        return true;
    }

    @Override
    public Double updateBranchCashDetail(EntityManager entityManager, Users user, Branch txnBranch, Double txnNetAmount,
            boolean isCredit, Date txnDate, ObjectNode result) throws IDOSException {
        Double creditAmount = 0.0;
        Double debitAmount = 0.0;
        Double resultantCash = 0.0;
        Double mainToPettyCash = 0.0;
        Double resultantCashMain = 0.0;
        double onDateBalance = branchCashService.getBranchCashBalanceOnDate(txnDate, user, txnBranch.getId(),
                entityManager, IdosConstants.CASH);
        StringBuilder newsbquery = new StringBuilder("select obj from BranchCashCount obj WHERE obj.branch.id=")
                .append(txnBranch.getId()).append(" AND obj.organization.id=")
                .append(txnBranch.getOrganization().getId()).append(" and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchCashCount> branchCashCount = genericDAO.executeSimpleQueryWithLimit(newsbquery.toString(),
                entityManager, 1);
        if (branchCashCount.size() > 0) {
            if (branchCashCount.get(0).getCreditAmount() == null) {
                creditAmount = txnNetAmount;
            } else if (branchCashCount.get(0).getCreditAmount() != null) {
                creditAmount = branchCashCount.get(0).getCreditAmount();
            }
            if (branchCashCount.get(0).getDebitAmount() == null) {
                debitAmount = 0.0;
            } else if (branchCashCount.get(0).getDebitAmount() != null) {
                debitAmount = branchCashCount.get(0).getDebitAmount();
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
                mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
            } else if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
                mainToPettyCash = 0.0;
            }

            Double openingBalance = 0.0;
            if (branchCashCount.get(0).getBranch() != null
                    && branchCashCount.get(0).getBranch().getBranchDepositKeys() != null
                    && branchCashCount.get(0).getBranch().getBranchDepositKeys().size() > 0
                    && branchCashCount.get(0).getBranch().getBranchDepositKeys().get(0).getOpeningBalance() != null) {
                openingBalance = branchCashCount.get(0).getBranch().getBranchDepositKeys().get(0).getOpeningBalance();
            }
            if (isCredit) {
                creditAmount += txnNetAmount;
                resultantCashMain = onDateBalance - txnNetAmount;
                resultantCash = openingBalance + debitAmount - creditAmount - mainToPettyCash;
                branchCashCount.get(0).setCreditAmount(creditAmount);
            } else {
                debitAmount += txnNetAmount;
                resultantCashMain = onDateBalance + txnNetAmount;
                resultantCash = openingBalance + debitAmount - creditAmount - mainToPettyCash;
                branchCashCount.get(0).setDebitAmount(debitAmount);
            }
            branchCashCount.get(0).setResultantCash(resultantCash);
            if (resultantCash >= 0) {
                genericDAO.saveOrUpdate(branchCashCount.get(0), user, entityManager);
            }
        } else {
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
                    "Cash account is not found", "Cash account is not found");
        }
        return resultantCashMain;
    }

    @Override
    public Double updateBranchPettyCashDetail(EntityManager em, GenericDAO genericDAO, Users user, Branch txnBranch,
            Double txnNetAmount, boolean isCredit, Date txnDate, ObjectNode result) throws IDOSException {
        Double creditAmount = 0.0;
        Double debittedPettyCahAmount = 0.0;
        Double creditedPettyCahAmount = 0.0;
        Double resultantPettyCashAmount = 0.0;
        double resultantPettyCashMain = 0.0;
        Double mainToPettyCash = 0.0;
        Double grandTotal = 0.0;
        double onDateBalance = branchCashService.getBranchCashBalanceOnDate(txnDate, user, txnBranch.getId(), em,
                IdosConstants.PETTY_CASH);
        StringBuilder newsbquery = new StringBuilder("select obj from BranchCashCount obj WHERE obj.branch.id=")
                .append(txnBranch.getId()).append(" AND obj.organization.id=")
                .append(txnBranch.getOrganization().getId()).append(" and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchCashCount> branchCashCount = genericDAO.executeSimpleQueryWithLimit(newsbquery.toString(), em, 1);
        if (branchCashCount.size() > 0) {
            if (branchCashCount.get(0).getResultantPettyCash() != null) {
                if (isCredit) {
                    if (branchCashCount.get(0).getDebittedPettyCashAmount() != null) {
                        debittedPettyCahAmount = branchCashCount.get(0).getDebittedPettyCashAmount() + txnNetAmount;
                    } else if (branchCashCount.get(0).getDebittedPettyCashAmount() == null) {
                        debittedPettyCahAmount = txnNetAmount;
                    }
                    resultantPettyCashAmount = branchCashCount.get(0).getResultantPettyCash() - txnNetAmount;
                    branchCashCount.get(0).setDebittedPettyCashAmount(debittedPettyCahAmount);
                } else {
                    if (branchCashCount.get(0).getDebittedPettyCashAmount() != null) {
                        debittedPettyCahAmount = branchCashCount.get(0).getDebittedPettyCashAmount() - txnNetAmount;
                    } else if (branchCashCount.get(0).getDebittedPettyCashAmount() == null) {
                        debittedPettyCahAmount = txnNetAmount;
                    }
                    resultantPettyCashAmount = branchCashCount.get(0).getResultantPettyCash() + txnNetAmount;
                    branchCashCount.get(0).setDebittedPettyCashAmount(debittedPettyCahAmount);
                }
            }
            if (resultantPettyCashAmount >= 0) {
                branchCashCount.get(0).setResultantPettyCash(resultantPettyCashAmount);
                genericDAO.saveOrUpdate(branchCashCount.get(0), user, em);
            }
        }
        if (isCredit) {
            resultantPettyCashMain = onDateBalance - txnNetAmount;
        } else {
            resultantPettyCashMain = onDateBalance + txnNetAmount;
        }
        result.put("resultantPettyCashAmount", onDateBalance);
        return resultantPettyCashMain;
    }

    @Override
    public double getBranchCashBalanceOnDate(Date txnDate, Users user, long branchId, EntityManager em, short cashType)
            throws IDOSException {
        String toDate = IdosConstants.MYSQLDF.format(txnDate);
        double cashBalanceOnDate = trialBalanceDao.getTrialBalanceBranchCashTotal(new TrialBalance(), user,
                IdosConstants.IDOS_START_DATE, toDate, null, cashType, branchId, em);
        return cashBalanceOnDate;
    }
}
