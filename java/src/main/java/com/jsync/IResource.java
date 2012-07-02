package com.jsync;

import java.io.*;

/**
 * User: sanders
 * Date: 12/29/11
 */
public interface IResource {
    public IResource[] list();
    public Boolean isDirectory();
    public Boolean isFile();
    public Boolean exists();
    public String getName();
    public IResource join(String name);
    public void mkdirs();
    public OutputStream getWriter();
    public InputStream getReader();
    public String toString();
    public Boolean finishWrite();
    public long lastModified();
    public float getSize();
    public void setSize(float size);
    public void delete();

    // File Properties

    public String getMimeType();
    public void setMimeType(String mimeType);
    public int getUid();
    public void setUid(int uid);
    public int getGid();
    public void setGid(int gid);
    public int getMode();
    public void setMode(int mode);
    public long getModifiedTime();
    public void setModifiedTime(long  modifiedTime);
    public long getAccessTime();
    public void setAccessTime(long accessTime);

    public boolean isSymlink();
    public String getSymlinkTarget();
    public void createSymlink(String target);
}
