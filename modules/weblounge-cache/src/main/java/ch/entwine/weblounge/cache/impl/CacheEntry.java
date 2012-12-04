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

package ch.entwine.weblounge.cache.impl;

import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class implements an entry into the cache.
 */
public final class CacheEntry implements Serializable {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(CacheEntry.class);

  /** Serial version uid */
  private static final long serialVersionUID = 5694887351734158681L;

  /** The key for this cache entry */
  private String key = null;

  /** The content buffer */
  private byte[] content;

  /** The content encoding */
  private String encoding = null;

  /** The response metadata */
  private CacheableHttpServletResponseHeaders headers = null;

  /** Date when the entry was added to the cache */
  private long creationDate = 0L;

  /** Date where the entry's contents were last modified */
  private long modificationDate = 0L;

  /** Time in ms for the client to revalidate */
  private long clientRevalidationTime = 0L;

  /** The etag */
  private String eTag = null;

  /** The response status */
  private int status;

  /**
   * Creates a new cache entry for the given handle, content and metadata.
   * <p>
   * Note that some cache information, such as the etag and the last
   * modification dates will be gathered from the response headers if available.
   * 
   * @param handle
   *          the cache handle
   * @param content
   *          the content
   * @param encoding
   *          the content encoding
   * @param headers
   *          the metadata
   * @param status
   *          the HTTP response status
   * @throws IllegalArgumentException
   *           if the content or the headers collection is <code>null</code>
   */
  protected CacheEntry(CacheHandle handle, byte[] content, String encoding,
      CacheableHttpServletResponseHeaders headers, int status) {
    if (handle == null)
      throw new IllegalArgumentException("Handle cannot be null");
    if (content == null)
      throw new IllegalArgumentException("Content cannot be null");
    if (headers == null)
      throw new IllegalArgumentException("Headers cannot be null");
    this.key = handle.getKey();
    this.encoding = encoding;
    this.content = content;
    this.creationDate = handle.getCreationDate();
    this.modificationDate = getTimeWithoutMilliseconds(handle.getModificationDate());
    this.clientRevalidationTime = handle.getClientRevalidationTime();
    this.eTag = createETag(modificationDate);
    this.status = status;
    setHeaders(headers);
  }

  /**
   * Returns the <code>HTTP</code> response status.
   * 
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Returns the key for this entry.
   * 
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the content encoding.
   * 
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns this entry's etag value.
   * 
   * @return the etag
   */
  public String getETag() {
    return eTag;
  }

  /**
   * Returns the date when this entry was created.
   * 
   * @return the creation date
   */
  public long getCreationDate() {
    return creationDate;
  }

  /**
   * Returns the date when this entry's content was last modified.
   * 
   * @return the modification date
   */
  public long getModificationDate() {
    return modificationDate;
  }

  /**
   * Returns the time in ms until the client needs to revalidate the response.
   * 
   * @return the client revalidation time
   */
  public long getClientRevalidationTime() {
    return clientRevalidationTime;
  }

  /**
   * Returns <code>true</code> if the entry is tagged with <code>tag</code>.
   * 
   * @param tag
   *          the tag
   * @return <code>true</code> if the entry is tagged
   */
  public boolean containsTag(CacheTag tag) {
    String keyPart = tag.getName() + "=" + tag.getValue();
    return key.contains(keyPart);
  }

  /**
   * Sets the response headers, which will be used to extract certain common
   * information such as the etag or the last-modified date.
   * 
   * @param headers
   *          the response headers
   */
  public void setHeaders(CacheableHttpServletResponseHeaders headers) {
    this.headers = headers;

    // Overwrite local Last-Modified?
    if (headers.containsHeader("Last-Modified")) {
      DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
      String lastModifiedHeader = (String) headers.getHeaders().get("Last-Modified");
      try {
        modificationDate = getTimeWithoutMilliseconds(new Date(Math.max(modificationDate, df.parse(lastModifiedHeader).getTime())));
        logger.trace("Changing modification date to '{}' as provided by response headers", lastModifiedHeader);
        eTag = createETag(modificationDate);
        logger.trace("Changing eTag to '{}' as provided by response headers", eTag);
      } catch (ParseException e) {
        logger.error("Unexpected date format for 'Last-Modified' header: {}", lastModifiedHeader);
      }
    }

    // Overwrite local ETag?
    if (headers.containsHeader("ETag")) {
      eTag = (String) headers.getHeaders().get("ETag");
      logger.trace("Changing eTag to '{}' as provided by response headers", eTag);
    }
  }

  /**
   * Returns the cached response headers.
   * 
   * @return the headers
   */
  public CacheableHttpServletResponseHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns the cached content.
   * 
   * @return the content
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * Returns the content type.
   * 
   * @return the content type
   */
  public String getContentType() {
    Object contentType = headers.getHeaders().get("Content-Type");
    if (contentType == null)
      return null;
    if (contentType instanceof String)
      return (String) contentType;
    throw new IllegalStateException("Response contained more than one 'Content-type' header");
  }

  /**
   * Returns <code>true</code> if date is not older than the creation date of
   * this cache entry.
   * 
   * @param date
   *          the date
   * @return <code>true</code> if this entry is older or equally old
   */
  public boolean notModified(long date) {
    return date >= modificationDate;
  }

  /**
   * Returns <code>true</code> if <code>eTag</code> is either blank (not
   * specified) or if it matches this entry's etag.
   * 
   * @param eTag
   *          the etag
   * @return <code>true</code> if the etag is either empty or matches
   */
  public boolean matches(String eTag) {
    return StringUtils.isNotBlank(eTag) && this.eTag.equals(eTag);
  }

  /**
   * Returns the etag (including the extra pair of quotes) for the given value,
   * which will usually be the creation time of the cached entry.
   * 
   * @param value
   *          the value
   * @return the etag
   */
  public static String createETag(long value) {
    return "\"WL-" + Long.toHexString(value) + "\"";
  }

  /**
   * Strips the milliseconds from the date and returns it.
   * 
   * @param date
   *          the data
   * @return the date without the milliseconds
   */
  private long getTimeWithoutMilliseconds(Date date) {
    long millis = date.getTime() / 1000;
    millis *= 1000;
    return millis;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return key;
  }

}
