package org.alessandrodalbello;

import com.matchbook.sdk.rest.ConnectionManager;
import com.matchbook.sdk.rest.EventsClient;
import com.matchbook.sdk.rest.EventsClientRest;
import com.matchbook.sdk.rest.dtos.events.EventRequest;
import com.matchbook.sdk.rest.dtos.events.EventsRequest;

public class EventsClientWrapperStream implements RestClientWrapper {

    private final EventsClient eventsClient;

    EventsClientWrapperStream(ConnectionManager connectionManager) {
        eventsClient = new EventsClientRest(connectionManager);
    }

    @Override
    public void getEntity(WaitingStreamObserver waitingStreamObserver) {
        EventRequest eventRequest = new EventRequest.Builder(42L).build();
        eventsClient.getEvent(eventRequest, (WaitingStreamObserverStream) waitingStreamObserver);
    }

    @Override
    public void getEntities(WaitingStreamObserver waitingStreamObserver) {
        EventsRequest eventsRequest = new EventsRequest.Builder().build();
        eventsClient.getEvents(eventsRequest, (WaitingStreamObserverStream) waitingStreamObserver);
    }

}
