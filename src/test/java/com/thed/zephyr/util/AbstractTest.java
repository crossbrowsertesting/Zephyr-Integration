package com.thed.zephyr.util;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aliakseimatsarski on 3/17/16.
 */
public class AbstractTest {

    final static String accessKey = "amlyYTo4MGNjZmU4Ny05YWE1LTQ5ZTYtODYwYS1mMzYzNzA1MWFlZGEgNWM2MWUzMzQ4MmNiNGY2NWVlZjU2OGQ1IFRlc3Q";/*replace with you credentials */
  //  final static String accessKey = "amlyYTo5OTY4ZGJiMy0yYzY3LTQyNzQtOGEyZC0wYjQwMGViOGQ0YjYgYWRtaW4";

    final static String secretKey = "iRwVoObvn-JaA3qc-7YKyPWKKr7-56N64CVk6v3WCJs";/*replace with you credentials */
 //   final static String secretKey = "ezBkGY4V0fnNyE3mAMNl813rhxqM5c79fijbdlf3eZQ";

    final static String accountId = "accountId";
    final static String zephyrBaseUrl = "https://cbt-dev-atlassian.net";
    public static ZFJCloudRestClient client;

    public Logger log = LoggerFactory.getLogger(AbstractTest.class);

    static {
        client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, accountId).build();
    }
}
