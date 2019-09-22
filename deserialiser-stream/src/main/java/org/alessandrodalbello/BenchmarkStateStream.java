package org.alessandrodalbello;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.matchbook.sdk.core.StreamObserver;
import com.matchbook.sdk.core.exceptions.MatchbookSDKException;
import com.matchbook.sdk.rest.ClientConfig;
import com.matchbook.sdk.rest.ConnectionManager;
import com.matchbook.sdk.rest.EventsClient;
import com.matchbook.sdk.rest.EventsClientRest;
import com.matchbook.sdk.rest.dtos.events.Event;
import com.matchbook.sdk.rest.dtos.events.EventsRequest;
import org.eclipse.jetty.http.HttpStatus;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class BenchmarkStateStream {

    EventsClient eventsClient;
    EventsRequest eventsRequest;
    WaitStreamObserver streamObserver;

    private WireMockServer wireMockServer;

    @Param({
            "1_event.json",
            "20_events_without_prices.json",
            "20_events_with_prices.json",
            "50_events_with_participants_and_prices.json"
    })
    private String response;

    @Param({"true", "false"})
    private boolean waitCompletion;

    @Setup(Level.Trial)
    public void setUpBenchmark() {
        WireMockConfiguration wireMockServerConfiguration = WireMockConfiguration.wireMockConfig()
                .usingFilesUnderDirectory("src/main/resources")
                .disableRequestJournal()
                .port(8080);
        wireMockServer = new WireMockServer(wireMockServerConfiguration);
        wireMockServer.start();

        wireMockServer.stubFor(get(urlPathEqualTo("/edge/rest/events"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(response)));

        ClientConfig clientConfig = new ClientConfig.Builder("anonymous".toCharArray(), "secret".toCharArray())
                .sportsUrl("http://localhost:8080/edge/rest")
                .loginUrl("http://localhost:8080/bpapi/rest/security/session")
                .build();
        ConnectionManager connectionManager = new ConnectionManager.Builder(clientConfig)
                .sessionAutoManage(false)
                .build();
        eventsClient = new EventsClientRest(connectionManager);
        eventsRequest = new EventsRequest.Builder().build();
    }

    @TearDown(Level.Trial)
    public void tearDownBenchmark() {
        wireMockServer.shutdown();
    }

    @Setup(Level.Invocation)
    public void setUpInvocation() {
        streamObserver = new WaitStreamObserver(waitCompletion);
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() {
        streamObserver = null;
    }

    static class WaitStreamObserver implements StreamObserver<Event> {

        private final boolean waitCompletion;
        private final CountDownLatch countDownLatch;

        private Exception matchbookSDKException;

        private WaitStreamObserver(boolean waitCompletion) {
            this.waitCompletion = waitCompletion;
            countDownLatch = new CountDownLatch(1);
        }

        void await() {
            try {
                countDownLatch.await(2, SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (matchbookSDKException != null) {
                throw new RuntimeException(matchbookSDKException);
            }
        }

        @Override
        public void onNext(Event event) {
            if (!waitCompletion) {
                countDownLatch.countDown();
            }
        }

        @Override
        public void onCompleted() {
            if (waitCompletion) {
                countDownLatch.countDown();
            }
        }

        @Override
        public <E extends MatchbookSDKException> void onError(E matchbookSDKException) {
            this.matchbookSDKException = matchbookSDKException;
            countDownLatch.countDown();
        }
    }

}
