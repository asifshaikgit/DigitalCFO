package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.persistence.*;
import java.util.Date;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 28-08-2017.
 */
@Entity
@Table(name = "TRANSACTION_INVOICE")
public class TransactionInvoice extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public TransactionInvoice() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String HQL = "select obj from TransactionInvoice obj WHERE obj.organization.id=?1 and obj.transaction.id=?2 and obj.presentStatus=1";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_ID")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Vendor vendorCustomer;

    @Column(name = "INVOICE_NUMBER")
    private String invoiceNumber;

    @Column(name = "TRANSPORTATION_MODE")
    private String tranportationMode;

    @Column(name = "VEHICLE_DETAIL")
    private String vehicleDetail;

    @Column(name = "DOS_TIME")
    private Date datetimeOfSupply;

    @Column(name = "TERMS")
    private String terms;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "CURRENCY_CODE")
    private String currencyCode;

    @Column(name = "CUR_CONV_RATE")
    private Double currencyConvRate;

    @Column(name = "DATE_REMOVAL_GOODS")
    private Date dateRemovalGoods;

    @Column(name = "APPL_NUM_GOODS_REMOVAL")
    private String applNumberGoodsRemoval;

    @Column(name = "GSTIN_ECOM_OPERATOR")
    private String gstinEcomOperator;

    @Column(name = "FILE_NAME")
    private String fileName = null;

    @Column(name = "INV_REF_DATE")
    private Date invRefDate;

    @Column(name = "INV_REF_NO")
    private String invRefNumber;

    @Column(name = "GRN_DATE")
    private Date grnDate;

    @Column(name = "GRN_REF_NO")
    private String grnRefNumber;

    @Column(name = "IMPORT_DATE")
    private Date importDate;

    @Column(name = "IMPORT_REF_NO")
    private String importRefNumber;

    @Column(name = "AMOUNT")
    private Double amount;

    @Column(name = "PORT_CODE")
    private String portCode;

    @Column(name = "REMARKS_ADD_DETAILS")
    private String remarksAddDetails;

    @Column(name = "INVOICE_HEADING")
    private Integer invoiceHeading;

    @Column(name = "REASON_FOR_RETURN")
    private Integer reasonForReturn;

    @Column(name = "DIGITAL_SIGNATURE_CONTENT")
    private String digitalSignatureContent;

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Vendor getVendorCustomer() {
        return this.vendorCustomer;
    }

    public void setVendorCustomer(Vendor vendorCustomer) {
        this.vendorCustomer = vendorCustomer;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTranportationMode() {
        return this.tranportationMode;
    }

    public void setTranportationMode(String tranportationMode) {
        this.tranportationMode = tranportationMode;
    }

    public String getVehicleDetail() {
        return this.vehicleDetail;
    }

    public void setVehicleDetail(String vehicleDetail) {
        this.vehicleDetail = vehicleDetail;
    }

    public Date getDatetimeOfSupply() {
        return this.datetimeOfSupply;
    }

    public void setDatetimeOfSupply(Date datetimeOfSupply) {
        this.datetimeOfSupply = datetimeOfSupply;
    }

    public String getTerms() {
        return this.terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Double getCurrencyConvRate() {
        return this.currencyConvRate;
    }

    public void setCurrencyConvRate(Double currencyConvRate) {
        this.currencyConvRate = currencyConvRate;
    }

    public Date getDateRemovalGoods() {
        return this.dateRemovalGoods;
    }

    public void setDateRemovalGoods(Date dateRemovalGoods) {
        this.dateRemovalGoods = dateRemovalGoods;
    }

    public String getGstinEcomOperator() {
        return this.gstinEcomOperator;
    }

    public void setGstinEcomOperator(String gstinEcomOperator) {
        this.gstinEcomOperator = gstinEcomOperator;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getApplNumberGoodsRemoval() {
        return this.applNumberGoodsRemoval;
    }

    public Date getGrnDate() {
        return this.grnDate;
    }

    public void setGrnDate(Date grnDate) {
        this.grnDate = grnDate;
    }

    public String getGrnRefNumber() {
        return this.grnRefNumber;
    }

    public void setGrnRefNumber(String grnRefNumber) {
        this.grnRefNumber = grnRefNumber;
    }

    public Date getImportDate() {
        return this.importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getImportRefNumber() {
        return this.importRefNumber;
    }

    public void setImportRefNumber(String importRefNumber) {
        this.importRefNumber = importRefNumber;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getInvRefDate() {
        return this.invRefDate;
    }

    public void setInvRefDate(Date invRefDate) {
        this.invRefDate = invRefDate;
    }

    public String getInvRefNumber() {
        return this.invRefNumber;
    }

    public void setInvRefNumber(String invRefNumber) {
        this.invRefNumber = invRefNumber;
    }

    public void setApplNumberGoodsRemoval(String applNumberGoodsRemoval) {
        this.applNumberGoodsRemoval = applNumberGoodsRemoval;
    }

    public String getPortCode() {
        return portCode;
    }

    public void setPortCode(String portCode) {
        this.portCode = portCode;
    }

    public String getRemarksAddDetails() {
        return remarksAddDetails;
    }

    public void setRemarksAddDetails(String remarksAddDetails) {
        this.remarksAddDetails = remarksAddDetails;
    }

    public Integer getInvoiceHeading() {
        return invoiceHeading;
    }

    public void setInvoiceHeading(Integer invoiceHeading) {
        this.invoiceHeading = invoiceHeading;
    }

    public Integer getReasonForReturn() {
        return reasonForReturn;
    }

    public void setReasonForReturn(Integer reasonForReturn) {
        this.reasonForReturn = reasonForReturn;
    }

    public String getDigitalSignatureContent() {
        return digitalSignatureContent;
    }

    public void setDigitalSignatureContent(String digitalSignatureContent) {
        this.digitalSignatureContent = digitalSignatureContent;
    }

    public static TransactionInvoice findByTransactionID(EntityManager entityManager, Long orgid, Long txnid) {
        TransactionInvoice invoiceLog = null;
        try {
            Query query = entityManager.createQuery(HQL);
            query.setParameter(1, orgid);
            query.setParameter(2, txnid);
            invoiceLog = (TransactionInvoice) query.getSingleResult();
        } catch (NoResultException ex) {
        }
        return invoiceLog;
    }

}
