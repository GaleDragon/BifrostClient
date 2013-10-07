/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

import bifrost.main.Common;

import com.google.api.services.drive.Drive;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author jeremymorgan
 */
public class DriveCommon extends Common {
    private final String USER_AGENT = "Bifrost/1.0";
    
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final List<String> SCOPES = Arrays.asList(new String[] { 
      "https://www.googleapis.com/auth/drive", 
      "https://www.googleapis.com/auth/userinfo.email", 
      "https://www.googleapis.com/auth/userinfo.profile", 
      "https://docs.google.com/feeds/" });

    private static String CLIENT_ID = "344938914370.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "x-8jCYSrh2TCfy-h7iN1rMnH";
    
    public DriveCommon() {
    	setService("drive");
    }
    
    public static GoogleCredential authorize(String credentialjson)
    {
        Gson g = new Gson();
        HashMap<?, ?> hm = g.fromJson(credentialjson, HashMap.class);
        
        GoogleCredential creds = new GoogleCredential.Builder()
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .build();
        
        String accessToken = hm.get("access_token").toString();
        String refreshToken = hm.get("refresh_token").toString();
        creds.setAccessToken(accessToken);
        creds.setRefreshToken(refreshToken);
        
        return creds;
    }
    
    public Drive make(Credential g) {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, g)
                .setApplicationName("Bifrost")
                .build();
        return drive;
    }
    
    public Drive make(String json) {
    	Credential g = authorize(json);
    	return make(g);
    }

    public HttpTransport gethttp() {
        return HTTP_TRANSPORT;
    }
    

}
