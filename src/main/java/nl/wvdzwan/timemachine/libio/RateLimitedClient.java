package nl.wvdzwan.timemachine.libio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RateLimitedClient extends LibrariesIoClient {

    private static Logger logger = LogManager.getLogger(RateLimitedClient.class);

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

        long waitRemaining = Math.max(earliestSlot - now, 0); // Don't wait for a negative time

        if (waitRemaining > 0) {
            logger.trace("Rate limiting, now: {}, wait_for: {}", now, waitRemaining);

            try {
                Thread.sleep(waitRemaining);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastRequest = System.currentTimeMillis();
    }
}
