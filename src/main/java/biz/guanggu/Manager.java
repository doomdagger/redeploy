package biz.guanggu;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Li He on 2014/6/15.
 *
 * @author Li He
 */

public class Manager {

    public static final String base = "http://124.93.200.210:92";

    public static void main(String[] args) {
        ErrorLogger logger = new ErrorLogger();
        Manager.stop(logger);
    }

    public static String handleResult(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
            //System.out.println(line);
        }
        reader.close();

        String result = builder.toString();

        Pattern pattern = Pattern.compile("/manager/html/stop(;jsessionid=[A-Z0-9]+)?\\?path=/&amp;org\\.apache\\.catalina\\.filters\\.CSRF_NONCE=([A-Z0-9]+)");


        Matcher matcher = pattern.matcher(result);

        String csrf = null;
        if(matcher.find()){
            csrf = matcher.group(2);
        }

        return csrf;
    }

    public static boolean stop(ErrorLogger logger) {

        try{
            URL url = new URL(base+"/manager/html");

            Authenticator.setDefault(new MyAuth());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String cookieHeader = connection.getHeaderField("Set-Cookie");
            String sessionId = cookieHeader.substring(cookieHeader.indexOf("=")+1, cookieHeader.indexOf(";"));

            String csrf = handleResult(connection.getInputStream());

            connection.disconnect();

            //System.out.println(csrf);
            //System.out.println(sessionId);

            URL stopInURL = new URL(base+"/manager/html/stop?path=/in&org.apache.catalina.filters.CSRF_NONCE="+csrf);
            URL stopOutURL = new URL(base+"/manager/html/stop?path=/out&org.apache.catalina.filters.CSRF_NONCE="+csrf);

            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpPost post = new HttpPost(stopInURL.toURI());
            post.setHeader("Authorization","Basic YWRtaW46MTE2MDUy");
            post.setHeader("Cache-Control","max-age=0");
            post.setHeader("Content-Length","0");
            post.setHeader("Cookie","JSESSIONID=D60CF8B2CE1B9CC51E51CE8A04284E3C");

            CloseableHttpResponse response = httpClient.execute(post);
            for(Header header : response.getAllHeaders()){
                System.out.println(header.toString());
            }

            HttpURLConnection stopInConnection = (HttpURLConnection) stopInURL.openConnection();
            stopInConnection.setRequestProperty("Cookie","JSESSIONID="+sessionId);
            stopInConnection.setRequestMethod("POST");
            stopInConnection.setUseCaches(false);
            stopInConnection.setDoOutput(true);
            stopInConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            stopInConnection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            String content = "submit=" + URLEncoder.encode("stop", "utf-8");
            out.writeBytes(content);
            out.flush();
            out.close(); // flush and close

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();


            stopInConnection.disconnect();

            HttpURLConnection stopOutConnection = (HttpURLConnection) stopOutURL.openConnection();
            stopOutConnection.setRequestProperty("Cookie","JSESSIONID="+sessionId);
            stopOutConnection.setRequestMethod("POST");
            stopOutConnection.setUseCaches(false);
            stopOutConnection.setDoOutput(true);
            stopOutConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            stopOutConnection.connect();
            stopOutConnection.disconnect();

            return true;
        }catch (Exception exception){
            logger.log(exception.getMessage()+"\n"+ Arrays.toString(exception.getStackTrace()));
            return false;
        }

    }


    public static boolean start(ErrorLogger logger) {

        try{
            URL url = new URL(base+"/manager/html");

            Authenticator.setDefault(new MyAuth());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String cookieHeader = connection.getHeaderField("Set-Cookie");
            String sessionId = cookieHeader.substring(cookieHeader.indexOf("=")+1, cookieHeader.indexOf(";"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                //System.out.println(line);
            }
            reader.close();
            connection.disconnect();

            String result = builder.toString();

            Pattern pattern = Pattern.compile("/manager/html/stop(;jsessionid=[A-Z0-9])?\\?path=/&amp;org\\.apache\\.catalina\\.filters\\.CSRF_NONCE=([A-Z0-9]+)");

            Matcher matcher = pattern.matcher(result);

            String csrf = null;
            if(matcher.find()){
                csrf = matcher.group(2);
            }

            URL stopInURL = new URL(base+"/manager/html/start;jsessionid="+sessionId+"?path=/in&org.apache.catalina.filters.CSRF_NONCE="+csrf);
            URL stopOutURL = new URL(base+"/manager/html/start;jsessionid="+sessionId+"?path=/out&org.apache.catalina.filters.CSRF_NONCE="+csrf);

            HttpURLConnection startInConnection = (HttpURLConnection) stopInURL.openConnection();
            startInConnection.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
            startInConnection.setRequestMethod("POST");
            startInConnection.setUseCaches(false);
            startInConnection.connect();

            startInConnection.disconnect();

            HttpURLConnection startOutConnection = (HttpURLConnection) stopOutURL.openConnection();
            startOutConnection.setRequestProperty("Cookie","JSESSIONID="+sessionId);
            startOutConnection.setRequestMethod("POST");
            startOutConnection.setUseCaches(false);
            startOutConnection.connect();
            startOutConnection.disconnect();

            return true;
        }catch (Exception exception){
            logger.log(exception.getMessage()+"\n"+ Arrays.toString(exception.getStackTrace()));
            return false;
        }


    }

    private static class MyAuth extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("admin", "116052".toCharArray());
        }
    }
}

