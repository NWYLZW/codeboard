package com.gazlaws.codeboard.based;

public class CustomStr implements java.io.Serializable {
    String customStr="";
    String strName="";

    public CustomStr() {
    }
    public CustomStr(String customStr, String strName) {
        this.customStr = customStr;
        this.strName = strName;
    }
    public String getCustomStr() {
        return customStr;
    }
    public void setCustomStr(String customStr) {
        this.customStr = customStr;
    }
    public String getStrName() {
        return strName;
    }
    public void setStrName(String strName) {
        this.strName = strName;
    }
}
