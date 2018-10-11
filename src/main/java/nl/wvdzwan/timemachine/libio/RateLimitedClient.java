package nl.wvdzwan.timemachine.libio;

public class RateLimitedClient implements LibrariesIOInterface {

    LibrariesIOInterface client;
    private long lastRequest = Long.MAX_VALUE;
    private long minWaitTime;


    RateLimitedClient(LibrariesIOInterface client, long minWaitTime) {
        this.client = client;
        this.minWaitTime = minWaitTime;
    }

    @Override
    public Project getProjectInfo(String identifier) {

        doWait();

        return client.getProjectInfo(identifier);
    }

    private void doWait() {

        long timeSinceLastRequest = System.currentTimeMillis() - lastRequest;

        long waitRemaining = minWaitTime - timeSinceLastRequest;

        if (waitRemaining > 0) {
            try {
                Thread.sleep(waitRemaining);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
