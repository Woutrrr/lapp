package nl.wvdzwan.librariesio;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

public class VersionDate {
    protected String number;
    protected LocalDateTime published_at;

    public VersionDate(String versionNumber, String publishedAt) {
        this.number = versionNumber;
        this.published_at = ZonedDateTime.parse(publishedAt).toLocalDateTime();
    }

    public String getNumber() {
        return number;
    }

    public LocalDateTime getPublished_at() {
        return published_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionDate that = (VersionDate) o;

        return Objects.equals(number, that.number) &&
                Objects.equals(published_at, that.published_at);
    }

}
