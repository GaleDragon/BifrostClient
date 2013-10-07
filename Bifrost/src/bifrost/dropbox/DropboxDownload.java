/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import com.dropbox.core.DbxClient;
//import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;

import bifrost.main.Common.HttpException;
import bifrost.main.Common;
import bifrost.main.Executor;

/**
 *
 * @author jeremymorgan
 */
public class DropboxDownload extends Executor{
	public DropboxCommon dbc = new DropboxCommon();
	
	public Common getModule() {
		return this.dbc;
	}
	
    public Status execute(){
    	try {
			String fetched = dbc.sendGet();
			DbxClient client = dbc.make(fetched);
			URL url = new URL(getInfilename());
			String path = url.getPath();
			
			// Get the given URL into a form suitable to use with the API.
			// I wish there was a regex to identify the path to make this more robust...
			String[] filepath = path.split("\\/");
			String dbpath = "";
			for (int i = 0; i<filepath.length; i++){
				if (i > 3) {
					dbpath += "/";
					dbpath += filepath[i];
				}
			}
			System.out.println(dbpath);
			
			if (!isForce()) {
				// Check cache
				boolean in_cache = false;
				if (in_cache) {
					return Status.IN_CACHE;
				}
				// ???
				
			}
			
			
			FileOutputStream out = new FileOutputStream( filepath[filepath.length-1] );
			if (isChunked()) {
				DbxClient.Downloader downloader = null;
				try {
					downloader = client.startGetFile(dbpath, null);
					byte[] buffer = new byte[1024];
					@SuppressWarnings("unused")
					int read;
					while ((read = downloader.body.read(buffer)) != 0){
						out.write( buffer );
					}
					
				} catch (DbxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					out.close();
					downloader.close();
				}
				
			} else {
				
				try {
					client.getFile(dbpath, null, out);
				} catch (DbxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					out.close();
				}
			}
			
			if (isZipped()) {
				System.out.println("GZIP FILE:"+filepath[filepath.length-1]);
				return Status.DOWNLOAD_SUCCESS;
			}
			
			File f = new File( filepath[filepath.length-1] );
			
			File renamed = new File( getOutfilename() );
			f.renameTo( renamed );
			
			return Status.DOWNLOAD_SUCCESS;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	return Status.DOWNLOAD_ERROR;
    }
}
