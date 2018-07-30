package com.dvsmedeiros.reportengine.core.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dvsmedeiros.reportengine.domain.DefaultConfig;
import com.dvsmedeiros.reportengine.domain.FileExtention;
import com.dvsmedeiros.reportengine.domain.Format;
import com.dvsmedeiros.reportengine.domain.Report;
import com.dvsmedeiros.reportengine.domain.ReportRequest;
import com.dvsmedeiros.reportengine.domain.ReportResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Component
public class JasperHandler implements IReportHandler {
    
    private static final Logger logger = LoggerFactory.getLogger( "REPORT-ENGINE" );
    
    @Value ( "${engine.report.path.template}" )
    private String templatePath;
    @Value ( "${engine.report.path.result}" )
    private String resultPath;
    
    @Override
    public ReportResponse execute ( ReportRequest request , Map < String , Object > params ) {
        
        ReportResponse response = new ReportResponse();
        
        try {
            
            boolean hasRequest = request != null;
            boolean hasReport = hasRequest && request.getReport() != null;
            
            Report reportRequest = request.getReport();
            
            params.put( "TITLE" , hasReport && reportRequest.getTitle() != null ? reportRequest.getTitle() : "" );
            params.put( "DESCRIPTION" , hasReport && reportRequest.getDescription() != null ? reportRequest.getDescription() : "" );
            params.put( "VERSION" , hasReport && reportRequest.getVersion() != null ? reportRequest.getVersion() : "" );
            params.put( "SUB_REPORT_DIR" , templatePath != null && !templatePath.isEmpty() ? templatePath : DefaultConfig.TEMPLATE.getValue());
            
            resultPath = resultPath != null && !resultPath.isEmpty() ? resultPath : DefaultConfig.RESULT.getValue();
            
            String jasperFileName = templatePath.concat( request.getReport().getName() ).concat( FileExtention.JASPER.getExtention() );
            JasperReport report = ( JasperReport ) JRLoader.loadObject( new File( jasperFileName ) );
            JasperPrint jasperPrint = JasperFillManager.fillReport( report , params , request.getJsonDataSource() );
            String responseFileName = resultPath.concat( request.getReport().getName() ).concat( "_" ).concat( UUID.randomUUID().toString() ).concat( request.getFormat().getExtension() );
            //Save to ${report.path.result} folder
            JasperExportManager.exportReportToPdfFile(jasperPrint , responseFileName );
            
            if(request.getFormat().equals( Format.HTML )) {
                JasperExportManager.exportReportToHtmlFile(jasperPrint , responseFileName );
            } else {
                JasperExportManager.exportReportToPdfFile(jasperPrint , responseFileName ); 
            }
            
            response.setFile(JasperExportManager.exportReportToPdf( jasperPrint ));
            response.setName( responseFileName );
            
        } catch ( JRException e ) {
            e.printStackTrace();
        }
        
        return response; 
    }

    public void compile () {
        
        try {
            Files.list( Paths.get( templatePath ) )
                .filter( file -> FileExtention.JRXML.getExtention().endsWith( FilenameUtils.getExtension( file.getFileName().toString() ) ) )
                .forEach( file -> {
                    logger.info( "compiling report design: " + file.getFileName() );
                    try {
                        /**
                         * Compile the report to a file name same as the JRXML file name
                         */
                        logger.info( "compiling report from: " + file.toAbsolutePath() );
                        String source = file.toAbsolutePath().toString();                    
                        String compileReportToFile = JasperCompileManager.compileReportToFile(source);
                        logger.info( compileReportToFile );
                    } catch ( JRException e ) {
                        e.printStackTrace();
                    }
                    logger.info( "done compiling." );
                } );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}
