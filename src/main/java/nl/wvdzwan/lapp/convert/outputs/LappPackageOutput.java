package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;

import nl.wvdzwan.lapp.protobuf.Lapp;

public interface LappPackageOutput {

    boolean export(OutputStream outputStream, Lapp.Package lappPackage);

}
