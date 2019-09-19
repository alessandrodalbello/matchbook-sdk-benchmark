package org.alessandrodalbello;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.jetty.http.HttpStatus;
import org.openjdk.jmh.infra.Blackhole;

public abstract class EventsBenchmark {

    protected void getSingleEvent(WireMockServer wireMockServer, RestClientWrapper restClient,
            WaitingStreamObserver streamObserver, Blackhole blackhole) {
        wireMockServer.stubFor(get(urlPathMatching("/edge/rest/events"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("single_event.json")));
        restClient.getEntities(streamObserver);
        try {
            streamObserver.awaitResult(2);
        } finally {
            blackhole.consume(streamObserver);
        }
    }

}
