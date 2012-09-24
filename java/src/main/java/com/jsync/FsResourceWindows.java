package com.jsync;

import java.io.File;
import java.util.ArrayList;

/**
 * User: sanders
 * Date: 9/23/12
 */
public class FsResourceWindows extends FsResource {

    public FsResourceWindows(File file) {
        super(file);
    }
    public FsResourceWindows(String fn) {
        super(new File(fn));
    }

    @Override
    public IResource[] list() {
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files == null) {
                return new FsResourceWindows[0];
            } else {
                ArrayList<FsResourceWindows> resultList;
                resultList = new ArrayList<FsResourceWindows>();
                for (String child: files) {
                    File resource = new File(file, child);
                    resultList.add(new FsResourceWindows(resource));
                }
                FsResourceWindows[] out = new FsResourceWindows[resultList.size()];
                return resultList.toArray(out);
            }
        } else if (file.isFile()) {
            return new FsResourceWindows[0];
        }
        return null;
    }

    @Override
    public FsResource join(String name) {
        return new FsResourceWindows(new File(file, name));
    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public int getMode() {
        return 0;
    }

    @Override
    public void setGid(int gid) {
    }

    @Override
    public int getGid() {
        return 0;
    }

    @Override
    public void setUid(int uid) {
    }

    @Override
    public int getUid() {
        return 0;
    }

    @Override
    public void setMode(int mode) {

    }

    @Override
    public void createSymlink(String target) {

    }

    @Override
    public long getAccessTime() {
        return 0;
    }

    @Override
    public void setAccessTime(long accessTime) {

    }

    @Override
    public long getModifiedTime() {
        return 0;
    }

    @Override
    public void setModifiedTime(long modifiedTime) {

    }

    @Override
    public String getSymlinkTarget() {
        return null;
    }
}
