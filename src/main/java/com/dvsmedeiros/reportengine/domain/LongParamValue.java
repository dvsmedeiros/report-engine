package com.dvsmedeiros.reportengine.domain;

@SuppressWarnings ( "serial" )
public class LongParamValue extends Param {

    private Long value;

    public LongParamValue () {
    }

    public LongParamValue ( ParamType type , String name , String label , Boolean required , Long value ) {
        super( type , name , label , required );
        this.value = value;
    }

    public Long getValue () {
        return value;
    }

    public void setValue ( Long value ) {
        this.value = value;
    }

}
