package biz.guanggu;

import javafx.util.Pair;
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

        execute("in", "start", logger);
    }

    public static boolean execute(String path,String action,ErrorLogger logger){
        try{
            Pair<String, String> key = login();

            String sessionId = key.getKey();
            String csrf = key.getValue();

            URL url = new URL(base+"/manager/html/"+action+"?path=/"+path+"&org.apache.catalina.filters.CSRF_NONCE="+csrf);

            connect(url,sessionId);

            return true;
        }catch (Exception exception){
            logger.log(exception.getMessage()+"\n"+ Arrays.toString(exception.getStackTrace()));
            exception.printStackTrace();
            return false;
        }
    }


    public static String handleResult(InputStream inputStream, boolean display) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
            if(display)
                System.out.println(line);
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

    public static void connect(URL url, String sessionId) throws IOException, URISyntaxException {
        CloseableHttpClient httpClient = HttpClients.createMinimal();

        HttpPost post = new HttpPost(url.toURI());
        post.setHeader("Authorization","Basic YWRtaW46MTE2MDUy");
        post.setHeader("Cache-Control","max-age=0");
        post.setHeader("Cookie","JSESSIONID="+sessionId);

        CloseableHttpResponse response = httpClient.execute(post);
//        for(Header header : response.getAllHeaders()){
//            System.out.println(header.toString());
//        }

//        handleResult(response.getEntity().getContent(), false);

        response.close();
        httpClient.close();

    }

    /**
     * login get session and csrf
     * @return pair
     * @throws IOException
     */
    public static Pair<String, String> login() throws IOException {
        URL url = new URL(base+"/manager/html");

        Authenticator.setDefault(new MyAuth());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String cookieHeader = connection.getHeaderField("Set-Cookie");

        String sessionId = cookieHeader.substring(cookieHeader.indexOf("=")+1, cookieHeader.indexOf(";"));
        String csrf = handleResult(connection.getInputStream(), false);

        connection.disconnect();

       return new Pair<String, String>(sessionId,csrf);
    }



    private static class MyAuth extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("admin", "116052".toCharArray());
        }
    }
}

