package com.zingtongroup.paralleljunit;

import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EndPointTest {

    HttpClient httpClient;
    String endPointUrl = "http://personsdb.mycompany.org/api/rest/v1/persons";

    @Before
    public void setup(){
        httpClient = HttpClient.newBuilder().build();
    }

    @LoadTest(
            maxThreadCount = 10,
            rampUpTimeInMilliseconds = 20000,
            totalDurationInMilliseconds = 30000,
            maxExecutionTimeIndividualIteration = 200,
            haltOnError = true)
    public void personPostingMultiThreadResponseTimeTest() throws IOException, InterruptedException {

        long shoeSize = Math.round(Math.random() * 10) + 35;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endPointUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{ 'name': 'Testus Testsson', 'shoeSize': " + shoeSize + " }"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());
        Assert.assertTrue(response.body().length() > 0);

    }
}
