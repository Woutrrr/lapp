package nl.wvdzwan.lapp.convert.outputs;

import java.io.IOException;
import java.io.OutputStream;

import nl.wvdzwan.lapp.protobuf.Lapp;

public class ProtobufOutput implements LappPackageOutput {

    @Override
    public boolean export(OutputStream outputStream, Lapp.Package lappPackage) {

        try {
            lappPackage.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
