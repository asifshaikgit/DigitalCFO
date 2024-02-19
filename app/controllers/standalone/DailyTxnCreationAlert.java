package controllers.standalone;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// import java.util.logging.Logger;
import java.util.logging.Level;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IdosConstants;

import pojo.DailyTxnReportPojo;

/**
 * Created by Ankush A. Sapkal on 04-11-2019.
 */

public class DailyTxnCreationAlert {

    private static final String ORGANIZATION_DETAILS_QUERY = "select ORGANIZATION_ID,PARAM_VALUE from ORGANIZATION_CONFIG where PRESENT_STATUS = 1 and PARAM_NAME='REPORT_EMAIL'";
    private static final String ORGANIZATION_NAME_QUERY = "SELECT NAME FROM ORGANIZATION where PRESENT_STATUS = 1 and ID = ?1";
    private static final String DAILY_REPORT_TO_EMAILS_LIST_QUERY = "select DISTINCT EMAIL from ROLE_has_USERS UR inner join USERS U on UR.USERS_ID = U.ID inner join ROLE R on UR.ROLE_ID = R.ID where USERS_BRANCH_ORGANIZATION_ID= ?1 and UR.PRESENT_STATUS = 1 and ROLE_ID in (3,4);";
    private static final String DAILY_TRANSACTION_REPORT_QUERY = "SELECT ID,TRANSACTION_PURPOSE FROM TRANSACTION_PURPOSE where PRESENT_STATUS=1;";
    private static final String TRANSACTIONS_WITHIN_DATE_RANGE_QUERY = "SELECT ID,TRANSACTION_STATUS FROM TRANSACTION where TRANSACTION_BRANCH_ORGANIZATION = ?1 and TRANSACTION_PURPOSE = ?2 and PRESENT_STATUS = 1 and CREATED_AT between ?3 and ?4";
    private static final String PJE_TRANSACTIONS_WITHIN_DATE_RANGE_QUERY = "SELECT ID,TRANSACTION_STATUS FROM PROVISION_JOURNAL_ENTRY where BRANCH_ORGANIZATION = ?1 and PRESENT_STATUS = 1 and CREATED_AT between ?2 and ?3";

    private static final String dbClassName = "com.mysql.jdbc.Driver";
    public static Logger log = Logger.getLogger("controllers");
    public static GenericDAO genericDAO = new GenericJpaDAO();
    private static Connection connection = null;
    private static Properties prop = new Properties();
    private static Properties conProp = new Properties();
    private static String username = null;
    private static String password = null;
    private static String dbname = null;
    private static String dbpassword = null;
    private static String connectionUrl = null;
    static{
		try {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "inside static");
			}
			prop.load(DailyTxnCreationAlert.class.getResourceAsStream("/dailyPwcMailConfig.properties"));
			username = prop.getProperty("username");
			password = prop.getProperty("password");
			dbname = prop.getProperty("dbname");
            dbpassword = prop.getProperty("dbpassword");
            connectionUrl = prop.getProperty("dbconnectionurl");
            if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "inside static, properties : "+prop);
			}

            conProp.put("user", dbname);
            conProp.put("password", dbpassword);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error", e);
		}
	}

    public static Session mailSession() {
        Session session = null;
        try {
        	if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "inside mailSession "+ session);
            }
            // Local Url
//		            	prop.load(new FileInputStream("/home/idos/IDOS/IDOS_DEMO/idosapp/scripts/dailyPwcMailConfig.properties"));
            // testport URL
