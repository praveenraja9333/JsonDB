package org.vrp.utils.Models;

import org.vrp.utils.meta.*;

import java.util.List;

public class Model1 {
    @RjsonObject
    @Rnullable
    private  SupportPhoneNumber supportPhoneNumber;

    private int siteId;
    private String countryCode;
    @RjsonArray(type="java.lang.String")
    private List<String> automaticallyMappedLocales;
    @RootElement
    @FieldMap(fieldname = "TPID")
    private int tpId;

    @RjsonArray(type="org.vrp.utils.Models.SupportedLocale")
    private List<SupportedLocale> supportedLocales;

    public List getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public int getTpId() {
        return tpId;
    }

    public void setTpId(int tpId) {
        this.tpId = tpId;
    }

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


    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }



}
