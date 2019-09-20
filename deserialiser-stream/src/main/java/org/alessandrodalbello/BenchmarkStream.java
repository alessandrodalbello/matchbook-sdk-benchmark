package org.alessandrodalbello;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
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

@Fork(value = 1, warmups = 0)
@Warmup(iterations = 3, time = 5, timeUnit = SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = SECONDS)
@Timeout(time = 30, timeUnit = SECONDS)
@OutputTimeUnit(MILLISECONDS)
public class BenchmarkStream {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get20EventsWithoutPrices_OverallTime(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "20_events_without_prices.json", 2, true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get20EventsWithoutPrices_TimeToFirst(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "20_events_without_prices.json", 2, false);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get20EventsWithPrices_OverallTime(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "20_events_with_prices.json", 2, true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get20EventsWithPrices_TimeToFirst(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "20_events_with_prices.json", 2, false);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get50EventsWithParticipantsAndPrices_OverallTime(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "50_events_with_participants_and_prices.json", 3, true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void get50EventsWithParticipantsAndPrices_TimeToFirst(BenchmarkStateStream benchmarkState) {
        getEvents(benchmarkState, "50_events_with_participants_and_prices.json", 3, false);
    }

    private void getEvents(BenchmarkStateStream benchmarkState, String responseFile, int waitTimeout, boolean waitCompletion) {
        benchmarkState.wireMockServer.stubFor(get(urlPathEqualTo("/edge/rest/events"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(responseFile)));

        EventsRequest eventsRequest = new EventsRequest.Builder().build();
        WaitStreamObserver streamObserver = new WaitStreamObserver(waitCompletion);
        benchmarkState.eventsClient.getEvents(eventsRequest, streamObserver);

        streamObserver.await(waitTimeout);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void getSingleEvent_OverallTime(BenchmarkStateStream benchmarkState) {
        getEvent(benchmarkState, true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void getSingleEvent_TimeToFirst(BenchmarkStateStream benchmarkState) {
        getEvent(benchmarkState, false);
    }

    private void getEvent(BenchmarkStateStream benchmarkState, boolean waitCompletion) {
        benchmarkState.wireMockServer.stubFor(get(urlPathMatching("/edge/rest/events/\\d+"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("single_event.json")));

        EventRequest eventRequest = new EventRequest.Builder(1227568266270017L).build();
        WaitStreamObserver streamObserver = new WaitStreamObserver(waitCompletion);
        benchmarkState.eventsClient.getEvent(eventRequest, streamObserver);

        streamObserver.await(1);
    }

    private static class WaitStreamObserver implements StreamObserver<Event> {

        private final boolean waitCompletion;
        private final CountDownLatch countDownLatch;

        private Exception matchbookSDKException;

        private WaitStreamObserver(boolean waitCompletion) {
            this.waitCompletion = waitCompletion;
            countDownLatch = new CountDownLatch(1);
        }

        private void await(int waitTimeout) {
            try {
                countDownLatch.await(waitTimeout, SECONDS);
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