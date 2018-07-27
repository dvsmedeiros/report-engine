package com.dvsmedeiros.reportengine.domain;

import java.util.List;

@SuppressWarnings ( "serial" )
public class Report extends DomainEntity {

    private String name;
    private String title;
    private String version;
    private List < Param > params;

    public String getName () {
        return name;
    }

    public void setName ( String name ) {
        this.name = name;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle ( String title ) {
        this.title = title;
    }

    public String getVersion () {
        return version;
    }

    public void setVersion ( String version ) {
        this.version = version;
    }

    public List < Param > getParams () {
        return params;
    }

    public void setParams ( List < Param > params ) {
        this.params = params;
    }

}
