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
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

/**
 * @goal deploy
 */
public class S3DeployMojo extends AbstractMojo {

  /** Number of retries */
  private static final int MAX_RETRIES = 3;

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

  /** True if an error occurred during uploading */
  private String erroneousUpload = null;

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    // Setup AWS S3 client
    AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
    AmazonS3Client uploadClient = new AmazonS3Client(credentials);
    TransferManager transfers = new TransferManager(credentials);

    // Make sure key prefix does not start with a slash but has one at the
    // end
    if (keyPrefix.startsWith("/"))
      keyPrefix = keyPrefix.substring(1);
    if (!keyPrefix.endsWith("/"))
      keyPrefix = keyPrefix + "/";

    // Keep track of how much data has been transferred
    long totalBytesTransferred = 0L;
    int items = 0;
    Queue<Upload> uploads = new LinkedBlockingQueue<Upload>();

    try {
      // Check if S3 bucket exists
      getLog().debug("Checking whether bucket " + bucket + " exists");
      if (!uploadClient.doesBucketExist(bucket)) {
        getLog().error("Desired bucket '" + bucket + "' does not exist!");
        return;
      }

      getLog().debug("Collecting files to transfer from " + resources.getDirectory());
      List<File> res = getResources();
      for (File file : res) {
        // Make path of resource relative to resources directory
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);
        String path = file.getPath().substring(resources.getDirectory().length());
        String key = concat("/", keyPrefix, path).substring(1);

        // Delete old file version in bucket
        getLog().debug("Removing existing object at " + key);
        uploadClient.deleteObject(bucket, key);

        // Setup meta data
        ObjectMetadata meta = new ObjectMetadata();
        meta.setCacheControl("public, max-age=" + String.valueOf(valid * 3600));

        FileInputStream fis = null;
        GZIPOutputStream gzipos = null;
        final File fileToUpload;

        if (gzip && ("js".equals(extension) || "css".equals(extension))) {
          try {
            fis = new FileInputStream(file);
            File gzFile = File.createTempFile(file.getName(), null);
            gzipos = new GZIPOutputStream(new FileOutputStream(gzFile));
            IOUtils.copy(fis, gzipos);
            fileToUpload = gzFile;
            meta.setContentEncoding("gzip");
            if ("js".equals(extension))
              meta.setContentType("text/javascript");
            if ("css".equals(extension))
              meta.setContentType("text/css");
          } catch (FileNotFoundException e) {
            getLog().error(e);
            continue;
          } catch (IOException e) {
            getLog().error(e);
            continue;
          } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(gzipos);
          }
        } else {
          fileToUpload = file;
        }

        // Do a random check for existing errors before starting the next upload
        if (erroneousUpload != null)
          break;

        // Create put object request
        long bytesToTransfer = fileToUpload.length();
        totalBytesTransferred += bytesToTransfer;
        PutObjectRequest request = new PutObjectRequest(bucket, key, fileToUpload);
        request.setProgressListener(new UploadListener(credentials, bucket, key, bytesToTransfer));
        request.setMetadata(meta);

