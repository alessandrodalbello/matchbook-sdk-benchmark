package org.alessandrodalbello;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;

import com.matchbook.sdk.core.StreamObserver;
import com.matchbook.sdk.core.exceptions.MatchbookSDKException;
import com.matchbook.sdk.rest.dtos.events.Event;
import com.matchbook.sdk.rest.dtos.events.EventRequest;
import com.matchbook.sdk.rest.dtos.events.EventsRequest;
import org.eclipse.jetty.http.HttpStatus;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, warmups = 0)
@Warmup(iterations = 2, time = 5, timeUnit = SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = SECONDS)
@Timeout(time = 1, timeUnit = MINUTES)
@OutputTimeUnit(MILLISECONDS)
public class BenchmarkMapper {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void getSingleEvent(ExecutionPlanMapper executionPlan, Blackhole blackhole) {
        executionPlan.wireMockServer.stubFor(get(urlPathEqualTo("/edge/rest/events/1227568266270017"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("single_event.json")));

        EventRequest eventRequest = new EventRequest.Builder(1227568266270017L).build();
        WaitingStreamObserver streamObserver = new WaitingStreamObserver(blackhole);
        executionPlan.eventsClient.getEvent(eventRequest, streamObserver);
        try {
            streamObserver.awaitResult(2);
        } finally {
            blackhole.consume(eventRequest);
            blackhole.consume(streamObserver);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get20EventsWithoutPrices(ExecutionPlanMapper executionPlan, Blackhole blackhole) {
        executionPlan.wireMockServer.stubFor(get(urlPathEqualTo("/edge/rest/events"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("20_events_without_prices.json")));

        EventsRequest eventsRequest = new EventsRequest.Builder().build();
        WaitingStreamObserver streamObserver = new WaitingStreamObserver(blackhole);
        executionPlan.eventsClient.getEvents(eventsRequest, streamObserver);
        try {
            streamObserver.awaitResult(2);
        } finally {
            blackhole.consume(eventsRequest);
            blackhole.consume(streamObserver);
        }
    }

    private static class WaitingStreamObserver implements StreamObserver<Event> {

        private final Blackhole blackhole;
        private final CountDownLatch countDownLatch;

        private Exception matchbookSDKException;

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
        public void onNext(Event event) {
            blackhole.consume(event);
        }

        @Override
        public void onCompleted() {
            countDownLatch.countDown();
        }

        @Override
        public <E extends MatchbookSDKException> void onError(E matchbookSDKException) {
            this.matchbookSDKException = matchbookSDKException;
            countDownLatch.countDown();
        }
    }

}
