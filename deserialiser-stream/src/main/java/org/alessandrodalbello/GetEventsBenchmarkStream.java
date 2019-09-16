package org.alessandrodalbello;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

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
 * Benchmark of Matchbook SDK version that uses a Jackson deserialisation stream-based implementation.
 */
@OutputTimeUnit(MILLISECONDS)
@Warmup(iterations = 1, time = 10, timeUnit = SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = SECONDS)
public class GetEventsBenchmarkStream {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void getOneTennisEvent(ExecutionPlan executionPlan) {
        EventsRequest eventsRequest = new EventsRequest.Builder()
                .perPage(1)
                .sportIds(Collections.singleton(9L))
                .includeEventParticipants(false)
                .build();
        WaitingStreamObserver<Event> waitingStreamObserver = new WaitingStreamObserver<>();
        executionPlan.eventsClient.getEvents(eventsRequest, waitingStreamObserver);

        waitingStreamObserver.awaitResult(10);
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

    private static class WaitingStreamObserver<T> implements StreamObserver<T> {

        private final CountDownLatch countDownLatch;

        private MatchbookSDKException matchbookSDKException;

        private WaitingStreamObserver() {
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

}
