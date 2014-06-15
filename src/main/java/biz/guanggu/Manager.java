package biz.guanggu;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Created by Li He on 2014/6/15.
 * @author Li He
 */

public class Manager {

    public static boolean stopDeploy(){

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(
                new AuthScope("219.216.192.11", AuthScope.ANY_PORT, AuthScope.ANY_REALM, "basic"),
                new UsernamePasswordCredentials("admin", "116052"));


        return true;
    }
}
