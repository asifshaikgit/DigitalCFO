package controllers.standalone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import model.ConfigParams;


public class BranchOperationalAlerts  {
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static final String connection ="jdbc:mysql://idosapp.cloudapp.net:3307/idostest";
	private static Properties prop = new Properties();
	
	 public static Session mailSession(){
		Session session = null;
		try {
			prop.load(BranchOperationalAlerts.class.getResourceAsStream("/dailyPwcMailConfig.properties"));
			String smtpHost="smtp.mandrillapp.com";
			String smtpPort="2525";
			final String username="alert@myidos.com";
			final String password=prop.getProperty("boapassword");
			Properties props=new Properties();
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.from",username);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.debug", "true");
			session=Session.getInstance(props,new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			System.out.println("Got session" + session);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return session;	
	}
		 
	public static void mailTimer(final String email,final String cc,final String body,final Session session,final String subject){
		final String alertusername = "alerts@myidos.com";
		System.out.println("mailTimer " + session);
		try{
			Email mail = new HtmlEmail();			
			mail.setMailSession(session);
			mail.setFrom(alertusername);
			mail.addTo(email);
			if(cc!=null){
				mail.addCc(cc);
			}
			mail.setSubject(subject);
			mail.setSentDate(new Date());
			mail.setMsg(body);
			mail.send();
		}catch(EmailException ex){
			//final Session reattemptsession=mailSession();
			//final String alertusername=Play.application().configuration().getString("smtpalert.user");
			//mailReattempt(body, alertusername, reattemptsession, email, cc, subject);
			ex.printStackTrace();
		}			
	}
	
	public static Connection getConnection() throws ClassNotFoundException,SQLException{
		 Class.forName(dbClassName);
		 Properties p = new Properties();
		 p.put("user","idostestuser");
		 p.put("password","testpwd#16");
		 Connection c = DriverManager.getConnection(connection,p);
		 return c;
	 }	
	
	
	public static void updateBranchStatutoryValidity() {				
		Connection con=null;Statement stmt=null;ResultSet rs=null;
		try{
			con= BranchOperationalAlerts.getConnection();
			stmt = con.createStatement();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			Calendar todaysDateCal = Calendar.getInstance();
			Date todaysDate = todaysDateCal.getTime();
			String currentDateStr = formatter.format(todaysDate);			
			System.out.println("currentDateStr " + currentDateStr);
		    rs = stmt.executeQuery("select statutory_details,registration_number,valid_from,VALID_TO,name_address_of_consultant,remarks,alert_for_action,alert_for_information from ORGANIZATION_STATUTORY where '"+ currentDateStr + "' < VALID_TO");
			System.out.println("10");
			while(rs.next()){	
				System.out.println("11");
				Date validTo = rs.getDate("VALID_TO");	
				Calendar cal = Calendar.getInstance();
				cal.setTime(validTo);
				cal.add(Calendar.DATE, -3);
				Date dateBefore3Days = cal.getTime();
				System.out.println("dateBefore3Days < " + dateBefore3Days + " crrentdate< " + todaysDate + "expiry date" + validTo);
				if(todaysDate.after(dateBefore3Days)){ //so send mail if valid_to-3 < currendate < valid_to i.e. start sending mail 3 days prior to expiry date
					System.out.println("12");
					System.out.println("inside rs " );
					String statDetails = rs.getString("statutory_details");
					String regNo = rs.getString("registration_number");
					Date validFrom = rs.getDate("valid_from");					
					String consultantDetails = rs.getString("name_address_of_consultant");
					String remarks = rs.getString("remarks");
					String emailIdForAction = rs.getString("alert_for_action");
					String emailIdForInfo = rs.getString("alert_for_information");
					
					String body = "<table border=1 bgcolor='#bfcfe4'>";
					body = body +"<tbody style='background-color: #dddddd;'><tr id='emailHeader'><td><h3>IDOS - Digital CFO</h3></td></tr>";
					body = body +"<tr id='emailBody'><td><table><tbody style='width: 600px;font-family: sans-serif;'>";
					body = body +"<tr><td colspan='2' style='font-size: 16px; font-weight: bold;'>Your Administrator requires you to attend to the following.</td></tr>";
					body = body + "<tr><td>Issue	 	 	   </td><td>:" + statDetails+"</td></tr>";
					body = body + "<tr><td>Reference	       </td><td>:" + regNo+"</td></tr>";
					body = body + "<tr><td>Validity From       </td><td>:" + validFrom+"</td></tr>";
					body = body + "<tr><td>Validity To 	       </td><td>:" + validTo+"</td></tr>";
					body = body + "<tr><td>Consultant Details  </td><td>:" + consultantDetails+"</td></tr>";
					body = body + "<tr><td>Remarks			   </td><td>:" + remarks+"</td></tr>";					
					
					String emailfooter = "<tr id='emailFooter'><br><td><table><tr><td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='"+ConfigParams.getInstance().getCompanyLogoPath()+"'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/fb.png'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/linkedin.png'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/twitter.png'/></td></tr></table></td></tr>";
					emailfooter = emailfooter + "<tr><td>IDOS</tr></td><tr><td>* This is a computer generated e-mail. Please do not reply.</tr></td></tbody></table>";
					
					String bodyAlertForAction = body + "<tr><td>CC To               </td><td>:" + emailIdForInfo+"</td></tr></tbody></table></td></tr>" + emailfooter;
					String bodyAlertForInfo = body + "<tr><td>CC To               </td><td>:" + emailIdForAction+"</td></tr></tbody></table></td></tr>" + emailfooter;
					
					Session session = mailSession();
					String subjectAlertForAction = "Statutory Alert For Action";
					String subjectAlertForInfo = "Statutory Alert For Information";
					mailTimer(emailIdForAction,null,bodyAlertForAction,session,subjectAlertForAction);
					mailTimer(emailIdForInfo,null,bodyAlertForInfo,session,subjectAlertForInfo);
				}				
			}						
		}catch(Exception ex){			
			ex.printStackTrace();			
		}
		finally {
		    // it is a good idea to release resources in a finally{} block
		    // in reverse-order of their creation if they are no-longer needed
		    if (rs != null) {
		        try {
		            rs.close();
		        } catch (SQLException sqlEx) { } // ignore
		        rs = null;
		    }
		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException sqlEx) { } // ignore
		        stmt = null;
		    }
		    if (con != null) {
		        try {
		        	con.close();
		        } catch (SQLException sqlEx) { } // ignore
		        con = null;
		    }		   
		}
	}
	
	//Branch Operational Reminders
	public static void sendBranchOperationalReminders() {				
		Connection con=null;Statement stmt=null;ResultSet rs=null;
		try{
			con= BranchOperationalAlerts.getConnection();
			stmt = con.createStatement();						
			Calendar todaysDateCal = Calendar.getInstance();
			todaysDateCal.set(Calendar.HOUR_OF_DAY, 0);
			todaysDateCal.set(Calendar.MINUTE, 0);
			todaysDateCal.set(Calendar.SECOND, 0);
			todaysDateCal.set(Calendar.MILLISECOND, 0);
			Date todaysDate = todaysDateCal.getTime();			
			System.out.println("Inside sendBranchOperationalReminders");
			rs = stmt.executeQuery("select DUE_ON,VALID_TO,requirements,alert_for_action,alert_for_information,remarks,recurrences from ORGANIZATION_OPERATIONAL_REMAINDERS where DUE_ON < curdate()  and curdate() < VALID_TO");
			
			while(rs.next()){
				System.out.println("inside while of sendBranchOperationalReminders");
			  Date validFrom = rs.getDate("DUE_ON");	
			  Date validTo = rs.getDate("VALID_TO");	
			  Date dateAfter7Days=validFrom;
			  boolean datesMatchedMailSent=false;
			  while((validTo.after(dateAfter7Days) || validTo.equals(dateAfter7Days)) && !datesMatchedMailSent){
				 System.out.println("2");
				if( todaysDate.equals(dateAfter7Days)){ //so send mail if valid_from+7 = currendate  and currentdate < valid_to i.e. start sending mail weekly till valid_to
					System.out.println("3");
					String requirements = rs.getString("requirements");						
					String remarks = rs.getString("remarks");
					String emailIdForAction = rs.getString("alert_for_action");
					String emailIdForInfo = rs.getString("alert_for_information");
											
					String body = "<table border=1 bgcolor='#bfcfe4'>";
					body = body +"<tbody style='background-color: #dddddd;'><tr id='emailHeader'><td><h3>IDOS - Digital CFO</h3></td></tr>";
					body = body +"<tr id='emailBody'><td><table><tbody style='width: 600px;font-family: sans-serif;'>";
					body = body +"<tr><td colspan='2' style='font-size: 16px; font-weight: bold;'>Your Administrator requires you to attend to the following.</td></tr>";
					body = body + "<tr><td>Issue	 	 	   </td><td>:" + requirements+"</td></tr>";						
					body = body + "<tr><td>Validity From       </td><td>:" + validFrom+"</td></tr>";
					body = body + "<tr><td>Validity To 	       </td><td>:" + validTo+"</td></tr>";						
					body = body + "<tr><td>Remarks			   </td><td>:" + remarks+"</td></tr>";					
					
					String emailfooter = "<tr id='emailFooter'><br><td><table><tr><td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='"+ConfigParams.getInstance().getCompanyLogoPath()+"'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/fb.png'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/linkedin.png'/></td>";
					emailfooter = emailfooter + "<td style='text-align: left;'>&nbsp;<img  border='0' width='30px' height='30px' src='https://www.myidos.com/assets/images/twitter.png'/></td></tr></table></td></tr>";
					emailfooter = emailfooter + "<tr><td>IDOS</tr></td><tr><td>* This is a computer generated e-mail. Please do not reply.</tr></td></tbody></table>";
					
					String bodyAlertForAction = body + "<tr><td>CC To               </td><td>:" + emailIdForInfo+"</td></tr></tbody></table></td></tr>" + emailfooter;
					String bodyAlertForInfo = body + "<tr><td>CC To               </td><td>:" + emailIdForInfo+"</td></tr></tbody></table></td></tr>" + emailfooter;
					String subjectAlertForAction = "Operational Alert For Action";
					String subjectAlertForInfo = "Operational Alert For Information";
							
					Session session = mailSession();
					datesMatchedMailSent=true;
					mailTimer(emailIdForAction,null,bodyAlertForAction,session,subjectAlertForAction);
					mailTimer(emailIdForInfo,null,bodyAlertForInfo,session,subjectAlertForInfo);
				}else{
					Calendar cal = Calendar.getInstance();
					cal.setTime(dateAfter7Days);
					Integer recurrences = rs.getInt("recurrences");
					if(recurrences == 1){//weekly						
						cal.add(Calendar.DATE, +7);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 2){//Monthly						
						cal.add(Calendar.MONTH, +1);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 3){//Quarterly						
						cal.add(Calendar.MONTH, +3);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 4){//Half Yearly				
						cal.add(Calendar.MONTH, +6);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 5){//Annually						
						cal.add(Calendar.YEAR, +1);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 6){//Once in 2 years						
						cal.add(Calendar.YEAR, +2);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 7){//Once in 2 years						
						cal.add(Calendar.YEAR, +2);
						dateAfter7Days = cal.getTime();
					}else if(recurrences == 8){//only Once, means send mails when dates match due_on=todays date 						
						datesMatchedMailSent=true;
					}						
				 }
			   }
			}						
		}catch(Exception ex){			
			ex.printStackTrace();			
		}
		finally {
		    // it is a good idea to release resources in a finally{} block
		    // in reverse-order of their creation if they are no-longer needed
		    if (rs != null) {
		        try {
		            rs.close();
		        } catch (SQLException sqlEx) { } // ignore
		        rs = null;
		    }
		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException sqlEx) { } // ignore
		        stmt = null;
		    }
		    if (con != null) {
		        try {
		        	con.close();
		        } catch (SQLException sqlEx) { } // ignore
		        con = null;
		    }		   
		}
	}
	
	public static void main(String[] args) {
		try{		
			updateBranchStatutoryValidity();
			sendBranchOperationalReminders();
		}catch(Exception ex){					
			ex.printStackTrace();
		}
	}


}
