package com.jsync;

import java.io.IOException;

public class JSync {
    static {
        if (System.getenv("LIBJSYNC_PATH") != null) {
            System.setProperty("jna.library.path", System.getenv("LIBJSYNC_PATH"));
        }
    }

    public static void exitWithHelp(JSyncOptions options, int code) {
        System.err.println("");
        System.err.println("Usage: jsync [options] <input_path> <output_path>");
        System.err.println("");
        System.err.println("Options: ");
        options.showHelp();
        System.exit(code);
    }

    public static void main(String[] args) throws IOException {

        JSyncOptions options = new JSyncOptions(args);

        if (options.needsHelp()) {
            exitWithHelp(options, 0);
        }

        ResourceGenerator generator =
                new ResourceGenerator(options);

        IResource in = null;
        IResource out = null;
        try {
            in = generator.getInputResource();
            out = generator.getOutputResource();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("No input or output directory specified");
            exitWithHelp(options, 1);
        }
        IResource back = generator.getBackupResource();

        ResourceCopier.copyDirectory(in, out, back);

        if (options.doDelete()) {
            ResourceCopier.performDeletions(in, out, back);
        }

    }
}

