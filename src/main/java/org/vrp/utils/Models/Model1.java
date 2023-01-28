package org.vrp.utils.Models;

import org.vrp.utils.meta.RjsonArray;
import org.vrp.utils.meta.RjsonObject;

import java.util.List;

public class Model1 {
    @RjsonObject
    private  SupportPhoneNumber supportPhoneNumber;

    private String siteId;
    private String countryCode;
    @RjsonArray(type="java.lang.String")
    private List<String> automaticallyMappedLocales;

    public List<String> getAutomaticallyMappedLocales() {
        return automaticallyMappedLocales;
    }

    public void setAutomaticallyMappedLocales(List<String> automaticallyMappedLocales) {
        this.automaticallyMappedLocales = automaticallyMappedLocales;
    }

    public SupportPhoneNumber getSupportPhoneNumber() {
        return supportPhoneNumber;
    }

    public void setSupportPhoneNumber(SupportPhoneNumber supportPhoneNumber) {
        this.supportPhoneNumber = supportPhoneNumber;
    }

    /*public Model1 getModel1() {
        return model1;
    }

    public void setModel1(Model1 model1) {
        this.model1 = model1;
    }*/
    //@Ignorable




    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }



}
