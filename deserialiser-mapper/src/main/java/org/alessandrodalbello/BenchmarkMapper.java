package org.alessandrodalbello;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

@Fork(value = 1, warmups = 0)
@Warmup(iterations = 5, time = 5, timeUnit = SECONDS)
@Measurement(iterations = 20, time = 5, timeUnit = SECONDS)
@Timeout(time = 30, timeUnit = SECONDS)
@OutputTimeUnit(MILLISECONDS)
public class BenchmarkMapper {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void getEvents(BenchmarkStateMapper benchmarkState) {
        benchmarkState.eventsClient.getEvents(benchmarkState.eventsRequest, benchmarkState.streamObserver);
        benchmarkState.streamObserver.await();
    }

}
