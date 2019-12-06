package nl.wvdzwan.lapp.flatten;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.convert.LappClassHierarchy;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.Util;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;
import nl.wvdzwan.lapp.protobuf.Protobuf;

import static nl.wvdzwan.lapp.call.Call.CallType.RESOLVED_DISPATCH;


@CommandLine.Command(
        name = "flatten",
        description = "Convert LappPackage to a different format"
)
public class FlattenMain implements Callable<Void> {
    protected static final Logger logger = LogManager.getLogger();

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "source",
            description = "Input to verify"
    )
    private File inputFile;

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "destination",
            description = "Location to save the result"
    )
    private File outputFile;



    public FlattenMain() {
    }

    @Override
    public Void call() throws Exception {

        //
        // Prepare input
        //
        Lapp.Package lappProto;
        try {
            lappProto = Lapp.Package.parseFrom(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
            logger.error("Input file {} not found!", inputFile);
            return null;
        } catch (IOException e) {
            logger.error("Error parsing file, are you sure it is a LappPackage file?");
            return null;
        }
        LappPackage lappPackage = LappPackageReader.from(lappProto);


        LappClassHierarchy cha = LappClassHierarchy.make(lappPackage);

        Set<Call> flattenedCalls = lappPackage.resolvedCalls.stream()
                .flatMap(c -> {
                    List<Call> newCalls = new ArrayList<>();
                    if (c.callType == Call.CallType.INTERFACE || c.callType == Call.CallType.VIRTUAL) {
                        Set<ClassRecord> implementors = cha.getImplementingClasses(c.target.namespace, c.target.symbol);

                        for (ClassRecord cr : implementors) {
                            ResolvedMethod dynamic_target = ResolvedMethod.findOrCreate(cr.name, c.target.symbol, cr.artifact);
                            if (c.source.toID().equals(dynamic_target.toID())) {
                                continue;
                            }
                            newCalls.add(new Call(c.source, dynamic_target, RESOLVED_DISPATCH, c.lineNumber, c.programCounter));
                        }
                    }
                    return newCalls.stream();
                }).collect(Collectors.toSet());

        lappPackage.resolvedCalls.addAll(flattenedCalls);

        Lapp.Package lappProtoNew = Protobuf.of(lappPackage);
        Util.saveProtoToFile(lappProtoNew, outputFile);


        return null;
    }


    public static void main(String[] args) throws Exception {

        FlattenMain callable = new FlattenMain();

        if(args.length < 2) {
            logger.error("Not enough parameters! Required parameters: [input] [output]");
            return;
        }

        callable.inputFile = new File(args[0]);
        callable.outputFile = new File(args[1]);

        callable.call();
    }

}
