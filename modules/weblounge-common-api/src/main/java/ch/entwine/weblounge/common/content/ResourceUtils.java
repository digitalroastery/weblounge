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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * This class contains utility methods intended to facilitate dealing with
 * resource versions and names.
 */
public final class ResourceUtils {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

  /**
   * This class is not intended to be instantiated.
   */
  private ResourceUtils() {
    // Nothing to do here
  }

  /**
   * Returns <code>true</code> if the two uris match either by id or path. This
   * implementation takes into account that certain fields such as the id may
   * not (yet) be set.
   * 
   * @param a
   *          the first uri
   * @param b
   *          the second uri
   * @return <code>true</code> if the two uris point at the same resource
   */
  public static boolean equalsByIdOrPathAndVersion(ResourceURI a, ResourceURI b) {
    return uriEquals(a, b, true);
  }

  /**
   * Returns <code>true</code> if the two uris match either by id or path and
   * version. This implementation takes into account that certain fields such as
   * the id may not (yet) be set.
   * 
   * @param a
   *          the first uri
   * @param b
   *          the second uri
   * @return <code>true</code> if the two uris point at the same resource
   */
  public static boolean equalsByIdOrPath(ResourceURI a, ResourceURI b) {
    return uriEquals(a, b, false);
  }

  /**
   * Returns <code>true</code> if the two uris match. This implementation takes
   * into account that certain fields such as the id may not (yet) be set.
   * 
   * @param a
   *          the first uri
   * @param b
   *          the second uri
   * @param checkVersions
   *          <code>true</code> to also check equality on the version
   * @return <code>true</code> if the two uris point at the same resource
   */
  private static boolean uriEquals(ResourceURI a, ResourceURI b,
      boolean checkVersions) {
    long versionA = a.getVersion();
    long versionB = b.getVersion();

    // Test the identifier
    String idA = a.getIdentifier();
    String idB = b.getIdentifier();
    if (idA != null && idB != null) {
      if (idA.equals(idB) && (!checkVersions || versionA == versionB))
        return true;
      return false;
    }

    // Test the path
    String pathA = a.getPath();
    String pathB = b.getPath();
    if (pathA != null && pathB != null) {
      if (pathA.equals(pathB) && (!checkVersions || versionA == versionB))
        return true;
      return false;
    }

    return false;
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean hasChanged(HttpServletRequest request,
      Resource<?> resource) throws IllegalArgumentException {
    return hasChanged(request, resource, null);
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean hasChanged(HttpServletRequest request,
      Resource<?> resource, Language language) throws IllegalArgumentException {
    if (request.getHeader("If-Modified-Since") != null) {
      return isModified(request, resource, language);
    } else if (request.getHeader("If-None-Match") != null) {
      return isMismatch(request, getETagValue(resource));
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param date
   *          the date
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean hasChanged(HttpServletRequest request, long date)
      throws IllegalArgumentException {
    if (request.getHeader("If-Modified-Since") != null) {
      return isModified(request, date);
    } else if (request.getHeader("If-None-Match") != null) {
      return isMismatch(request, getETagValue(date));
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @param style
   *          the imageStle
   * @param langauge
   *          the language
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  // public static boolean hasChanged(HttpServletRequest request,
  // Resource<?> resource, ImageStyle style, Language language)
  // throws IllegalArgumentException {
  // if (request.getHeader("If-Modified-Since") != null) {
  // return isModified(request, resource, language);
  // } else if (request.getHeader("If-None-Match") != null) {
  // return isMismatch(request, resource, style);
  // }
  // return true;
  // }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean isModified(HttpServletRequest request,
      Resource<?> resource) throws IllegalArgumentException {
    return isModified(request, getModificationDate(resource, null).getTime());
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean isModified(HttpServletRequest request,
      Resource<?> resource, Language language) throws IllegalArgumentException {
    return isModified(request, getModificationDate(resource, language).getTime());
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param request
   *          the client request
   * @param date
   *          the date
   * @return <code>true</code> if the resource is more recent than the version
   *         that is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean isModified(HttpServletRequest request, long date)
      throws IllegalArgumentException {
    if (request.getHeader("If-Modified-Since") != null) {
      try {
        long cachedModificationDate = request.getDateHeader("If-Modified-Since");
        return cachedModificationDate < date;
      } catch (IllegalArgumentException e) {
        logger.debug("Client sent malformed 'If-Modified-Since' header: {}");
      }
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the resource has not been modified,
   * <code>false</code> if the cached version on the client side is out dated or
   * if the request did not contain caching information.
   * <p>
   * The decision is based on the availability and value of the
   * <code>If-None-Match</code> header (called <code>ETag</code>).
   * <p>
   * 
   * @param request
   *          the client request
   * @param resource
   *          the resource
   * @param style
   *          the imagestyle
   * @return <code>true</code> if the resource's calculated eTag matches the one
   *         specified
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> cannot be converted to a
   *           date.
   */
  // public static boolean isMismatch(HttpServletRequest request,
  // Resource<?> resource, ImageStyle style) throws IllegalArgumentException {
  // String eTagHeader = request.getHeader("If-None-Match");
  // if (StringUtils.isBlank(eTagHeader))
  // return true;
  // String eTag = getETagValue(resource, style);
  // return !eTagHeader.equals(eTag);
  // }

  /**
   * Returns <code>true</code> if the resource has not been modified according
   * to the expected <code>ETag</code> value, <code>false</code> if the cached
   * version on the client side is out dated or if the request did not contain
   * caching information.
   * <p>
   * The decision is based on the availability and value of the
   * <code>If-None-Match</code> header (called <code>ETag</code>). The computed
   * value of <code>eTag</code> is expected to be plain, i. e. without
   * surrounding quotes.
   * 
   * @param eTag
   *          the expected eTag value
   * @param request
   *          the client request
   * @return <code>true</code> if the resource's calculated eTag matches the one
   *         specified
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> cannot be converted to a
   *           date.
   */
  public static boolean isMismatch(HttpServletRequest request, String eTag)
      throws IllegalArgumentException {
    String eTagHeader = request.getHeader("If-None-Match");
    if (StringUtils.isBlank(eTagHeader))
      return true;
    return !eTagHeader.equals(eTag);
  }

  /**
   * Returns the value for the <code>ETag</code> header field, which is
   * calculated from the resource identifier and the resource's modification
   * date.
   * 
   * @param resource
   *          the resource
   * @return the <code>ETag</code> value
   */
  public static String getETagValue(Resource<?> resource) {
    long etag = resource.getIdentifier().hashCode();
    etag += getModificationDate(resource, null).getTime();
    return new StringBuffer().append("\"").append("WL-" + etag).append("\"").toString();
  }

  /**
   * Returns the value for the <code>ETag</code> header field, which is
   * calculated from the given modification date.
   * 
   * @param date
   *          the modification date
   * @return the <code>ETag</code> value
   */
  public static String getETagValue(Date date) {
    if (date == null)
      throw new IllegalArgumentException("Date must not be null");
    return getETagValue(date.getTime());
  }

  /**
   * Returns the value for the <code>ETag</code> header field, which is
   * calculated from the given modification date.
   * 
   * @param date
   *          the date in milliseconds
   * @return the <code>ETag</code> value
   */
  public static String getETagValue(long date) {
    return new StringBuffer().append("\"").append("WL-" + date).append("\"").toString();
  }

  /**
   * Returns the modification date. If the resource has never been modified, its
   * creation date is returned instead.
   * 
   * @param resource
   *          the resource
   * @return the modification date
   * @throws IllegalArgumentException
   *           if the resource is <code>null</code>
   */
  public static Date getModificationDate(Resource<?> resource)
      throws IllegalArgumentException {
    return getModificationDate(resource, null);
  }

  /**
   * Returns the modification date. If the resource has never been modified, its
   * creation date is returned instead.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return the modification date
   * @throws IllegalArgumentException
   *           if the resource is <code>null</code>
   */
  public static Date getModificationDate(Resource<?> resource, Language language)
      throws IllegalArgumentException {
    if (resource == null)
      throw new IllegalArgumentException("Resource cannot be null");

    // Has a language been specified? If so, use the localized content
    if (language != null) {
      ResourceContent content = resource.getContent(language);
      if (content != null)
        return content.getCreationDate();
    }

    // The resource's modified date is the last resort. If nothing else helps,
    // return the creation date.
    return resource.getLastModified();
  }

  /**
   * Returns the version for the given version identifier. Available versions
   * are:
   * <ul>
   * <li>{@link Resource#LIVE}</li>
   * <li>{@link Resource#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static long getVersion(String version) {
    if ("live".equals(version) || "index".equals(version)) {
      return Resource.LIVE;
    } else if ("work".equals(version)) {
      return Resource.WORK;
    } else {
      try {
        return Long.parseLong(version);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

  /**
   * Returns the document name for the given version. For the live version, this
   * method will return <code>index.xml</code>. Available versions are:
   * <ul>
   * <li>{@link Resource#LIVE}</li>
   * <li>{@link Resource#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getDocument(long version) {
    if (version == Resource.LIVE)
      return "index.xml";
    else if (version == Resource.WORK)
      return "work.xml";
    else
      return Long.toString(version) + ".xml";
  }

  /**
   * Returns the version identifier for the given version. Available versions
   * are:
   * <ul>
   * <li>{@link Resource#LIVE}</li>
   * <li>{@link Resource#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getVersionString(long version) {
    if (version == Resource.LIVE)
      return "live";
    else if (version == Resource.WORK)
      return "work";
    else
      return Long.toString(version);
  }

}