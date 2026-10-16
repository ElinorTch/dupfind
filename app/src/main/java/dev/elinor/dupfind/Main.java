package dev.elinor.dupfind;

import picocli.CommandLine;
import picocli.CommandLine.Command;


public class Main implements Runnable {
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override public void run() {
        CommandLine.usage(this, System.out);
    }
}