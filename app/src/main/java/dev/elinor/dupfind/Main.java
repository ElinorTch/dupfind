package dev.elinor.dupfind;

import picocli.CommandLine;

public class Main implements Runnable {
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override public void run() {
        CommandLine.usage(this, System.out);
    }
}