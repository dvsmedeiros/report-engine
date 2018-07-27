package com.dvsmedeiros.reportengine.domain;

public enum FileExtention {

    JRXML ( ".jrxml" ) , JASPER ( ".jasper" );

    private String extention;

    private FileExtention ( String extention ) {
        this.extention = extention;
    }

    public String getExtention () {
        return extention;
    }

    public void setExtention ( String extention ) {
        this.extention = extention;
    }

}
