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


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{  
    //https://app.crossbrowsertesting.com/account
    public final String cbtUsername = "";
    public final String cbtAuthkey = "";
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
    }
}