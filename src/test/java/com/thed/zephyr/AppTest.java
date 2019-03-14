package com.thed.zephyr;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.net.*;
import java.io.*;
import com.crossbrowsertesting.CapsBuilder;
import com.crossbrowsertesting.AutomatedTest;
import com.crossbrowsertesting.Video;
import com.crossbrowsertesting.Snapshot;
import com.crossbrowsertesting.Builders;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Feature;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{  
    //***VARIABLES***
    //Cross Browser Testing credientials found on the manage page. 
    //https://app.crossbrowsertesting.com/account
    public  String cbtUsername = "";
    public  String cbtAuthkey = "";

    //Zephyr credentials found under the apps tab on JIRA
    public String accessKey = "";
    public String secretKey = "";

    //Jira credentials
    public String jiraUsername = "";
    public String jiraPassword = "";

    //Version Id has to be set to use zephyr. After being set up you can find it here : 
    //https://docs.atlassian.com/software/jira/docs/api/REST/7.6.1/?_ga=2.239951107.141879896.1552503325-1964191470.1549919588#api/2/project-getProject
    public String versionId = "";      


    //Jira Variables
    //This is the issue key an issue on Jira(this may not be generated until after your first test as Zephyr will create a new card)
    public String issueTag = ""; //Ex: ZEP-3
    public String zephyrCycleName = ""; //Ex: Our Zephyr Cycle

    //Info that will be sent to the Jira following a passed CBT test
    public String comment = "Attachment-through-ZAPI-CLoud";

    //Likely wont need to change these.
    public String entityName = "execution"; 
    public String API_ZEPHYR = "{SERVER}/public/rest/api/1.0/";
    public String zephyrBaseUrl = "https://prod-api.zephyr4jiracloud.com/connect";
    public int expirationInSec = 360;

    public ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, jiraUsername).build();
    public JwtGenerator jwtGenerator = client.getJwtGenerator();
    public AutomatedTest myTest;

    public void testApp() throws Exception
    {
      //START CBT 
      try{
        System.out.println("Starting Selenium Test Through CBT");
        Builders builder = new Builders();
        builder.login(cbtUsername, cbtAuthkey);

        //Build the caps for our driver
        CapsBuilder capsBuilder = new CapsBuilder(builder.username, builder.authkey);
        capsBuilder.withPlatform("Mac OSX 10.14").withBuild("1.0").withBrowser("Safari12").build();

        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://" + builder.username + ":" + builder.authkey + "@hub.crossbrowsertesting.com:80/wd/hub"),capsBuilder.caps);
        
        //initialize an AutomatedTest object with our selnium session id
        myTest = new AutomatedTest(driver.getSessionId().toString());

        //start up a video
        Video video = myTest.startRecordingVideo();

        driver.get("http://google.com");

        //take a snapshot of where we currently are
        Snapshot googleSnap = myTest.takeSnapshot();
        googleSnap.setDescription("google.com");

        driver.get("http://crossbrowsertesting.com");

        //take a snapshot and set description all at once
        myTest.takeSnapshot("cbt.com");

        //stop our video and set a descripiton
        video.setDescription("google and cbt video");
        video.stopRecording();

        //set a score for our test and end it
        myTest.setScore("pass");
        myTest.stop();

        //save our video
        video.saveLocally("test/myvideo.mp4");
        googleSnap.saveLocally("test/google.png");
        //give time for directory to update
        Thread.sleep(5000);
      }
      catch(Exception e){
        myTest.stop();
        fail("Exception caught.");
      }
      //END CBT TEST

      //GATHER JIRA INFO
      String values[] = getJiraProjectId();
      String issueId = values[0];
      String projectId = values[1];

      //GATHER ZEPHYR INFO
      String cycleId = getZephyrCycleId(projectId);
      String entityId = getZephyrEntityId(projectId,issueId);

      //POST ARTIFACTS FROM CBT TO JIRA THROUGH ZEPHYR
      //These would be the artifacts saved locally from above. 
      String filePath = "/"; //Ex: "/Users/you/.jenkins/workspace/MyProject/mypicture.png"
      String videofilePath = "/"; 
      attachArtifactZephyr(issueId, cycleId, entityId, projectId, filePath);
      attachArtifactZephyr(issueId, cycleId, entityId, projectId, videofilePath);
      
    }
    public String[] getJiraProjectId() throws Exception{
      HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(jiraUsername, jiraPassword);
      Response response = ClientBuilder.newClient()
        .target("https://cbt-dev.atlassian.net/rest/api/2/issue/" + issueTag)
        .register(feature)
        .request()
        //.header("Content-Type", "text/plain")
        .get();

      JSONObject issueObject = new JSONObject(response.readEntity(String.class));
      String issueId = issueObject.getString("id");
      JSONObject fieldObject = issueObject.getJSONObject("fields");
      JSONObject projectObject = fieldObject.getJSONObject("project");
      String projectId = projectObject.getString("id");
      String ar[] = new String[2];
      ar[0] = issueId;
      ar[1] = projectId;
      return ar;
    }
    public String getZephyrEntityId(String projectId, String issueId) throws Exception{
      String attachmentUri = API_ZEPHYR.replace("{SERVER}", zephyrBaseUrl) +"executions?" + "issueId=" + issueId + "&offset=0&size=50" + "&projectId=" + projectId;
      URI uri = new URI(attachmentUri);

      String jwt = jwtGenerator.generateJWT("GET", uri, expirationInSec);

      Response response = ClientBuilder.newClient()
        .target(attachmentUri)
        //.register(feature)
        .request()
        .header("Authorization", jwt)
        .header("zapiAccessKey", accessKey)
        .header("Content-Type", "text/plain")
        .get();

      JSONObject executionObject = new JSONObject(response.readEntity(String.class));
      JSONArray executionArray = executionObject.getJSONArray("executions");

      long mostRecent = 0L;
      String entityId = null;

      for (int i=0; i < executionArray.length(); i++){
        JSONObject executionSingleObject = executionArray.getJSONObject(i);
        JSONObject execution = executionSingleObject.getJSONObject("execution");
        String name = execution.getString("cycleName");
        if (name.equals(zephyrCycleName)){
          long mostRecentCheck = execution.getLong("creationDate");
          if (mostRecentCheck > mostRecent){
            entityId = execution.getString("id");
          }
        }
      }
      return entityId;
    }
    public String getZephyrCycleId(String projectId) throws Exception{
      String attachmentUri = API_ZEPHYR.replace("{SERVER}", zephyrBaseUrl) +"cycles/search?" + "versionId=" + versionId + "&projectId=" + projectId;
      URI uri = new URI(attachmentUri);

      String jwt = jwtGenerator.generateJWT("GET", uri, expirationInSec);

      Response response = ClientBuilder.newClient()
        .target(attachmentUri)
        //.register(feature)
        .request()
        .header("Authorization", jwt)
        .header("zapiAccessKey", accessKey)
        .header("Content-Type", "text/plain")
        .get();

      JSONArray jsonArr = new JSONArray(response.readEntity(String.class));
      JSONObject jsonObj = null;
      for (int i=0; i < jsonArr.length(); i++){
        jsonObj = jsonArr.getJSONObject(i);
        String name = jsonObj.getString("name");
        if (name.equals(zephyrCycleName)){
          break;
        }
      }
      String cycleId = jsonObj.getString("id");
      return cycleId;
    }
    public void attachArtifactZephyr(String issueId, String cycleId, String entityId, String projectId, String filePath) throws Exception{

      String attachmentUri = API_ZEPHYR.replace("{SERVER}", zephyrBaseUrl) + "attachment?" + "issueId=" + issueId
          + "&versionId=" + versionId + "&entityName=" + entityName + "&cycleId=" + cycleId + "&entityId="
          + entityId + "&comment=" + comment + "&projectId=" + projectId;
      URI uri = new URI(attachmentUri);

      String jwt = jwtGenerator.generateJWT("POST", uri, expirationInSec);

      HttpResponse response2 = null;
      HttpClient restClient = new DefaultHttpClient();

      File file = new File(filePath);
      MultipartEntity entity = new MultipartEntity();
      entity.addPart("attachment", new FileBody(file));

      HttpPost addAttachmentReq = new HttpPost(uri);
      addAttachmentReq.addHeader("Authorization", jwt);
      addAttachmentReq.addHeader("zapiAccessKey", accessKey);
      addAttachmentReq.setEntity(entity);

      try {
        response2 = restClient.execute(addAttachmentReq);
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      HttpEntity entity1 = response2.getEntity();
      int statusCode = response2.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode < 300) {
        System.out.println("Attachment added Successfully");
      } else {
        try {
          String string = null;
          string = EntityUtils.toString(entity1);
          System.out.println(string);
          throw new ClientProtocolException("Unexpected response status: " + statusCode);
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        }
      }
    }
}