        // Schedule put object request
        getLog().info("Uploading " + key + " (" + FileUtils.byteCountToDisplaySize((int) bytesToTransfer) + ")");
        Upload upload = transfers.upload(request);
        uploads.add(upload);
        items ++;
      }
    } catch (AmazonServiceException e) {
      getLog().error("Uploading resources failed: " + e.getMessage());
    } catch (AmazonClientException e) {
      getLog().error("Uploading resources failed: " + e.getMessage());
    }

    // Wait for uploads to be finished
    String currentUpload = null;
    try {
      Thread.sleep(1000);
      getLog().info("Waiting for " + uploads.size() + " uploads to finish...");
      while (!uploads.isEmpty()) {
        Upload upload = uploads.poll();
        currentUpload = upload.getDescription().substring("Uploading to ".length());
        if (TransferState.InProgress.equals(upload.getState()))
          getLog().debug("Waiting for upload " + currentUpload + " to finish");
        upload.waitForUploadResult();
      }
    } catch (AmazonServiceException e) {
      throw new MojoExecutionException("Error while uploading " + currentUpload);
    } catch (AmazonClientException e) {
      throw new MojoExecutionException("Error while uploading " + currentUpload);
    } catch (InterruptedException e) {
      getLog().debug("Interrupted while waiting for upload to finish");
    }

    // Check for errors that happened outside of the actual uploading
    if (erroneousUpload != null) {
      throw new MojoExecutionException("Error while uploading " + erroneousUpload);
    }

    getLog().info("Deployed " + items + " files (" + FileUtils.byteCountToDisplaySize((int) totalBytesTransferred) + ") to s3://" + bucket);
  }

  /**
   * @return
   * @throws MojoExecutionException
   */
  @SuppressWarnings("unchecked")
  protected List<File> getResources() throws MojoExecutionException {
    File directory = new File(resources.getDirectory());
    String includes = StringUtils.join(resources.getIncludes(), ",");
    String excludes = StringUtils.join(resources.getExcludes(), ",");
    try {
      List<File> files = FileUtils.getFiles(directory, includes, excludes);
      getLog().debug("Adding " + files.size() + " objects to the list of items to deploy");
      return files;
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to get resources to deploy", e);
    }
  }

  /**
   * Concatenates the url elements with respect to leading and trailing slashes.
   * The path will always end with a trailing slash.
   * 
   * @param urlElements
   *          the path elements
   * @return the concatenated url of the two arguments
   * @throws IllegalArgumentException
   *           if less than two path elements are provided
   */
  private static String concat(String... urlElements)
      throws IllegalArgumentException {
    if (urlElements == null || urlElements.length < 1)
      throw new IllegalArgumentException("Prefix cannot be null or empty");
    if (urlElements.length < 2)
      throw new IllegalArgumentException("Suffix cannot be null or empty");

    StringBuffer b = new StringBuffer();
    for (String s : urlElements) {
      if (StringUtils.isBlank(s))
        throw new IllegalArgumentException("Path element cannot be null");
      String element = checkSeparator(s);
      element = removeDoubleSeparator(element);

      if (b.length() == 0) {
        b.append(element);
      } else if (b.lastIndexOf("/") < b.length() - 1 && !element.startsWith("/")) {
        b.append("/").append(element);
      } else if (b.lastIndexOf("/") == b.length() - 1 && element.startsWith("/")) {
        b.append(element.substring(1));
      } else {
        b.append(element);
      }
    }

    return b.toString();
  }

  /**
   * Checks that the path only contains the web path separator "/". If not,
   * wrong ones are replaced.
   */
  private static String checkSeparator(String path) {
    String sp = File.separator;
    if ("\\".equals(sp))
      sp = "\\\\";
    return path.replaceAll(sp, "/");
  }

  /**
   * Removes any occurrence of double separators ("//") and replaces it with
   * "/".
   * 
   * @param path
   *          the path to check
   * @return the corrected path
   */
  private static String removeDoubleSeparator(String path) {
    int protocolIndex = path.indexOf("://");
    protocolIndex += protocolIndex == -1 ? 0 : 3;
    int index = Math.max(0, protocolIndex);
    while ((index = path.indexOf("//", index)) != -1) {
      path = path.substring(0, index) + path.substring(index + 1);
    }
    return path;
  }

  /**
   * Progress listener that is monitoring upload progress of an S3 item and on
   * successful upload adjust the object's ACL to public read access.
   */
  protected class UploadListener implements ProgressListener {

    private AmazonS3Client client;
    private String bucket;
    private String key;
    private long size = 0;
    private long bytesTransferred = 0;

    /**
     * Creates a new ACL controller that will be using the client object to
     * monitor upload progress to the object identified by <code>key</code>.
     * 
     * @param credentials
     *          the amazon credentials
     * @param bucket
     *          the bucket
     * @param key
     *          the key identifying the object
     * @param size
     *          the number of bytes to upload
     */
    protected UploadListener(AWSCredentials credentials, String bucket,
        String key, long size) {
      this.client = new AmazonS3Client(credentials);
      this.bucket = bucket;
      this.key = key;
      this.size = size;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.amazonaws.services.s3.model.ProgressListener#progressChanged(com.amazonaws.services.s3.model.ProgressEvent)
     */
    public void progressChanged(ProgressEvent pe) {
      bytesTransferred += pe.getBytesTransfered();
      switch (pe.getEventCode()) {
        case ProgressEvent.STARTED_EVENT_CODE:
          getLog().debug("Upload of " + key + " started");
          break;
        case ProgressEvent.COMPLETED_EVENT_CODE:
          getLog().debug("Upload of '" + key + "' completed (" + bytesTransferred + " of " + size + " transferred)");
          setPublicAccess();
          break;
        case ProgressEvent.CANCELED_EVENT_CODE:
          getLog().info("Upload of " + key + " canceled");
          break;
        case ProgressEvent.FAILED_EVENT_CODE:
          getLog().warn("Upload of " + key + " failed");
          break;
        default:
          // Nothing to do
      }
    }

    /**
     * Adjusts access control to the object to public read.
     */
    private void setPublicAccess() {
      int retries = 0;
      while (retries < MAX_RETRIES) {
        try {
          // Make sure S3 can get its act together before we bother it again
          Thread.sleep(2000);
          // Ask S3 to open the resource up for public read
          client.setObjectAcl(bucket, key, CannedAccessControlList.PublicRead);
          getLog().debug("Access control on " + key + " adjusted to public read");
        } catch (AmazonServiceException e) {
          getLog().warn("Access control on " + key + " cannot be set: " + e.getMessage());
        } catch (AmazonClientException e) {
          getLog().warn("Error adjusting access control on " + key + ": " + e.getMessage());
        } catch (Throwable t) {
          getLog().warn("Error adjusting access control on " + key + ": " + t.getMessage());
        }

        // Check the object's current ACL to make sure the operation succeeded
        AccessControlList acl = client.getObjectAcl(bucket, key);
        boolean publicAccessGranted = false;
        for (Grant grant : acl.getGrants()) {
          if (GroupGrantee.AllUsers.equals(grant.getGrantee())) {
            publicAccessGranted = Permission.Read.equals(grant.getPermission());
          }
        }

        if (!publicAccessGranted) {
          retries++;
          if (retries < MAX_RETRIES)
            getLog().warn("Setting of access control entries on " + key + " failed, retrying");
          else if (erroneousUpload == null) {
            getLog().error("S3 is not responding to acl update request on " + key);
            erroneousUpload = key;
          }
        } else {
          getLog().debug("Access control on " + key + " verified to be public read access");
          if (retries > 0)
            getLog().info("Setting of access control entries on " + key + " finally succeeded");
          return;
        }
      }
    }

  }

}
