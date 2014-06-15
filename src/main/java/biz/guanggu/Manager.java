package biz.guanggu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Li He on 2014/6/15.
 *
 * @author Li He
 */

public class Manager {

    public static final String base = "http://124.93.200.210:92";

    public static void main(String[] args) throws IOException {
        Manager.stopDeploy();
    }

    public static boolean stopDeploy(ErrorLogger logger) throws IOException {

        try{
            URL url = new URL(base+"/manager/html");

            Authenticator.setDefault(new MyAuth());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String cookieHeader = connection.getHeaderField("Set-Cookie");
            String sessionId = cookieHeader.substring(cookieHeader.indexOf("=")+1, cookieHeader.indexOf(";"));
            System.out.println(sessionId);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                System.out.println(line);
            }

            String result = builder.toString();

            Pattern pattern = Pattern.compile("/manager/html/stop(;jsessionid=[A-Z0-9])?\\?path=/&amp;org\\.apache\\.catalina\\.filters\\.CSRF_NONCE=([A-Z0-9]+)");

            Matcher matcher = pattern.matcher(result);

            String csrf = null;
            if(matcher.find()){
                csrf = matcher.group(2);
            }

            URL stopInURL = new URL(base+"/manager/html/stop;jsessionid="+sessionId+"?path=/in&amp;org.apache.catalina.filters.CSRF_NONCE="+csrf);
            URL stopOutURL = new URL(base+"/manager/html/stop;jsessionid="+sessionId+"?path=/out&amp;org.apache.catalina.filters.CSRF_NONCE="+csrf);

            HttpURLConnection stopInConnection = (HttpURLConnection) stopInURL.openConnection();
            stopInConnection.connect();
            stopInConnection.disconnect();

            HttpURLConnection stopOutConnection = (HttpURLConnection) stopOutURL.openConnection();
            stopOutConnection.connect();
            stopOutConnection.disconnect();

        }catch (Exception exception){
            logger.log(exception.getMessage()+"\n"+exception.getStackTrace());
        }

//        HttpHost targetHost = new HttpHost("124.93.200.210", 92, "http");
//
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//
//        credsProvider.setCredentials(
//                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//                new UsernamePasswordCredentials("admin", "116052"));
//
//        // Create AuthCache instance
//        AuthCache authCache = new BasicAuthCache();
//        BasicScheme basicAuth = new BasicScheme();
//        authCache.put(targetHost, basicAuth);
//
//
//        CloseableHttpClient httpClient = HttpClients.createMinimal();
//
//        HttpClientContext context = HttpClientContext.create();
//
//        context.setCredentialsProvider(credsProvider);
//        context.setAuthCache(authCache);
//
//        HttpPost httpPost = new HttpPost("http://124.93.200.210:92/manager/html");
//        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//
//        for (int i = 0; i < 3; i++) {
//            CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
//            HttpEntity entity = response.getEntity();
//
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
//
//            String line;
//
//            while((line=reader.readLine())!=null){
//                System.out.println(line);
//            }
//        }


        return true;
    }

    private static class MyAuth extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("admin", "116052".toCharArray());
        }
    }
}

