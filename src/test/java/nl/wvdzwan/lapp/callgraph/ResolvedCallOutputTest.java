package nl.wvdzwan.lapp.callgraph;

import java.io.IOException;
import java.io.Writer;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_calls.ResolvedCallOutput;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ResolvedCallOutputTest {


    @Test
    void testIOException() {

        ResolvedCallOutput output = new ResolvedCallOutput(new IOExceptionOnFlushWriter());

        boolean result = output.export(new DefaultDirectedGraph<>(Edge.class));

        assertFalse(result);

    }

    class IOExceptionOnFlushWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // Ignore
        }

        @Override
        public void flush() throws IOException {
            throw new IOException();
        }

        @Override
        public void close() throws IOException {
            // Do nothing
        }
    }
}
