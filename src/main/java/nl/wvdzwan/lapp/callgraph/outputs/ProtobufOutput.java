package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.IOException;
import java.io.OutputStream;

import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.Protobuf;

public class ProtobufOutput implements LappPackageOutput {


    private final OutputStream output;

    public ProtobufOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    public boolean export(LappPackage lappPackage) {

        Lapp.Package protobuf = Protobuf.of(lappPackage);

        try {
            protobuf.writeTo(output);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
