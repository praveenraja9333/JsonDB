package org.vrp.utils;

import org.vrp.utils.meta.Ignorable;
import org.vrp.utils.meta.RjsonArray;
import org.vrp.utils.meta.RjsonObject;

public class Model1 {
    @RjsonObject
    private Model1 model;

    public Model1 getModel() {
        return model;
    }

    public void setModel(Model1 model) {
        this.model = model;
    }

    public Model1 getModel1() {
        return model1;
    }

    public void setModel1(Model1 model1) {
        this.model1 = model1;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    //@Ignorable
    @RjsonArray
    private Model1 model1;
    private String siteId;
    private String supportPhoneNumber;
    public String getSupportPhoneNumber() {
        return supportPhoneNumber;
    }

    public void setSupportPhoneNumber(String supportPhoneNumber) {
        this.supportPhoneNumber = supportPhoneNumber;
    }


}
