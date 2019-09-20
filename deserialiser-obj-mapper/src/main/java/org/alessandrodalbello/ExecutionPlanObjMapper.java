package org.alessandrodalbello;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.matchbook.sdk.rest.ClientConfig;
import com.matchbook.sdk.rest.ConnectionManager;
import com.matchbook.sdk.rest.EventsClient;
import com.matchbook.sdk.rest.EventsClientRest;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ExecutionPlanObjMapper {

    WireMockServer wireMockServer;
    EventsClient eventsClient;

    @Setup(Level.Trial)
    public void setUp() {
        WireMockConfiguration wireMockServerConfiguration = WireMockConfiguration.wireMockConfig()
                .usingFilesUnderDirectory("src/main/resources")
//                    .disableRequestJournal()
                .port(8080);
        wireMockServer = new WireMockServer(wireMockServerConfiguration);
        wireMockServer.start();

        ClientConfig clientConfig = new ClientConfig.Builder("anonymous".toCharArray(), "secret".toCharArray())
                .sportsUrl("http://localhost:8080/edge/rest")
                .loginUrl("http://localhost:8080/bpapi/rest/security/session")
                .build();
        ConnectionManager connectionManager = new ConnectionManager.Builder(clientConfig)
                .sessionAutoManage(false)
                .build();
        eventsClient = new EventsClientRest(connectionManager);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        wireMockServer.stop();
        wireMockServer.shutdown();
    }

}
