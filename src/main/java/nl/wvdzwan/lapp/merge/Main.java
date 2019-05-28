package nl.wvdzwan.lapp.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import com.google.protobuf.util.JsonFormat;

import nl.wvdzwan.lapp.protobuf.Lapp;

public class Main {

    public static void main(String[] args) throws IOException {
        String file = "output/app/app.buf";
        String file2 = "output/core/app.buf";

        // Read the existing address book.
        Lapp.Package lappPackage =
                Lapp.Package.parseFrom(new FileInputStream(file));

        Lapp.Package lappPackage2 =
                Lapp.Package.parseFrom(new FileInputStream(file2));

        LappPackageMerger merger = new LappPackageMerger();

        Lapp.Package newPackage = merger.add(lappPackage)
                .add(lappPackage2)
                .merge();

        newPackage.writeTo(new FileOutputStream(new File("output/merged", "merged.buf")));
        String json = JsonFormat.printer().print(newPackage);
        PrintWriter printer = new PrintWriter(new FileOutputStream(new File("output/merged", "merged.json")));

        printer.println(json);
        printer.flush();

        printPackageStatistics(lappPackage);
        printPackageStatistics(lappPackage2);
        printPackageStatistics(newPackage);

    }

    static void printPackageStatistics(Lapp.Package p) {

        System.out.printf("Number of artifacts: %s  (%s) \n", p.getArtifactsCount(), p.getArtifactsList().stream().map(a -> a.getGroup() +":" + a.getName() + ":" + a.getVersion()).collect(Collectors.toList()));
        System.out.printf("Methods: %d\n", p.getMethodsCount());
        System.out.printf("Calls: Resolved: %6d    Unresolved: %6d\n", p.getResolvedCallsCount(), p.getUnresolvedCallsCount());
        System.out.printf("Cha:   Resolved: %6d    Unresolved: %6d\n", p.getChaCount(), p.getUnresolvedChaCount());
        System.out.println("----");
    }
}
