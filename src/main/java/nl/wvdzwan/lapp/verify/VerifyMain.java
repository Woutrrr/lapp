package nl.wvdzwan.lapp.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import picocli.CommandLine;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.core.ExpectedCall;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;


@CommandLine.Command(
        name = "verify",
        description = "Verify lapp package for contained assertions"
)
public class VerifyMain implements Callable<Void> {
    protected static final Logger logger = LogManager.getLogger();

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "source",
            description = "Input to verify"
    )
    private String inputFile;

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "destination",
            description = "Location to save the result"
    )
    private File outputFile;

    enum Result {
        SOUND,
        UNSOUND
    }

    private Graph<Method, Call> graph;
    private LappPackage lappPackage;
    @Override
    public Void call() throws Exception {

        //
        // Prepare input
        //
        Lapp.Package lappProto;
        try {
            lappProto = Lapp.Package.parseFrom(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
            logger.error("File {} not found!", inputFile);
            return null;
        } catch (IOException e) {
            logger.error("Error parsing {}, are you sure it is a LappPackage file?", inputFile);
            return null;
        }

        this.graph = LappPackageTransformer.toGraph(lappProto);
        this.lappPackage = LappPackageReader.from(lappProto);
        List<ExpectedCall> expectedCalls = lappPackage.classRecords.stream()
                .flatMap(classRecord -> classRecord.expectedCalls.stream())
                .collect(Collectors.toList());

        if (expectedCalls.size() == 0) {
            logger.warn("{}: No expected edges defined!", inputFile);
            writeResult(Result.UNSOUND);
            return null;
        }

        for (ExpectedCall assertion : expectedCalls) {
            logger.info("Finding {}", assertion.toString());
            if (!findExpectedCall(assertion)) {
                logger.info("Didn't find expected call, call graph is unsound");
                writeResult(Result.UNSOUND);
                return null;
            }
        }

        logger.info("Found all expected calls");
        writeResult(Result.SOUND);

        return null;
    }

    public boolean findExpectedCall(ExpectedCall assertion) throws FileNotFoundException, VertexNotFoundException {

        Method resolvedSource = lappPackage.resolvedCalls.parallelStream()
                .filter(call -> {
                    return call.source.namespace.equals(assertion.source.namespace)
                            && call.source.symbol.equals(assertion.source.symbol);
                }).findAny().map(call -> call.source).orElse(null);
        if (resolvedSource == null) {
            logger.warn("Expected call source not found");
            return false;
        }

        Method resolvedTarget = graph.vertexSet().parallelStream()
                .filter(vertex -> {
                    return vertex.namespace.equals(assertion.target.namespace)
                            && vertex.symbol.equals(assertion.target.symbol);
                }).findAny().orElse(null);
        if (resolvedTarget == null) {
            logger.warn("Expected call target not found");
            return false;
        }

        int c = 0;

        Set<Method> done = new HashSet<>();
        Deque<Method> workset = new LinkedList<>();

        workset.offerLast(resolvedSource);

        while (!workset.isEmpty()) {

            if (c%100 == 0) {
                logger.info("Loop: {} Done: {} Worklist: {}", c, done.size(), workset.size());
            }
            c++;

            Method workItem = workset.poll();
            if (done.contains(workItem)) { continue; }
            done.add(workItem);

            if (workItem.equals(resolvedTarget)) {
                logger.info("Found path from source to target");
                return true;
            }

            List<Method> nextItems = graph.outgoingEdgesOf(workItem).stream()
                    .map(call -> call.target)
                    .filter(method -> !done.contains(method))
                    .collect(Collectors.toList());

            workset.addAll(nextItems);
        }

        logger.info("No path from source to target in call graph found");
        return false;
    }

    private void writeResult(Result result) throws FileNotFoundException {

        if (!outputFile.getAbsoluteFile().getParentFile().exists()) {
            outputFile.getAbsoluteFile().getParentFile().mkdirs();
        }

        PrintStream ps = new PrintStream(outputFile);
        String resultText = result == Result.SOUND ? "sound" : "unsound";

        ps.println(inputFile +": " + resultText);
        ps.close();
    }

    public static void main(String[] args) throws Exception {

        VerifyMain callable = new VerifyMain();

        if(args.length < 2) {
            logger.error("Not enough parameters! Required parameters: [source] [results_file]");
            return;
        }

        callable.inputFile = args[0];
        callable.outputFile = new File(args[1]);

        callable.call();
    }

    class VertexNotFoundException extends Exception {

    }

}
