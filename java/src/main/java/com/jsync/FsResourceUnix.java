package com.jsync;

import java.io.File;

/**
 * User: sanders
 * Date: 9/23/12
 */
public class FsResourceUnix extends FsResource {

    public FsResourceUnix(File file) {
        super(file);
    }
    public FsResourceUnix(String fn) {
        super(new File(fn));
    }

}
