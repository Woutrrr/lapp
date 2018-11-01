package nl.wvdzwan.timemachine.libio;

import javax.sound.midi.SysexMessage;

public class RateLimitedClient extends LibrariesIOClient {


    private long lastRequest = 0;
    private long minWaitTime = 500; // Default wait time of 1000ms

    public void setWaitTime(long newWaitTime) {
        this.minWaitTime = newWaitTime;
    }

    @Override
    public Project getProjectInfo(String identifier) {

        doWait();

        return super.getProjectInfo(identifier);
    }

    private void doWait() {
        long earliestSlot = lastRequest + minWaitTime;
        long now = System.currentTimeMillis();

        long waitRemaining = Math.max(earliestSlot - now, 0);
        System.out.print("Now: " + now + " Wait: " + waitRemaining + " Unpause at: " + (now+waitRemaining) );

        if (waitRemaining > 0) {
            try {
                Thread.sleep(waitRemaining);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastRequest = System.currentTimeMillis();
        System.out.println(" Done at: " + lastRequest);
    }
}
