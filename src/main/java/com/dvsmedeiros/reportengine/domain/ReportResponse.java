package com.dvsmedeiros.reportengine.domain;

public class ReportResponse {

    private byte[] file;
    private String name;

    public byte[] getFile () {
        return file;
    }

    public void setFile ( byte[] file ) {
        this.file = file;
    }

    public String getName () {
        return name;
    }

    public void setName ( String name ) {
        this.name = name;
    }

    public Integer getSize () {
        return file.length;
    }

}
