package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 04-02-2017.
 */
@Entity
@Table(name = "INVOICE_LOG")
public class InvoiceLog extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public InvoiceLog() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String TXN_JPQL = "select obj from InvoiceLog obj WHERE obj.transaction.id=?1 and obj.presentStatus=1";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION")
    private Organization organization;

    @Column(name = "SHIPPING_ADDRESS")
    private String shippingAddress;

    @Column(name = "SHIPPING_LOCATION")
    private String shippingLocation;

    @Column(name = "SHIPPING_PHONE_COUNTRY_CODE")
    private String shippingPhoneCountryCode;

    @Column(name = "SHIPPING_PHONE")
    private String shippingPhone;

    @Column(name = "SHIPPING_COUNTRY")
    private Integer shippingCountry;

    @Column(name = "FILE_NAME")
    private String fileName;

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getShippingAddress() {
        return this.shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingPhoneCountryCode() {
        return this.shippingPhoneCountryCode;
    }

    public void setShippingPhoneCountryCode(String shippingPhoneCountryCode) {
        this.shippingPhoneCountryCode = shippingPhoneCountryCode;
    }

    public String getShippingPhone() {
        return this.shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public Integer getShippingCountry() {
        return this.shippingCountry;
    }

    public void setShippingCountry(Integer shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getShippingLocation() {
        return this.shippingLocation;
    }

    public void setShippingLocation(String shippingLocation) {
        this.shippingLocation = shippingLocation;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static InvoiceLog findByTransactionID(EntityManager entityManager, Long id) {
        InvoiceLog invoiceLog = null;
        try {
            Query query = entityManager.createQuery(TXN_JPQL);
            query.setParameter(1, id);
            invoiceLog = (InvoiceLog) query.getSingleResult();
        } catch (NoResultException ex) {
        }
        return invoiceLog;
    }
}
