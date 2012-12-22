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

    String bucket;

    URI path;
    List<S3Resource> children;
    Boolean isDirectory;
    AmazonS3Client client;
    S3ResourceGenerator s3ResourceGenerator;
    S3ObjectSummary s3ObjectSummary;
    ObjectMetadata s3MetaData;
    boolean useReducedRedundancy;
    int offset;

    public S3Resource(S3ObjectSummary s3ObjectSummary,
                      String bucket,
                      String path,
                      List<S3Resource> children,
                      Boolean isDirectory,
                      S3ResourceGenerator s3ResourceGenerator,
                      AmazonS3Client client,
                      boolean useReducedRedundacy,
                      int offset) {
        try {
            this.path = new URI(path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.s3ObjectSummary = s3ObjectSummary;
        this.bucket = bucket;
        this.isDirectory = isDirectory;
        this.client = client;
        this.children = children;
        this.s3ResourceGenerator = s3ResourceGenerator;
        this.s3MetaData = null;
        this.useReducedRedundancy = useReducedRedundacy;
        this.offset = offset;
    }

    public AmazonS3Client getClient() { return client; }
    public String getBucket() { return bucket; }
    public String getKey() {
        if (s3ObjectSummary != null) {
            return s3ObjectSummary.getKey();
        } else {
            return path.getPath().substring(1);
        }
    }

    public String toString () {
        return path.toString();
    }

    public List<S3Resource> getChildren() {
        if (this.children != null) {
            return this.children;
        }
        try {
            this.children = this.s3ResourceGenerator.getChildren(this.path.getPath(),this.offset);
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
                    bucket,
                    builder.build().toString(),
                    null,
                    false,
                    s3ResourceGenerator,
                    client,
                    useReducedRedundancy, this.offset);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void mkdirs() {}

    public OutputStream getWriter() {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out;
        try {
            out = new PipedOutputStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        s3ResourceGenerator.incrementThreadCount();
        new Thread(
                new Runnable(){
                    public void run(){
                        try {
                            PutObjectRequest request = new PutObjectRequest(bucket, path.getPath().substring(1), in, getS3Metadata());
                            if (useReducedRedundancy) {
                                request.setStorageClass(StorageClass.ReducedRedundancy.toString());
                            }
                            client.putObject(request);
                        } catch (AmazonClientException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Thread.yield();
                        s3ResourceGenerator.decrementThreadCount();
                    }
                }
        ).start();
        return out;
    }

    public InputStream getReader() {
        S3Object obj = this.client.getObject(this.bucket, this.path.getPath().substring(1));
        return obj.getObjectContent();
    }
    
    public Boolean finishWrite () {
        while (s3ResourceGenerator.getThreadsOpen() >= 1) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void delete() {
        client.deleteObject(this.getBucket(), this.path.getPath().substring(1));
    }

    public long lastModified() {
        if (s3ObjectSummary == null) {
            Date lastModifed = getS3Metadata().getLastModified();
            if (lastModifed != null) {
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
                this.s3MetaData = client.getObjectMetadata(bucket, this.getKey());
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
