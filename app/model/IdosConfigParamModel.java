package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "IDOS_CONFIG_PARAM")
public class IdosConfigParamModel extends AbstractBaseModel {
    @Column(name = "PARAM_NAME")
    private String parameterName;

    @Column(name = "PARAM_VALUE")
    private String parameterValue;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

}