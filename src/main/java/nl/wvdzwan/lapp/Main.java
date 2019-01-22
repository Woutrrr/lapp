package nl.wvdzwan.lapp;

import java.util.concurrent.Callable;

import picocli.CommandLine;

import nl.wvdzwan.lapp.IRDotMerger.IRDotMerger;
import nl.wvdzwan.lapp.analyse.AnalyseCommand;
import nl.wvdzwan.lapp.filter.FilterMain;

@CommandLine.Command(
        name = "lapp",
        descriptionHeading = "%n",
        description = "Lapp is a tool to iteratively generate a call graphs for maven artifacts for a specified moment in time.",
        optionListHeading = "%n",
        mixinStandardHelpOptions = true,
        commandListHeading = "%nCommands:%n",
        version = "0.1",
        subcommands = {
                nl.wvdzwan.lapp.resolver.Main.class,
                nl.wvdzwan.lapp.callgraph.CallGraphMain.class,
                AnalyseCommand.class,
                IRDotMerger.class,
                FilterMain.class
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
