package bifrost.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import bifrost.helpers.BifrostClientFile;

public class Executor {
	public enum Status {
        DOWNLOAD_SUCCESS,
        IN_CACHE,
        DOWNLOAD_ERROR,
        UPLOAD_SUCCESS,
        UPLOAD_ERROR,
        NOT_IMPLEMENTED
    }
	
	private boolean zipped = false;
	private boolean force = false;
	private boolean chunked = false;
	private String parent = null;
	
	private String outfilename = null;
	private String infilename = null;
	private String displayfilename = null;
	
	private boolean rar = false;
	
	public class SerializableGoogleFile implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8906948531870808004L;
		public BifrostClientFile metadata;
		
		public void collectMeta(com.google.api.services.drive.model.File googleFile ) {
			metadata = new BifrostClientFile(googleFile, null);
		}
	}
	
	public File compress(File f) throws FileNotFoundException, IOException {
		String outfile = f.getPath() + ".gz";
    	File zipped = new File(outfile);
    	zipped.createNewFile();
    	BufferedWriter bufferedWriter = new BufferedWriter(
    		new OutputStreamWriter(
    			new GZIPOutputStream(
    				new FileOutputStream(outfile)
    			)
            )
        );

    	BufferedReader bufferedReader = new BufferedReader(new FileReader(f.getPath()));
    	String line = null;
    	while ((line = bufferedReader.readLine()) != null) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
    	bufferedWriter.close();
    	bufferedReader.close();
    	return new File(outfile);
	}
	
	@SuppressWarnings("unused")
	private File decompress(File f ) throws FileNotFoundException, IOException {
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
        
       return new File(f.getName());
    }
	
	public String getOutfilename() {
		return outfilename;
	}

	public void setOutfilename(String outfilename) {
		this.outfilename = outfilename;
	}

	public String getInfilename() {
		return infilename;
	}

	public void setInfilename(String infilename) {
		this.infilename = infilename;
	}

	public String getDisplayfilename() {
		return displayfilename;
	}

	public void setDisplayfilename(String displayfilename) {
		this.displayfilename = displayfilename;
	}

	public boolean isZipped() {
		return zipped;
	}

	public void setZipped(boolean zipped) {
		this.zipped = zipped;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public boolean isChunked() {
		return chunked;
	}

	public void setChunked(boolean chunked) {
		this.chunked = chunked;
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public boolean isRar() {
		return rar;
	}

	public void setRar(boolean rar) {
		this.rar = rar;
	}

	protected Status execute(String save_as, String parent) {
		return Status.NOT_IMPLEMENTED;
	}
	protected Status execute(String save_as){
		return Status.NOT_IMPLEMENTED;
	}
}
