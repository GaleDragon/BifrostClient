/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrostfx;

import bifrost.drive.DriveDownload;
import bifrost.drive.DriveUpload;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jeremymorgan
 */
public class BifrostFXMLController implements Initializable {
    private enum service_type {
        DROPBOX, 
        DRIVE;
    };
    private enum direction {
        UPLOAD,
        DOWNLOAD;
    };
    
    @FXML
    private Stage s;
    
    @FXML
    private Button bttn;
    @FXML
    private ChoiceBox<?> destination;
    @FXML
    private ChoiceBox<?> service;
    @FXML
    private Label message;
    @FXML
    private TextField key;
    @FXML
    private TextField file;
    @FXML
    private ProgressBar progress;
    @FXML
    private CheckBox chunked;
    @FXML
    private CheckBox force;
    @FXML
    private CheckBox compress;
    
    private String filepath;

    @FXML
    public void submit () {
        if ( validate() ) {
            //Bifrost bridge = new Bifrost();
        
            String[] service_args = { file.getText(), key.getText(), null };
            //boolean[] service_bools = {bridge.chunked, bridge.force, bridge.compressed};
            boolean[] service_bools = {chunked.isSelected(), force.isSelected(), compress.isSelected()};
        
            if ( service.getValue().toString().toUpperCase().equals( "GOOGLE DRIVE" ) ){
                if ( destination.getValue().toString().toUpperCase().equals( direction.DOWNLOAD.toString() ) ) {
                    DriveDownload dd = new DriveDownload(service_args);
                    dd.setHost("c2g2-bifrost.herokuapp.com");
                    dd.setConfigs(service_bools);
                    dd.setListener(new BifrostDownloadListener());
                    
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Save File As...");
                	String userDirectoryString = System.getProperty("user.home");
                	System.out.println( userDirectoryString );
                	java.io.File userDirectory = new java.io.File(userDirectoryString);
                	if(!userDirectory.canRead()) {
                	    userDirectory = new java.io.File("/");
                	}
                	fc.setInitialDirectory(userDirectory);
                	java.io.File f = fc.showSaveDialog(s);
                	
                	if (f != null) {
                		filepath = f.getAbsolutePath();
                		dd.setSavedName(filepath);
                		DriveDownload.Status s = dd.execute();
                        
                        if (s == DriveDownload.Status.DOWNLOAD_SUCCESS) {
                        	
                        	file.setText("");
                        	progress.setProgress(0);
                        }
                	}
                }
                else if (destination.getValue().toString().toUpperCase().equals( direction.UPLOAD.toString() ) ){
                    DriveUpload du = new DriveUpload(service_args);
                    du.setHost("c2g2-bifrost.herokuapp.com");
                    du.setConfigs(service_bools);
                    du.setListener(new BifrostUploadListener());
                    DriveUpload.Status s = du.execute();
                    
                    if (s == DriveUpload.Status.UPLOAD_SUCCESS) {
                    	
                    	file.setText("");
                    	progress.setProgress(0);
                    }
                    
                }
            }
            else if ( service.getValue().toString().toUpperCase().equals( service_type.DROPBOX.toString() ) ){
            	message.setText("Dropbox hasn't been implemented yet :(");
                if (destination.getValue().toString().toUpperCase().equals( direction.DOWNLOAD.toString() )) {

                }
                else if (destination.getValue().toString().toUpperCase().equals( direction.UPLOAD.toString() )){

                }
            }
        }
    }
    
    public void browse() {
    	FileChooser fc = new FileChooser();
    	if (destination.getValue().toString().equalsIgnoreCase("upload")) {
        	fc.setTitle("Select A File to Transfer.");
        	//Set to user directory or go to default if cannot access
        	String userDirectoryString = System.getProperty("user.home");
        	System.out.println( userDirectoryString );
        	java.io.File userDirectory = new java.io.File(userDirectoryString);
        	if(!userDirectory.canRead()) {
        	    userDirectory = new java.io.File("/");
        	}
        	fc.setInitialDirectory(userDirectory);
        	java.io.File f = fc.showOpenDialog(s);
        	
        	if (f != null) {
        		file.setText(f.getAbsolutePath());
        	}
        	
    	} 
    	
    }
    
    public class BifrostDownloadListener implements MediaHttpDownloaderProgressListener {
		@Override
		public void progressChanged(MediaHttpDownloader mhd)
				throws IOException {
			double prog = mhd.getProgress();
			progress.setProgress(prog);
		}
    	
    }
    
    public class BifrostUploadListener implements MediaHttpUploaderProgressListener {
		@Override
		public void progressChanged(MediaHttpUploader mhu) throws IOException {
			double prog = mhu.getProgress();
			progress.setProgress(prog);
		}
    	
    }
    
    public void setup(Stage stage) {
    	service.getSelectionModel().selectFirst();
    	destination.getSelectionModel().selectFirst();
    	
    	s = stage;
    	
    }
    
    public boolean validate() {
        
        boolean validate = true;
        
        validate = validate && ( service.getValue() != null );
        if (!validate) {
            message.setText("A cloud service must be chosen.");
            return false;
        }
        
        validate = validate && ( destination.getValue() != null );
        if (!validate) {
            message.setText("A transfer destination must be selected.");
            return false;
        }
        
        validate = validate && ( !key.getText().equals("") );
        if (!validate) {
            message.setText("A security key must be offered.");
            return false;
        }
        
        validate = validate && ( !file.getText().equals("") );
        if (!validate) {
            message.setText("A file must be selected to transfer.");
            return false;
        }
        
        return validate;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    } 
}
