package org.alessandrodalbello;

import com.matchbook.sdk.core.StreamObserver;
import com.matchbook.sdk.core.exceptions.MatchbookSDKException;
import com.matchbook.sdk.rest.dtos.events.Event;
import org.openjdk.jmh.infra.Blackhole;

public class WaitingStreamObserverObjMapper extends WaitingStreamObserver implements StreamObserver<Event> {

    WaitingStreamObserverObjMapper(Blackhole blackhole) {
        super(blackhole);
    }

    @Override
    public void onNext(Event event) {
        onNextWrapper(event);
    }

    @Override
    public void onCompleted() {
        onCompletedWrapper();
    }

    @Override
    public <E extends MatchbookSDKException> void onError(E matchbookSDKException) {
        onErrorWrapper(matchbookSDKException);
    }

}
