package com.dvsmedeiros.reportengine.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings ( "serial" )
@JsonIgnoreProperties ( ignoreUnknown = true )
@JsonTypeInfo ( use = JsonTypeInfo.Id.NAME , include = JsonTypeInfo.As.EXISTING_PROPERTY , property = "type" , visible = true )
@JsonSubTypes ( { @JsonSubTypes.Type ( value = StringParamValue.class , name = Param.STRING ) , @JsonSubTypes.Type ( value = IntegerParamValue.class , name = Param.INTEGER ) ,
        @JsonSubTypes.Type ( value = LongParamValue.class , name = Param.LONG ) , @JsonSubTypes.Type ( value = DoubleParamValue.class , name = Param.DOUBLE ) ,
        @JsonSubTypes.Type ( value = CalendarParamValue.class , name = Param.DATE ) } )
public class Param extends DomainEntity {

    public static final String STRING = "STRING";
    public static final String INTEGER = "INTEGER";
    public static final String LONG = "LONG";
    public static final String DOUBLE = "DOUBLE";
    public static final String DATE = "DATE";

    private ParamType type;
    private String name;
    private String label;
    private Boolean required;

    public Param () {

    }

    public Param ( ParamType type , String name , String label , Boolean required ) {
        this.type = type;
        this.name = name;
        this.label = label;
        this.required = required;
    }

    public ParamType getType () {
        return type;
    }

    public void setType ( ParamType type ) {
        this.type = type;
    }

    public String getName () {
        return name;
    }

    public void setName ( String name ) {
        this.name = name;
    }

    public String getLabel () {
        return label;
    }

    public void setLabel ( String label ) {
        this.label = label;
    }

    public Boolean getRequired () {
        return required;
    }

    public void setRequired ( Boolean required ) {
        this.required = required;
    }

}
