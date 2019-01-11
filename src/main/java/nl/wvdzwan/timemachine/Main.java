package nl.wvdzwan.timemachine;

import java.util.concurrent.Callable;

import picocli.CommandLine;

import nl.wvdzwan.timemachine.IRDotMerger.IRDotMerger;
import nl.wvdzwan.timemachine.filter.FilterMain;

@CommandLine.Command(
        name = "timemachine",
        descriptionHeading = "%n",
        description = "Timemachine is a tool to iteratively generate a call graphs for maven artifacts for a specified moment in time.",
        optionListHeading = "%n",
        mixinStandardHelpOptions = true,
        commandListHeading = "%nCommands:%n",
        version = "0.1",
        subcommands = {
                nl.wvdzwan.timemachine.resolver.Main.class,
                nl.wvdzwan.timemachine.callgraph.CallGraphMain.class,
                IRDotMerger.class
        }
)
public class Main implements Callable<Void> {

    public static void main(String[] args) {

        CommandLine commandLine = new CommandLine(new Main());

        commandLine.parseWithHandler(new CommandLine.RunLast(), args);

    }

    @Override
    public Void call() {
        CommandLine.usage(new Main(), System.out);

        return null;
    }
}
