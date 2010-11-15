/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * This class contains utility methods intended to facilitate dealing with
 * resource versions and names.
 */
public final class ResourceUtils {

  /**
   * This class is not intended to be instantiated.
   */
  private ResourceUtils() {
    // Nothing to do here
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param resource
   *          the resource
   * @param request
   *          the client request
   * @return <code>true</code> if the page is more recent than the version that
   *         is cached at the client.
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> header cannot be converted
   *           to a date.
   */
  public static boolean isModified(Resource<?> resource,
      HttpServletRequest request) throws IllegalArgumentException {
    long cachedModificationDate = request.getDateHeader("If-Modified-Since");
    Date resourceModificationDate = resource.getModificationDate();
    return cachedModificationDate < resourceModificationDate.getTime();
  }

  /**
   * Returns <code>true</code> if the resource has not been modified,
   * <code>false</code> if the cached version on the client side is out dated or
   * if the request did not contain caching information.
   * <p>
   * The decision is based on the availability and value of the
   * <code>If-None-Match</code> header (called <code>ETag</code>).
   * <p>
   * Note that if <code>language</code> is <code>null</code>, the
   * <code>ETag</code> calculation will be performed on the resource level
   * rather than the resource content.
   * 
   * @param resource
   *          the resource
   * @param request
   *          the client request
   * @return <code>true</code> if the resource's calculated eTag matches the one
   *         specified
   * @throws IllegalArgumentException
   *           if the <code>If-Modified-Since</code> cannot be converted to a
   *           date.
   */
  public static boolean isMismatch(Resource<?> resource, Language language,
      HttpServletRequest request) throws IllegalArgumentException {
    String eTagHeader = request.getHeader("If-None-Match");
    if (StringUtils.isBlank(eTagHeader))
      return true;
    String eTag = getETagValue(resource, language);
    return !eTagHeader.equals("\"" + eTag + "\"");
  }

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
  public static boolean isMismatch(String eTag, HttpServletRequest request)
      throws IllegalArgumentException {
    String eTagHeader = request.getHeader("If-None-Match");
    if (StringUtils.isBlank(eTagHeader))
      return true;
    return !eTagHeader.equals("\"" + eTag + "\"");
  }

  /**
   * Returns the value for the <code>ETag</code> header field, which is
   * calculated from the resource identifier, the language identifier and the
   * resource's modification date.
   * <p>
   * Note that if <code>language</code> is <code>null</code>, the
   * <code>ETag</code> calculation will be performed on the resource level
   * rather than the resource content.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the requested language
   * @return the <code>ETag</code> value
   */
  public static String getETagValue(Resource<?> resource, Language language) {
    long etag = resource.getIdentifier().hashCode();
    if (language != null)
      etag += language.getIdentifier().hashCode();
    etag += resource.getModificationDate().getTime();
    return new StringBuffer().append("\"").append(etag).append("\"").toString();
  }

  /**
   * Returns the value for the <code>ETag</code> header field, which is
   * calculated from the resource identifier, the language identifier and the
   * resource's modification date.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the requested language
   * @param style
   *          the image style
   * @return the <code>ETag</code> value
   */
  public static String getETagValue(Resource<?> resource, Language language,
      ImageStyle style) {
    if (style == null)
      return getETagValue(resource, language);
    long etag = resource.getIdentifier().hashCode();
    if (language != null)
      etag += language.getIdentifier().hashCode();
    etag += style.getIdentifier().hashCode();
    etag += resource.getModificationDate().getTime();
    return new StringBuffer().append("\"").append(etag).append("\"").toString();
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