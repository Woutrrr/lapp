package nl.wvdzwan.lapp.convert.outputs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.convert.LappClassHierarchy;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

public class JcgOutput implements LappPackageOutput {
    @Override
    public boolean export(OutputStream outputStream, Lapp.Package lappPackage) {

        LappPackage lapp = LappPackageReader.from(lappPackage);
        LappClassHierarchy cha = LappClassHierarchy.make(lapp);

        Map<String, ReachableMethod> reachableMethods = new HashMap<>();

        // First build reachable methods map
        lapp.classRecords.forEach(classRecord -> {
            classRecord.methods.forEach(methodSignature -> {
                ReachableMethod reachableMethod = new ReachableMethod();

                reachableMethod.method = Target.from(classRecord, methodSignature);

                String key = classRecord.name + "." + methodSignature;

                reachableMethods.put(key, reachableMethod);
            });
        });

        // Add all calls except calls resolved by lapp
        lapp.resolvedCalls.stream()
                .filter(call -> call.callType != Call.CallType.RESOLVED_DISPATCH)
                .forEach(call -> {
                    String key = call.source.namespace + "." + call.source.symbol;
                    ReachableMethod reachableMethod = reachableMethods.get(key);

                    Target declaredTarget = Target.from(call.target);
                    CallSite cs = new CallSite();
                    cs.type = call.callType.label;
                    cs.declaredTarget = declaredTarget;


                    cs.targets.add(Target.from(call.target));

                    // Resolve other possible implementations
                    Set<ClassRecord> implementors = cha.getImplementingClasses(call.target.namespace, call.target.symbol);
                    for (ClassRecord cr : implementors) {
                        cs.targets.add(Target.from(cr, call.target.symbol));
                    }

                    // Verify call site doesn't exist, if it does something went wrong
                    String callSiteKey = call.target.namespace + "." + call.target.symbol;
                    if (reachableMethod.callSiteKeys.contains(callSiteKey)) {
                        System.err.println("Call site already exists!");
                        System.err.println(call);
                    } else {
                        reachableMethod.callSites.add(cs);
                    }
                });

        List<ReachableMethod> reachableMethodsList = new ArrayList<>(reachableMethods.values());

        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.disableHtmlEscaping();

            Gson gson = gsonBuilder.create();


            PrintWriter printer = new PrintWriter(outputStream);
            JsonWriter jsonWriter = gson.newJsonWriter(printer);



            gson.toJson(reachableMethodsList, reachableMethodsList.getClass(), jsonWriter);
            printer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    static class ReachableMethod {
        Target method;
        List<CallSite> callSites = new ArrayList<>();
        Set<String> callSiteKeys = new HashSet<>();
    }

    static class CallSite {
        Target declaredTarget;
        String type;
        int line;
        int pc;
        List<Target> targets = new ArrayList<Target>();

        static CallSite from(Call call) {
            CallSite callSite = new CallSite();

            callSite.declaredTarget = Target.from(call.source);
            return callSite;
        }
    }

    static class Target {
        String name;
        String declaringClass;
        String returnType;
        String[] parameterTypes;


        static Target from(ClassRecord classRecord, String methodSignature) {
            return from(classRecord.name, methodSignature);
        }

        static Target from(Method method) {
            return from(method.namespace, method.symbol);
        }

        static Target from(String declaringClass, String signature) {
            Target target = new Target();

            target.declaringClass = "L" + declaringClass.replace(".", "/") + ";";

            int parenthesisOpen = signature.indexOf("(");
            int parenthesisClose = signature.indexOf(")");

            target.name = signature.substring(0, parenthesisOpen);
            if (parenthesisOpen + 1 == parenthesisClose) {
                target.parameterTypes = new String[]{};
            } else {
                target.parameterTypes = signature.substring(parenthesisOpen + 1, parenthesisClose).split(";");
            }
            target.returnType = signature.substring(parenthesisClose+1);

            return target;
        }
    }


//
//    class LappPackageAdapter implements JsonSerializer<Lapp.Package> {
//
//        @Override
//        public JsonElement serialize(Lapp.Package src, Type typeOfSrc, JsonSerializationContext context) {
//
//            List<Lapp.Call> resolvedCalls = src.getResolvedCallsList();
//            JsonArray reachableMethods = context.serialize()
//
//            JsonObject element = new JsonObject();
//
//            element.add("reachableMethods", );
//
//            return null;
//        }
//    }

}
