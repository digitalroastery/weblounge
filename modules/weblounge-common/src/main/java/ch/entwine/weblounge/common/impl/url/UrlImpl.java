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

package ch.entwine.weblounge.common.impl.url;

import ch.entwine.weblounge.common.url.Path;

/**
 * This class implements common url and path behavior.
 */
public class UrlImpl implements Path {

  /** Serial version uid */
  private static final long serialVersionUID = 3314907623908539157L;

  /** Default path separator */
  private static final char URL_PATH_SEPARATOR = '/';

  /** The url that is represented */
  protected String path = null;

  /** The path separator */
  protected String pathElementSeparator = null;

  /** The path separator character */
  protected char pathElementSeparatorChar = 0;

  /** Static constant used to filter out double path separators */
  protected String doublePathElementSeparator = null;

  /**
   * Creates a new url from a given path using the default path separator
   * <code>"/"</code>.
   * 
   * @param path
   *          the url path
   */
  public UrlImpl(String path) {
    this(path, URL_PATH_SEPARATOR);
  }

  /**
   * Creates a new url from a given path using the default path separator
   * <code>"/"</code>.
   * 
   * @param url
   *          the parent url
   * @param path
   *          the url path
   */
  public UrlImpl(Path url, String path) {
    this(concat(url.getPath(), path, url.getPathSeparator()), url.getPathSeparator());
  }

  /**
   * Creates a new url from a given path and separator.
   * 
   * @param path
   *          the url path
   * @param separator
   *          the path separator
   */
  public UrlImpl(String path, char separator) {
    this.pathElementSeparatorChar = separator;
    this.pathElementSeparator = Character.toString(separator);

    // Build the double path separator
    StringBuffer buf = new StringBuffer();
    buf.append(separator).append(separator);
    if ('\\' == separator)
      buf.append(separator).append(separator);
    this.doublePathElementSeparator = buf.toString();

    if (path != null)
      setPath(path);
  }

  /**
   * Creates a url pointing to root with the given separator.
   * 
   * @param separator
   *          the path separator character
   */
  protected UrlImpl(char separator) {
    this(null, separator);
    this.path = "/";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#getPathSeparator()
   */
  public char getPathSeparator() {
    return pathElementSeparatorChar;
  }

  /**
   * Sets the path.
   * 
   * @param path the path
   */
  public void setPath(String path) {
    this.path = trim(path);
    if (this.path != null && !this.path.startsWith(Character.toString(pathElementSeparatorChar)))
      this.path = pathElementSeparatorChar + this.path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#getPath()
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
    String separator = Character.toString(pathElementSeparatorChar);
    if (path.equals(separator))
      return null;
    String p = path;
    if (p.endsWith(separator))
      p = path.substring(0, path.length() - 1);
    int lastSeparator = p.lastIndexOf(pathElementSeparatorChar);
    return (lastSeparator > 0) ? path.substring(0, lastSeparator) : separator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#startsWith(java.lang.String)
   */
  public boolean startsWith(String path) {
    if (path == null)
      throw new IllegalArgumentException("Cannot contain null string!");
    String trimmedPathelement = trim(path);
    return this.path.startsWith(trimmedPathelement);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#endsWith(java.lang.String)
   */
  public boolean endsWith(String path) {
    if (path == null)
      throw new IllegalArgumentException("Cannot contain null string!");
    String trimmedPathelement = trim(path);
    return this.path.endsWith(trimmedPathelement);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#isPrefixOf(ch.entwine.weblounge.common.url.Path)
   */
  public boolean isPrefixOf(Path url) {
    if (url == null)
      throw new IllegalArgumentException("Cannot be prepended by null!");
    return url.getPath().indexOf(path) == 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.Path#isExtensionOf(ch.entwine.weblounge.common.url.Path)
   */
  public boolean isExtensionOf(Path url) {
    return path.indexOf(url.getPath()) == 0;
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
      return null;
    url = url.trim().toLowerCase();
    url = url.replace(doublePathElementSeparator, pathElementSeparator);
    if (url.endsWith(pathElementSeparator) || url.equals(pathElementSeparator))
      return url;
    int index = url.lastIndexOf(pathElementSeparator);
    int dotIndex = url.indexOf(".", index);
    int anchorIndex = url.indexOf("#", index);
    if (dotIndex == -1 && anchorIndex == -1)
      url += pathElementSeparator;
    return url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return path.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Path) {
      Path url = (Path) obj;
      return path.equals(url.getPath());
    }
    return super.equals(obj);
  }

  /**
   * Returns the <code>String</code> representation of this object, which is
   * equal to the url's path as returned by {@link #getPath()}.
   * 
   * @return the path
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return path;
  }

}