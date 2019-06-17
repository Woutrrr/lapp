package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import nl.wvdzwan.lapp.protobuf.Lapp;

public class JsonOutput implements LappPackageOutput {
    @Override
    public boolean export(OutputStream outputStream, Lapp.Package lappPackage) {

        try {
            String json = JsonFormat.printer().print(lappPackage);
            PrintWriter printer = new PrintWriter(outputStream);

            printer.println(json);
            printer.flush();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
