package org.vrp.utils;

import org.vrp.utils.meta.Ignorable;
import org.vrp.utils.meta.RjsonArray;
import org.vrp.utils.meta.RjsonObject;

public class Model1 {
    /*@RjsonObject
    private Model1 model;
     @RjsonArray
    private Model1 model1;

    public Model1 getModel() {
        return model;
    }

    public void setModel(Model1 model) {
        this.model = model;
    }*/

    /*public Model1 getModel1() {
        return model1;
    }

    public void setModel1(Model1 model1) {
        this.model1 = model1;
    }*/
    //@Ignorable


    private String siteId;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    private String countryCode;
    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }



}
