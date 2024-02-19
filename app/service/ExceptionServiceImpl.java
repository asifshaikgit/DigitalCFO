package service;

import javax.mail.Session;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.inject.Inject;
import controllers.StaticController;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import views.html.*;

public class ExceptionServiceImpl implements ExceptionService {

	@Inject
	public ExceptionServiceImpl(){
	}

	@Override
	public void sendExceptionReport(String exceptionTrace,String userEmail,String organizationName,String methodName) {
		Session session= StaticController.exceptionEmailSession;
		final String username=ConfigFactory.load().getString("smtpexception.user");
		String body=exceptionPage.render(exceptionTrace,userEmail,organizationName,methodName).body();
		String subject="Exception Details";
		StaticController.mailTimer(body, username, session, "alerts@myidos.com", "allusers@myidos.com", subject);
	}
}
