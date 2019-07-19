package nl.wvdzwan.lapp.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.protobuf.Lapp;

@CommandLine.Command(
        name = "merge",
        description = "Merge two or more intermediate lapp files"
)
public class MergeMain implements Callable<Void> {
    private static final Logger logger = LogManager.getLogger();

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "Output file",
            description = "File to save merge result"
    )
    private File output;

    @CommandLine.Parameters(
            index = "1..*",
            arity = "0..*",
            paramLabel = "files",
            description = "Files to merge"
    )
    private ArrayList<String> files = new ArrayList<>();

    public Void call() throws Exception {

        LappPackageMerger merger = new LappPackageMerger();

        for(String file : files) {
            File f = new File(file);
            if (!(f.isFile() || f.canRead())) {
                logger.error("File not readable", () -> file);
                continue;
            }

            Lapp.Package lappPackage =
                    Lapp.Package.parseFrom(new FileInputStream(file));
            printPackageStatistics(lappPackage);
            merger.add(lappPackage);
        }



        Lapp.Package newPackage = merger.merge();

        newPackage.writeTo(new FileOutputStream(output));

        printPackageStatistics(newPackage);

        return null;
    }

    static void printPackageStatistics(Lapp.Package p) {

        System.out.printf("Number of artifacts: %s  (%s) \n", p.getArtifactsCount(), p.getArtifactsList());
        System.out.printf("Classes: %d\n", p.getClassRecordsCount());
        System.out.printf("Calls: Resolved: %6d    Unresolved: %6d\n", p.getResolvedCallsCount(), p.getUnresolvedCallsCount());
        System.out.println("----");
    }
}
