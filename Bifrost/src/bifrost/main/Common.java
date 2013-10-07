package bifrost.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class Common {
	private final String USER_AGENT = "Bifrost/1.0";
	private String service = null;
	private String host = null;
	private String key = null;
	
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String sendGet() throws IOException, HttpException {

        String url = "http://"+getHost()+"/rest/"+getService()+"/ask?k="+getKey();
        // System.out.println(url);
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
	
	public abstract Object make(String access_token);
	
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

    public String toString(){
    	return USER_AGENT+" "+service;
    }
}
