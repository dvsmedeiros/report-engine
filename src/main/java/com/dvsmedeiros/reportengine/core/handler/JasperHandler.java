package com.dvsmedeiros.reportengine.core.handler;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dvsmedeiros.reportengine.domain.FileExtention;
import com.dvsmedeiros.reportengine.domain.Format;
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

    @Value ( "${engine.report.path.template}" )
    private String templatePath;
    @Value ( "${engine.report.path.result}" )
    private String resultPath;

    @Override
    public ReportResponse execute ( ReportRequest request , Map < String , Object > params ) {
        
        ReportResponse response = new ReportResponse();
        
        try {
            
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
        String sourceFileName = "./reports/" + "/example.jrxml";
        System.out.println( "Compiling Report Design ..." );
        try {
            /**
             * Compile the report to a file name same as the JRXML file name
             */
            JasperCompileManager.compileReportToFile( sourceFileName );
        } catch ( JRException e ) {
            e.printStackTrace();
        }
        System.out.println( "Done compiling!!! ..." );
    }

}
