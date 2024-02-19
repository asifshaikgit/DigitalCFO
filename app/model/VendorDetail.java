package model;

import org.hibernate.annotations.*;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by Sunil K. Namdev on 15-07-2017.
 */
@Entity
@Table(name = "VENDOR_DETAIL")
public class VendorDetail extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public VendorDetail() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private final static String VENDOR_DETAIL_JQL = "select obj from VendorDetail obj WHERE obj.vendor.id=?1 and obj.gstin=?2 and obj.presentStatus=1";
    private final static String VENDOR_DETAIL_LOCATION_JQL = "select obj from VendorDetail obj WHERE obj.vendor.id=?1 and obj.gstin=?2 and obj.location=?3 and obj.presentStatus=1";
    private static final String DETAIL_BYVENID_HQL = "select obj from VendorDetail obj WHERE obj.vendor.id=?1 and obj.presentStatus=1";
    @GenericGenerator(name = "genvendor", strategy = "foreign", parameters = @org.hibernate.annotations.Parameter(name = "property", value = "vendor"))
    @GeneratedValue(generator = "genvendor")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @Column(name = "GSTIN")
    private String gstin;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "COUNTRY_STATE")
    private String countryState;

    @Column(name = "COUNTRY")
    private Integer country;

    @Column(name = "PHONE_COUNTRY_CODE")
    private String phoneCtryCode;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "STATE_CODE")
    private String stateCode;

    public Vendor getVendor() {
        return this.vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getGstin() {
        return this.gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountryState() {
        return this.countryState;
    }

    public void setCountryState(String countryState) {
        this.countryState = countryState;
    }

    public Integer getCountry() {
        return this.country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public String getPhoneCtryCode() {
        return this.phoneCtryCode;
    }

    public void setPhoneCtryCode(String phoneCtryCode) {
        this.phoneCtryCode = phoneCtryCode;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStateCode() {
        return this.stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public static VendorDetail findByVendorDetailID(Long id) {
        return entityManager.find(VendorDetail.class, id);
    }

    public static VendorDetail findByVendorID(EntityManager entityManager, Long vendorID) {
        VendorDetail vdetail = null;
        try {
            Query query = entityManager.createQuery(DETAIL_BYVENID_HQL);
            query.setParameter(1, vendorID);
            vdetail = (VendorDetail) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.getStackTrace();
        }
        return vdetail;
    }

    public static List<VendorDetail> findGstByVendorID(EntityManager entityManager, Long vendorID) {
        List<VendorDetail> vdetail = null;
        try {
            Query query = entityManager.createQuery(DETAIL_BYVENID_HQL);
            query.setParameter(1, vendorID);
            vdetail = query.getResultList();
        } catch (NoResultException ex) {
            ex.getStackTrace();
        }
        return vdetail;
    }

    public static VendorDetail findByVendorGSTNID(EntityManager entityManager, Long vendorID, String gstin) {
        VendorDetail detail = null;
        Query query = entityManager.createQuery(VENDOR_DETAIL_JQL);
        query.setParameter(1, vendorID);
        query.setParameter(2, gstin);
        List<VendorDetail> list = query.getResultList();
        if (list != null && list.size() > 0) {
            detail = list.get(0);
        }
        return detail;
    }

    public static VendorDetail findByVendorGSTNLocationID(EntityManager entityManager, Long vendorID, String gstin,
            String location) {
        VendorDetail detail = null;
        try {
            if (vendorID != null) {
                Query query = entityManager.createQuery(VENDOR_DETAIL_LOCATION_JQL);
                query.setParameter(1, vendorID);
                query.setParameter(2, gstin);
                query.setParameter(3, location);
                detail = (VendorDetail) query.getSingleResult();
            }
        } catch (NoResultException ex) {
            ex.getStackTrace();
        }
        return detail;
    }

    public static VendorDetail findById(Long id) {
        return entityManager.find(VendorDetail.class, id);
    }

}
