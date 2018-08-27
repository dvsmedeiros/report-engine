package com.dvsmedeiros.reportengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
@PropertySource ( "file:${report.engine.home}/config.properties" )
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
        
    	long start = System.currentTimeMillis();
    	logger.info("=================== encoding ====================");
		if (System.getProperty("file.encoding") == null || !System.getProperty("file.encoding").equalsIgnoreCase(StandardCharsets.UTF_8.name())) {
			System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
		}
		logger.info(String.format("file.encoding: %s", System.getProperty("file.encoding")));    	    	
    	
        Long reportId = 0L;
        String dataSourceName = "input.json";
        
        boolean hasArgs = args != null;
        boolean hasReporIdArg = hasArgs && args.length > 0;
        boolean hasInputDataSourceArg = hasArgs && args.length > 1;
        boolean hasOutputFileNameArg = hasArgs && args.length > 2;
        boolean hasReportId = hasReporIdArg && args[0].chars().allMatch( Character::isDigit );
        boolean hasDataSourceName = hasInputDataSourceArg && !args[ 1 ].isEmpty();  
        boolean hasOutputFileName = hasOutputFileNameArg && !args[ 2 ].isEmpty();
        
        logger.info("================ report request =================");
        if ( !hasReporIdArg || !hasReportId ) {
            logger.error( "report id arg is required: ");
            return;
        }
        reportId = Long.parseLong( args[ 0 ] );
        logger.info( "ID         : " + reportId );
        
        if ( hasInputDataSourceArg && hasDataSourceName ) {
            dataSourceName = args[ 1 ];
        }
        logger.info( "DATA SOURCE: " + dataSourceName );        
        init();
        
        if(createExample != null && createExample.equals( Boolean.TRUE )) {
            createReportsJsonWithExample();
        }
        
        logger.info("=============== start processing ================");
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
		if (hasOutputFileName) {
			request.setOutputFileName(args[2]);
		}
        
        Map<String, Object> params = new HashMap<>();
        request.getReport().getParams().forEach( param -> {
            
            //TODO refactory to use Strategy
            if ( param instanceof StringParamValue ) {
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
        long end = System.currentTimeMillis();
        
        logger.info("============ Executed at: " +  (end - start) + "(ms) ==============");
    }

    private Optional < Report > findReportById(Long id) throws JsonParseException, JsonMappingException, IOException {
        logger.info( "searching report with id: " + id );
		InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(config.concat(configReports)), StandardCharsets.UTF_8);
        Report[] configured = new ObjectMapper().readValue(inputStreamReader, Report[].class);        
        return Arrays.asList( configured ).stream().filter( report -> report.getId().equals( id ) ).findFirst();
    }
    
    private Optional < JsonNode > readInputDataSource(String fileName) throws JsonParseException, JsonMappingException, IOException {
        logger.info( "searching input data source with name: " + fileName );                       
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(input.concat( fileName )), StandardCharsets.UTF_8);
        JsonNode readValue = new ObjectMapper().readValue( inputStreamReader, JsonNode.class);        
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
                if ( ! file.exists() ) {                	
                	logger.info( "create directory: " + file.getAbsolutePath() );
                    file.mkdirs();
                }                    
            } );
        if(compile != null && compile.equals( Boolean.TRUE )) {
            handler.compile();
        }
    }
    
    private void createReportsJsonWithExample () throws JsonGenerationException , JsonMappingException , IOException {
    	
    	File reports = new File(config.concat(configReports));
    	logger.info("=========== create config example file ==========");
    	if(reports.exists()) {
    		logger.info("reports.json already exist. For create a new example file remove ".concat(config.concat(configReports)).concat(" and restart app."));
    		return;
    	}
    	
        Report example = new Report();
        example.setName( "sample" );
        example.setId( 1L );
        example.setDescription( "Sample Description - ReportEngine" );
        example.setTitle( "Sample - ReportEngine" );
        example.setVersion( "1.0.0" );
        
        String base64image = "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";
        
        Param paramImage = new StringParamValue( ParamType.BASE64 , "param_image" , "Image: " , true , base64image );
        Param paramFoo = new StringParamValue( ParamType.STRING , "param_foo" , "Foo: " , true , "Bar" );
        Param paramInteger = new IntegerParamValue( ParamType.INTEGER , "param_integer" , "Number: " , true , 1 );
        Param paramLong = new LongParamValue( ParamType.LONG , "param_long" , "Long Number: " , true , 1L );
        Param paramDouble = new DoubleParamValue( ParamType.DOUBLE , "param_double" , "Double Number: " , true , 1D );
        Calendar christmas = Calendar.getInstance();
        christmas.set( Calendar.DAY_OF_MONTH , 25 );
        christmas.set( Calendar.MONTH , Calendar.DECEMBER );
        christmas.set( Calendar.YEAR , 2018 );
        Param paramCalendar = new CalendarParamValue( ParamType.DATE , "param_calendar" , "Now: " , true , christmas );

        example.setParams( Arrays.asList(paramImage, paramFoo , paramInteger , paramLong , paramDouble , paramCalendar ) );
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue( new File( config.concat( configReports ) ) , Arrays.asList( example ) );
    }
}
