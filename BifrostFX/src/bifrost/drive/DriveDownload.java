/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeremymorgan
 */
public class DriveDownload {
    
    private DriveCommon common = new DriveCommon();
    private String filename;
    private String key;
    private String host;
    public MediaHttpDownloaderProgressListener progressListener;
    private boolean chunked;
    private boolean compressed;
    private boolean force;
    private String saved_name;
    
    public enum Status {
        DOWNLOAD_SUCCESS,
        IN_CACHE,
        DOWNLOAD_ERROR
    }
    
    public void setSavedName(String sn) {
    	saved_name = sn;
    }
    
    public DriveDownload(String... args) {
        this.filename = args[0];
        this.key = args[1];
        //this.key = "qXi8JKOH7q5vguhRLfPUmsLUDERRfqYRVbghCTIaqgefiKfi2g2vULjlScCpBckI";
    }
    
    public void setConfigs(boolean... bools) {
        this.chunked = bools[0];
        this.force = bools[1];
        this.compressed = bools[2];
    }
    
    public void setHost(String h) {
    	host = h;
    }
    
    public ArrayList<Boolean> getConfigs() {
        ArrayList<Boolean> al = new ArrayList<Boolean>();
        al.add(this.chunked);
        al.add(this.force);
        al.add(this.compressed);
        return al;
    }
    
    public void setListener(MediaHttpDownloaderProgressListener listener) {
    	progressListener = listener;
    }
    
    public Status execute(){
        try {
            String fetched = common.sendGet(host, key );

            Credential c = DriveCommon.authorize(fetched);
            
            Drive drive = common.make(c);

            
            String[] array = filename.split("\\/");
            String id = null;
            if (array.length > 1) {
                id = array[5];
            } else if (array.length == 1) {
                id = array[1];
            }
              
            Drive.Files.Get request = drive.files().get(id);
            File f = request.execute();
            
            java.io.File local = new java.io.File(saved_name);
            String drive_md5 = f.getMd5Checksum();
            
            if ( !force ) {
                if (local.exists()) {
                FileInputStream fis = new FileInputStream(local);
                String local_md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                if (local_md5.equals( drive_md5 ))
                    return Status.IN_CACHE;
                }
            }
            
            MediaHttpDownloader mhd = new MediaHttpDownloader(common.gethttp(), c);
            mhd.setProgressListener(progressListener);
            FileOutputStream bos = new FileOutputStream(saved_name);
            
            if ( !chunked ) {
                mhd.setDirectDownloadEnabled(true);
            }
            
            GenericUrl gu = new GenericUrl(f.getDownloadUrl());
            
            mhd.download(gu, bos);
            
            //System.out.println(name);
            return Status.DOWNLOAD_SUCCESS;
                
        } catch (IOException | DriveCommon.HttpException e) {
            e.printStackTrace();
        }
        return Status.DOWNLOAD_ERROR;
        
    }
    
}