//		            	prop.load(new FileInputStream("/home/azureuser/cronJobs/dailyPwcMailConfig.properties"));  
            // PWC server Url
            //prop.load(new FileInputStream("/home/vmuser/idos-1.0-SNAPSHOT/script/cronjob/dailyPwcMailConfig.properties"));

            session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "Got session" + session);
            }
        } catch (Exception io) {
        	log.log(Level.SEVERE, "Error", io);
        }

        return session;
    }

    public static Connection getConnectionDB() throws ClassNotFoundException, SQLException {
        try {
        	if(log.isLoggable(Level.FINE)) {
        		log.log(Level.FINE, "inside getConnectionDB");
        	}
            if (connection == null) {
                Class.forName(dbClassName);
                connection = DriverManager.getConnection(connectionUrl, conProp);
            }
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "got connection for DB: ");
            }
        } catch (Exception e) {
        	log.log(Level.SEVERE, "Error", e);
        }
        return connection;
    }

    public static void mailTimer(final String email, final String cc, final String body, final Session session, final String subject) {
        final String alertusername = "alerts@myidos.com";
        if(log.isLoggable(Level.FINE)) {
        	log.log(Level.FINE, "mailTimer " + session);
        }
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(alertusername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            }
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "end mail timer"+session);
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        }
    }

    public static void sendDailyTranxactionReport() {
        Statement stmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        PreparedStatement stmt2 = null;
        try {
        	if(log.isLoggable(Level.FINE)) {
        		log.log(Level.FINE, "inside sendDailyTranxactionReport");
        	}
            stmt = DailyTxnCreationAlert.getConnectionDB().createStatement();
            rs = stmt.executeQuery(ORGANIZATION_DETAILS_QUERY);
            while (rs.next()) {
                Long orgId = rs.getLong("ORGANIZATION_ID");
                String orgName = "";
                String emailForReceiptant = rs.getString("PARAM_VALUE");
                stmt2 = DailyTxnCreationAlert.getConnectionDB().prepareStatement(ORGANIZATION_NAME_QUERY);
                stmt2.setLong(1, orgId);
                rs1 = stmt2.executeQuery();
                if (rs1.next()) {
                    orgName = rs1.getString("NAME");
                }

                List<DailyTxnReportPojo> dailyTransactionList = dailyTransactionReport(orgId, DailyTxnCreationAlert.getConnectionDB());
                String dailyReportToEmails = dailyReportToEmails(orgId, DailyTxnCreationAlert.getConnectionDB());

                String body = "<b><font size='5'>Hi! </font></b><br/><b><font size='5'>Greetings For The Day</font></b><br/><br/>";
                body += "<p><font size='5'>" + orgName + " -  Approval required for entries posted in Tabulate</font></p>";
                body += "<table class='table table-striped table-bordered' id='mailTable'><thead><tr style='background-color: #dddddd;'>";
                body += "	<th>Transaction Type</th>";
                body += "	<th>No. of entries created</th>";
                body += "	<th>No. of entries approved</th>";
                body += "    <th>No. of entries pending for additional approval</th>";
                body += "	<th>No. of entries accounting completed</th>";
                body += "	</tr></thead><tbody style='width: 600px;font-family: sans-serif;'>";
                if (dailyTransactionList != null) {
                    int count = 0;
                    for (DailyTxnReportPojo item : dailyTransactionList) {
                        if (count % 2 == 0) {
                            body += "<tr style='background-color: #B7F5B3;'>";
                        } else {
                            body += "<tr style='background-color: #E5F3E4;'>";
                        }
                        count++;
                        body += "<td>" + item.getTransactionPurposeName() + "</td>";
                        body += "<td>" + item.getNoOfTxnCreated() + "</td>";
                        body += "<td>" + item.getNoOfTxnApproved() + "</td>";
                        body += "<td>" + item.getNoOfTxnPendingForAdditionalApprover() + "</td>";
                        body += "<td>" + item.getNoOfTxnAccounted() + "</td></tr>";
                    }
                }
                body += "	</tbody></table>";

                String subject = orgName + "- Approval required for entries posted in Tabulate";
                Session session = mailSession();
                mailTimer(dailyReportToEmails, emailForReceiptant, body, session, subject);
                if(log.isLoggable(Level.FINE)) {
            		log.log(Level.FINE, "end sendDailyTranxactionReport"+session);
            	}
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 

                rs = null;
            }
            if (rs1 != null) {
                try {
                    rs1.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 

                rs1 = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                stmt = null;
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                stmt2 = null;
            }
            if (connection != null) {
		        try {
		        	connection.close();
		        } catch (SQLException sqlEx) {
		        	log.log(Level.SEVERE, "Error", sqlEx);
		        } 

		        connection = null;
		    }		  
        }
    }

    public static String dailyReportToEmails(Long orgId, Connection connection) {
        PreparedStatement stmt = null;
        ResultSet resultSetForEmail = null;
        String emails = "";
        try {
        	if(log.isLoggable(Level.FINE)) {
        		log.log(Level.FINE, "inside dailyReportToEmails"+connection);
        	}
            stmt = connection.prepareStatement(DAILY_REPORT_TO_EMAILS_LIST_QUERY);
            stmt.setLong(1, orgId);
            resultSetForEmail = stmt.executeQuery();
            while (resultSetForEmail.next()) {
                String email = resultSetForEmail.getString("EMAIL");
                emails = emails + email + ",";

            }
            if (emails.length() > 0) {
                emails = emails.substring(0, (emails.length() - 1));
            }
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "end dailyReportEmails, emails : "+ emails + " ,connection : "+ connection);
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        } finally {
            if (resultSetForEmail != null) {
                try {
                    resultSetForEmail.close();
                    resultSetForEmail = null;
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
            }
        }
        return emails;
    }


    public static List<DailyTxnReportPojo> dailyTransactionReport(Long orgId, Connection connection) {
        Statement stmt = null;
        ResultSet resultSet = null;
        List<DailyTxnReportPojo> dailyReportList = new ArrayList<DailyTxnReportPojo>();
        try {
        	if(log.isLoggable(Level.FINE)) {
        		log.log(Level.FINE, "inside dailyTransactionReport, con : "+connection);
        	}
            Date toDate = Calendar.getInstance().getTime();
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date fromDate = cal.getTime();
            String fromDateSql = IdosConstants.MYSQLDTF.format(fromDate);
            String toDateSql = IdosConstants.MYSQLDTF.format(toDate);
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery(DAILY_TRANSACTION_REPORT_QUERY);
            while (resultSet.next()) {
                Long transactionPurposeId = resultSet.getLong("ID");
                String transactionPurposeName = resultSet.getString("TRANSACTION_PURPOSE");
                DailyTxnReportPojo reportDataRow = null;
                if (transactionPurposeId != null && transactionPurposeName != null) {
                    if (transactionPurposeId == IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY) {
                        reportDataRow = getPJEDetailsForToandFromDate(orgId, transactionPurposeId, transactionPurposeName, toDateSql, fromDateSql, connection);
                    } else {
                        reportDataRow = getTxnDetailsForToandFromDate(orgId, transactionPurposeId, transactionPurposeName, toDateSql, fromDateSql, connection);
                    }
                }
                if (reportDataRow != null) {
                    dailyReportList.add(reportDataRow);
                }
            }
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, ">>>>>>>>>>>>>> end dailyReportList, con : "+connection);
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 

                resultSet = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                stmt = null;
            }
        }
        return dailyReportList;
    }

    public static DailyTxnReportPojo getTxnDetailsForToandFromDate(Long orgId, Long transactionPurposeId, String transactionPurposeName, String toDateSql, String fromDateSql, Connection connection) {
        PreparedStatement stmt = null;
        ResultSet resultSetForTxn = null;
        Integer noOfTxnCreated = 0;
        Integer noOfTxnApproved = 0;
        Integer noOfTxnPendingForAdditionalApprover = 0;
        Integer noOfTxnAccounted = 0;
        DailyTxnReportPojo dailyTxnData = new DailyTxnReportPojo();
        dailyTxnData.setTransactionPurposeId(transactionPurposeId);
        dailyTxnData.setTransactionPurposeName(transactionPurposeName);
        try {
        	if(log.isLoggable(Level.FINE)) {
        		log.log(Level.FINE, "inside getTxnDetailsForToandFromDate, con : "+ connection);
        	}
            stmt = connection.prepareStatement(TRANSACTIONS_WITHIN_DATE_RANGE_QUERY);
            stmt.setLong(1, orgId);
            stmt.setLong(2, transactionPurposeId);
            stmt.setString(3, fromDateSql);
            stmt.setString(4, toDateSql);
            resultSetForTxn = stmt.executeQuery();
            while (resultSetForTxn.next()) {
                Long txnId = resultSetForTxn.getLong("ID");
                String txnStatus = resultSetForTxn.getString("TRANSACTION_STATUS");
                noOfTxnCreated += 1;
                if (txnStatus.equals(IdosConstants.TXN_STATUS_ACCOUNTED)) {
                    noOfTxnApproved += 1;
                    noOfTxnAccounted += 1;
                }
                if (txnStatus.equals(IdosConstants.TXN_STATUS_APPROVED)) {
                    noOfTxnApproved += 1;
                }
                if (txnStatus.equals(IdosConstants.TXN_STATUS_REQUIRE_ADDITIONAL_APPROVAL)) {
                    noOfTxnPendingForAdditionalApprover += 1;
                }
            }
            dailyTxnData.setNoOfTxnCreated(noOfTxnCreated);
            dailyTxnData.setNoOfTxnApproved(noOfTxnApproved);
            dailyTxnData.setNoOfTxnPendingForAdditionalApprover(noOfTxnPendingForAdditionalApprover);
            dailyTxnData.setNoOfTxnAccounted(noOfTxnAccounted);
            if(log.isLoggable(Level.FINE)) {
            	log.log(Level.FINE, "end getTxnDetailsForToandFromDate, con : "+ connection);
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        } finally {
            if (resultSetForTxn != null) {
                try {
                    resultSetForTxn.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                resultSetForTxn = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                stmt = null;
            }
        }
        return dailyTxnData;
    }

    public static DailyTxnReportPojo getPJEDetailsForToandFromDate(Long orgId, Long transactionPurposeId, String transactionPurposeName, String toDateSql, String fromDateSql, Connection connection) {
        PreparedStatement stmt = null;
        ResultSet rseultSetForPJE = null;
        Integer noOfTxnCreated = 0;
        Integer noOfTxnApproved = 0;
        Integer noOfTxnPendingForAdditionalApprover = 0;
        Integer noOfTxnAccounted = 0;
        DailyTxnReportPojo dailyTxnData = new DailyTxnReportPojo();
        dailyTxnData.setTransactionPurposeId(transactionPurposeId);
        dailyTxnData.setTransactionPurposeName(transactionPurposeName);
        try {
        	if(log.isLoggable(Level.FINE)){
        		log.log(Level.FINE, "inside getPJEDetailsForToandFromDate, con : "+ connection);
        	}
            stmt = connection.prepareStatement(PJE_TRANSACTIONS_WITHIN_DATE_RANGE_QUERY);
            stmt.setLong(1, orgId);
            stmt.setString(2, fromDateSql);
            stmt.setString(3, toDateSql);
            rseultSetForPJE = stmt.executeQuery();
            while (rseultSetForPJE.next()) {
                Long pjeId = rseultSetForPJE.getLong("ID");
                String pjeStatus = rseultSetForPJE.getString("TRANSACTION_STATUS");
                noOfTxnCreated += 1;
                if (pjeStatus.equals(IdosConstants.TXN_STATUS_ACCOUNTED)) {
                    noOfTxnApproved += 1;
                    noOfTxnAccounted += 1;
                }
                if (pjeStatus.equals(IdosConstants.TXN_STATUS_APPROVED)) {
                    noOfTxnApproved += 1;
                }
                if (pjeStatus.equals(IdosConstants.TXN_STATUS_REQUIRE_ADDITIONAL_APPROVAL)) {
                    noOfTxnPendingForAdditionalApprover += 1;
                }
            }
            dailyTxnData.setNoOfTxnCreated(noOfTxnCreated);
            dailyTxnData.setNoOfTxnApproved(noOfTxnApproved);
            dailyTxnData.setNoOfTxnPendingForAdditionalApprover(noOfTxnPendingForAdditionalApprover);
            dailyTxnData.setNoOfTxnAccounted(noOfTxnAccounted);
            if(log.isLoggable(Level.FINE)){
            	log.log(Level.FINE, "end getPJEDetailsForToandFromDate, con : "+ connection);
            }
        } catch (Exception ex) {
        	log.log(Level.SEVERE, "Error", ex);
        } finally {
            if (rseultSetForPJE != null) {
                try {
                    rseultSetForPJE.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 

                rseultSetForPJE = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                	log.log(Level.SEVERE, "Error", sqlEx);
                } 
                stmt = null;
            }
        }
        return dailyTxnData;
    }

    public static void main(String[] args) {
        try {
        	 log.log(Level.SEVERE, " Started Daily Mails >>> "+(new Date()).toString());
            sendDailyTranxactionReport();
            log.log(Level.SEVERE, " Succesfully Send Daily Mails >>> "+(new Date()).toString());
        } catch (Throwable ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }
}


