package model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Sunil Namdev on 20-07-2017.
 */
@Entity
@Table(name = "CUSTOMER_WALKIN_DETAIL")
public class CustomerWalkinDetail extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public CustomerWalkinDetail() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String walkinCustJQL = "select obj from CustomerWalkinDetail obj WHERE obj.gstin=?1 and obj.customerName=?2 and obj.presentStatus=1";
    private static final String ORG_CUST_JQL = "select obj from CustomerWalkinDetail obj WHERE obj.organization.id=?1 and obj.presentStatus=1";
    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "GSTIN")
    private String gstin;

    @Column(name = "BILLING_ADDRESS")
    private String billingaddress;

    @Column(name = "BILLING_LOCATION")
    private String billinglocation;

    @Column(name = "BILLING_PHONE_COUNTRY_CODE")
    private String billingphoneCtryCode;

    @Column(name = "BILLING_PHONE")
    private String billingphone;

    @Column(name = "BILLING_COUNTRY")
    private Integer billingcountry;

    @Column(name = "BILLING_STATE")
    private String billingState;

    @Column(name = "SHIPPING_ADDRESS")
    private String shippingaddress;

    @Column(name = "SHIPPING_LOCATION")
    private String shippinglocation;

    @Column(name = "SHIPPING_PHONE_COUNTRY_CODE")
    private String shippingphoneCtryCode;

    @Column(name = "SHIPPING_PHONE")
    private String shippingphone;

    @Column(name = "SHIPPING_STATE")
    private String shippingState;

    @Column(name = "SHIPPING_STATE_CODE")
    private String shippingStateCode;

    @Column(name = "SHIPPING_COUNTRY")
    private Integer shippingcountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    public String getShippingaddress() {
        return this.shippingaddress;
    }

    public void setShippingaddress(String shippingaddress) {
        this.shippingaddress = shippingaddress;
    }

    public String getShippinglocation() {
        return this.shippinglocation;
    }

    public void setShippinglocation(String shippinglocation) {
        this.shippinglocation = shippinglocation;
    }

    public String getShippingphoneCtryCode() {
        return this.shippingphoneCtryCode;
    }

    public void setShippingphoneCtryCode(String shippingphoneCtryCode) {
        this.shippingphoneCtryCode = shippingphoneCtryCode;
    }

    public String getShippingphone() {
        return this.shippingphone;
    }

    public void setShippingphone(String shippingphone) {
        this.shippingphone = shippingphone;
    }

    public Integer getShippingcountry() {
        return this.shippingcountry;
    }

    public void setShippingcountry(Integer shippingcountry) {
        this.shippingcountry = shippingcountry;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getShippingState() {
        return this.shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getGstin() {
        return this.gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getBillingaddress() {
        return this.billingaddress;
    }

    public void setBillingaddress(String billingaddress) {
        this.billingaddress = billingaddress;
    }

    public String getBillinglocation() {
        return this.billinglocation;
    }

    public void setBillinglocation(String billinglocation) {
        this.billinglocation = billinglocation;
    }

    public String getBillingphoneCtryCode() {
        return this.billingphoneCtryCode;
    }

    public void setBillingphoneCtryCode(String billingphoneCtryCode) {
        this.billingphoneCtryCode = billingphoneCtryCode;
    }

    public String getBillingphone() {
        return this.billingphone;
    }

    public void setBillingphone(String billingphone) {
        this.billingphone = billingphone;
    }

    public Integer getBillingcountry() {
        return this.billingcountry;
    }

    public void setBillingcountry(Integer billingcountry) {
        this.billingcountry = billingcountry;
    }

    public String getBillingState() {
        return this.billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getShippingStateCode() {
        return this.shippingStateCode;
    }

    public void setShippingStateCode(String shippingStateCode) {
        this.shippingStateCode = shippingStateCode;
    }

    public static CustomerWalkinDetail findByCustomerID(EntityManager entityManager, Long customerId) {
        return entityManager.find(CustomerWalkinDetail.class, customerId);
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public static CustomerWalkinDetail findByNameAndGSTNID(EntityManager entityManager, String name, String gstin) {
        CustomerWalkinDetail customer = null;
        try {
            Query query = entityManager.createQuery(walkinCustJQL);
            query.setParameter(1, gstin);
            query.setParameter(2, name);
            customer = (CustomerWalkinDetail) query.getSingleResult();
        } catch (NoResultException ex) {
        }
        return customer;
    }

    public static List<CustomerWalkinDetail> findByOrgID(EntityManager entityManager, Long orgid) {
        List<CustomerWalkinDetail> customerList = null;
        try {
            Query query = entityManager.createQuery(ORG_CUST_JQL);
            query.setParameter(1, orgid);
            customerList = query.getResultList();
        } catch (NoResultException ex) {
        }
        return customerList;
    }
}
