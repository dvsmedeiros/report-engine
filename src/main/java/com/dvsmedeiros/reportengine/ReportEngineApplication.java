package com.dvsmedeiros.reportengine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import com.dvsmedeiros.reportengine.core.handler.IReportHandler;
import com.dvsmedeiros.reportengine.domain.CalendarParamValue;
import com.dvsmedeiros.reportengine.domain.DoubleParamValue;
import com.dvsmedeiros.reportengine.domain.Format;
import com.dvsmedeiros.reportengine.domain.IntegerParamValue;
import com.dvsmedeiros.reportengine.domain.LongParamValue;
import com.dvsmedeiros.reportengine.domain.Param;
import com.dvsmedeiros.reportengine.domain.ParamType;
import com.dvsmedeiros.reportengine.domain.Report;
import com.dvsmedeiros.reportengine.domain.ReportRequest;
import com.dvsmedeiros.reportengine.domain.ReportResponse;
import com.dvsmedeiros.reportengine.domain.StringParamValue;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@PropertySource ( "file:./config.properties" )
public class ReportEngineApplication implements CommandLineRunner {
    
    private Logger logger = LoggerFactory.getLogger("REPORT-ENGINE");
    
    @Value ( "${engine.report.path.template}" )
    private String template;
    @Value ( "${engine.report.path.result}" )
    private String result;
    @Value ( "${engine.report.path.config}" )
    private String config;
    @Value ( "${engine.report.config.report}" )
    private String configReports;

    @Autowired
    @Qualifier("jasperHandler")
    private IReportHandler handler;

    public static void main ( String[] args ) {
        SpringApplication app = new SpringApplication( ReportEngineApplication.class );
        app.setBannerMode( Banner.Mode.OFF );
        app.run( args );
    }

    @Override
    public void run ( String... args ) throws Exception {

        init();
        createReportsJsonWithExample();
        
        Long id = 1L;
        Optional < Report > found = findReportById( id );
        if(!found.isPresent()) {
            //configuração do relatorio não foi encontrada.
            //inserir configuração em em ./config/reports.json
            logger.error( "report id: " + id + " - configuration not found." );
            logger.error( "create or edit file: " + config.concat( configReports ) + " and included config of this report" );
            return;
        }
        Report report = found.get();
        logger.info( "found configuration of report id: " +  id + " with name: " +  report.getDescription());
        
        ReportRequest request = new ReportRequest();
        request.setReport( report );
        String rawJsonData = "[{\"name\":\"Jerry\", \"value\":\"Jesus\"}," + "{\"name\":\"Gideon\", \"value\": \"Loves\"}," + "{\"name\":\"Eva\", \"value\": \"You\"}" + "]";
        request.setDataSource( rawJsonData );
        request.setFormat( Format.PDF );
        request.setOwner( "Administrador" );
        
        Map<String, Object> params = new HashMap<>();
        request.getReport().getParams().forEach( param -> {
            
            //TODO refactory to use Strategy
            if(param instanceof StringParamValue) {
                StringParamValue parsed = (StringParamValue) param;
                params.put( parsed.getName() , parsed.getValue() );
            } else if(param instanceof IntegerParamValue) {
                IntegerParamValue parsed = (IntegerParamValue) param;
                params.put( parsed.getName() , parsed.getValue() );
            } else if(param instanceof LongParamValue) {
                LongParamValue parsed = (LongParamValue) param;
                params.put( parsed.getName() , parsed.getValue() );
            } else if(param instanceof DoubleParamValue) {
                DoubleParamValue parsed = (DoubleParamValue) param;
                params.put( parsed.getName() , parsed.getValue() );
            } else if(param instanceof CalendarParamValue) {
                CalendarParamValue parsed = (CalendarParamValue) param;
                params.put( parsed.getName() , parsed.getValue() );
            }
            
        });
        
        
        ReportResponse reponse = handler.execute( request , params );
        
        logger.info( "Report Name: ".concat( reponse.getName() ) );
        logger.info( "Report Size: " + reponse.getSize() );

    }

    private Optional < Report > findReportById(Long id) throws JsonParseException, JsonMappingException, IOException {
        logger.info( "searching report with id: " + id );
        Report[] configured = new ObjectMapper().readValue(new File(config.concat( configReports )), Report[].class);
        return Arrays.asList( configured ).stream().filter( report -> report.getId().equals( id ) ).findFirst();
    }
    
    private void init () {

        Arrays.asList( template , result , config ).stream()
            .filter( dir -> ! new File( dir ).exists() )
            .forEach( dir -> {
                File file = new File( dir );
                if ( ! file.exists() )
                    logger.info( "create diretório: " + file.getAbsolutePath() );
                    file.mkdirs();
            } );
    }
    
    private void createReportsJsonWithExample () throws JsonGenerationException , JsonMappingException , IOException {

        Report example = new Report();
        example.setName( "example" );
        example.setId( 1L );
        example.setDescription( "Relatório de exemplo - ReportEngine" );
        example.setTitle( "Example" );
        example.setVersion( "1.0.0" );

        Param title = new StringParamValue( ParamType.STRING , "title" , "Title" , true , "Jasper PDF Example XXX" );
        Param name = new StringParamValue( ParamType.STRING , "name" , "Name" , true , "Nome" );
        Param value = new StringParamValue( ParamType.STRING , "value" , "Value" , true , "Valor" );

        Param integer = new IntegerParamValue( ParamType.INTEGER , "integer_test" , "Integer" , true , 1 );
        Param longValue = new LongParamValue( ParamType.LONG , "long_test" , "Long" , true , 1L );
        Param doubleValue = new DoubleParamValue( ParamType.DOUBLE , "double_test" , "Double" , true , 1D );
        Param calendar = new CalendarParamValue( ParamType.DATE , "calendar_teste" , "Calendar" , true , Calendar.getInstance() );

        example.setParams( Arrays.asList( title , name , value , integer , longValue , doubleValue , calendar ) );

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue( new File( config.concat( configReports ) ) , Arrays.asList( example ) );
    }
}
