package nl.wvdzwan.lapp.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.ibm.wala.types.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.convert.outputs.ProtobufOutput;
import nl.wvdzwan.lapp.protobuf.Lapp;

public class Util {
    private static final Logger logger = LogManager.getLogger();


    public static String typeReferenceToNamespace(TypeReference typeReference) {
        return typeReferenceStringToNamespace(typeReference.getName().toString());
    }
    public static String typeReferenceStringToNamespace(String typeString) {
        return typeString.substring(1).replace('/', '.');
    }

    public static void saveProtoToFile(Lapp.Package proto, File output) {
        try {
            if (!output.getAbsoluteFile().getParentFile().exists()) {
                output.getAbsoluteFile().getParentFile().mkdirs();
            }

            OutputStream outputStream = new FileOutputStream(output);

            ProtobufOutput protobufOutput = new ProtobufOutput();
            protobufOutput.export(outputStream, proto);
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage());
        }
    }
}
