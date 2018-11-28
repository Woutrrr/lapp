package nl.wvdzwan.librariesio;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class VersionDateTest {

    @Test
    void getNumber() {
        VersionDate vd = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");

        assertEquals("4.1", vd.getNumber());
    }


    @Test
    void getPublished_at() {
        LocalDateTime expected = LocalDateTime.of(2018, 1, 1, 2, 4, 13, 0);

        VersionDate vd = new VersionDate("4.1", "2018-01-01T02:04:13.000Z");

        assertEquals(expected, vd.getPublished_at());
    }

    @Test
    void malformedDateTime () {

        assertThrows(DateTimeParseException.class, () -> new VersionDate("1.1", "Not a datetime"));

    }

    @Test
    void equals() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");
        VersionDate versionDate2 = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");

        assertEquals(versionDate, versionDate2);
        assertEquals(versionDate2, versionDate);
    }

    @Test
    void equalsSelf() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");

        assertEquals(versionDate, versionDate);
    }

    @Test
    void notEqualsDifferentVersion() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");
        VersionDate versionDate2 = new VersionDate("4.1.1", "2018-01-01T02:04:00.000Z");

        assertNotEquals(versionDate, versionDate2);
        assertNotEquals(versionDate2, versionDate);
    }

    @Test
    void notEqualsDifferentDate() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");
        VersionDate versionDate2 = new VersionDate("4.1", "2018-01-01T02:04:02.000Z");

        assertNotEquals(versionDate, versionDate2);
        assertNotEquals(versionDate2, versionDate);
    }

    @Test
    void equalsWithNull() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");

        assertFalse(versionDate.equals(null));
    }

    @Test
    void equalsWrongClass() {
        VersionDate versionDate = new VersionDate("4.1", "2018-01-01T02:04:00.000Z");

        assertFalse(versionDate.equals(new Object()));
    }
}