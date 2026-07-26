package dev.elinor.dupfind;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "dupfind", mixinStandardHelpOptions = true, version = "0.1.0",
        subcommands = { ScanCommand.class })
public class Main implements Runnable {
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override public void run() {
        CommandLine.usage(this, System.out);
    }
}