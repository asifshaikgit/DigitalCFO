package service;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import model.Branch;
import model.Organization;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * Created by Sunil Namdev on 27-12-2016.
 */
public interface DashboardService extends BaseService {
        ObjectNode getBranchesOrProjectsOrOperationalData(final Organization org, final int type, EntityManager entityManager);

        ObjectNode getOrganizationDetails(final Organization org) throws Exception;

        ObjectNode getBranchDetails(final long branchId, final int subType, EntityManager entityManager)
                        throws Exception;

        ObjectNode getVendorOrCustomerDetails(final long vendorId) throws Exception;

        ObjectNode getChartOfAccount(final long fetchDataId) throws Exception;

        ObjectNode getProject(final long fetchDataId) throws Exception;

        void downloadOperationalVendorCustomerData(final Organization org, final int type) throws Exception;

        ObjectNode getTransactionExceedingBudgetGroupDetails(final long specificId, final Users user) throws Exception;

        ObjectNode getUser(final Long id) throws Exception;

        ObjectNode getExpenseClaims(final Long id) throws Exception;

        ObjectNode getTravelClaims(final Long id) throws Exception;

        ObjectNode getDashboardProjectFinancial(final Users user, final JsonNode json,
                        final EntityManager entityManager);

        ObjectNode getDashboardFinancial(final Users user, final JsonNode json, final EntityManager entityManager);

        ObjectNode getProjectGraph(final Users user, final EntityManager entityManager, final JsonNode json);

        ObjectNode getGraph(final Users user, final EntityManager entityManager, final JsonNode json);

        ObjectNode getProjectCashCreditExpenseIncome(final Users user, final EntityManager entityManager,
                        final int dashboardType, final int graphType, final JsonNode json);

        ObjectNode getCashCreditExpenseIncome(final Users user, final EntityManager entityManager,
                        final int dashboardType,
                        final int graphType, final JsonNode json);

        ObjectNode recPayablesOpeningBalAndCurrentYearTotal(JsonNode json, String tabElement, Users user,
                        EntityManager entityManager);

        ObjectNode branchWiseReceivablePayablesGraphData(JsonNode json, String tabElement, Users user,
                        EntityManager entityManager);

        ObjectNode displayBarnchAndPeriodWiseCustVend(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager);

        ObjectNode custVendOpeningBalanceBreakup(ObjectNode result, String tabElement, Users user,
                        EntityManager entityManager);

        ObjectNode getVendorsOrCustomers(final Organization org, final int type, EntityManager entityManager);

        ObjectNode customerwiseProformaInvoice(ObjectNode result, Branch branch, Users user,
                        EntityManager entityManager);
}
