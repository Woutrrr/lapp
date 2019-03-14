package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import nl.wvdzwan.lapp.LappPackage;
import nl.wvdzwan.lapp.Protobuf;
import nl.wvdzwan.lapp.protos.LappProtos;

public class JsonOutput implements LappPackageOutput {


    private final OutputStream output;

    public JsonOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    public boolean export(LappPackage lappPackage) {

        LappProtos.Package protobuf = Protobuf.of(lappPackage);

        try {
            String json = JsonFormat.printer().print(protobuf);
            PrintWriter printer = new PrintWriter(output);

            printer.println(json);
            printer.flush();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
