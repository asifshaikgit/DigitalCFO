package model;

import javax.persistence.*;

/**
 * @auther Sunil K. Namdev created on 14.08.2018
 */
@Entity
@Table(name="coa_liabilities_items_view")
public class CoaLiabilities extends AbstractBaseModel {

    @Column(name="name")
    public String name;

    @Column(name="headType")
    public String headType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="organization")
    private Organization organization;


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadType() {
        return this.headType;
    }

    public void setHeadType(String headType) {
        this.headType = headType;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
