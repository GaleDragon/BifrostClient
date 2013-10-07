/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author jeremymorgan
 */
public class DriveCommon {
    private final String USER_AGENT = "Bifrost/1.0";
    public static final int IN_CACHE = 1;
    public static final int DOWNLOAD_SUCCESS = 0;
    public static final int DOWNLOAD_FAIL = -1;
    
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final List<String> SCOPES = Arrays.asList(new String[] { 
      "https://www.googleapis.com/auth/drive", 
      "https://www.googleapis.com/auth/userinfo.email", 
      "https://www.googleapis.com/auth/userinfo.profile", 
      "https://docs.google.com/feeds/" });

    private static String CLIENT_ID = "344938914370.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "x-8jCYSrh2TCfy-h7iN1rMnH";
    
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
    
    public String sendGet(String host, String key) throws IOException, HttpException {

        String url = "http://"+host+"/rest/drive/ask?k="+key;
        
        //url = "http://localhost:8000/rest/drive/ask?k="+key;

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);
        
        int status = response.getStatusLine().getStatusCode();
        
        switch ( status / 100 ){
            case 3:
                break;
            case 4:
                ClientException ce = new ClientException();
                ce.setStatus(status);
                throw ce;
            case 5:
                ServerException se = new ServerException();
                se.setStatus(status);
                throw se;
            default:
                break;
        }
        BufferedReader rd = new BufferedReader(
               new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
    
    public Drive make(Credential g) {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, g)
                .setApplicationName("Bifrost")
                .build();
        return drive;
    }

    public HttpTransport gethttp() {
        return HTTP_TRANSPORT;
    }
    
    public class HttpException extends Exception {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int status = 0;
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public int getStatus() {
            return this.status;
        }
    }
    
    public class ClientException extends HttpException {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public String getMessage() {
            return "There was a client exception " + String.valueOf(status);
        }
    }
    
    public class ServerException extends HttpException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public String getMessage() {
            return "There was a server exception " + String.valueOf(status);
        }
    }

}
