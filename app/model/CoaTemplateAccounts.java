package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by Sunil Namdev on 04-05-2016.
 */

@Entity
@Table (name = "COA_TEMPLATE_ACCOUNTS")
public class CoaTemplateAccounts extends AbstractBaseModel {
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Column (name = "ACCOUNT_NAME")
    private String accountName;

    public String getSubAccountName() {
        return subAccountName;
    }

    public void setSubAccountName(String subAccountName) {
        this.subAccountName = subAccountName;
    }

    @Column (name = "SUB_ACCOUNT_NAME")
    private String subAccountName;

    public int getParticularType() {
        return particularType;
    }

    public void setParticularType(int particularType) {
        this.particularType = particularType;
    }

    @Column (name = "PARTICULAR_TYPE")
    private int particularType;
}