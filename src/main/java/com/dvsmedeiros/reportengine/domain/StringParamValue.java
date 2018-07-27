package com.dvsmedeiros.reportengine.domain;

@SuppressWarnings ( "serial" )
public class StringParamValue extends Param {

    private String value;
    
    public StringParamValue () {

    }
    
    public StringParamValue(ParamType type , String name , String label , Boolean required, String value) {
        super( type , name , label , required );
        this.value = value;
    }
    
    public String getValue () {
        return value;
    }

    public void setValue ( String value ) {
        this.value = value;
    }

}
