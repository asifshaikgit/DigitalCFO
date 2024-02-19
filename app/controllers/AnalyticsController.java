package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;


import javax.transaction.Transactional;
import play.mvc.Result;
import play.mvc.Results;
import service.AnalyticsService;
import service.AnalyticsServiceImpl;
import java.util.logging.Level;

public class AnalyticsController extends SellerStaticController {
	@Transactional
	public Result getVendorSellerAnalytics(final String email) {
		log.log(Level.FINE,">>>> Start");
		ObjectNode on = null;
		try {
			on = ANALYTICS_SERVICE.getVendorSellerAnalytics(email);
		} catch (Exception ex) {
			log.log(Level.SEVERE,"Error",ex.getMessage());
			String strBuff=getStackTraceMessage(ex);
		   expService.sendExceptionReport(strBuff,"Get Analytics", "Get Analytics", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(on);
	}

}
