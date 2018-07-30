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
import com.dvsmedeiros.reportengine.domain.DefaultConfig;
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
        boolean hasReportId = hasReporIdArg && args[0].chars().allMatch( Character::isDigit );
        boolean hasDataSourceName = hasInputDataSourceArg && !args[ 1 ].isEmpty();
        
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
        
        template = template != null && !template.isEmpty() ? template : DefaultConfig.TEMPLATE.getValue();
        result = result != null && !result.isEmpty() ? result : DefaultConfig.RESULT.getValue();
        config = config != null && !config.isEmpty() ? config : DefaultConfig.CONFIG.getValue();
        input = input != null && !input.isEmpty() ? input : DefaultConfig.INPUT.getValue();
        
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
        example.setName( "sample" );
        example.setId( 1L );
        example.setDescription( "Sample Description - ReportEngine" );
        example.setTitle( "Sample - ReportEngine" );
        example.setVersion( "1.0.0" );

        Param paramFoo = new StringParamValue( ParamType.STRING , "param_foo" , "Foo: " , true , "Bar" );

        Param paramInteger = new IntegerParamValue( ParamType.INTEGER , "param_integer" , "Number: " , true , 1 );
        Param paramLong = new LongParamValue( ParamType.LONG , "param_long" , "Long Number: " , true , 1L );
        Param paramDouble = new DoubleParamValue( ParamType.DOUBLE , "param_double" , "Double Number: " , true , 1D );
        Calendar christmas = Calendar.getInstance();
        christmas.set( Calendar.DAY_OF_MONTH , 25 );
        christmas.set( Calendar.MONTH , Calendar.DECEMBER );
        christmas.set( Calendar.YEAR , 2018 );
        Param paramCalendar = new CalendarParamValue( ParamType.DATE , "param_calendar" , "Now: " , true , christmas );

        example.setParams( Arrays.asList(paramFoo , paramInteger , paramLong , paramDouble , paramCalendar ) );

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue( new File( config.concat( configReports ) ) , Arrays.asList( example ) );
    }
}
