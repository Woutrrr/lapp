package nl.wvdzwan.lapp.split;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nl.wvdzwan.lapp.Main;

public class RunnerMain {

    public static long run(String target, String output) {
        long before = System.nanoTime();
        long after = 0;
        try {
            Map<String, List<String>> entryGroups = SplitMain.split(target);
System.out.println(target);
System.out.println(output);
System.out.println(entryGroups);
            for (Map.Entry<String, List<String>> entry : entryGroups.entrySet()) {

                List<String> args = new ArrayList<>(Arrays.asList(
                        "callgraph",
                        "-o", entry.getKey() + ".buf"
                ));
                args.addAll(entry.getValue());

                System.out.println(args);
                Main.main(args.toArray(new String[]{}));

            }



            List<String> ir_buffers = entryGroups.keySet().stream().map(s -> s + ".buf").collect(Collectors.toList());

            List<String> args_merge = new ArrayList<>(Arrays.asList(
                    "merge",
                    "merged.buf"
            ));
            args_merge.addAll(ir_buffers);
            System.out.println(args_merge);
            Main.main(args_merge.toArray(new String[]{}));


            List<String> flatArgs = new ArrayList<>(Arrays.asList(
                    "flatten",
                    "merged.buf",
                    "lapp_flattened.buf"
            ));
            System.out.println(flatArgs);
            Main.main(flatArgs.toArray(new String[]{}));

            after = System.nanoTime();

            // Convert
            List<String> convertArgs = new ArrayList<>(Arrays.asList(
                    "convert",
                    "jcg",
                    "lapp_flattened.buf",
                    output
            ));
            System.out.println(convertArgs);
            Main.main(convertArgs.toArray(new String[]{}));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return after-before;

    }

    public static void main(String[] args) {
        run(args[0], args[1]);
    }
}
