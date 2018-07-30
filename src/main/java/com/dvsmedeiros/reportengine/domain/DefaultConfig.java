package com.dvsmedeiros.reportengine.domain;

public enum DefaultConfig {

    TEMPLATE ( "./template/" ) , RESULT ( "./result/" ) , CONFIG ( "./config/" ) , INPUT ( "./input/" );

    private String value;

    DefaultConfig ( String value ) {
        this.value = value;
    }

    public String getValue () {
        return value;
    }

}
