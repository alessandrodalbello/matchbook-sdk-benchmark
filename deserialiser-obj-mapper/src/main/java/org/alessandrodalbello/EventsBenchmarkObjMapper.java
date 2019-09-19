package org.alessandrodalbello;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, warmups = 0)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@OutputTimeUnit(MILLISECONDS)
public class EventsBenchmarkObjMapper extends EventsBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void getSingleEvent(ExecutionPlanObjMapper executionPlan, Blackhole blackhole) {
        super.getSingleEvent(executionPlan.wireMockServer, executionPlan.restClient,
                new WaitingStreamObserverObjMapper(blackhole), blackhole);
    }

}
