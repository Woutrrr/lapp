package nl.wvdzwan.lapp.callgraph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class CallGraphCommandTest {

    @Test
    void requiresMainJarArgument() {

        String[] args = {};

        CommandLine commandLine = new CommandLine(new CallGraphMain());

        Assertions.assertThrows(CommandLine.ParameterException.class, () -> {
            commandLine.parse(args);
        }, "Should have complained about missing parameters.");

    }
}
