package com.dvsmedeiros.reportengine.domain;

@SuppressWarnings ( "serial" )
public class DoubleParamValue extends Param {

    private Double value;

    public DoubleParamValue () {
    }

    public DoubleParamValue ( ParamType type , String name , String label , Boolean required , Double value ) {
        super( type , name , label , required );
        this.value = value;
    }

    public Double getValue () {
        return value;
    }

    public void setValue ( Double value ) {
        this.value = value;
    }

}
