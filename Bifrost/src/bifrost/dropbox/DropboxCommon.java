package bifrost.dropbox;

import bifrost.main.Common;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

public class DropboxCommon extends Common {
	private final String USER_AGENT = "Bifrost/1.0";
	
	public DropboxCommon() {
		setService("dropbox");
	}

    public DbxClient make(String access_token) {
        DbxRequestConfig dbxconfig = new DbxRequestConfig(USER_AGENT, null);
        DbxClient client = new DbxClient(dbxconfig, access_token);
        return client;
    }
    
    
}
