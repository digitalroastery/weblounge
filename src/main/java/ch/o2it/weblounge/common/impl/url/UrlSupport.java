/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.site.Site;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * <code>UrlSupport</code> is a helper class to deal with urls.
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */

public class UrlSupport {

  /**
   * This class should not be instanciated, since it only provides static
   * utility methods.
   */
  private UrlSupport() {
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
   * Concatenates the two urls with respect to leading and trailing slashes.
   * <p>
   * Note that returned path will only end with a slash if <code>suffix</code>
   * does. If you need a trailing slash, see
   * {@link #concat(String, String, boolean)}.
   * 
   * @return the concatenated url of the two arguments
   */
  public static String concat(String prefix, String suffix) {
    return concat(prefix, suffix, false);
  }

  /**
   * Concatenates the two urls with respect to leading and trailing slashes. The
   * path will always end with a trailing slash.
   * 
   * @return the concatenated url of the two arguments
   */
  public static String concat(String prefix, String suffix, boolean close) {
    if (prefix == null)
      throw new IllegalArgumentException("Argument prefix is null");
    if (suffix == null)
      throw new IllegalArgumentException("Argument suffix is null");

    prefix = checkSeparator(prefix);
    suffix = checkSeparator(suffix);
    prefix = removeDoubleSeparator(prefix);
    suffix = removeDoubleSeparator(suffix);

    if (!prefix.endsWith("/") && !suffix.startsWith("/"))
      prefix += "/";
    if (prefix.endsWith("/") && suffix.startsWith("/"))
      suffix = suffix.substring(1);

    prefix += suffix;

    // Close?
    if (close && !prefix.endsWith("/")) {
      prefix += "/";
    }
    return prefix;
  }

  /**
   * Concatenates the urls with respect to leading and trailing slashes.
   * 
   * @param parts
   *          the parts to concat
   * @return the concatenated url
   */
  public static String concat(String[] parts) {
    if (parts == null)
      throw new IllegalArgumentException("Argument parts is null");
    if (parts.length == 0)
      throw new IllegalArgumentException("Array parts is empty");
    String path = parts[0];
    for (int i = 1; i < parts.length; i++) {
      path = concat(path, parts[i]);
    }
    return path;
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
      throw new IllegalArgumentException("Argument url is null");

    url = checkSeparator(url);
    url.trim();

    if (url.endsWith("/") || (url.length() == 1))
      return url;

    int index = url.lastIndexOf("/");
    index = url.indexOf(".", index);
    if (index == -1)
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
    String link = concat(new String[] { Env.getURI(), Env.getServletPath(), path });
    return link;
  }

  /**
   * Checks that the path only contains the web path separator "/". If not,
   * wrong ones are replaced.
   */
  private static String checkSeparator(String path) {
    String sp = File.separator;
    if (sp.equals("\\"))
      sp = "\\\\";
    return path.replaceAll(sp, "/");
  }

  /**
   * Removes any occurence of double separators ("//") and replaces it with "/".
   * 
   * @param path
   *          the path to check
   * @return the corrected path
   */
  private static String removeDoubleSeparator(String path) {
    int index = 0;
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
      int value = (new Character(original.charAt(i))).charValue();
      // a-z
      if (value >= new Character('a').charValue() && value <= new Character('z').charValue()) {
        continue;
      }
      // A-Z
      if (value >= new Character('A').charValue() && value <= new Character('Z').charValue()) {
        continue;
      }
      // 0-9
      if (value >= new Character('0').charValue() && value <= new Character('9').charValue()) {
        continue;
      }
      // Special characters
      if ((value == new Character('-').charValue()) || (value == new Character('_').charValue()) || (value == new Character('.').charValue()) || (value == new Character(',').charValue()) || (value == new Character(';').charValue())) {
        continue;
      }
      return new Character(original.charAt(i));
    }
    return null;
  }

}