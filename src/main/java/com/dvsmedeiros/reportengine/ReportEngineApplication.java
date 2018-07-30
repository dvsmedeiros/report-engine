package com.dvsmedeiros.reportengine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@PropertySource ( "file:./config.properties" )
public class ReportEngineApplication implements CommandLineRunner {
    
    private Logger logger = LoggerFactory.getLogger("REPORT-ENGINE");
    
    @Value ( "${engine.report.path.template}" )
    private String template;
    @Value ( "${engine.report.path.input}" )
    private String input;
    @Value ( "${engine.report.path.result}" )
    private String result;
    @Value ( "${engine.report.path.config}" )
    private String config;
    @Value ( "${engine.report.config.report}" )
    private String configReports;
    @Value ( "${engine.report.create.example}" )
    private Boolean createExample;
    @Value ( "${engine.report.compile.all}" )
    private Boolean compile;
    
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
        
        Long reportId = 0L;
        String dataSourceName = "input.json";
        
        boolean hasArgs = args != null;
        boolean hasReporIdArg = hasArgs && args.length > 0;
        boolean hasInputDataSourceArg = hasArgs && args.length > 1;
        boolean hasReportId = hasReporIdArg && StringUtils.isNumeric( args[ 0 ] );
        boolean hasDataSourceName = hasInputDataSourceArg && StringUtils.isNotEmpty( args[ 1 ] );
        
        if ( !hasReporIdArg || !hasReportId ) {
            logger.error( "report id arg is required: ");
            return;
        }
        reportId = Long.parseLong( args[ 0 ] );
        logger.info( "report id: " + reportId );
        
        if ( hasInputDataSourceArg && hasDataSourceName ) {
            dataSourceName = args[ 1 ];
        }
        logger.info( "data source name: " + dataSourceName );
        
        init();
        
        if(createExample != null && createExample.equals( Boolean.TRUE )) {
            createReportsJsonWithExample();
        }
        
        Optional < Report > found = findReportById( reportId );
        if(!found.isPresent()) {
            //configuração do relatorio não foi encontrada.
            //inserir configuração em em ./config/reports.json
            logger.error( "report id: " + reportId + " - configuration not found." );
            logger.error( "create or edit file: " + config.concat( configReports ) + " and included config of this report" );
            return;
        }
        
        Report report = found.get();
        logger.info( "found configuration of report id: " +  reportId + " with name: " +  report.getDescription());
        
        ReportRequest request = new ReportRequest();
        request.setReport( report );
        
        readInputDataSource( dataSourceName ).ifPresent( ds -> request.setDataSource( ds ) );        
        
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
    
    private Optional < JsonNode > readInputDataSource(String fileName) throws JsonParseException, JsonMappingException, IOException {
        logger.info( "searching input data source with name: " + fileName );
        JsonNode readValue = new ObjectMapper().readValue(new File(input.concat( fileName )), JsonNode.class);
        return Optional.of( readValue );
    }
    
    private void init () {
        
        Arrays.asList( template , result , config, input ).stream()
            .filter( dir -> ! new File( dir ).exists() )
            .forEach( dir -> {
                File file = new File( dir );
                if ( ! file.exists() )
                    logger.info( "create diretório: " + file.getAbsolutePath() );
                    file.mkdirs();
            } );
        
        if(compile != null && compile.equals( Boolean.TRUE )) {
            handler.compile();
        }
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
