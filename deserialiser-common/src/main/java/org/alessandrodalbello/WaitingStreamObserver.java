package org.alessandrodalbello;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;

import org.openjdk.jmh.infra.Blackhole;

public abstract class WaitingStreamObserver {

    private final Blackhole blackhole;
    private final CountDownLatch countDownLatch;

    private Exception matchbookSDKException;

    protected WaitingStreamObserver(Blackhole blackhole) {
        this.blackhole = blackhole;
        countDownLatch = new CountDownLatch(1);
    }

    public void awaitResult(int timeoutSeconds) {
        try {
            countDownLatch.await(timeoutSeconds, SECONDS);
            if (matchbookSDKException != null) {
                throw new RuntimeException(matchbookSDKException);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> void onNextWrapper(T entity) {
        blackhole.consume(entity);
    }

    protected void onCompletedWrapper() {
        countDownLatch.countDown();
    }

    protected void onErrorWrapper(Exception exception) {
        matchbookSDKException = exception;
        countDownLatch.countDown();
    }

}
