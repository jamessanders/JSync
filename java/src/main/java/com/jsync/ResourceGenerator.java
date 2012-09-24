package com.jsync;

import com.amazonaws.auth.AWSCredentials;
import org.apache.commons.lang3.SystemUtils;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: sanders
 * Date: 12/30/11
 */
public class ResourceGenerator {

    AWSCredentials awsCreds;
    JSyncOptions options;

    public ResourceGenerator(JSyncOptions options) {
        this.options = options;
    }

    public IResource getOutputResource() {
        return generateResource(options.getOutputPath());
    }

    public IResource getInputResource() {
        return generateResource(options.getInputPath());
    }

    public IResource getBackupResource() {
        String backupPath = options.getBackupPath();
        if (backupPath !=  null) {
            return generateResource(backupPath);
        } else {
            return null;
        }
    }

    public IResource generateResource(String path) {
        URI uri;
        try {
            uri = new URI(path);
        } catch (URISyntaxException e) {
            if (SystemUtils.IS_OS_WINDOWS) {
                return new FsResourceWindows(path);
            } else {
                return new FsResource(path);
            }
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            return new FsResource(uri.getPath());
        } else if (scheme.equals("s3")) {
            S3ResourceGenerator s3generator = new S3ResourceGenerator(options.getAwsCredentials(), path, options.useReducedRedundancy());
            try {
                return s3generator.getResource(path);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
