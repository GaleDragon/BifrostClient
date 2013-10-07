/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author jeremymorgan
 */
public class DriveUpload {
    private DriveCommon common = new DriveCommon();
    private String filename;
    private String key;
    private String meta;
    private String host;
    private MediaHttpUploaderProgressListener progressListener;
    private boolean chunked = false;
    @SuppressWarnings("unused")
	private boolean compressed;
    @SuppressWarnings("unused")
	private boolean forced;
    
    public enum Status {
        UPLOAD_SUCCESS,
        UPLOAD_ERROR
    }
    
    class CustomProgressListener implements MediaHttpUploaderProgressListener {
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    //System.out.println("Initiation has started!");
                    break;
                case INITIATION_COMPLETE:
                    //System.out.println("Initiation is complete!");
                    break;
                case MEDIA_IN_PROGRESS:
                    //System.out.println(uploader.getProgress());
                    break;
                case MEDIA_COMPLETE:
                    //System.out.println("Upload is complete!");
			case NOT_STARTED:
				break;
			default:
				break;
            }
        }
    }
    
    public DriveUpload(String... args) {
        this.filename = args[0];
        this.key = args[1];
        this.meta = args[2];
        //this.key = "5habU82UwLBgnvd7f7sR9Jz48eiZ6Ghx2bJ2MUwEfJQ5nurhh0bBeN1hYVzCgir4";
    }
    
    public void setConfigs(boolean... bools) {
    	this.chunked = bools[0];
        this.forced = bools[1];
        this.compressed = bools[2];
    }
    
    private File applyMeta(File f, String meta){
    	if (meta != null) {
    		Gson g = new Gson();
            HashMap<?, ?> hm = g.fromJson(meta, HashMap.class);
            f.setMimeType(hm.get("mime").toString());
    	}
    	else {
    		f.setMimeType("text/plain");
    	}
        
        return f;
    }
    
    public void setHost(String h) {
    	host = h;
    }
    
    public Status execute() {
        String fetched;
        try {
            fetched = common.sendGet(host, key );
        } catch (IOException | DriveCommon.HttpException io) {
            System.err.println("IOException: "+io.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        Credential c = DriveCommon.authorize(fetched);
        Drive drive = common.make(c);
        //Gson g = new Gson();
        java.io.File mediaFile = new java.io.File(filename);
        
        
        InputStreamContent mediaContent;
        try {
            FileInputStream fis = new FileInputStream(mediaFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            mediaContent = new InputStreamContent(meta, bis);
        } catch (FileNotFoundException f) {
            System.err.println("FileNotFoundException: "+f.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        mediaContent.setLength(mediaFile.length());
        
        File f = new File();
        f = applyMeta(f, this.meta);
        String[] comps = filename.split("\\/");
        String basename = comps[comps.length-1];
        f.setTitle(basename);

        try {
            Drive.Files.Insert request = drive.files().insert(f, mediaContent);
            request.getMediaHttpUploader().setProgressListener(progressListener);
            if (chunked){
            	request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
            }
            request.execute();
        } catch (IOException io) {
            System.err.println("IOException: "+io.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        
        return Status.UPLOAD_SUCCESS;
    }

	public void setListener(MediaHttpUploaderProgressListener uploadListener) {
		progressListener = uploadListener;
		
	}
}
