package org.cwatch.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * Created by draktina on 1/17/15.
 */
public class HttpRestClient {

    HttpClient httpClient = HttpClients.createDefault();
    String body;

    public String restGet(String url, String param) throws URISyntaxException, IOException {

        try {
            URIBuilder builder;
            builder = new URIBuilder(url).addParameter("name", param);
            HttpGet getRequest = new HttpGet(
                    builder.build());
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);


            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            if (response.getEntity() != null) {

                StringWriter writer = new StringWriter();
                IOUtils.copy(response.getEntity().getContent(), writer);
                body = writer.toString();
            }


        } finally {
            HttpClientUtils.closeQuietly(httpClient);
        }

        return body;
    }
}
