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

package ch.entwine.weblounge.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

/**
 * Implementation of the resource serializer api which is using Amazon S3 to
 * store weblounge assets.
 */
public class S3ResourceSerializer {
  
  /** The logging implementation */
  private static final Logger logger = LoggerFactory.getLogger(S3ResourceSerializer.class);

  /** Amazon web services access credentials */
  private AWSCredentials accessCredentials = null;

  /**
   * Creates an instance of the serializer, using <code>credentials</code> to
   * access Amazon S3.
   * 
   *  @param credentials the aws credentials
   */
  public S3ResourceSerializer(AWSCredentials credentials) {
    this.accessCredentials = credentials;
  }

  void main(String[] args) throws IOException {
    /*
     * Important: Be sure to fill in your AWS access credentials in the
     * AwsCredentials.properties file before you try to run this sample.
     * http://aws.amazon.com/security-credentials
     */
    AmazonS3 s3 = new AmazonS3Client(accessCredentials);

    String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
    String key = "MyObjectKey";

    logger.info("===========================================");
    logger.info("Getting Started with Amazon S3");
    logger.info("===========================================\n");

    try {
      /*
       * Create a new S3 bucket - Amazon S3 bucket names are globally unique, so
       * once a bucket name has been taken by any user, you can't create another
       * bucket with that same name.
       * 
       * You can optionally specify a location for your bucket if you want to
       * keep your data closer to your applications or users.
       */
      logger.info("Creating bucket " + bucketName + "\n");
      s3.createBucket(bucketName);

      /*
       * List the buckets in your account
       */
      logger.info("Listing buckets");
      for (Bucket bucket : s3.listBuckets()) {
        logger.info(" - " + bucket.getName());
      }

      /*
       * Upload an object to your bucket - You can easily upload a file to S3,
       * or upload directly an InputStream if you know the length of the data in
       * the stream. You can also specify your own metadata when uploading to
       * S3, which allows you set a variety of options like content-type and
       * content-encoding, plus additional metadata specific to your
       * applications.
       */
      logger.info("Uploading a new object to S3 from a file\n");
      s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));

      /*
       * Download an object - When you download an object, you get all of the
       * object's metadata and a stream from which to read the contents. It's
       * important to read the contents of the stream as quickly as possibly
       * since the data is streamed directly from Amazon S3 and your network
       * connection will remain open until you read all the data or close the
       * input stream.
       * 
       * GetObjectRequest also supports several other options, including
       * conditional downloading of objects based on modification times, ETags,
       * and selectively downloading a range of an object.
       */
      logger.info("Downloading an object");
      S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
      logger.info("Content-Type: " + object.getObjectMetadata().getContentType());
      displayTextInputStream(object.getObjectContent());

      /*
       * List objects in your bucket by prefix - There are many options for
       * listing the objects in your bucket. Keep in mind that buckets with many
       * objects might truncate their results when listing their objects, so be
       * sure to check if the returned object listing is truncated, and use the
       * AmazonS3.listNextBatchOfObjects(...) operation to retrieve additional
       * results.
       */
      logger.info("Listing objects");
      ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix("My"));
      for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        logger.info(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
      }

      /*
       * Delete an object - Unless versioning has been turned on for your
       * bucket, there is no way to undelete an object, so use caution when
       * deleting objects.
       */
      logger.info("Deleting an object\n");
      s3.deleteObject(bucketName, key);

      /*
       * Delete a bucket - A bucket must be completely empty before it can be
       * deleted, so remember to delete any objects from your buckets before you
       * try to delete them.
       */
      logger.info("Deleting bucket " + bucketName + "\n");
      s3.deleteBucket(bucketName);
    } catch (AmazonServiceException ase) {
      logger.info("Caught an AmazonServiceException, which means your request made it " + "to Amazon S3, but was rejected with an error response for some reason.");
      logger.info("Error Message:    " + ase.getMessage());
      logger.info("HTTP Status Code: " + ase.getStatusCode());
      logger.info("AWS Error Code:   " + ase.getErrorCode());
      logger.info("Error Type:       " + ase.getErrorType());
      logger.info("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      logger.info("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
      logger.info("Error Message: " + ace.getMessage());
    }
  }

  /**
   * Creates a temporary file with text data to demonstrate uploading a file to
   * Amazon S3
   * 
   * @return A newly created temporary file with text data.
   * 
   * @throws IOException
   */
  private static File createSampleFile() throws IOException {
    File file = File.createTempFile("aws-java-sdk-", ".txt");
    file.deleteOnExit();

    Writer writer = new OutputStreamWriter(new FileOutputStream(file));
    writer.write("abcdefghijklmnopqrstuvwxyz\n");
    writer.write("01234567890112345678901234\n");
    writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
    writer.write("01234567890112345678901234\n");
    writer.write("abcdefghijklmnopqrstuvwxyz\n");
    writer.close();

    return file;
  }

  /**
   * Displays the contents of the specified input stream as text.
   * 
   * @param input
   *          The input stream to display as text.
   * 
   * @throws IOException
   */
  private static void displayTextInputStream(InputStream input)
      throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    while (true) {
      String line = reader.readLine();
      if (line == null)
        break;

      logger.info("    " + line);
    }
  }

}
