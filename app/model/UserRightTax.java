package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="USER_HAS_RIGHTS_FOR_TAX")
public class UserRightTax extends AbstractBaseModel {

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    private Users user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_RIGHT_ID")
    private UserRights userRights;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TAX_ID")
    private BranchTaxes tax;

    @Column(name="FROM_AMOUNT")
    private Double fromAmount;

    @Column(name="TO_AMOUNT")
    private Double toAmount;

    @Column(name="TAX_TYPE")
    private Integer taxType;

    @Column(name="PARTICULAR")
    private Integer particular;

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Users getUser() {
        return this.user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public UserRights getUserRights() {
        return this.userRights;
    }

    public void setUserRights(UserRights userRights) {
        this.userRights = userRights;
    }

    public BranchTaxes getTax() {
        return this.tax;
    }

    public void setTax(BranchTaxes tax) {
        this.tax = tax;
    }

    public Double getFromAmount() {
        return this.fromAmount;
    }

    public void setFromAmount(Double fromAmount) {
        this.fromAmount = fromAmount;
    }

    public Double getToAmount() {
        return this.toAmount;
    }

    public void setToAmount(Double toAmount) {
        this.toAmount = toAmount;
    }

    public Integer getTaxType() {
        return this.taxType;
    }

    public void setTaxType(Integer taxType) {
        this.taxType = taxType;
    }

    public Integer getParticular() {
        return this.particular;
    }

    public void setParticular(Integer particular) {
        this.particular = particular;
    }
}
