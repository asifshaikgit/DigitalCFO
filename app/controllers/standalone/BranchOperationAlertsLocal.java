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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// import java.util.logging.Logger;
import java.util.logging.Level;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

import model.ConfigParams;

	public class BranchOperationAlertsLocal  {
		private static final String dbClassName = "com.mysql.jdbc.Driver";
		 private static final String connection ="jdbc:mysql://localhost:3307/idos";
		 public static Logger log = Logger.getLogger("controllers");
		 public static GenericDAO genericDAO=new GenericJpaDAO();
		 
		 
		 
		 public static void mailSessionLocal(String emailIdForAction, String emailIdForInfo, String subject, String body){
		      // Recipient's email ID needs to be mentioned.
		      String to = emailIdForAction;

		      // Sender's email ID needs to be mentioned
		      String from = "alerts@myidos.com";

		      // Assuming you are sending email from localhost
		      String host = "localhost";

		      // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.setProperty("mail.smtp.host", host);

		      // Get the default Session object.
		      Session session = Session.getDefaultInstance(properties);

		      try {
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		         // Set Subject: header field
		         message.setSubject(subject);

		         // Send the actual HTML message, as big as you like
		        // message.setContent("<h1>This is actual message</h1>", "text/html");
		         message.setContent(body, "text/html");
		         // Send message
		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      }
		   }
		
		 
		 public static Connection getConnectionLocal() throws ClassNotFoundException,SQLException{
			 Class.forName(dbClassName);
			 Properties p = new Properties();
			 p.put("user","root");
			 p.put("password","root");
			 Connection c = DriverManager.getConnection(connection,p);
			 return c;
		 }	
		
		
		public static void updateBranchStatutoryValidityLocal() {
			log.log(Level.FINE, ">>>> Start");		
			Connection con=null;Statement stmt=null;ResultSet rs=null;
			try{
				con= getConnectionLocal();
				stmt = con.createStatement();							
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
				Calendar todaysDateCal = Calendar.getInstance();
				Date todaysDate = todaysDateCal.getTime();
				String currentDateStr = formatter.format(todaysDate);
								
			    rs = stmt.executeQuery("select statutory_details,registration_number,valid_from,VALID_TO,name_address_of_consultant,remarks,alert_for_action,alert_for_information from organization_statutory where '"+ currentDateStr + "' < VALID_TO"); 
				
				while(rs.next()){
					Date validTo = rs.getDate("VALID_TO");	
					Calendar cal = Calendar.getInstance();
					cal.setTime(validTo);
					cal.add(Calendar.DATE, -3);
					Date dateBefore3Days = cal.getTime();
					if(todaysDate.after(dateBefore3Days)){ //so send mail if valid_to-3 < currendate < valid_to i.e. start sending mail 3 days prior to expiry date
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
						String bodyAlertForInfo = body + "<tr><td>CC To               </td><td>:" + emailIdForInfo+"</td></tr></tbody></table></td></tr>" + emailfooter;
						String subjectAlertForAction = "Statutory Alert For Action";
						String subjectAlertForInfo = "Statutory Alert For Information";
								
						mailSessionLocal(emailIdForAction,emailIdForInfo,subjectAlertForAction,bodyAlertForAction);
						mailSessionLocal(emailIdForInfo,emailIdForAction,subjectAlertForInfo,bodyAlertForInfo);
					}		
				}						
			}catch(Exception ex){
				
				log.log(Level.SEVERE, "Error", ex);
				// log.log(Level.SEVERE, ex.getMessage());
				ex.printStackTrace();
				
			}
			finally {
			    // it is a good idea to release
			    // resources in a finally{} block
			    // in reverse-order of their creation
			    // if they are no-longer needed

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
		
		
		public static void sendBranchOperationalRemindersLocal() {
			log.log(Level.FINE, ">>>> Start");		
			Connection con=null;Statement stmt=null;ResultSet rs=null;
			try{
				con= getConnectionLocal();
				stmt = con.createStatement();							
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
				Calendar todaysDateCal = Calendar.getInstance();
				todaysDateCal.set(Calendar.HOUR_OF_DAY, 0);
				todaysDateCal.set(Calendar.MINUTE, 0);
				todaysDateCal.set(Calendar.SECOND, 0);
				todaysDateCal.set(Calendar.MILLISECOND, 0);
				Date todaysDate = todaysDateCal.getTime();
				String currentDateStr = formatter.format(todaysDate);
								
				rs = stmt.executeQuery("select DUE_ON,VALID_TO,requirements,alert_for_action,alert_for_information,remarks,recurrences from ORGANIZATION_OPERATIONAL_REMAINDERS where DUE_ON < curdate()  and curdate() < VALID_TO");
				
				while(rs.next()){
				  Date validFrom = rs.getDate("DUE_ON");	
				  Date validTo = rs.getDate("VALID_TO");	
				  Date dateAfter7Days=validFrom;
				  boolean datesMatchedMailSent=false;
				  while((validTo.after(dateAfter7Days) || validTo.equals(dateAfter7Days)) && !datesMatchedMailSent){					
					if( todaysDate.equals(dateAfter7Days)){ //so send mail if valid_from+7 = currendate  and currentdate < valid_to i.e. start sending mail weekly till valid_to
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
								
						mailSessionLocal(emailIdForAction,emailIdForInfo,subjectAlertForAction,bodyAlertForAction);
						datesMatchedMailSent=true;
						//mailSessionLocal(emailIdForInfo,emailIdForAction,subjectAlertForInfo,bodyAlertForInfo);
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
				
				log.log(Level.SEVERE, "Error", ex);
				// log.log(Level.SEVERE, ex.getMessage());
				ex.printStackTrace();
				
			}
			finally {
			    // it is a good idea to release
			    // resources in a finally{} block
			    // in reverse-order of their creation
			    // if they are no-longer needed

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
				
				//updateBranchStatutoryValidityLocal();
				sendBranchOperationalRemindersLocal();
			}catch(Exception ex){		
				log.log(Level.SEVERE, "Error", ex);
				// log.log(Level.SEVERE, ex.getMessage());	
				ex.printStackTrace();
			}
		}


	}

