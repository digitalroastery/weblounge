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

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.url.Url;

/**
 * This class implements common url and path behavior.
 */
public class UrlImpl implements Url {

  /** Default path separator */
  private static final char URL_PATH_SEPARATOR = '/';

  /** The url that is represented */
  protected String path = null;

  /** The path separator character */
  protected char separatorChar = 0;

  /**
   * Creates a new url from a given path and using the default path separator
   * <code>"/"</code>.
   * 
   * @param path
   *          the url path
   */
  public UrlImpl(String path) {
    this(path, URL_PATH_SEPARATOR);
  }

  /**
   * Creates a new url from a given path and using the default path separator
   * <code>"/"</code>.
   * 
   * @param url
   *          the parent url
   * @param path
   *          the url path
   */
  public UrlImpl(Url url, String path) {
    this(concat(url.getPath(), path, url.getPathSeparator()), url.getPathSeparator());
  }

  /**
   * Creates a new url from a given path.
   * 
   * @param path
   *          the url path
   * @param separator
   *          the path separator
   */
  public UrlImpl(String path, char separator) {
    if (path == null)
      throw new IllegalArgumentException("Url path cannot be null");
    if (path.length() == 0 || path.charAt(0) != separator)
      throw new IllegalArgumentException("Url path must be absolute");
    this.separatorChar = separator;
    this.path = trim(path);
  }

  /**
   * Creates a url pointing to root with the given separator.
   * 
   * @param separator
   *          the path separator character
   */
  protected UrlImpl(char separator) {
    this.separatorChar = separator;
    this.path = "/";
  }

  /**
   * Returns the url separator character, like '/' in web urls.
   * 
   * @return the separator character
   */
  public char getPathSeparator() {
    return separatorChar;
  }

  /**
   * Returns url path, e. g.<code>/news/articles/</code>.
   * 
   * @return the url path
   * @see ch.o2it.weblounge.common.url.Url#getPath()
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the parent path or <code>null</code> if the current path has now
   * parent, i. e. the current path represents the root.
   * 
   * @return the parent path
   */
  protected String getParentPath() {
    String separator = Character.toString(separatorChar);
    if (path.equals(separator))
      return null;
    String p = path;
    if (p.endsWith(separator))
      p = path.substring(0, path.length() - 1);
    int lastSeparator = p.lastIndexOf(separatorChar);
    return (lastSeparator > 0) ? path.substring(0, lastSeparator) : separator;
  }

  /**
   * Returns the hash code for this url which equals the hash code taken from
   * the url path.
   * <p>
   * <b>Note:</b> This hash code is not site dependent. Overwrite this method if
   * you want to be able to distinguish between two urls with the same paths but
   * from different sites.
   * 
   * @return the url hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return path.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is of type
   * <code>BaseUrl</code> object literally representing the same instance than
   * this one.
   * 
   * <b>Note:</b> This implementation is not site sensitive. Overwrite it if you
   * want to be able to distinguish between two urls with the same paths but
   * from different sites.
   * 
   * @param obj
   *          the object to test for equality
   * @return <code>true</code> if <code>obj</code> represents the same
   *         <code>BaseUrl</code>
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof UrlImpl) {
      UrlImpl url = (UrlImpl) obj;
      return path.equals(url.path);
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the url contains the given path.
   * 
   * @param path
   *          the path
   * @return <code>true</code> if the path is contained in this url
   */
  public boolean contains(String path) {
    if (path == null)
      throw new IllegalArgumentException("Cannot contain null string!");
    String trimmedPathelement = trim(path);
    int found = this.path.indexOf(trimmedPathelement);
    return found != -1;
  }

  /**
   * Returns <code>true</code> if this url is a prefix of <code>url</code> by
   * means of the implementation of <code>equals</code>. Note that this url is
   * also a prefix if it is identical to <code>url</code>.
   * 
   * <b>Note:</b> This implementation is not site sensitive. Overwrite it if you
   * want to be able to distinguish between two urls with the same paths but
   * from different sites.
   * 
   * @param url
   *          the url
   * @return <code>true</code> if this url is a prefix
   */
  public boolean isPrefixOf(Url url) {
    if (url == null)
      throw new IllegalArgumentException("Cannot be prepended by null!");
    return url.getPath().indexOf(path) == 0;
  }

  /**
   * Returns <code>true</code> if this url is an extension of <code>url</code>
   * by means of the implementation of <code>equals</code>. Note that this url
   * is also an extension if it is identical to <code>url</code>.
   * 
   * <b>Note:</b> This implementation is not site sensitive. Overwrite it if you
   * want to be able to distinguish between two urls with the same paths but
   * from different sites.
   * 
   * @param url
   *          the url
   * @return <code>true</code> if this url is a prefix
   */
  public boolean isExtensionOf(Url url) {
    return path.indexOf(url.getPath()) == 0;
  }

  /**
   * Returns the string representation of this object, which is equal to the url
   * path.
   * 
   * @return the path
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return path;
  }

  /**
   * Concatenates the two urls with respect to leading and trailing slashes.
   * 
   * @return the concatenated url of the two arguments
   */
  protected static String concat(String prefix, String suffix,
      char separatorChar) {
    if (prefix == null)
      throw new IllegalArgumentException("Prefix cannot be null");
    if (suffix == null)
      throw new IllegalArgumentException("Suffix cannot be null");

    String separator = Character.toString(separatorChar);
    if (!prefix.endsWith(separator) && !suffix.startsWith(separator))
      prefix += separator;
    if (prefix.endsWith(separator) && suffix.startsWith(separator))
      suffix = suffix.substring(1);

    prefix += suffix;
    return prefix;
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
  protected String trim(String url) {
    if (url == null)
      throw new IllegalArgumentException("Url cannot be null");
    url.trim();
    String separator = Character.toString(separatorChar);
    url = url.replaceAll(separator + separator, separator);
    if (!url.startsWith(separator))
      url = separator + url;
    if (url.endsWith(separator) || (url.length() == 1))
      return url;
    int index = url.lastIndexOf(separator);
    index = url.indexOf(".", index);
    if (index < 0)
      url += separator;
    return url;
  }

}