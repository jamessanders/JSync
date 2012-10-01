package com.jsync;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.util.List;

public class JSyncOptions {

    OptionParser parser;
    OptionSet options;
    List<String> arguments;

    public JSyncOptions(String[] args) {

        parser = new OptionParser() {
            {
                accepts("aws", "aws credentials (seperated by ':')").withRequiredArg().describedAs( "credentials" );
                accepts("delete", "delete files at destination that do not exists in the source dir");
                accepts("reduced-redundancy", "use reduced redundancy when uploading files to S3");
                accepts("rr-storage", "same as --reduced-redundancy");
                accepts("verbose",  "be verbose");
                accepts("help", "show this help");
                accepts("backup", "backup any modified or deleted files" ).withRequiredArg().describedAs("path");
            }
        };
        options = parser.parse(args);
        arguments = options.nonOptionArguments();
    }

    public void showHelp() {
        try {
            parser.printHelpOn(System.err);
        } catch (IOException e) {

        }
    }

    public boolean needsHelp() {
        return options.has("help");
    }

    public boolean doDelete() {
        return options.has("delete");
    }

    public boolean useReducedRedundancy() {
        return (options.has("reduced-redundancy") || options.has("rr-storage"));
    }

    public String getBackupPath() {
        return (String) options.valueOf("backup");
    }

    public String getInputPath() {
        return arguments.get(0);
    }

    public String getOutputPath() {
        return arguments.get(1);
    }

    public boolean doVerboseMode() {
        return (options.has("verbose"));
    }

    public AWSCredentials getAwsCredentials() {
        String awsCredentialsOption = (String) options.valueOf("aws");
        String[] awsc;
        if (awsCredentialsOption == null) {
            awsc = JSyncOptions.getAwsCredentialsFromConfig();
        } else {
            awsc = awsCredentialsOption.split(":");
        }
        if (awsc != null) {
            return new BasicAWSCredentials(awsc[0], awsc[1]);
        }
        return null;
    }

    public static String[] getAwsCredentialsFromConfig() {
        BufferedReader reader;
        String[] creds = new String[2];
        File configPath = new File(System.getProperty("user.home"),".awssecret");
        try {
            reader = new BufferedReader(new FileReader(configPath));
        } catch (FileNotFoundException e) {
            return null;
        }
        try {
            creds[0] = reader.readLine();
            creds[1] = reader.readLine();
        } catch (IOException e) {
            return null;
        }
        return creds;
    }
}
