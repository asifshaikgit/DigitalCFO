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
 * Created by Sunil Namdev on 02-02-2017.
 */
@Entity
@Table(name = "CUSTOMER_DETAIL")
public class CustomerDetail extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public CustomerDetail() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String CUSTOMER_DETAIL_HQL = "select obj from CustomerDetail obj WHERE obj.customer.id=?1 and obj.presentStatus=1";
    private static final String CUSTDET_BYID_GSTIN = "select obj from CustomerDetail obj WHERE obj.customer.id=?1 and obj.gstin like ?2 and obj.presentStatus=1";
    private static final String CUSTDET_BYID_BILLING_STATE = "select obj from CustomerDetail obj WHERE obj.customer.id=?1 and obj.billingStateCode=?2 and obj.presentStatus=1";
    private static final String CUSTDET_BYID_SHIP_STATE = "select obj from CustomerDetail obj WHERE obj.customer.id=?1 and obj.shippingStateCode=?2 and obj.presentStatus=1";
    private static final String WALKIN_CUSTDET_HQL = "select obj from CustomerDetail obj WHERE obj.organization.id=?1 and obj.customerName=?2 and obj.presentStatus=1";
    @GenericGenerator(name = "gencustomer", strategy = "foreign", parameters = @Parameter(name = "property", value = "customer"))
    @GeneratedValue(generator = "gencustomer")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Vendor customer;

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

    @Column(name = "BILLING_STATE_CODE")
    private String billingStateCode;

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

    @Column(name = "SAME_AS_BILLING_ADDRESS")
    private Integer isSameAsBillingAddress;

    public Integer getIsSameAsBillingAddress() {
        return isSameAsBillingAddress;
    }

    public void setIsSameAsBillingAddress(Integer isSameAsBillingAddress) {
        this.isSameAsBillingAddress = isSameAsBillingAddress;
    }

    public Vendor getCustomer() {
        return this.customer;
    }

    public void setCustomer(Vendor customer) {
        this.customer = customer;
    }

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

    public String getBillingStateCode() {
        return billingStateCode;
    }

    public void setBillingStateCode(String billingStateCode) {
        this.billingStateCode = billingStateCode;
    }

    public static CustomerDetail findByID(Long id) {
        return entityManager.find(CustomerDetail.class, id);
    }

    public static CustomerDetail findByCustomerID(EntityManager entityManager, Long customerId) {
        CustomerDetail customerDetail = null;
        try {
            Query query = entityManager.createQuery(CUSTOMER_DETAIL_HQL);
            query.setParameter(1, customerId);
            customerDetail = (CustomerDetail) query.getSingleResult();
        } catch (NoResultException ex) {
        }
        return customerDetail;
    }

    public static List<CustomerDetail> findGstByCustomerID(EntityManager entityManager, Long customerId) {
        List<CustomerDetail> customerDetail = null;
        try {
            Query query = entityManager.createQuery(CUSTOMER_DETAIL_HQL);
            query.setParameter(1, customerId);
            customerDetail = query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
        }
        return customerDetail;
    }

    public static CustomerDetail findByCustomerGSTNID(EntityManager entityManager, Long customerId, String gstin) {
        List<CustomerDetail> customerDetailList = null;
        CustomerDetail customerDetail = null;
        Query query = entityManager.createQuery(CUSTDET_BYID_GSTIN);
        query.setParameter(1, customerId);
        query.setParameter(2, "%" + gstin + "%");
        customerDetailList = query.getResultList();
        if (customerDetailList != null && customerDetailList.size() > 0) {
            customerDetail = customerDetailList.get(0);
        }
        return customerDetail;
    }

    public static CustomerDetail findByCustomerBillingState(EntityManager entityManager, Long customerId,
            String stateCode) {
        List<CustomerDetail> customerDetailList = null;
        CustomerDetail customerDetail = null;
        Query query = entityManager.createQuery(CUSTDET_BYID_BILLING_STATE);
        query.setParameter(1, customerId);
        query.setParameter(2, stateCode);
        customerDetailList = query.getResultList();
        if (customerDetailList != null && customerDetailList.size() > 0) {
            customerDetail = customerDetailList.get(0);
        }
        return customerDetail;
    }

    public static CustomerDetail findByCustomerShippingState(EntityManager entityManager, Long customerId,
            String stateCode) {
        List<CustomerDetail> customerDetailList = null;
        CustomerDetail customerDetail = null;
        Query query = entityManager.createQuery(CUSTDET_BYID_SHIP_STATE);
        query.setParameter(1, customerId);
        query.setParameter(2, stateCode);
        customerDetailList = query.getResultList();
        if (customerDetailList != null && customerDetailList.size() > 0) {
            customerDetail = customerDetailList.get(0);
        }
        return customerDetail;
    }
}
