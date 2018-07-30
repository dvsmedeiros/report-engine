package com.dvsmedeiros.reportengine.core.handler;

import java.util.Map;

import com.dvsmedeiros.reportengine.domain.ReportRequest;
import com.dvsmedeiros.reportengine.domain.ReportResponse;

public interface IReportHandler {
    public ReportResponse execute ( ReportRequest request , Map < String , Object > params );
    public void compile();
}
