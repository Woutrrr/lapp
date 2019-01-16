package nl.wvdzwan.lapp.resolver.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.resolution.DependencyResult;

import nl.wvdzwan.lapp.resolver.util.SimpleDependencyTreePrinter;

public class DependencyTreeWriterOutput implements ResolveOutputTask {
    protected static final Logger logger = LogManager.getLogger(DependencyTreeWriterOutput.class.getSimpleName());

    private final File outputFile;

    public DependencyTreeWriterOutput(File outputFile) {
        this.outputFile = outputFile;
    }


    @Override
    public boolean makeOutput(DependencyResult result) {

        PrintStream printStream;
        try {
            printStream = new PrintStream(this.outputFile);
        } catch (FileNotFoundException e) {
            logger.warn("Output file {} not found, failing output task", this.outputFile);
            return false;
        }

        return result.getRoot().accept(new SimpleDependencyTreePrinter(printStream));
    }
}
