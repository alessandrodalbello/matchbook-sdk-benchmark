package org.alessandrodalbello;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public abstract class ExecutionPlan {

    WireMockServer wireMockServer;
    RestClientWrapper restClient;

    protected abstract RestClientWrapper newRestClient(char[] username, char[] password, String sportsUrl, String loginUrl);

    protected void start() {
        WireMockConfiguration wireMockServerConfiguration = WireMockConfiguration.wireMockConfig()
                .usingFilesUnderDirectory("src/main/resources")
//                    .disableRequestJournal()
                .port(8080);
        wireMockServer = new WireMockServer(wireMockServerConfiguration);
        wireMockServer.start();

        restClient = newRestClient("anonymous".toCharArray(), "secret".toCharArray(),
                "http://localhost:8080/edge/rest", "http://localhost:8080/bpapi/rest/security/session");
    }

    protected void stop() {
        wireMockServer.stop();
        wireMockServer.shutdown();
    }

}
