/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.drive;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import bifrost.main.Common;
import bifrost.main.Executor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.common.io.ByteStreams;

/**
 *
 * @author jeremymorgan
 */
public class DriveUpload extends Executor {
	private DriveCommon common = new DriveCommon();
    
    public Common getModule() {
		return this.common;
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
    
    /*
    private java.io.File compress(java.io.File f) throws FileNotFoundException, IOException {
    	String outfile = f.getPath() + ".gz";
    	java.io.File zipped = new java.io.File(outfile);
    	zipped.createNewFile();
    	BufferedWriter bufferedWriter = new BufferedWriter(
    		new OutputStreamWriter(
    			new GZIPOutputStream(
    				new FileOutputStream(outfile)
    			)
            )
        );
    	System.out.println("FILE: "+f.getPath());
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
    */
    
    public DriveUpload(){
    	common = new DriveCommon();
    }
    
    private String getParentId() throws MalformedURLException {
    	if (getParent() != null && getParent().length() > 0){
	    	URL parenturl = new URL(getParent());
			String query = parenturl.getQuery();
			String[] pairs = query.split("&");
			// The form is assumed id=SOMEVALUE&foo=bar&.....
			String parentID = pairs[0].split("=")[1];
			return parentID;
    	} else {
    		return "root";
    	}
    }
    
    private boolean uploadChunks(Drive d, java.io.File f) throws IOException{
    	long fileLength = f.length();
    	final int PACKAGE =  Integer.MAX_VALUE;
    	
    	System.out.print(fileLength);
    	System.out.print("<");
    	System.out.println(PACKAGE);
    	
    	if (fileLength < PACKAGE || !isRar() ){
    		return false;
    	}
    	
    	byte[] totalBytes = ByteStreams.toByteArray( new FileInputStream(f) );
    	
    	
    	File container = new File();
    	container.setMimeType("application/vnd.google-apps.folder");
    	String folderName;
    	if (getOutfilename() != null) {
    		folderName = getOutfilename().replace(".", "-");
    	} else {
    		folderName = getInfilename().replace(".", "-");
    	}
    	container.setTitle( folderName );
    	container.setParents(Arrays.asList(new ParentReference().setId(getParentId())));
    	File parent = d.files().insert( container ).execute();
    	
    	ByteArrayInputStream all = new ByteArrayInputStream( totalBytes );
    	ByteArrayInputStream part;
    	
    	String fileBaseName = null;
    	if (getOutfilename() != null) {
    		fileBaseName = getOutfilename();
    	} else {
    		fileBaseName = getInfilename();
    	}
    	
    	double totalRead = 0;
    	int c = 0;
    	byte[] buffer = new byte[PACKAGE];
    	int counter = 1;
    	while ((c = all.read( buffer , 0, PACKAGE)) != -1) {
    		
    		
    		File driveFile = new File();
    		driveFile.setParents( Arrays.asList(new ParentReference().setId( parent.getId() )) );
    		driveFile.setTitle( fileBaseName + "." + String.valueOf( counter ));
            InputStreamContent mediaContent;
            
            System.out.print("Read: ");
            System.out.print(totalRead / fileLength);
            System.out.println("%");
            
            part = new ByteArrayInputStream( buffer );
            BufferedInputStream bis = new BufferedInputStream( part );
            mediaContent = new InputStreamContent(null, bis);
            
            mediaContent.setLength(c);
    		
    		Drive.Files.Insert request = d.files().insert(driveFile, mediaContent);
            request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
            request.execute();
            
            counter++;
    	}
    	
    	return true;
    }
    
    public Status execute() {
        String fetched;
        
        try {
            fetched = common.sendGet( );
        } catch (IOException | DriveCommon.HttpException io) {
        	io.printStackTrace();
            System.err.println("IOException: "+io.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        Credential c = DriveCommon.authorize(fetched);
        Drive drive = common.make(c);
        
        java.io.File mediaFile = null;
        //System.out.println(filename);
        if (isZipped()) {
        	try {
				mediaFile = compress(new java.io.File(getInfilename()));
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	mediaFile = new java.io.File(getInfilename());
        }
        
        boolean isBigEnough = false;
        try {
			isBigEnough = uploadChunks(drive, mediaFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        if (isBigEnough){
        	return Status.UPLOAD_SUCCESS;
        }
        
        InputStreamContent mediaContent;
        try {
            FileInputStream fis = new FileInputStream(mediaFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            mediaContent = new InputStreamContent(null, bis);
        } catch (FileNotFoundException f) {
        	f.printStackTrace();
            System.err.println("FileNotFoundException: "+f.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        
        
        mediaContent.setLength(mediaFile.length());
        
        File f = new File();
        
        f.setTitle(getOutfilename());
        FileInputStream is = null;
        try {
          is = new FileInputStream(mediaFile);

          ContentHandler contenthandler = new BodyContentHandler(-1);
          Metadata metadata = new Metadata();
          metadata.set(Metadata.RESOURCE_NAME_KEY, mediaFile.getName());
          Parser parser = new AutoDetectParser();
          
          ParseContext context = new ParseContext();
          parser.parse(is, contenthandler, metadata, context);
          f.setMimeType( metadata.get(Metadata.CONTENT_TYPE).toString());
        }
        catch (Exception e) {
          f.setMimeType("text/plain");
          e.printStackTrace();
        }
        finally {
            if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        
        if (getParent() != null && getParent().length() > 0) {
        	String parentID = null;
        	try {
				parentID = getParentId();
				f.setParents(Arrays.asList(new ParentReference().setId(parentID)));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
          }


        try {
        	if (getOutfilename() != null) {
        		f.setTitle(getOutfilename());
            } else {
            	f.setTitle(mediaFile.getName());
            }
            Drive.Files.Insert request = drive.files().insert(f, mediaContent);
            request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
            File inserted = request.execute();
            
            
        } catch (IOException io) {
        	io.printStackTrace();
            System.err.println("IOException: "+io.getLocalizedMessage());
            return Status.UPLOAD_ERROR;
        }
        
        
        return Status.UPLOAD_SUCCESS;
    }
}
