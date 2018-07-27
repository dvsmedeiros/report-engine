package com.dvsmedeiros.reportengine.domain;

import java.io.ByteArrayInputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;

public class ReportRequest {

    private Report report;
    private String dataSource;
    private Format format;
    private String owner;

    public Report getReport () {
        return report;
    }

    public void setReport ( Report report ) {
        this.report = report;
    }

    public String getDataSource () {
        return dataSource;
    }

    public void setDataSource ( String dataSource ) {
        this.dataSource = dataSource;
    }

    public Format getFormat () {
        return format;
    }

    public void setFormat ( Format format ) {
        this.format = format;
    }

    public String getOwner () {
        return owner;
    }

    public void setOwner ( String owner ) {
        this.owner = owner;
    }

    public JsonDataSource getJsonDataSource () {
        try {
            ByteArrayInputStream jsonDataStream = new ByteArrayInputStream( this.dataSource.getBytes() );
            return new JsonDataSource( jsonDataStream );
        } catch ( JRException e ) {
            e.printStackTrace();
        }
        return null;
    }

}
