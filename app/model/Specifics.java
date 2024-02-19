package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Table(name = "SPECIFICS")
public class Specifics extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public Specifics() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String BYNAME_JPQL = "select obj from Specifics obj where obj.organization.id= ?1 and upper(obj.name) = ?2 and obj.presentStatus = 1";
    private static final String BYNAME_HEAD_TYPE_JPQL = "select obj from Specifics obj where obj.organization.id= ?1 and upper(obj.name) = ?2 and obj.accountCodeHirarchy like ?3 and obj.presentStatus = 1";
    private static final String MAX_ACCODE_PART = "select MAX(obj.accountCode) from Specifics obj where obj.organization.id=?1 and obj.particularsId.id=?2 and obj.presentStatus=1";
    private static final String MAX_ACCD_PARENT = "select MAX(obj.accountCode) from Specifics obj where obj.organization.id=?1 and obj.parentSpecifics.id=?2 and obj.presentStatus=1";
    private static final String ORG_PARENT = "select obj from Specifics obj where obj.organization.id=?1 and obj.parentSpecifics.id=?2 and obj.presentStatus=1";
    private static final String ITEMS_NAME_JPQL = "select obj from Specifics obj where obj.organization.id= ?1 and upper(obj.name) like ?2 and obj.presentStatus=1";

    private static final String BY_NAME_SQL_SPECIFICS = "SELECT t1.* FROM SPECIFICS AS t1 LEFT JOIN SPECIFICS as t2 ON t1.id = t2.PARENT_SPECIFIC WHERE t1.ORGANIZATION_ID = ?1 and t1.PRESENT_STATUS=1 and t1.ACCOUNT_CODE like ?2 and t1.NAME like ?3 and t2.id IS NULL order by t1.NAME";

    private static final String FIND_BY_LINKED_ITEM = "select obj from Specifics obj where obj.organization.id= ?1 and obj.linkIncomeExpenseSpecifics.id=?2 and obj.presentStatus=1";

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "particulars_id")
    private Particulars particularsId;

    @Column(name = "IS_EMPLOYEE_CLAIM_ITEM")
    private Integer employeeClaimItem;

    @Column(name = "PRESENT_STATUS")
    private Integer presentStatus;

    public Integer getPresentStatus() {
        return presentStatus;
    }

    public void setPresentStatus(Integer presentStatus) {
        this.presentStatus = presentStatus;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specificsVendors")
    private List<VendorSpecific> specificsVendors;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    List<BranchSpecifics> specificsBranch;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    List<SpecificsTransactionPurpose> specificsTransactionPurposes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    List<SpecificsKnowledgeLibrary> specificsKl;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    List<SpecificsDocUploadMonetoryRuleForBranch> specificsDocUploadRule;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    List<WarehouseItemStockReorderLevel> itemWarehouseStockReorderLevels;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specifics")
    private List<Expense> specificsExpenses;

    @Column(name = "INCOME_SPECIFICS_PER_UNIT_PRICE")
    private Double incomeSpecfPerUnitPrice;

    @Column(name = "INCOME_EXPENSE")
    private Integer incomeOrExpenseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @Column(name = "ACCOUNT_CODE")
    private Long accountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_SPECIFIC")
    private Specifics parentSpecifics;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_expense_link_specific")
    private Specifics linkIncomeExpenseSpecifics;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_BANK_ACCOUNT_ID")
    private BranchBankAccounts branchBankAccounts;

    @Column(name = "WITHHOLDING_APPLICABLE")
    private Integer isWithholdingApplicable;

    @Column(name = "WITHHOLDING_TYPE")
    private Integer withholdingType;

    @Column(name = "WITHHOLDING_RATE")
    private Double withHoldingRate;

    @Column(name = "WITHHOLDING_LIMIT")
    private Double withHoldingLimit;

    @Column(name = "WITHHOLDING_MONETORY_LIMIT")
    private Double withholdingMonetoryLimit;

    @Column(name = "opening_balance")
    private Double totalOpeningBalance;

    @Column(name = "IS_CAPTURE_INPUT_TAXES")
    private Integer isCaptureInputTaxes = 0;

    @Column(name = "IS_FIXED_ASSETS")
    private Integer isFixedAsset;

    @Column(name = "IS_MOVABLE_IMMOVABLE")
    private Integer isMovableImmovableAsset;

    @Column(name = "IS_TAGGABLE")
    private Integer isTaggableAsset;

    @Column(name = "CAPITALIZA_AMOUNT")
    private Double capitalizaAmount;

    @Column(name = "THRESHOLD_LIMIT")
    private Double thresholdLimit;

    @Column(name = "CAPITALIZE_LIFE_SPAN")
    private Double capitalizaLifeSpan;

    @Column(name = "TAGGABLE_CODE")
    private String taggableCode;

    @Column(name = "NUM_OF_EXPENSE_UNITS")
    private Double noOfExpenseUnits;

    @Column(name = "EXPENSE_UNITS_MEASURE")
    private String expenseUnitsMeasure;

    @Column(name = "NUM_OF_INCOME_UNITS")
    private Double noOfIncomeUnits;

    @Column(name = "EXPENSE_TO_INCOME_CONVERSION_RATE")
    private Double expenseToIncomeConverstionRate;

    @Column(name = "INCOME_UNITS_MEASURE")
    private String incomeUnitsMeasure;

    @Column(name = "OPENING_BALANCE_UNITS")
    private Double totalInvOpeningBalUnits;

    @Column(name = "OPENING_BALANCE_RATE")
    private Double totalInvOpeningBalRate;

    @Column(name = "INVENTORY_OPENING_BALANCE")
    private Double totalInvOpeningBalance;

    @Column(name = "TRADING_INVENTORY_CALC_METHOD")
    private String tradingInventoryCalcMethod;

    @Column(name = "IS_TRADING_INVENTORY_ITEM")
    private Integer isTradingInvenotryItem;

    @Column(name = "INVOICE_DESCRIPTION2")
    private String invoiceItemDescription2;

    @Column(name = "INVOICE_DESCRIPTION1")
    private String invoiceItemDescription1;

    @Column(name = "IS_INVOICE_DESC1")
    private Integer isInvoiceDescription1;

    @Column(name = "IS_INVOICE_DESC2")
    private Integer isInvoiceDescription2;

    @Column(name = "IS_TRANSACTION_EDITABLE")
    private Integer isTransactionEditable;

    @Column(name = "IS_COMBINATION_SALES")
    // means item=LAPTOP then true, not true for items contained in laptop say RAM
    // etc it is false
    private Integer isCombinationSales;

    @Column(name = "GST_ITEM_CODE")
    private String gstItemCode;

    @Column(name = "GST_TYPE_OF_SUPPLY") // 1=goods or 2=services
    private String gstTypeOfSupply;

    //@Column(name = "IS_GST_APPLICABLE") // 
    //private String GSTApplicable;

    //public String getGSTApplicable() {
    //    return GSTApplicable;
    //}

    //public void setGSTApplicable(String gSTApplicable) {
    //    GSTApplicable = gSTApplicable;
    //}

    @Column(name = "GST_ITEM_CATEGORY")
    private String gstItemCategory; // 1=GST Exempt Goods/Services, 2=Nil Rate Goods /Services, 3=Non GST Goods/
                                    // Services

    @Column(name = "GST_TAX_RATE")
    private Double gstTaxRate;

    @Column(name = "CESS_TAX_RATE")
    private Double cessTaxRate;

    @Column(name = "IDENT_PL_BS")
    private String identificationForDataValidPLorBS;

    @Column(name = "account_code_hirarchy")
    private String accountCodeHirarchy;

    @Column(name = "IDENT_DATA_VALID")
    private String identificationForDataValid;

    @Column(name = "IS_PRICE_INCLUSIVE")
    private Integer isPriceInclusive;

    @Column(name = "BARCODE")
    private String barcode;

    @Column(name = "GST_DESC")
    private String gstDesc;

    @Column(name = "IS_ELIGIBLE_INPUT_TAX_CREDIT")
    private Integer isEligibleInputTaxCredit;

    @Column(name = "IS_COMPOSITION_SCHEME")
    private Integer isCompositionScheme;

    @Column(name = "TAX_APPLICABLE_DATE")
    private Date taxApplicableDate;

    @Column(name = "GST_TAX_RATE_SELECTED")
    private String gstTaxRateSelected;

    @Column(name = "CESS_TAX_RATE_SELECTED")
    private String cessTaxRateSelected;

    @Column(name = "IS_TDS_VENDOR_SPEC")
    private Integer isTdsVendorSpecific;

    public Integer getIsEligibleInputTaxCredit() {
        return isEligibleInputTaxCredit;
    }

    public void setIsEligibleInputTaxCredit(Integer isEligibleInputTaxCredit) {
        this.isEligibleInputTaxCredit = isEligibleInputTaxCredit;
    }

    public String getGstDesc() {
        return gstDesc;
    }

    public void setGstDesc(String gstDesc) {
        this.gstDesc = gstDesc;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getEmployeeClaimItem() {
        return employeeClaimItem;
    }

    public void setEmployeeClaimItem(Integer employeeClaimItem) {
        this.employeeClaimItem = employeeClaimItem;
    }

    public List<SpecificsDocUploadMonetoryRuleForBranch> getSpecificsDocUploadRule() {
        return specificsDocUploadRule;
    }

    public void setSpecificsDocUploadRule(
            List<SpecificsDocUploadMonetoryRuleForBranch> specificsDocUploadRule) {
        this.specificsDocUploadRule = specificsDocUploadRule;
    }

    public List<SpecificsKnowledgeLibrary> getSpecificsKl() {
        return specificsKl;
    }

    public void setSpecificsKl(List<SpecificsKnowledgeLibrary> specificsKl) {
        this.specificsKl = specificsKl;
    }

    public Integer getIncomeOrExpenseType() {
        return incomeOrExpenseType;
    }

    public void setIncomeOrExpenseType(Integer incomeOrExpenseType) {
        this.incomeOrExpenseType = incomeOrExpenseType;
    }

    public List<SpecificsTransactionPurpose> getSpecificsTransactionPurposes() {
        return specificsTransactionPurposes;
    }

    public void setSpecificsTransactionPurposes(
            List<SpecificsTransactionPurpose> specificsTransactionPurposes) {
        this.specificsTransactionPurposes = specificsTransactionPurposes;
    }

    public Double getIncomeSpecfPerUnitPrice() {
        return incomeSpecfPerUnitPrice;
    }

    public void setIncomeSpecfPerUnitPrice(Double incomeSpecfPerUnitPrice) {
        this.incomeSpecfPerUnitPrice = incomeSpecfPerUnitPrice;
    }

    public String getGstTaxRateSelected() {
        return gstTaxRateSelected;
    }

    public void setGstTaxRateSelected(String gstTaxRateSelected) {
        this.gstTaxRateSelected = gstTaxRateSelected;
    }

    public String getCessTaxRateSelected() {
        return cessTaxRateSelected;
    }

    public void setCessTaxRateSelected(String cessTaxRateSelected) {
        this.cessTaxRateSelected = cessTaxRateSelected;
    }

    public Double getWithholdingMonetoryLimit() {
        return withholdingMonetoryLimit;
    }

    public void setWithholdingMonetoryLimit(Double withholdingMonetoryLimit) {
        this.withholdingMonetoryLimit = withholdingMonetoryLimit;
    }

    public Integer getIsCombinationSales() {
        return isCombinationSales;
    }

    public void setIsCombinationSales(Integer isCombinationSales) {
        this.isCombinationSales = isCombinationSales;
    }

    public Integer getIsTransactionEditable() {
        return this.isTransactionEditable;
    }

    public void setIsTransactionEditable(Integer isTransactionEditable) {
        this.isTransactionEditable = isTransactionEditable;
    }

    public Integer getIsInvoiceDescription1() {
        return this.isInvoiceDescription1;
    }

    public void setIsInvoiceDescription1(Integer isInvoiceDescription1) {
        this.isInvoiceDescription1 = isInvoiceDescription1;
    }

    public Integer getIsInvoiceDescription2() {
        return this.isInvoiceDescription2;
    }

    public void setIsInvoiceDescription2(Integer isInvoiceDescription2) {
        this.isInvoiceDescription2 = isInvoiceDescription2;
    }

    public String getInvoiceItemDescription1() {
        return this.invoiceItemDescription1;
    }

    public void setInvoiceItemDescription1(String invoiceItemDescription1) {
        this.invoiceItemDescription1 = IdosUtil.escapeHtml(invoiceItemDescription1);
    }

    public String getInvoiceItemDescription2() {
        return this.invoiceItemDescription2;
    }

    public void setInvoiceItemDescription2(String invoiceItemDescription2) {
        this.invoiceItemDescription2 = IdosUtil.escapeHtml(invoiceItemDescription2);
    }

    public Integer getIsCaptureInputTaxes() {
        return isCaptureInputTaxes;
    }

    public void setIsCaptureInputTaxes(Integer isCaptureInputTaxes) {
        this.isCaptureInputTaxes = isCaptureInputTaxes;
    }

    public Integer getIsWithholdingApplicable() {
        return isWithholdingApplicable;
    }

    public void setIsWithholdingApplicable(Integer isWithholdingApplicable) {
        this.isWithholdingApplicable = isWithholdingApplicable;
    }

    public Integer getWithholdingType() {
        return withholdingType;
    }

    public void setWithholdingType(Integer withholdingType) {
        this.withholdingType = withholdingType;
    }

    public Double getWithHoldingRate() {
        return withHoldingRate;
    }

    public void setWithHoldingRate(Double withHoldingRate) {
        this.withHoldingRate = withHoldingRate;
    }

    public Double getWithHoldingLimit() {
        return withHoldingLimit;
    }

    public void setWithHoldingLimit(Double withHoldingLimit) {
        this.withHoldingLimit = withHoldingLimit;
    }

    public BranchBankAccounts getBranchBankAccounts() {
        return branchBankAccounts;
    }

    public void setBranchBankAccounts(BranchBankAccounts branchBankAccounts) {
        this.branchBankAccounts = branchBankAccounts;
    }

    public String getAccountCodeHirarchy() {
        return accountCodeHirarchy;
    }

    public void setAccountCodeHirarchy(String accountCodeHirarchy) {
        this.accountCodeHirarchy = accountCodeHirarchy;
    }

    public String getIdentificationForDataValid() {
        return identificationForDataValid;
    }

    public void setIdentificationForDataValid(String identificationForDataValid) {
        this.identificationForDataValid = identificationForDataValid;
    }

    public String getIdentificationForDataValidPLorBS() {
        return identificationForDataValidPLorBS;
    }

    public void setIdentificationForDataValidPLorBS(String identificationForDataValidPLorBS) {
        this.identificationForDataValidPLorBS = identificationForDataValidPLorBS;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<BranchSpecifics> getSpecificsBranch() {
        return specificsBranch;
    }

    public void setSpecificsBranch(List<BranchSpecifics> specificsBranch) {
        this.specificsBranch = specificsBranch;
    }

    public Long getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(Long accountCode) {
        this.accountCode = accountCode;
    }

    public List<VendorSpecific> getSpecificsVendors() {
        return specificsVendors;
    }

    public void setSpecificsVendors(List<VendorSpecific> specificsVendors) {
        this.specificsVendors = specificsVendors;
    }

    public String getName() {
        return IdosUtil.escapeHtml(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Particulars getParticularsId() {
        return particularsId;
    }

    public void setParticularsId(Particulars particularsId) {
        this.particularsId = particularsId;
    }

    public Date getTaxApplicableDate() {
        return taxApplicableDate;
    }

    public void setTaxApplicableDate(Date taxApplicableDate) {
        this.taxApplicableDate = taxApplicableDate;
    }

    public Integer getIsTdsVendorSpecific() {
        return isTdsVendorSpecific;
    }

    public void setIsTdsVendorSpecific(Integer isTdsVendorSpecific) {
        this.isTdsVendorSpecific = isTdsVendorSpecific;
    }

    /**
     * Find a specifics by id.
     */
    public static Specifics findById(Long id) {
        System.out.println("MAX_ACCD_PARENT" + entityManager);
        return entityManager.find(Specifics.class, id);
    }

    /**
     * Find a specifics by id.
     */
    public static List<Specifics> findByName(EntityManager entityManager, Organization orgn, String specificsName) {
        Query query = entityManager.createQuery(BYNAME_JPQL);
        query.setParameter(1, orgn.getId());
        query.setParameter(2, specificsName.toUpperCase());
        List<Specifics> specifics = query.getResultList();
        return specifics;
    }

    public static List<Specifics> findByNameAndHeadType(EntityManager entityManager, Organization orgn,
            String specificsName, String headType) {
        Query query = entityManager.createQuery(BYNAME_HEAD_TYPE_JPQL);
        query.setParameter(1, orgn.getId());
        query.setParameter(2, specificsName.toUpperCase());
        query.setParameter(3, "/" + headType + "%");
        List<Specifics> specifics = query.getResultList();
        return specifics;
    }

    /**
     * list of particulars.
     */
    public static List<Specifics> list(EntityManager entityManager, Organization orgn) {
        String sbquery = ("select obj from Specifics obj where obj.organization.id='" + orgn.getId()
                + "' and obj.presentStatus=1");
        List<Specifics> specifics = entityManager.createQuery(sbquery).getResultList();
        return specifics;
    }

    public static Specifics findByOrganizationAndMappingID(EntityManager entityManager, Organization orgn,
            String mappingID) {
        Specifics specifics = null;
        try {
            String sbquery = "select obj from Specifics obj where obj.organization.id=?1 and obj.identificationForDataValid=?2 and obj.presentStatus=1";
            Query query = entityManager.createQuery(sbquery);
            query.setParameter(1, orgn.getId());
            query.setParameter(2, mappingID);
            specifics = (Specifics) query.getSingleResult();
        } catch (NoResultException ex) {
            // Eatup the exception and caller should handle the no record condition
        }
        return specifics;
    }

    /**
     */
    public static int checkIfSpecificExists(EntityManager entityManager, Organization orgn, int iddatavalidation,
            String parentaccountCode) {
        /*
         * StringBuilder sbquery = new
         * StringBuilder("select obj.identificationForDataValid from Specifics obj where obj.organization.id = "
         * )
         * .append(orgn.getId()).append(" and obj.identificationForDataValid = ").append
         * (iddatavalidation)
         * .append(" and obj.accountCodeHirarchy like '%").append(parentaccountCode).
         * append("/'");
         */

        StringBuilder sbquery = new StringBuilder(
                "select obj.identificationForDataValid from Specifics obj where obj.organization.id = ")
                .append(orgn.getId()).append(" and obj.identificationForDataValid = ").append(iddatavalidation)
                .append(" and obj.presentStatus=1");

        Query jpaQuery = entityManager.createQuery(sbquery.toString());
        List list = jpaQuery.getResultList();
        Object idForDataValid = null;
        if (list.size() > 0)
            idForDataValid = list.get(0);
        int identForDataValid = 0;
        if (idForDataValid != null && !"".equals(idForDataValid)) {
            identForDataValid = Integer.parseInt(idForDataValid.toString());
        }
        return identForDataValid;
    }

    public Specifics getParentSpecifics() {
        return parentSpecifics;
    }

    public void setParentSpecifics(Specifics parentSpecifics) {
        this.parentSpecifics = parentSpecifics;
    }

    public List<Expense> getSpecificsExpenses() {
        return specificsExpenses;
    }

    public void setSpecificsExpenses(List<Expense> specificsExpenses) {
        this.specificsExpenses = specificsExpenses;
    }

    public Specifics getLinkIncomeExpenseSpecifics() {
        return linkIncomeExpenseSpecifics;
    }

    public void setLinkIncomeExpenseSpecifics(Specifics linkIncomeExpenseSpecifics) {
        this.linkIncomeExpenseSpecifics = linkIncomeExpenseSpecifics;
    }

    public Integer getIsFixedAsset() {
        return isFixedAsset;
    }

    public void setIsFixedAsset(Integer isFixedAsset) {
        this.isFixedAsset = isFixedAsset;
    }

    public Integer getIsMovableImmovableAsset() {
        return isMovableImmovableAsset;
    }

    public void setIsMovableImmovableAsset(Integer isMovableImmovableAsset) {
        this.isMovableImmovableAsset = isMovableImmovableAsset;
    }

    public Integer getIsTaggableAsset() {
        return isTaggableAsset;
    }

    public void setIsTaggableAsset(Integer isTaggableAsset) {
        this.isTaggableAsset = isTaggableAsset;
    }

    public Double getCapitalizaAmount() {
        return capitalizaAmount;
    }

    public void setCapitalizaAmount(Double capitalizaAmount) {
        this.capitalizaAmount = capitalizaAmount;
    }

    public Double getThresholdLimit() {
        return thresholdLimit;
    }

    public void setThresholdLimit(Double thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
    }

    public Double getCapitalizaLifeSpan() {
        return capitalizaLifeSpan;
    }

    public void setCapitalizaLifeSpan(Double capitalizaLifeSpan) {
        this.capitalizaLifeSpan = capitalizaLifeSpan;
    }

    public List<WarehouseItemStockReorderLevel> getItemWarehouseStockReorderLevels() {
        return itemWarehouseStockReorderLevels;
    }

    public void setItemWarehouseStockReorderLevels(
            List<WarehouseItemStockReorderLevel> itemWarehouseStockReorderLevels) {
        this.itemWarehouseStockReorderLevels = itemWarehouseStockReorderLevels;
    }

    public String getTaggableCode() {
        return taggableCode;
    }

    public void setTaggableCode(String taggableCode) {
        this.taggableCode = taggableCode;
    }

    public Double getNoOfExpenseUnits() {
        return noOfExpenseUnits;
    }

    public void setNoOfExpenseUnits(Double noOfExpenseUnits) {
        this.noOfExpenseUnits = noOfExpenseUnits;
    }

    public String getExpenseUnitsMeasure() {
        return expenseUnitsMeasure;
    }

    public void setExpenseUnitsMeasure(String expenseUnitsMeasure) {
        this.expenseUnitsMeasure = IdosUtil.escapeHtml(expenseUnitsMeasure);
    }

    public Double getNoOfIncomeUnits() {
        return noOfIncomeUnits;
    }

    public void setNoOfIncomeUnits(Double noOfIncomeUnits) {
        this.noOfIncomeUnits = noOfIncomeUnits;
    }

    public String getIncomeUnitsMeasure() {
        return incomeUnitsMeasure;
    }

    public void setIncomeUnitsMeasure(String incomeUnitsMeasure) {
        this.incomeUnitsMeasure = IdosUtil.escapeHtml(incomeUnitsMeasure);
    }

    public String getTradingInventoryCalcMethod() {
        return tradingInventoryCalcMethod;
    }

    public void setTradingInventoryCalcMethod(String tradingInventoryCalcMethod) {
        this.tradingInventoryCalcMethod = tradingInventoryCalcMethod;
    }

    public Integer getIsTradingInvenotryItem() {
        return isTradingInvenotryItem;
    }

    public void setIsTradingInvenotryItem(Integer isTradingInvenotryItem) {
        this.isTradingInvenotryItem = isTradingInvenotryItem;
    }

    public Double getExpenseToIncomeConverstionRate() {
        return expenseToIncomeConverstionRate;
    }

    public void setExpenseToIncomeConverstionRate(
            Double expenseToIncomeConverstionRate) {
        this.expenseToIncomeConverstionRate = expenseToIncomeConverstionRate;
    }

    @Override
    public String toString() {
        return "Specifics{" +
                "id='" + id +
                " map=" + identificationForDataValid +
                " name=" + name +
                '}';
    }

    public String getGstItemCode() {
        return this.gstItemCode;
    }

    public void setGstItemCode(String gstItemCode) {
        this.gstItemCode = gstItemCode;
    }

    public String getGstTypeOfSupply() {
        return this.gstTypeOfSupply;
    }

    public void setGstTypeOfSupply(String gstTypeOfSupply) {
        this.gstTypeOfSupply = gstTypeOfSupply;
    }

    public String getGstItemCategory() {
        return this.gstItemCategory;
    }

    public void setGstItemCategory(String gstItemCategory) {
        this.gstItemCategory = gstItemCategory;
    }

    public Double getGstTaxRate() {
        return this.gstTaxRate;
    }

    public void setGstTaxRate(Double gstTaxRate) {
        this.gstTaxRate = gstTaxRate;
    }

    public Double getTotalOpeningBalance() {
        return this.totalOpeningBalance;
    }

    public void setTotalOpeningBalance(Double totalOpeningBalance) {
        this.totalOpeningBalance = totalOpeningBalance;
    }

    public Double getTotalInvOpeningBalUnits() {
        return this.totalInvOpeningBalUnits;
    }

    public void setTotalInvOpeningBalUnits(Double totalInvOpeningBalUnits) {
        this.totalInvOpeningBalUnits = totalInvOpeningBalUnits;
    }

    public Double getTotalInvOpeningBalRate() {
        return this.totalInvOpeningBalRate;
    }

    public void setTotalInvOpeningBalRate(Double totalInvOpeningBalRate) {
        this.totalInvOpeningBalRate = totalInvOpeningBalRate;
    }

    public Double getTotalInvOpeningBalance() {
        return this.totalInvOpeningBalance;
    }

    public void setTotalInvOpeningBalance(Double totalInvOpeningBalance) {
        this.totalInvOpeningBalance = totalInvOpeningBalance;
    }

    public Integer getIsPriceInclusive() {
        return isPriceInclusive;
    }

    public void setIsPriceInclusive(Integer isPriceInclusive) {
        this.isPriceInclusive = isPriceInclusive;
    }

    public Integer getIsCompositionScheme() {
        return isCompositionScheme;
    }

    public void setIsCompositionScheme(Integer isCompositionScheme) {
        this.isCompositionScheme = isCompositionScheme;
    }

    public Double getCessTaxRate() {
        return cessTaxRate;
    }

    public void setCessTaxRate(Double cessTaxRate) {
        this.cessTaxRate = cessTaxRate;
    }

    public static List findMaxAccountCode4Particular(EntityManager entityManager, Long orgid, Long particularid) {
        Query query = entityManager.createQuery(MAX_ACCODE_PART);
        query.setParameter(1, orgid);
        query.setParameter(2, particularid);
        List particulars = query.getResultList();
        return particulars;
    }

    public static List findMaxAccountCode4Specific(EntityManager entityManager, Long orgid, Long specificid) {
        Query query = entityManager.createQuery(MAX_ACCD_PARENT);
        query.setParameter(1, orgid);
        query.setParameter(2, specificid);
        List particulars = query.getResultList();
        return particulars;
    }

    public static List<Specifics> findChildBySpecificId(EntityManager entityManager, Long orgid, Long specificid) {
        Query query = entityManager.createQuery(ORG_PARENT);
        query.setParameter(1, orgid);
        query.setParameter(2, specificid);
        List<Specifics> particulars = query.getResultList();
        return particulars;
    }

    public static List<Specifics> findCoaByNameAndHead(EntityManager em, Users user, int headType, String name) {
        TypedQuery<Specifics> query = (TypedQuery<Specifics>) entityManager.createNativeQuery(BY_NAME_SQL_SPECIFICS,
                Specifics.class);
        query.setParameter(1, user.getOrganization().getId());
        query.setParameter(2, headType + "%");
        query.setParameter(3, "%" + name + "%");
        List<Specifics> leafNodes = query.getResultList();
        return leafNodes;
    }

}
