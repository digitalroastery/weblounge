/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.maven;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @goal deploy
 */
public class S3DeployMojo extends AbstractMojo {

  /**
   * AWS access key used to access the S3 bucket
   * 
   * @parameter name="AWSAccessKey"
   * @required
   */
  private String awsAccessKey;

  /**
   * AWS secret key used to access the S3 bucket
   * 
   * @parameter name="AWSSecretKey"
   * @required
   */
  private String awsSecretKey;

  /**
   * Name of the destination S3 bucket.
   * 
   * @parameter
   * @required
   */
  private String bucket;

  /**
   * Prefix for the resource key.
   * 
   * @parameter
   * @required
   */
  private String keyPrefix;

  /**
   * Resources to deploy to the S3 bucket.
   * 
   * @parameter
   * @required
   */
  private FileSet resources;

  /**
   * Duration (in hours) the file remains valid (HTTP Expires header will be set
   * to now + valid). Default value is 8'760h (1 year).
   * 
   * @parameter default-value="8760"
   */
  private int valid;

  /**
   * Enable/disable GZip compression of files. Default value is 'true'.
   * 
   * @parameter default-value="true"
   */
  private boolean gzip;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    // Setup AWS S3 client
    AWSCredentials cred = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
    AmazonS3Client client = new AmazonS3Client(cred);

    try {
      // Check, if S3 bucket exists
      if (!client.doesBucketExist(bucket)) {
        getLog().error("Desired bucket '" + bucket + "' does not exist!");
        return;
      }

      // Setup transfer manager
      TransferManager trans = new TransferManager(cred);

      List<File> res = getResources();
      for (File file : res) {
        // Make path of resource relative to resources directory
        String path = file.getPath().replace(resources.getDirectory(), "");
        String extension = FileUtils.extension(file.getName());

        // Remove starting slash
        if (path.startsWith("/"))
          path = path.substring(1);

        // Make sure, key prefix does not start with a slash but has one at the
        // end
        if (keyPrefix.startsWith("/"))
          keyPrefix = keyPrefix.substring(1);
        if (!keyPrefix.endsWith("/"))
          keyPrefix = keyPrefix + "/";

        // Concatenate key
        String key = keyPrefix + path;

        // Delete old file version in bucket
        client.deleteObject(bucket, path);

        // Setup meta data
        ObjectMetadata meta = new ObjectMetadata();
        meta.setCacheControl("public, max-age=" + String.valueOf(valid * 3600));

        FileInputStream fis = null;
        GZIPOutputStream gzipos = null;
        if (gzip && ("js".equals(extension) || "css".equals(extension))) {
          try {
            fis = new FileInputStream(file);
            File gzFile = File.createTempFile(file.getName(), null);
            gzipos = new GZIPOutputStream(new FileOutputStream(gzFile));
            IOUtils.copy(fis, gzipos);
            file = gzFile;
            meta.setContentEncoding("gzip");
            if ("js".equals(extension))
              meta.setContentType("text/javascript");
            if ("css".equals(extension))
              meta.setContentType("text/css");
          } catch (FileNotFoundException e) {
            getLog().error(e);
          } catch (IOException e) {
            getLog().error(e);
          } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(gzipos);
          }
        }

        // Create put object request
        PutObjectRequest request = new PutObjectRequest(bucket, key, file);
        request.setProgressListener(new DeployProgressListener(client, bucket, key));
        request.setMetadata(meta);

        // Schedule put object request
        trans.upload(request);
      }
    } catch (AmazonServiceException e) {
      getLog().error("Uploading resources failed: " + e.getMessage());
    } catch (AmazonClientException e) {
      getLog().error("Uploading resources failed: " + e.getMessage());
    }

  }

  /**
   * @return
   * @throws MojoExecutionException
   */
  @SuppressWarnings("unchecked")
  protected List<File> getResources() throws MojoExecutionException {
    File directory = new File(resources.getDirectory());
    String includes = getCommaSeparatedList(resources.getIncludes());
    String excludes = getCommaSeparatedList(resources.getExcludes());
    try {
      return FileUtils.getFiles(directory, includes, excludes);
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to get resources to deploy", e);
    }
  }

  protected String getCommaSeparatedList(List<String> list) {
    StringBuffer buffer = new StringBuffer();
    for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
      String element = iterator.next();
      buffer.append(element.toString());
      if (iterator.hasNext()) {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

  protected class DeployProgressListener implements ProgressListener {

    private AmazonS3Client client;
    private String bucket;
    private String key;

    protected DeployProgressListener(AmazonS3Client client, String bucket,
        String key) {
      this.client = client;
      this.bucket = bucket;
      this.key = key;
    }

    public void progressChanged(ProgressEvent pe) {
      if (pe.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
        getLog().info("Resource '" + key + "' deployed");
        client.setObjectAcl(bucket, key, CannedAccessControlList.PublicRead);
      }
    }

  }

}
