package bifrost.tests.drive;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import bifrost.drive.DriveCommon;
import bifrost.drive.DriveUpload;
import static org.mockito.Mockito.*;

public class DriveUploadTestSuite {
	public DriveUpload du;
	
	
	
	@Rule
	File smallMock = new File("mock-small");

	@Before
	public void setUp() throws Exception {
		du = new DriveUpload();
		du.getModule().setHost("c2g2-bifrost.herokuapp.com");
		du.getModule().setKey("HSPfbOXyA3NDMF2pOTjMI5Wvem1A4iFZjnneIJlesBoeu2TmeYXZKgnQwz22xI9A");
		//du.setParent("root");
		
		FileOutputStream fos = new FileOutputStream( smallMock );
		for (int i=0; i<100; i++)
			fos.write(i);
		fos.close();
	}

	@After
	public void tearDown() throws Exception{
		smallMock.delete();
	}

	@Test
	public void testConstructor()
	{
		DriveCommon dc = (DriveCommon) du.getModule();
		assertNotNull(dc);
	}
	
	@Test
	public void testLargeFile() throws IOException, ClassNotFoundException
	{
		
		
	}
	
	@Test
	public void testSmallFile() throws IOException, ClassNotFoundException
	{
		
		long size = smallMock.length();
		
		du.setInfilename(smallMock.getName());
		du.execute();
		FileInputStream fis = new FileInputStream(new File("test/meta"));
		ObjectInputStream ois = new ObjectInputStream( fis );
		com.google.api.services.drive.model.File inserted = (com.google.api.services.drive.model.File) ois.readObject();
		long receivedSize = inserted.getFileSize();
		
		assertEquals(size, receivedSize);
	}
	
	@Test
	public void testExecute()
	{
		
	}

}
