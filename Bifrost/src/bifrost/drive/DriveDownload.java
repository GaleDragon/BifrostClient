/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

import bifrost.main.Common;
import bifrost.main.Executor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 *
 * @author jeremymorgan
 */
public class DriveDownload extends Executor{
    
    private DriveCommon common = new DriveCommon();
    
    public Common getModule() {
		return this.common;
	}
    
    /*private java.io.File decompress(java.io.File f ) throws FileNotFoundException, IOException {
    	byte[] buffer = new byte[1024];
    	//long len = f.length();
    	 
        try{
    
       	 GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(f));
       	 FileOutputStream out = new FileOutputStream(f.getName());
    
           int len;
           while ((len = gzis.read(buffer)) > 0) {
           		out.write(buffer, 0, len);
           }
    
           gzis.close();
       	out.close();
    
       }catch(IOException ex){
          ex.printStackTrace();   
       }
        
       return new java.io.File(f.getName());
    }*/
    
    public Status execute(){
        try {
            String fetched = common.sendGet( );
            
            Credential c = DriveCommon.authorize(fetched);
            
            Drive drive = common.make(c);
            
            
            String[] array = getInfilename().split("\\/");
            System.out.println( getInfilename() );
            String id = null;
            if (array.length > 1) {
                id = array[5];
            } else if (array.length == 1) {
                id = array[0];
            }
              
            Drive.Files.Get request = drive.files().get(id);
            File f = request.execute();
            
            String drive_title = f.getTitle();
            
            String final_out = null;
            System.out.println( getOutfilename() );
            if (getOutfilename() != null) {
            	final_out = getOutfilename();
            } 
            else {
            	if (f.getFileExtension().equals("gz")) {
            		final_out = drive_title.substring(0, f.getTitle().lastIndexOf('.'));
            	} else {
            		final_out = drive_title;
            	}
            }
            

            java.io.File local = new java.io.File( f.getTitle() );
            String drive_md5 = f.getMd5Checksum();
            
            if ( !isForce() ) {
                if (local.exists()) {
                FileInputStream fis = new FileInputStream(local);
                String local_md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                if (local_md5.equals( drive_md5 ))
                    return Status.IN_CACHE;
                }
            }
            
            MediaHttpDownloader mhd = new MediaHttpDownloader(common.gethttp(), c);
            FileOutputStream bos = new FileOutputStream(final_out);
            
            if ( !isChunked() ) {
                mhd.setDirectDownloadEnabled(true);
            }
            
            GenericUrl gu = new GenericUrl(f.getDownloadUrl());
            mhd.download(gu, bos);
            bos.close();
            
            /*
            System.out.println(final_out);
            System.out.println(drive_title);
            if ( ( this.compressed || f.getFileExtension().equals("gz") ) ) {
            	java.io.File decompressed = decompress( new java.io.File(drive_title) );
            	decompressed.renameTo( new java.io.File(final_out) );
            }
            */
            System.out.println("GZIP FILE:"+drive_title);            
            //System.out.println(name);
            return Status.DOWNLOAD_SUCCESS;
                
        } catch (IOException | DriveCommon.HttpException e) {
            System.err.println(e.getMessage());
        }
        return Status.DOWNLOAD_ERROR;
        
    }
    
}
