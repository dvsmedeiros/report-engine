package com.dvsmedeiros.reportengine.domain;

@SuppressWarnings ( "serial" )
public class IntegerParamValue extends Param {

    private Integer value;

    public IntegerParamValue () {
    }

    public IntegerParamValue ( ParamType type , String name , String label , Boolean required , Integer value ) {
        super( type , name , label , required );
        this.value = value;
    }

    public Integer getValue () {
        return value;
    }

    public void setValue ( Integer value ) {
        this.value = value;
    }

}
