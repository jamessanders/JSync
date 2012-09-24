package com.jsync;

import java.io.IOException;

public class JSync {
    static {
        if (System.getenv("LIBJSYNC_PATH") != null) {
            System.setProperty("jna.library.path", System.getenv("LIBJSYNC_PATH"));
        }
    }
    public static void main(String[] args) throws IOException {

        JSyncOptions options = new JSyncOptions(args);

        System.out.println(" + Generating Resources");
        ResourceGenerator generator =
                new ResourceGenerator(options);

        System.out.println(" + Starting Copy");

        IResource in = generator.getInputResource();
        IResource out = generator.getOutputResource();
        IResource back = generator.getBackupResource();

        ResourceCopier.copyDirectory(in, out, back);

        if (options.doDelete()) {
            ResourceCopier.performDeletions(in, out, back);
        }

    }
}

