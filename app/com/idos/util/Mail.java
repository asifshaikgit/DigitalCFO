package com.idos.util;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import javax.mail.Session;
import java.util.Date;

public class Mail {

    public static class Builder{
        private Session session;
        private String from;
        private String to;
        private String cc;
        private String subject;
        private String messageBody;
        private Date date;
        Email mail = new HtmlEmail();

        public Builder withSession(Session session){
            this.session= session;
            return this;
        }
        public Builder withFrom(String from){
            this.from = from;
            return this;
        }
        public Builder withTo(String to){
            this.to = to;
            return this;
        }
        public Builder withCC(String cc){
            this.cc = cc;
            return this;
        }
        public Builder withSubject(String subject){
            this.subject=subject;
            return this;
        }
        public Builder withMessageBody(String messageBody){
            this.messageBody = messageBody;
            return this;
        }
        public Builder withSentDate(Date sentDate){
            this.date = sentDate;
            return this;
        }

        public Builder sendMail(){
            try {
                mail.setMailSession(this.session);
                mail.setFrom(this.from);
                mail.addTo(this.to);
                if(this.cc != null) {
                    mail.addCc(this.cc);
                }
                mail.setSubject(this.subject);
                mail.setSentDate(this.date);
                mail.setMsg(this.messageBody);
                // mail.send();
            } catch (EmailException e) {
                e.printStackTrace();
            }
            return this;
        }

    }

}
