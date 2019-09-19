package org.alessandrodalbello;

public interface RestClientWrapper {

    <O extends WaitingStreamObserver> void getEntity(O waitingStreamObserver);

    <O extends WaitingStreamObserver> void getEntities(O waitingStreamObserver);

}
