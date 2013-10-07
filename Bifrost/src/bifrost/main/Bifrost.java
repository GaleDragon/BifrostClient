/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.main;

import bifrost.drive.DriveDownload;
import bifrost.drive.DriveUpload;
import bifrost.dropbox.DropboxDownload;
import bifrost.dropbox.DropboxUpload;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Jeremy Morgan
 */
public class Bifrost {
    private enum service_type {
        DROPBOX, 
        DRIVE;
    };
    private enum direction {
        UPLOAD,
        DOWNLOAD;
    };
    
    static boolean DEBUG = false;
    
    @Option(name = "-s", aliases = { "--service" }, required = true, usage = "Service type to use, either DROPBOX or DRIVE." )
    private service_type service;
    
    @Option(name = "-d", aliases = { "--direction" }, required = true, usage = "Either UPLOAD or DOWNLOAD." )
    private direction destination;
    
    @Option(name = "-n", aliases = { "--name" }, required = true, usage = "file identifier to download." )
    private String filename;
    
    @Option(name = "-k", aliases = { "--key" }, required = true, usage = "institution key to retrieve credentials." )
    private String key;
    
    @Option(name = "-c", aliases = { "--chunked" }, usage = "declare if a file is to be downloaded in chunks, ie resumable.")
    private boolean chunked;
    
    @Option(name = "-z", aliases = { "--zipped" }, usage = "declare if a file to be downloaded is gzipped and gunzip it.")
    private boolean compressed;
    
    @Option(name = "-f", aliases = { "--force" }, usage = "invalidates the cache to force a download." )
    private boolean force;
    
    @Option(name = "-o", aliases = { "--save-as" }, usage = "declares the new transfered file's name." )
    private String save_as;
    
    @Option(name = "-p", aliases = {"--parent"}, usage = "Uploads to a specified Google folder.")
    private String parent;
    
    public Bifrost(String... args){
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(200);
        try {
            parser.parseArgument(args);
 
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
        
    }
    
    public Bifrost() {
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	Bifrost bridge = null;
    	bridge = new Bifrost(args);

        if (bridge.service == service_type.DRIVE){
            if (bridge.destination == direction.DOWNLOAD) {
                DriveDownload dd = new DriveDownload();
                dd.setInfilename(bridge.filename);
                dd.setOutfilename(bridge.save_as);
                dd.getModule().setHost("c2g2-bifrost.herokuapp.com");
                dd.getModule().setKey(bridge.key);
                dd.setChunked(bridge.chunked);
                dd.setForce(bridge.force);
                dd.setZipped(bridge.compressed);
                DriveDownload.Status s = dd.execute();
                System.out.println(s);
                if (s == DriveDownload.Status.DOWNLOAD_SUCCESS) {
                	System.exit(0);
                }
                else{
                	System.exit(1);
                }

            }
            else if (bridge.destination == direction.UPLOAD){
                DriveUpload du = new DriveUpload();
                du.setInfilename(bridge.filename);
                du.setOutfilename(bridge.save_as);
                du.setParent(bridge.parent);
                du.setChunked(bridge.chunked);
                du.setForce(bridge.force);
                du.setZipped(bridge.compressed);
                du.getModule().setHost("c2g2-bifrost.herokuapp.com");
                du.getModule().setKey(bridge.key);
                DriveUpload.Status s = du.execute();
                System.out.println(s);
                if (s == DriveUpload.Status.UPLOAD_SUCCESS) {
                	System.exit(0);
                }
                else{
                	System.exit(1);
                }
            }
        }
        else if ( bridge.service == service_type.DROPBOX ){
            if (bridge.destination == direction.DOWNLOAD) {
                DropboxDownload dd = new DropboxDownload();
                dd.setInfilename(bridge.filename);
                dd.setOutfilename(bridge.save_as);
                dd.getModule().setHost("c2g2-bifrost.herokuapp.com");
                dd.getModule().setKey(bridge.key);
                dd.setChunked(bridge.chunked);
                dd.setForce(bridge.force);
                dd.setZipped(bridge.compressed);
                DropboxDownload.Status s = dd.execute();
                System.out.println(s);
                if (s == DropboxDownload.Status.DOWNLOAD_SUCCESS) {
                	System.exit(0);
                }
                else{
                	System.exit(1);
                }
            }
            else if (bridge.destination == direction.UPLOAD){
            	DropboxUpload du = new DropboxUpload();
            	du.setInfilename(bridge.filename);
            	du.setOutfilename(bridge.save_as);
            	du.setParent(bridge.parent);
                du.setChunked(bridge.chunked);
                du.setForce(bridge.force);
                du.setZipped(bridge.compressed);
                du.getModule().setHost("c2g2-bifrost.herokuapp.com");
                du.getModule().setKey(bridge.key);
                DropboxUpload.Status s = du.execute(); 
                System.out.println(s);
                if (s == DropboxUpload.Status.UPLOAD_SUCCESS) {
                	System.exit(0);
                }
                else{
                	System.exit(1);
                }
            }
        }
        
        
    }
}
