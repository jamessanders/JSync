package com.jsync;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.http.client.utils.URIBuilder;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: sanders
 * Date: 12/30/11
 */

public class S3Resource implements IResource {

    URI path;
    List<S3Resource> children;
    Boolean isDirectory;
    S3Client client;
    S3ObjectSummary s3ObjectSummary;
    ObjectMetadata s3MetaData;
    int offset;

    public S3Resource(S3ObjectSummary s3ObjectSummary,
                      URI path,
                      Boolean isDirectory,
                      S3Client client,
                      int offset) {

        this.path = path;
        this.client = client;
        this.s3ObjectSummary = s3ObjectSummary;
        this.isDirectory = isDirectory;
        this.children = null;
        this.s3MetaData = null;
        this.offset = offset;
    }

    private S3Client getClient() { return client; }

    public String getBucket() {
        return this.getClient().getBucket();
    }

    public String getKey() {
        if (s3ObjectSummary != null) {
            return s3ObjectSummary.getKey();
        } else {
            return path.getPath().substring(1);
        }
    }

    public void copy(S3Resource out) {

        this.getClient().copyObject(this, out);

    }

    public String toString () {
        return path.toString();
    }

    private List<S3Resource> getChildren() {
        if (this.children != null) {
            return this.children;
        }
        try {
            this.children = this.getClient().getChildren(this.path.getPath(), this.offset);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return this.children;
    }

    public IResource[] list() {
        List<S3Resource> children = this.getChildren();
        if (children != null) {
            IResource[] ir = new IResource[children.size()];
            children.toArray(ir);
            return ir;
        } else {
            return new IResource[0];
        }
    }

    public Boolean isDirectory() {
        return isDirectory;
    }

    public Boolean isFile() {
        return !isDirectory();
    }

    public Boolean exists() {
        return (s3ObjectSummary != null);
    }

    public String getName() {
        return (new File(path.getPath())).getName();
    }

    public IResource join(String name) {
        URIBuilder builder = new URIBuilder(this.path);
        if (path.getPath().endsWith("/")) {
            builder.setPath(path.getPath() + name);
        } else {
            builder.setPath(path.getPath() + "/" + name);
        }
        for (IResource child : this.list()) {
            try {
                if (builder.build().toString().equals(child.toString())) {
                    return child;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        try {
            return new S3Resource(null,
                    builder.build(),
                    false,
                    client,
                    this.offset);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void mkdirs() {}

    public OutputStream getWriter() {
        return this.getClient().getWriter(this.path, getS3Metadata());
    }

    public InputStream getReader() {
        S3Object obj = this.getClient().getObject(this);
        return obj.getObjectContent();
    }

    public Boolean finishWrite() {
        return this.getClient().cleanupWrites();
    }


    public void delete() {
        this.getClient().deleteObject(this);
    }

    public long lastModified() {
        ObjectMetadata metadata = this.getS3Metadata();
        String mtime = metadata.getUserMetadata().get("mtime");
        if (mtime != null) {
            return (new Date(Long.parseLong(mtime))).getTime()*1000;
        } else if (s3ObjectSummary == null) {
            Date lastModifed = getS3Metadata().getLastModified();
            if (lastModifed != null) {
                System.err.println("Using current time as last modifed");
                return lastModifed.getTime();
            } else {
                return 0;
            }
        } else {
            return s3ObjectSummary.getLastModified().getTime();
        }
    }

    public ObjectMetadata getS3Metadata() {
        if (this.s3MetaData == null) {
            try {
                this.s3MetaData = client.getObjectMetadata(this);
            } catch (Throwable e) {
                this.s3MetaData = new ObjectMetadata();
            }
        }
        return this.s3MetaData;
    }


    public long getSize() {
        if (s3ObjectSummary != null) {
            return s3ObjectSummary.getSize();
        } else {
            return getS3Metadata().getContentLength();
        }
    }

    public void setSize(long size) {
        getS3Metadata().setContentLength(size);
    }

    public String getMimeType() {
        return getS3Metadata().getContentType();
    }

    public void setMimeType(String mimeType) {
        getS3Metadata().setContentType(mimeType);
    }

    private int defaultId(String sid) {
        if (sid != null) {
            return Integer.parseInt(sid);
        } else {
            return -1;
        }
    }
    private long  defaultTime(String sid) {
        if (sid != null) {
            return Long.parseLong(sid);
        } else {
            return -1;
        }
    }
    public int getUid() {
        return defaultId(getS3Metadata().getUserMetadata().get("uid"));
    }

    public void setUid(int uid) {
        getS3Metadata().getUserMetadata().put("uid", Integer.toString(uid));
    }

    public int getGid() {
       return defaultId(getS3Metadata().getUserMetadata().get("gid"));
    }

    public void setGid(int gid) {
        getS3Metadata().getUserMetadata().put("gid", Integer.toString(gid));
    }

    public int getMode() {
        return defaultId(getS3Metadata().getUserMetadata().get("mode"));
    }

    public void setMode(int mode) {
        getS3Metadata().getUserMetadata().put("mode", Integer.toString(mode));
    }

    public long getModifiedTime() {
        return defaultTime(getS3Metadata().getUserMetadata().get("mtime"));
    }

    public void setModifiedTime(long modifiedTime) {
        getS3Metadata().getUserMetadata().put("mtime", Long.toString(modifiedTime));
    }

    public long getAccessTime() {
        return defaultTime(getS3Metadata().getUserMetadata().get("atime"));
    }

    public void setAccessTime(long accessTime) {
        getS3Metadata().getUserMetadata().put("atime", Long.toString(accessTime));
    }

    public boolean isSymlink() {
        String type = getS3Metadata().getUserMetadata().get("type");
        return (type != null && type.equals("link"));
    }

    public String getSymlinkTarget() {
        return getS3Metadata().getUserMetadata().get("link-target").trim();
    }

    public void createSymlink(String target) {
        getS3Metadata().getUserMetadata().put("type", "link");
        getS3Metadata().getUserMetadata().put("link-target", target);
        String content = "Symlink: " + target;
        this.setSize(content.length());
        OutputStream writer = getWriter();
        try {
            writer.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
