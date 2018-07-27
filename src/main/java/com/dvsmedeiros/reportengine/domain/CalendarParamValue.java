package com.dvsmedeiros.reportengine.domain;

import java.util.Calendar;

@SuppressWarnings ( "serial" )
public class CalendarParamValue extends Param {

    private Calendar value;
    
    public CalendarParamValue () {
    }

    public CalendarParamValue ( ParamType type , String name , String label , Boolean required , Calendar value ) {
        super( type , name , label , required );
        this.value = value;
    }

    public Calendar getValue () {
        return value;
    }

    public void setValue ( Calendar value ) {
        this.value = value;
    }
}
