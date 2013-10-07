/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bifrost.main;

import bifrost.drive.DriveDownload;
import bifrost.drive.DriveUpload;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author jeremymorgan
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
    
    @Option(name = "-s", aliases = { "--service" }, required = true, usage = "Service type to use, either DROPBOX or DRIVE." )
    private service_type service;
    
    @Option(name = "-d", aliases = { "--direction" }, required = true, usage = "Either UPLOAD or DOWNLOAD." )
    private direction destination;
    
    @Option(name = "-n", aliases = { "--name" }, required = true, usage = "file identifier to download." )
    private String filename;
    
    @Option(name = "-k", aliases = { "--key" }, required = true, usage = "institution key to retrieve credentials." )
    private String key;
    
    @Option(name = "-m", aliases = { "--meta" }, usage = "File containing metadata, in JSON format.")
    private String meta;
    
    @Option(name = "-c", aliases = { "--chunked" }, usage = "declare if a file is to be downloaded in chunks, ie resumable.")
    private boolean chunked;
    
    @Option(name = "-z", aliases = { "--zipped" }, usage = "declare if a file to be downloaded is gzipped and gunzip it.")
    private boolean compressed;
    
    @Option(name = "-f", aliases = { "--force" }, usage = "invalidates the cache to force a download." )
    private boolean force;
    
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
        Bifrost bridge = new Bifrost(args);
        
        
        String[] service_args = {bridge.filename, bridge.key, bridge.meta};
        boolean[] service_bools = {bridge.chunked, bridge.force, bridge.compressed};
        
        if (bridge.service == service_type.DRIVE){
            if (bridge.destination == direction.DOWNLOAD) {
                DriveDownload dd = new DriveDownload(service_args);
                dd.setConfigs(service_bools);
                DriveDownload.Status s = dd.execute();
                System.out.println(s);
            }
            else if (bridge.destination == direction.UPLOAD){
                DriveUpload du = new DriveUpload(service_args);
                du.setConfigs(service_bools);
                DriveUpload.Status s = du.execute();
                System.out.println(s);
            }
        }
        else if ( bridge.service == service_type.DROPBOX ){
            if (bridge.destination == direction.DOWNLOAD) {
                
            }
            else if (bridge.destination == direction.UPLOAD){
                
            }
        }
        
    }
}
