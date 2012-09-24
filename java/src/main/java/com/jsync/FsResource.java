package com.jsync;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;

public class FsResource implements IResource {

    private class FileProperties {
        int uid;
        int gid;
        int mode;
        long atime;
        long mtime;
        public FileProperties() {
            uid = -1;
            gid = -1;
            mode = -1;
            atime = -1;
            mtime = -1;
        }
    }

    File file;
    FileProperties props;

    public FsResource(File file) {
        this.file = file;
        this.props = null;
    }
    public FsResource(String fn) {
        this(new File(fn));
    }

    private FileProperties getProperties() {
        if (props == null) {
            props = new FileProperties();
            if (exists()) {
                JSyncPosixTools.ShortStat stat = JSyncPosixTools.stat(file.getPath());
                props.uid = stat.uid;
                props.gid = stat.gid;
                props.mode = stat.mode;
                props.mtime = stat.mtime.longValue();
                props.atime = stat.atime.longValue();
            }
        }
        return props;

    }

    public Boolean isDirectory() {
        return file.isDirectory();
    }

    public Boolean isFile() {
        return file.isFile();
    }

    public Boolean exists() {
        return file.exists();
    }

    public void mkdirs() {
        if (isDirectory()) {
            file.mkdirs();
        } else {
            File parent = new File(file.getParent());
            parent.mkdirs();
        }
    }

    public String getName() {
        return file.getName();
    }

    public FsResource join(String name) {
        return new FsResource(new File(file, name));
    }

    public IResource[] list() {
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files == null) {
                return new FsResource[0];
            } else {
                ArrayList<FsResource> resultList;
                resultList = new ArrayList<FsResource>();
                for (String child: files) {
                    File resource = new File(file, child);
                    resultList.add(new FsResource(resource));
                }
                FsResource[] out = new FsResource[resultList.size()];
                return resultList.toArray(out);
            }
        } else if (file.isFile()) {
            return new FsResource[0];
        }
        return null;
    }

    public OutputStream getWriter()  {
        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getReader() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public String toString() {
        return file.toString();
    }

    public Boolean finishWrite() {
        if (this.props != null) {
            if (props.uid != -1 && props.gid != -1) {
                JSyncPosixTools.chown(file.getPath(), props.uid, props.gid);
            }
            if (props.mode != -1) {
                JSyncPosixTools.chmod(file.getPath(), props.mode);
            }
            if (props.atime != -1 && props.mtime != -1){
                JSyncPosixTools.setTimes(
                        file.getPath(),
                        props.atime,
                        props.mtime);
            }
        }
        return true;
    }

    public void delete() {
        file.delete();
    }

    public long lastModified () {
        return file.lastModified();
    }

    public long getSize() {
        return file.length();
    }

    public void setSize(long size) {

    }

    public String getMimeType() {
        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public void setMimeType(String mimeType) {

    }

    public int getUid() {
        return getProperties().uid;
    }

    public void setUid(int uid) {
        getProperties();
        props.uid = uid;
    }

    public int getGid() {
        return getProperties().gid;
    }

    public void setGid(int gid) {
        getProperties();
        props.gid = gid;
    }

    public int getMode() {
        return getProperties().mode;
    }

    public void setMode(int mode) {
        getProperties();
        props.mode = mode;
    }

    public long getModifiedTime() {
        return getProperties().mtime;
    }

    public void setModifiedTime(long modifiedTime) {
        getProperties();
        props.mtime = modifiedTime;
    }

    public long getAccessTime() {
        return getProperties().atime;
    }

    public void setAccessTime(long accessTime) {
        getProperties();
        props.atime = accessTime;
    }

    public boolean isSymlink() {
        return (JSyncPosixTools.getLinkTarget(file.getPath()) != null);
    }

    public String getSymlinkTarget() {
        return JSyncPosixTools.getLinkTarget(file.getPath());
    }

    public void createSymlink(String target) {
        JSyncPosixTools.createSymlink(this.file.getPath(), target);
    }
}
