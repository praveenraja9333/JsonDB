package org.vrp.utils.common;

public enum META_ENUM {
    RJSONOBJECT("RjsonObject"),
    RJSONARRAY("RjsonArray"),
    RJSONROUTE("RjsonRoute"),
    IGNORABLE("Ignorable"),
    RNULLABLE("Rnullable");

    private String classname;
    META_ENUM(String classname){
        this.classname=classname;
    }

    public static META_ENUM getEnum(String classname){
        for(META_ENUM meta:META_ENUM.values()){
            if(meta.toString().equalsIgnoreCase(classname))return meta;
        }
        return null;
    }

    @Override
    public String toString() {
        return classname;
    }
}
