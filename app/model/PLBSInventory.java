package model;

import javax.persistence.*;

/**
 * Created by Sunil Namdev on 28-11-2016.
 */
@Entity
@Table(name="PLBS_INVENTORY")
public class PLBSInventory extends AbstractBaseModel{

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)

    @JoinColumn(name="BRANCH_ID")
    private Branch branch;

    @Column(name="OPENING_BALANCE_CR")
    private Double openingBalanceCr;
    @Column(name="CLOSING_BALANCE_CR")
    private Double closingBalanceCr;
    @Column(name="OPENING_BALANCE_PR")
    private Double openingBalancePr;
    @Column(name="CLOSING_BALANCE_PR")
    private Double closingBalancePr;
    @Column(name="INVENTORY_TYPE")
    private Integer inventoryType;

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Double getOpeningBalanceCr() {
        return this.openingBalanceCr;
    }

    public void setOpeningBalanceCr(Double openingBalanceCr) {
        this.openingBalanceCr = openingBalanceCr;
    }

    public Double getClosingBalanceCr() {
        return this.closingBalanceCr;
    }

    public void setClosingBalanceCr(Double closingBalanceCr) {
        this.closingBalanceCr = closingBalanceCr;
    }

    public Double getOpeningBalancePr() {
        return this.openingBalancePr;
    }

    public void setOpeningBalancePr(Double openingBalancePr) {
        this.openingBalancePr = openingBalancePr;
    }

    public Double getClosingBalancePr() {
        return this.closingBalancePr;
    }

    public void setClosingBalancePr(Double closingBalancePr) {
        this.closingBalancePr = closingBalancePr;
    }

    public Integer getInventoryType() {
        return this.inventoryType;
    }

    public void setInventoryType(Integer inventoryType) {
        this.inventoryType = inventoryType;
    }
}
