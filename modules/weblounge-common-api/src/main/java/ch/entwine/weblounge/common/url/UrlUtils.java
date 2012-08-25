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

package ch.entwine.weblounge.common.url;

import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

/**
 * <code>UrlUtils</code> is a helper class to deal with urls.
 */
public final class UrlUtils {

  /**
   * This class should not be instantiated, since it provides static utility
   * methods only.
   */
  private UrlUtils() {
    // Nothing to be done here
  }

  /**
   * Sorts the given urls by path.
   * 
   * @param urls
   *          the urls to sort
   * @return the sorted urls
   */
  public static String[] sort(String[] urls) {
    TreeSet<String> set = new TreeSet<String>();
    for (int i = 0; i < urls.length; i++)
      set.add(urls[i]);
    String[] result = new String[urls.length];
    Iterator<String> i = set.iterator();
    int index = 0;
    while (i.hasNext()) {
      result[index++] = i.toString();
    }
    return result;
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
  public static String concat(String... urlElements)
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
   * Returns the trimmed url. Trimmed means that the url is free from leading or
   * trailing whitespace characters, and that a directory url like
   * <code>/news/</code> is closed by a slash (<code>/</code>).
   * 
   * @param url
   *          the url to trim
   * @return the trimmed url
   */
  public static String trim(String url) {
    if (url == null)
      throw new IllegalArgumentException("Url cannot be null");

    url = checkSeparator(url);
    url = removeDoubleSeparator(url);
    url = url.trim();

    if (url.endsWith("/") || (url.length() == 1))
      return url;

    int index = url.lastIndexOf("/");
    int dotIndex = url.indexOf(".", index);
    int anchorIndex = url.indexOf("#", index);
    if (dotIndex == -1 && anchorIndex == -1)
      url += "/";
    return url;
  }

  /**
   * Returns the link created from the given partition and path. This link will
   * include the weblounge mountpoint.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the database path
   * @return the link
   */
  public static String getLink(Site site, String path) {
    return path;
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
   * Returns <code>true</code> if url <code>a</code> is a direct prefix of url
   * <code>b</code>. For example, <code>/news</code> is the parent of
   * <code>/news/today</code>.
   * <p>
   * Note that <code>a</code> is also an extended prefix of <code>b</code> if
   * <code>a</code> and <code>b</code> are equal.
   * 
   * @param a
   *          the first url
   * @param b
   *          the second url
   * @return <code>true</code> if <code>a</code> is the direct prefix of
   *         <code>b</code>
   */
  public static boolean isPrefix(String a, String b) {
    if (isExtendedPrefix(a, b)) {
      if (a.length() < b.length()) {
        String bRest = b.substring(a.length() + 1);
        if (bRest.endsWith("/"))
          bRest = bRest.substring(0, bRest.length() - 2);
        return bRest.indexOf("/", 1) < 0;
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if url <code>a</code> is a prefix of url
   * <code>b</code>. For example, <code>/news</code> is an ancestor of
   * <code>/news/today/morning</code>.
   * <p>
   * Note that <code>a</code> is also an extended prefix of <code>b</code> if
   * <code>a</code> and <code>b</code> are equal.
   * 
   * @param a
   *          the first url
   * @param b
   *          the second url
   * @return <code>true</code> if <code>a</code> is a prefix of <code>b</code>
   */
  public static boolean isExtendedPrefix(String a, String b) {
    if (b.startsWith(a)) {
      if (b.length() > a.length())
        return a.endsWith("/") || b.substring(a.length()).startsWith("/");
      else
        return true;
    }
    return false;
  }

  /**
   * Returns the url extension that <code>url</code> defines over
   * <code>prefix</code>. For example, the extension of url
   * <code>/news/today</code> and prefix <code>/news</code> is
   * <code>today</code>.
   * <p>
   * If <code>prefix</code> is not a prefix of <code>url</code>, this method
   * returns <code>null</code>, if <code>url</code> and <code>prefix</code>
   * match, the empty string is returned.
   * 
   * @param prefix
   *          the url prefix
   * @param url
   *          the url
   * @return the url extension over the prefix
   */
  public static String getExtension(String prefix, String url) {
    prefix = prefix.trim();
    if (isExtendedPrefix(prefix, url)) {
      if (url.length() > prefix.length()) {
        String extension = url.substring(prefix.length() + 1);
        if (extension.endsWith("/")) {
          extension = extension.substring(0, extension.length() - 1);
        }
        return extension;
      } else
        return "";
    }
    return null;
  }

  /**
   * Returns the extension that is encoded into the url. Possible extensions
   * are:
   * <ul>
   * <li>/*</li>
   * <li>/**</li>
   * <li><code>null</code></li>
   * </ul>
   * 
   * @param url
   *          the url with extension
   * @return the url extension or <code>null</code> if no extension can be found
   */
  public static String getExtension(String url) {
    if (url.endsWith("/**"))
      return "/**";
    else if (url.endsWith("/*"))
      return "/*";
    else
      return null;
  }

  /**
   * Strips off the extension and returns the pure url.
   * 
   * @param url
   *          the url with extension
   * @return the url
   */
  public static String stripExtension(String url) {
    String extension = getExtension(url);
    if (extension == null)
      return url;
    else
      return url.substring(0, url.length() - extension.length());
  }

  /**
   * Returns <code>true</code> if the url is valid, that is, if it contains only
   * allowed characters.
   * 
   * @return <code>true</code> or the invalid character
   */
  public static boolean isValid(String url) {
    return (checkUrl(url) == null);
  }

  /**
   * Returns <code>null</code> if the url is valid, that is, if it contains only
   * allowed characters. otherwise, the invalid character is returned.
   * 
   * @return <code>null</code> or the invalid character
   */
  public static Character getInvalidCharacter(String url) {
    Character c = checkUrl(url);
    return c;
  }

  /**
   * Returns <code>null</code> if the url is valid, that is, if it contains only
   * allowed characters. otherwise, the invalid character is returned.
   * 
   * @return <code>null</code> or the invalid character
   */
  private static Character checkUrl(String url) {
    StringBuffer original = new StringBuffer(url);
    for (int i = 0; i < original.length(); i++) {
      int value = original.charAt(i);
      // a-z
      if (value >= 'a' && value <= 'z') {
        continue;
      }
      // A-Z
      if (value >= 'A' && value <= 'Z') {
        continue;
      }
      // 0-9
      if (value >= '0' && value <= '9') {
        continue;
      }
      // Special characters
      if (value == '-' || value == '_' || value == '.' || value == ',' || value == ';') {
        continue;
      }
      return original.charAt(i);
    }
    return null;
  }

  /**
   * Returns the request url as a string.
   * 
   * @param request
   *          the request
   * @param includePath
   *          <code>true</code> to also include the request uri
   * @param includeQuery
   *          <code>true</code> to include the query string
   * @return the url as a string
   */
  public static URL toURL(HttpServletRequest request, boolean includePath,
      boolean includeQuery) {
    try {
      StringBuffer buf = new StringBuffer(request.getScheme());
      buf.append("://");
      buf.append(request.getServerName());
      if (request.getServerPort() != 80)
        buf.append(":").append(request.getServerPort());
      if (includePath && request.getRequestURI() != null)
        buf.append(request.getRequestURI());
      if (includeQuery && StringUtils.isNotBlank(request.getQueryString()))
        buf.append(request.getQueryString());
      return new URL(buf.toString());
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

}