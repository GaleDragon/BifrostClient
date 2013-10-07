/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import bifrost.main.Common.HttpException;
import bifrost.main.Common;
import bifrost.main.Executor;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxStreamWriter;
import com.dropbox.core.DbxWriteMode;

/**
 *
 * @author jeremymorgan
 */
public class DropboxUpload extends Executor {
	public DropboxCommon dbc = new DropboxCommon();
	
	public Common getModule() {
		return this.dbc;
	}
    
    public java.io.File compress(java.io.File f) throws FileNotFoundException, IOException {
    	String outfile = f.getPath() + ".gz";
    	BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(outfile))
                ));
    	BufferedReader bufferedReader = new BufferedReader(new FileReader(f.getPath()));
    	String line = null;
    	while ((line = bufferedReader.readLine()) != null) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
    	bufferedWriter.close();
    	bufferedReader.close();
    	return new java.io.File(outfile);
    }
	
    public Status execute(){
    	try {
			String fetched = dbc.sendGet();
			DbxClient client = dbc.make(fetched);
			File f = new File(getInfilename());
			
			/*
			 * 	File inputFile = new File("working-draft.txt");
				FileInputStream inputStream = new FileInputStream(inputFile);
				try {
				    DbxEntry.File uploadedFile = client.uploadFile("/magnum-opus.txt",
				        DbxWriteMode.add(), inputFile.length(), inputStream);
				    System.out.println("Uploaded: " + uploadedFile.toString());
				} finally {
				    inputStream.close();
				}
			 */
			
			if (isZipped()) {
				f = compress(f);
			}
			
			DbxWriteMode mode = null;
			if (isForce()) {
				mode = DbxWriteMode.force();
			} else {
				mode = DbxWriteMode.add();
			}
			String[] parentRoot = getParent().split("[0-9a-z]{15}/");
			
			File joined = new File("/"+parentRoot[1], getOutfilename() );
			FileInputStream inputStream = new FileInputStream(f);
			DbxEntry.File uploadedFile = null;
			if (isChunked()) {
				DbxStreamWriter<IOException> dsw = new DbxStreamWriter.InputStreamCopier(inputStream);
				try {
					uploadedFile = client.uploadFileChunked(joined.getPath(), mode, f.length(), dsw);
				} catch (DbxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					inputStream.close();
				}
				
			} else {
				
				try{
					uploadedFile = client.uploadFile(joined.getPath(),
					        mode, f.length(), inputStream);
				} catch (DbxException e) {
					e.printStackTrace();
				} finally {
					inputStream.close();
				}
			}
			System.out.println(uploadedFile.name);
			return Status.UPLOAD_SUCCESS;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	return Status.UPLOAD_ERROR;
    }
}
