package nl.wvdzwan.lapp.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.convert.outputs.*;
import nl.wvdzwan.lapp.protobuf.Lapp;


@CommandLine.Command(
        name = "convert",
        description = "Convert LappPackage to a different format"
)
public class ConvertMain implements Callable<Void> {
    protected static final Logger logger = LogManager.getLogger();

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "format",
            description = "Format to convert file to"
    )
    private String type;

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "source",
            description = "Input to convert"
    )
    private String inputFile;

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "destination",
            description = "Location to save the result"
    )
    private String outputFile;

    private Map<String, Class<? extends LappPackageOutput>> types = new HashMap<>();

    public ConvertMain() {
        types.put("json", JsonOutput.class);
        types.put("proto", ProtobufOutput.class);
        types.put("xdot", HumanReadableDotGraph.class);
        types.put("udot", UnifiedCallGraphExport.class);
        types.put("chadot", ClassHierarchyDotGraph.class);
        types.put("resolved", ResolvedDispatchCallGraphExport.class);
        types.put("jcg", JcgOutput.class);
    }

    @Override
    public Void call() {

        //
        // Prepare input
        //
        Lapp.Package lappPackage;
        try {
            lappPackage = Lapp.Package.parseFrom(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
            logger.error("File {} not found!", inputFile);
            return null;
        } catch (IOException e) {
            logger.error("Error parsing file, are you sure it is a LappPackage file?");
            return null;
        }

        //
        // Prepare output
        //
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(new File(outputFile));
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage());
            return null;
        }

        // Prepare converter
        Class<? extends LappPackageOutput> outputClass = types.get(type);
        if (outputClass == null) {
            logger.error("Unknown format, available options are {}", types.keySet());
            return null;
        }

        //
        // Convert
        //
        LappPackageOutput converter = null;
        try {
            converter = outputClass.newInstance();
            converter.export(outputStream, lappPackage);
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Could not build converter type");
            logger.error(e.getMessage());
            return null;
        }

        return null;
    }

    private LappPackageOutput getInstance(Class<? extends LappPackageOutput> klass, OutputStream output) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends LappPackageOutput> constructor = klass.getConstructor(OutputStream.class);
        return constructor.newInstance(output);
    }

    public static void main(String[] args) {

        ConvertMain callable = new ConvertMain();

        if(args.length < 3) {
            logger.error("Not enough parameters! Required parameters: [type] [source] [destination]");
            return;
        }

        callable.type = args[0];
        callable.inputFile = args[1];
        callable.outputFile = args[2];

        callable.call();
    }


}
