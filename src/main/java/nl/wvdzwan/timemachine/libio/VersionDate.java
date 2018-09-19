package nl.wvdzwan.timemachine.libio;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class VersionDate {
    protected String number;
    protected LocalDateTime published_at;

    public String getNumber() {
        return number;
    }

    public LocalDateTime getPublished_at() {
        return published_at;
    }
}
