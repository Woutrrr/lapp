package nl.wvdzwan.lapp.core;

import java.util.ArrayList;
import java.util.List;

public class ClassRecord {

    public final String artifact;
    public final String name;

    public boolean isPublic;
    public boolean isPrivate;
    public boolean isInterface;
    public boolean isAbstract;



    public String superClass;
    public final List<String> interfaces = new ArrayList<>();
    public final List<String> methods = new ArrayList<>();


    public ClassRecord(String artifact, String reference) {
        this.artifact = artifact;
        this.name = reference;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public void addInterface(String directInterface) {
        this.interfaces.add(directInterface);
    }

    public void addMethod(String method) {
        this.methods.add(method);
    }

}
