package eu.europa.emsa.http;

import org.apache.http.conn.HttpHostConnectException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by draktina on 1/17/15.
 */
public class HttpRestClientTest {


    HttpRestClient restClient = new HttpRestClient();
    @Test
    public void testGetRequest() throws IOException, URISyntaxException {
        String body = restClient.restGet("http://localhost:8080/jerseyrest/rest/jsonServices", "test");
        System.out.print(body);


    }

    @Test(expected = HttpHostConnectException.class)
    public void testGetFail() throws IOException, URISyntaxException {
        String body = restClient.restGet("http://localhost:900/jerseyrest/rest/jsonServices", "test");

    }
}
