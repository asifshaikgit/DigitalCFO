package com.idos.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.Json.*;


/**
 * Created by Sunil Namdev on 10-05-2016.
 */
public class VerifyRecaptcha {

    private static final  String url = ConfigFactory.load().getString("recaptcha.url");
    private static final  String secret =  ConfigFactory.load().getString("recaptcha.private.key");
    private final static String USER_AGENT = ConfigFactory.load().getString("recaptcha.user.agent");
    private final static String CONNECTION_TIMEOUT = ConfigFactory.load().getString("recpatcha.request.timeout");
    private final static String READ_TIMEOUT = ConfigFactory.load().getString("recpatcha.read.timeout");
    ///public static final  String url = "https://www.google.com/recaptcha/api/siteverify";
    ///public static final  String secret = "6LfIlB4TAAAAAJdaRTxZgb0TJCpdEiOjkZUX_E_U";
    //private final static String USER_AGENT = "Mozilla/5.0";

    public static String verify(String gRecaptchaResponse) throws Exception {
        if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
            return null;
        }
        String response = "";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setConnectTimeout(Integer.parseInt(CONNECTION_TIMEOUT));
        con.setReadTimeout(Integer.parseInt(READ_TIMEOUT));
        String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response += inputLine;
        }
        in.close();

        // print result       
        com.fasterxml.jackson.databind.JsonNode jn = Json.parse(response);
        response = jn.findValue("success") == null ? "" : jn.findValue("success").asText();
        return response;
    }
}
