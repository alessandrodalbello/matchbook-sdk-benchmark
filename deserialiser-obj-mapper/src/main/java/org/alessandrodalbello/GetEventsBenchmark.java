package org.alessandrodalbello;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Objects;

import com.matchbook.sdk.core.StreamObserver;
import com.matchbook.sdk.core.exceptions.MatchbookSDKException;
import com.matchbook.sdk.rest.ClientConfig;
import com.matchbook.sdk.rest.ConnectionManager;
import com.matchbook.sdk.rest.EventsClient;
import com.matchbook.sdk.rest.EventsClientRest;
import com.matchbook.sdk.rest.dtos.events.Event;
import com.matchbook.sdk.rest.dtos.events.EventsRequest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark of Matchbook SDK version that uses a Jackson ObjectMapper deserialisation implementation.
 */
@OutputTimeUnit(MILLISECONDS)
@Warmup(iterations = 1, time = 10, timeUnit = SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = SECONDS)
public class GetEventsBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void getOneTennisEvent(ExecutionPlan executionPlan) {
        EventsRequest eventsRequest = new EventsRequest.Builder()
                .perPage(1)
                .sportIds(Collections.singleton(9L))
                .includeEventParticipants(false)
                .build();
        executionPlan.eventsClient.getEvents(eventsRequest, new StreamObserver<Event>() {

            @Override
            public void onNext(Event event) {
                Objects.requireNonNull(event);
            }

            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public <E extends MatchbookSDKException> void onError(E exception) {
                throw exception;
            }
        });
    }

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        private EventsClient eventsClient;

        @Setup(Level.Invocation)
        public void setUp() {
            ClientConfig clientConfig = new ClientConfig.Builder("anonymous".toCharArray(), null).build();
            ConnectionManager connectionManager = new ConnectionManager.Builder(clientConfig).build();
            eventsClient = new EventsClientRest(connectionManager);
        }
    }

}
