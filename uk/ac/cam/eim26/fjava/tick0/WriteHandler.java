package uk.ac.cam.eim26.fjava.tick0;

import java.nio.channels.CompletionHandler;

public class WriteHandler implements CompletionHandler<Integer, Integer> {
    private boolean[] isFree = new boolean[256];
    private int successCounter = 0;
    private int failCounter = 0;

    public WriteHandler() {
        for (int i = 0; i < 256; i++) {
            isFree[i] = true;
        }
    }

    private synchronized void setFreeValue(int index, boolean value) {
        isFree[index] = value;
    }

    @Override
    public synchronized void completed(Integer result, Integer attachment) {
        setFreeValue(attachment, true);
        successCounter++;
    }

    @Override
    public synchronized void failed(Throwable exc, Integer attachment) {
        failCounter++;
    }

    public void setFree(int index) {
        setFreeValue(index, false);
    }

    public boolean getFree(int index) {
        return isFree[index];
    }

    public int getSuccessCounter() {
        return successCounter;
    }

    public int getFailCounter() {
        return failCounter;
    }
}
