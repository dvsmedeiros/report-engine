package com.dvsmedeiros.reportengine.domain;

import java.io.ByteArrayInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;

public class ReportRequest {

    private Report report;
    private JsonNode dataSource;
    private Format format;
    private String owner;
    private String outputFileName;
    
    public Report getReport () {
        return report;
    }

    public void setReport ( Report report ) {
        this.report = report;
    }
    
    public JsonNode getDataSource () {
        return dataSource;
    }

    public void setDataSource ( JsonNode dataSource ) {
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
    
    public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public JsonDataSource getJsonDataSource () {
        try {
            String jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( this.dataSource );
            ByteArrayInputStream jsonDataStream = new ByteArrayInputStream( jsonString.getBytes() );
            return new JsonDataSource( jsonDataStream );
        } catch ( JRException e ) {
            e.printStackTrace();
        } catch ( JsonProcessingException e ) {
            e.printStackTrace();
        }
        return null;
    }

}
