# Matchbook SDK Benchmark

A simple benchmark for [Matchbook SDK](https://github.com/matchbook-technology/matchbook-sdk).

## Mapper vs Stream

The aim of the benchmark was to compare two different strategies implemented in Matchbook SDK used to deserialise the
REST API server responses. The first deserialisation strategy is based on standard mappers to convert JSON data into
Java objects. It is the easiest to implement, but also the most inefficient, in theory. The second deserialisation
strategy makes use of a streaming API and a few custom parsers to extract data before the whole deserialisation process
is finished. It is the most complex to implement, though the most performant, especially since it fits very well with
the stream-based design of Matchbook SDK.

### Benchmarks

The same tests suite was executed against the two Matchbook SDK versions: the objects mapper deserialisation (module
_deserialiser-mapper_) and the stream deserialisation (module _deserialiser-stream_). Each test simulates a request to
retrieve Matchbook events. Four kinds of responses were tested in order to simulate different sizes and contents. For
each kind of response, two different tests were run: one to measure the time to get the first event
(`waitCompletion == false`) and another one to benchmark the time to get the full contents, that is all the events
included in the response (`waitCompletion == true`).

The system environment and the benchmark parameters:
```text
# JMH version: 1.21
# VM version: JDK 1.8.0_222, OpenJDK 64-Bit Server VM, 25.222-b10
# VM invoker: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
# Warmup: 5 iterations, 5 s each
# Measurement: 20 iterations, 5 s each
# Timeout: 30 s per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
```

### Results

Objects mapper deserialisation:
```text
                                 (response)  (waitCompletion)  Mode  Cnt   Score   Error  Units
                               1_event.json              true  avgt   20   1,668 ± 0,070  ms/op
                               1_event.json             false  avgt   20   1,675 ± 0,083  ms/op
              20_events_without_prices.json              true  avgt   20   5,952 ± 0,419  ms/op
              20_events_without_prices.json             false  avgt   20   5,894 ± 0,328  ms/op
                 20_events_with_prices.json              true  avgt   20  11,919 ± 0,288  ms/op
                 20_events_with_prices.json             false  avgt   20  11,905 ± 0,308  ms/op
50_events_with_participants_and_prices.json              true  avgt   20  31,686 ± 0,433  ms/op
50_events_with_participants_and_prices.json             false  avgt   20  32,028 ± 0,345  ms/op
```

Stream deserialisation:
```text
                                 (response)  (waitCompletion)  Mode  Cnt   Score   Error  Units
                               1_event.json              true  avgt   20   1,502 ± 0,131  ms/op
                               1_event.json             false  avgt   20   1,524 ± 0,139  ms/op
              20_events_without_prices.json              true  avgt   20   5,513 ± 0,583  ms/op
              20_events_without_prices.json             false  avgt   20   3,640 ± 0,265  ms/op
                 20_events_with_prices.json              true  avgt   20  10,838 ± 0,472  ms/op
                 20_events_with_prices.json             false  avgt   20   7,479 ± 0,320  ms/op
50_events_with_participants_and_prices.json              true  avgt   20  30,711 ± 0,398  ms/op
50_events_with_participants_and_prices.json             false  avgt   20  11,391 ± 0,075  ms/op
```

Unsurprisingly, there is no time difference in getting the first bunch of data or the full response in the objects
mapper Matchbook SDK version. However, there is big difference in the stream deserialisation version, where it is
evident that the larger the response and the more impacting is the benefit of using a stream-based approach. In the case
of the largest tested response of this benchmark, the performance improvement was almost 64% between the two Matchbook
SDK versions. Moreover, the stream deserialisation version demonstrated a general performance boost even in reading the
full content of the response.

## License
```
    MIT License
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
```