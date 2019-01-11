package nl.wvdzwan.timemachine.IRDotMerger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MergedInputReaderTest {

    @Test
    void ignoreWhitespaceAtEndFile1() throws IOException {
        // Arrange
        File file1 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/app.dot").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/core.dot").getFile());


        MergedInputReader reader = new MergedInputReader(new FileReader(file1),
                new FileReader(file2));
        char[] buf = new char[25000];

        // Act
        int read = reader.read(buf, 0, 25000);


        String output = new String(buf);

        // Assert
        assertEquals(read, output.indexOf('\0'));
        assertEquals(11913, read);
    }

    @Test
    void checkCombinedContent() throws IOException {
        // Arrange
        File file1 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/app.dot").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/core.dot").getFile());

        File combinedFile = new File(getClass().getClassLoader().getResource("mergedreader-test-data/combined.dot").getFile());
        char[] expected_buf = new char[25000];
        int expected_read = (new FileReader(combinedFile)).read(expected_buf);



        MergedInputReader reader = new MergedInputReader(new FileReader(file1),
                new FileReader(file2));
        char[] buf = new char[25000];

        // Act
        int read = reader.read(buf, 0, 25000);

        // Assert
        assertEquals(expected_read, read);
        assertArrayEquals(expected_buf, buf);
    }

}