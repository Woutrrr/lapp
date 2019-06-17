package nl.wvdzwan.lapp.core;

import com.ibm.wala.types.TypeReference;

public class Util {

    public static String typeReferenceToNamespace(TypeReference typeReference) {
        return typeReference.getName().toString().substring(1).replace('/', '.');

    }
}
