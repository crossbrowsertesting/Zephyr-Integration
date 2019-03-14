package com.thed.zephyr;

import com.thed.zephyr.cloud.rest.client.JwtGenerator;
import com.thed.zephyr.util.AbstractTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;

/**
 * Created by aliakseimatsarski on 3/15/16.
 */
public class GenerateJWT extends AbstractTest {

    private static JwtGenerator jwtGenerator;

    @BeforeClass
    public static void setUp() throws Exception{
        jwtGenerator = client.getJwtGenerator();
    }

    @Test
    public void testGenerateJWT() throws URISyntaxException {
                // Replace Zephyr BaseUrl with the <ZAPI_CLOUD_URL> shared with ZAPI Cloud Installation
        String zephyrBaseUrl = "https://prod-api.zephyr4jiracloud.com/connect";
        // zephyr accessKey , we can get from Addons >> zapi section
        String accessKey = "amlyYTo4MGNjZmU4Ny05YWE1LTQ5ZTYtODYwYS1mMzYzNzA1MWFlZGEgNWM2MWUzMzQ4MmNiNGY2NWVlZjU2OGQ1IFRlc3Q";
        // zephyr secretKey , we can get from Addons >> zapi section
        String secretKey = "iRwVoObvn-JaA3qc-7YKyPWKKr7-56N64CVk6v3WCJs";
        // Jira accountId
        String accountId = "5c61e33482cb4f65eef568d5";
        ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, accountId).build();
        JwtGenerator jwtGenerator = client.getJwtGenerator();
        
        // API to which the JWT token has to be generated
        // https://prod-api.zephyr4jiracloud.com/connect/public/rest/api/1.0/attachment?issueId=10100&versionId=10103&entityName=stepResult&cycleId=badc6f64-9f60-4038-9240-e4ad6c4e2a31&entityId=a8f3ddb2-e2d0-42ae-aa3b-424cda22b7d8&comment=Attachment Attempt&projectId=10101
        String createCycleUri = zephyrBaseUrl  + "/public/rest/api/1.0/executions?issueId=11230&offset=0&size=50&projectId=10101";
        //"/public/rest/api/1.0/attachment?issueId=10100&versionId=10103&entityName=stepResult&cycleId=badc6f64-9f60-4038-9240-e4ad6c4e2a31&entityId=a8f3ddb2-e2d0-42ae-aa3b-424cda22b7d8&comment=AttachmentAttempt&projectId=10101";
        
        URI uri = new URI(createCycleUri);
        //int expirationInSec = 360;

        //String urlStr = "https://cbt-dev.atlassian.net/rest/connect/public/rest/api/1.0/cycles/search?versionId=&expand=&projectId=";

        //URI url = new URI(urlStr);
        String jwt = jwtGenerator.generateJWT("GET", uri, 3600);
        System.out.println(jwt);
        System.out.println(uri);
    }


}
