package org.alessandrodalbello;

import com.matchbook.sdk.rest.ClientConfig;
import com.matchbook.sdk.rest.ConnectionManager;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ExecutionPlanStream extends ExecutionPlan {

    @Setup(Level.Trial)
    public void setUp() {
        start();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        stop();
    }

    @Override
    protected RestClientWrapper newRestClient(char[] username, char[] password, String sportsUrl, String loginUrl) {
        ClientConfig clientConfig = new ClientConfig.Builder(username, password)
                .sportsUrl(sportsUrl)
                .loginUrl(loginUrl)
                .build();
        ConnectionManager connectionManager = new ConnectionManager.Builder(clientConfig)
                .sessionAutoManage(false)
                .build();
        return new EventsClientWrapperStream(connectionManager);
    }

}
