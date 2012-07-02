package com.jsync;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class S3ResourceGenerator {

    String bucket;
    AmazonS3Client client;
    List<S3ObjectSummary> objects;
    URI uri;
    int threadsOpen;
    boolean useReducedRedundancy;

    public S3ResourceGenerator(AWSCredentials creds, String base, boolean useReducedRedundancy) {
        URI uri = null;
        try {
            uri = new URI(base);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.uri = uri;
        bucket = uri.getHost();
        client = new AmazonS3Client(creds);
        threadsOpen = 0;
        this.useReducedRedundancy = useReducedRedundancy;
    }

    public void incrementThreadCount() {
        threadsOpen += 1;
    }
    public  void decrementThreadCount() {
        threadsOpen -= 1;
    }
    public int getThreadsOpen() {
        return threadsOpen;
    }

    public AmazonS3Client getClient() {
        return client;
    }

    private List<S3ObjectSummary> getObjectList() {
        if (this.objects == null) {
            this.objects = new ArrayList<S3ObjectSummary>();
            ObjectListing listing = client.listObjects(bucket, uri.getPath().substring(1));
            while (true) {
                for (S3ObjectSummary obj : listing.getObjectSummaries()) {
                    this.objects.add(obj);
                }
                if (listing.isTruncated()) {
                    listing = client.listNextBatchOfObjects(listing);
                } else {
                    break;
                }
            }
        }
        /*
        for (S3ObjectSummary summary : this.objects) {
            System.out.println(summary.getKey());
        }
        System.exit(1);
        */
        return this.objects;
    }

    public S3Resource getResource(String path) throws URISyntaxException {
        return this.getResource(path, 0);
    }

    public S3Resource getResource(String path, int point) throws URISyntaxException {
        URI uri;
        uri = new URI(path);

        List<S3Resource> files = new ArrayList<S3Resource>();
        List<S3Resource> subdirs = new ArrayList<S3Resource>();
        List<String> visitedSubdirs = new ArrayList<String>();

        S3ObjectSummary theFile = null;

        int c = point;

        for (S3ObjectSummary object : getObjectList().subList(point, getObjectList().size())) {
            String key = "/" + object.getKey();
            String repl = key.replaceFirst("^" + Pattern.quote(uri.getPath()) + "/", "");

            Boolean matches = key.matches("^" + Pattern.quote(uri.getPath()) + "/.*$");

            if (key.equals(uri.getPath())) {
                theFile = object;
            }

            // is subdir
            if (repl.contains("/") && !key.equals(uri.getPath()) && matches) {
                String name = repl.substring(0, repl.indexOf("/", 1));
                if (!visitedSubdirs.contains(name)) {
                    URIBuilder builder = new URIBuilder();
                    builder.setScheme("s3");
                    builder.setHost(bucket);
                    builder.setPath(uri.getPath() + "/" + name);
                    subdirs.add(this.getResource(builder.build().toString(), c));
                    visitedSubdirs.add(name);
                }
            }

            // is file
            else if (!repl.contains("/") && !key.equals(uri.getPath()) && matches) {
                URIBuilder builder = new URIBuilder();
                builder.setScheme("s3");
                builder.setHost(bucket);
                builder.setPath(key);
                files.add(new S3Resource(object, bucket, builder.build().toString(), null, false, this, client, useReducedRedundancy));
            } else if ((files.size() != 0 || subdirs.size() != 0) || theFile != null){
                break;
            }

            c += 1;
        }


        for (S3Resource dir : subdirs) {
            files.add(dir);
        }

        //System.out.println(files);

        if (files.size() > 0) {
            return new S3Resource(theFile, bucket, path, files, true, this, client, useReducedRedundancy);
        } else {
            return new S3Resource(theFile, bucket, path, files, false, this, client, useReducedRedundancy);
        }
    }
}
