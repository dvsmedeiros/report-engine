package com.dvsmedeiros.reportengine.domain;

import java.util.Calendar;
import java.util.Date;

@SuppressWarnings ( "serial" )
public class CalendarParamValue extends Param {

    private Date value;
    
    public CalendarParamValue () {
    }

    public CalendarParamValue ( ParamType type , String name , String label , Boolean required , Calendar value ) {
        super( type , name , label , required );
        this.value = value.getTime();
    }

    public Date getValue () {
        return value;
    }

    public void setValue ( Date value ) {
        this.value = value;
    }
}
