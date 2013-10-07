package bifrost.tests.drive;



import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import bifrost.drive.DriveCommon;
import bifrost.main.Common.HttpException;

public class DriveCommonTestSuite {
	public DriveCommon common;

	@Before
	public void setUp() throws Exception
	{
		common = new DriveCommon();
		common.setKey("HSPfbOXyA3NDMF2pOTjMI5Wvem1A4iFZjnneIJlesBoeu2TmeYXZKgnQwz22xI9A");
		common.setHost("c2g2-bifrost.herokuapp.com");
	}

	@After
	public void tearDown() throws Exception
	{
		
	}
	
	@Test
	public void testConstructor()
	{
		String service = common.getService();
		assertEquals(service, "drive");
	}

	@Test
	public void testGetHttp()
	{
		HttpTransport http = common.gethttp();
		assertNotNull(http);
	}
	
	@Test
	public void testAuthorize() throws IOException, HttpException
	{
		String creds = common.sendGet();
		Credential c = DriveCommon.authorize(creds);
		assertNotNull(c);
	}

	@Test
	public void testMakeString() throws IOException, HttpException
	{
		String c = common.sendGet();
		Drive d = common.make(c);
		FileList fl = d.files().list().execute();
		int s = fl.getItems().size();
		assertNotSame(s, null);
	}
	
	@Test
	public void testMakeCredential() throws IOException, HttpException
	{
		String inter = common.sendGet();
		Credential cred = DriveCommon.authorize(inter);
		Drive d = common.make(cred);
		FileList fl = d.files().list().execute();
		int s = fl.getItems().size();
		assertNotSame(s, null);
	}
}
