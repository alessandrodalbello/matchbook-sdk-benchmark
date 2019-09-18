package org.alessandrodalbello;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Objects;
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
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark of Matchbook SDK version that uses a Jackson deserialisation stream-based implementation.
 */
@OutputTimeUnit(MILLISECONDS)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class GetEventsBenchmarkStream {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void getFiftyEventsWithParticipants(ExecutionPlan executionPlan, Blackhole blackhole) {
        executionPlan.wireMockServer.stubFor(get(urlPathEqualTo("/edge/rest/events"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("files/mock-responses/50_events_with_participants_and_prices.json")));

        EventsRequest eventsRequest = new EventsRequest.Builder()
                .includeEventParticipants(true)
                .perPage(50)
                .build();
        WaitingStreamObserver<Event> waitingStreamObserver = new WaitingStreamObserver<>(blackhole);
        executionPlan.eventsClient.getEvents(eventsRequest, waitingStreamObserver);

        waitingStreamObserver.awaitResult(10);
    }

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        private WireMockServer wireMockServer;
        private EventsClient eventsClient;

        @Setup(Level.Trial)
        public void setUp() {
            WireMockConfiguration wireMockServerConfiguration = WireMockConfiguration.wireMockConfig()
                    .disableRequestJournal()
                    .port(8081);
            wireMockServer = new WireMockServer(wireMockServerConfiguration);
            wireMockServer.start();

            ClientConfig clientConfig = new ClientConfig.Builder("anonymous".toCharArray(), "secret".toCharArray())
                    .sportsUrl("http://localhost:8081/edge/rest")
                    .loginUrl("http://localhost:8081/bpapi/rest/security/session")
                    .build();
            ConnectionManager connectionManager = new ConnectionManager.Builder(clientConfig)
                    .sessionAutoManage(false)
                    .build();
            eventsClient = new EventsClientRest(connectionManager);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            wireMockServer.stop();
        }
    }

    private static class WaitingStreamObserver<T> implements StreamObserver<T> {

        private final Blackhole blackhole;
        private final CountDownLatch countDownLatch;

        private MatchbookSDKException matchbookSDKException;

        private WaitingStreamObserver(Blackhole blackhole) {
            this.blackhole = blackhole;
            countDownLatch = new CountDownLatch(1);
        }

        private void awaitResult(int timeoutSeconds) {
            try {
                countDownLatch.await(timeoutSeconds, SECONDS);
                if (matchbookSDKException != null) {
                    throw new RuntimeException(matchbookSDKException);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onNext(T entity) {
            Objects.requireNonNull(entity);
            blackhole.consume(entity);
        }

        @Override
        public void onCompleted() {
            countDownLatch.countDown();
        }

        @Override
        public <E extends MatchbookSDKException> void onError(E exception) {
            matchbookSDKException = exception;
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

}
