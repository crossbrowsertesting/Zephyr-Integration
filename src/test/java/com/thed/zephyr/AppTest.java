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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response.Status.Family;
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
    public final String cbtUsername = "";
    public final String cbtAuthkey = "";

    //Zephyr credentials found under the apps tab on JIRA
    public final String accessKey = "";
    public final String secretKey = "";

    //Jira credentials
    public final String jiraUsername = "";
    public final String jiraPassword = "";

    //Version Id has to be set to use zephyr. After being set up you can find it here : 
    public final String versionName = ""; //Ex: 1.0


    //Jira Variables
    //This is the issue key an issue on Jira(this may not be generated until after your first test as Zephyr will create a new card)
    public final String issueTag = ""; //Ex: ZEP-3
    public final String zephyrCycleName = ""; //Ex: Our Zephyr Cycle

    //Info that will be sent to the Jira following a passed CBT test
    public final String comment = "Attachment-through-ZAPI-CLoud";
    //In your test you will need to save images locally so that they can be sent to jira. 
    //This is an abosolute path to upload those files. 

    //Likely wont need to change these.
    public final String entityName = "execution"; 
    public final String API_ZEPHYR = "{SERVER}/public/rest/api/1.0/";
    public final String zephyrBaseUrl = "https://prod-api.zephyr4jiracloud.com/connect";
    public final int expirationInSec = 360;

    public final ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, jiraUsername).build();
    public final JwtGenerator jwtGenerator = client.getJwtGenerator();
    public AutomatedTest myTest;

    public void testApp() throws Exception
    {
      //START CBT 
      String driverId = null;
      try{
        System.out.println("Starting Selenium Test Through CBT");
        Builders builder = new Builders();
        builder.login(cbtUsername, cbtAuthkey);

        //Build the caps for our driver
        CapsBuilder capsBuilder = new CapsBuilder(builder.username, builder.authkey);
        capsBuilder.withPlatform("Mac OSX 10.14").withBuild("1.0").withBrowser("Safari12").build();

        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://" + builder.username + ":" + builder.authkey + "@hub.crossbrowsertesting.com:80/wd/hub"),capsBuilder.caps);
        
        //initialize an AutomatedTest object with our selnium session id
        driverId = driver.getSessionId().toString();
        myTest = new AutomatedTest(driverId);

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
      String versionId = values[2];

      //GATHER ZEPHYR INFO
      String cycleId = getZephyrCycleId(projectId, versionId);
      String entityId = getZephyrEntityId(projectId,issueId);

      //POST ARTIFACTS FROM CBT TO JIRA THROUGH ZEPHYR
      String filePath = ""; //Ex: "/Users/you/.jenkins/workspace/MyProject/mypicture.png"
      String videofilePath = "";
      attachArtifactZephyr(issueId, cycleId, entityId, projectId, versionId, filePath);
      attachArtifactZephyr(issueId, cycleId, entityId, projectId, versionId, videofilePath);
      String resultURL = cbtHistoryLink(driverId);
      String zephyrComment = "Last test provided by CrossBrowserTesting.com : " + resultURL;
      setZephyrExecutionComment(issueId, cycleId, projectId, versionId, entityId, zephyrComment);
      
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

      response = ClientBuilder.newClient()
        .target("https://cbt-dev.atlassian.net/rest/api/2/project/" + projectId)
        .register(feature)
        .request()
        //.header("Content-Type", "text/plain")
        .get();

        projectObject = new JSONObject(response.readEntity(String.class));
        String versionId = null;
        JSONArray versionArray = projectObject.getJSONArray("versions");
        for (int i=0; i < versionArray.length(); i++){
          JSONObject versionObject = versionArray.getJSONObject(i);
          String versionNameTmp = versionObject.getString("name");
          if(versionName.equals(versionNameTmp)){
            versionId = versionObject.getString("id");
            break;
          }
        }

      String ar[] = new String[3];
      ar[0] = issueId;
      ar[1] = projectId;
      ar[2] = versionId;
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

      String entityId = null;
      String executionId = null;

      for (int i=0; i < executionArray.length(); i++){
        JSONObject executionSingleObject = executionArray.getJSONObject(i);
        JSONObject execution = executionSingleObject.getJSONObject("execution");
        String name = execution.getString("cycleName");
        if (name.equals(zephyrCycleName)){
          int issueIdInt = execution.getInt("issueId");
          String issueIdTemp = Integer.toString(issueIdInt);
          if (issueIdTemp.equals(issueId)){
            entityId = execution.getString("id");
            break;
          }
        }
      }

      return entityId;
    }
    public String getZephyrCycleId(String projectId, String versionId) throws Exception{
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
    public void setZephyrExecutionComment(String issueId, String cycleId, String projectId, String versionId, String executionId,String comment) throws Exception{
          String attachmentUri = API_ZEPHYR.replace("{SERVER}", zephyrBaseUrl) +"execution" + "/" + executionId;
          URI uri = new URI(attachmentUri);
          String commentObj = new JSONObject()
            .put("projectId", projectId)
            .put("issueId", issueId)
            .put("cycleId", cycleId)
            .put("versionId", versionId)
            .put("comment", comment).toString();
          String jwt = jwtGenerator.generateJWT("PUT", uri, expirationInSec);
          Response response = ClientBuilder.newClient()
            .target(attachmentUri)
            //.register(feature)
            .request()
            .header("Authorization", jwt)
            .header("zapiAccessKey", accessKey)
            .header("Content-Type", "text/plain")
            .put(Entity.entity(commentObj, MediaType.APPLICATION_JSON));
          return;
        }

    public String cbtHistoryLink(String driverId) throws Exception{
      HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(cbtUsername, cbtAuthkey);
      String attachmentUri = "https://crossbrowsertesting.com/api/v3/selenium?format=json&num=10";
      URI uri = new URI(attachmentUri);

      Response response = ClientBuilder.newClient()
        .target(attachmentUri)
        .register(feature)
        .request()
        //.header("Content-Type", "text/plain")
        .get();
        JSONObject historyObj = new JSONObject(response.readEntity(String.class));
        JSONArray seleniumHistoryArr = historyObj.getJSONArray("selenium");
        String resultURL = null;
        for (int i=0; i < seleniumHistoryArr.length(); i++){
          JSONObject seleniumHistoryObj = seleniumHistoryArr.getJSONObject(i);
          String id = seleniumHistoryObj.getString("selenium_session_id");
          if (id.equals(driverId)){
            resultURL = seleniumHistoryObj.getString("show_result_web_url");
            break;
          }
        }
      return resultURL;
    }

    public void attachArtifactZephyr(String issueId, String cycleId, String entityId, String projectId, String versionId, String filePath) throws Exception{

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

