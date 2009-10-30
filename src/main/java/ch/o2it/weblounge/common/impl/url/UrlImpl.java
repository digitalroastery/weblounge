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

import ch.o2it.weblounge.common.url.Url;

/**
 * This class implements common url behavior.
 */
public class UrlImpl implements Url {

  /** The url that is represented */
  private String path_ = null;

  /** The url flavor */
  private String flavor_ = null;

  /** The path separator character */
  protected char separator = 0;

  /** The path separator as a string */
  private String separator_ = null;

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
    this.separator = separator;
    separator_ = Character.toString(separator);
    path_ = trim(path);
    flavor_ = extractFlavor(path);
  }

  /**
   * Returns the url separator character, like '/' in web urls.
   * 
   * @return the separator character
   */
  public char getSeparator() {
    return separator;
  }

  /**
   * Returns url path, e. g.<code>/news/articles/</code>.
   * 
   * @return the url path
   * @see ch.o2it.weblounge.common.url.Url#getPath()
   */
  public String getPath() {
    return path_;
  }

  /**
   * Returns the url flavor. For example, in case of "index.xml" the flavor will
   * be <code>xml</code>.
   * 
   * @return the url flavor
   */
  public String getFlavor() {
    return flavor_;
  }

  /**
   * Sets the url flavor.
   * 
   * @param flavor
   *          the flavor
   */
  public void setFlavor(String flavor) {
    flavor_ = flavor;
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
    return path_.hashCode();
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
      return path_.equals(url.path_);
    }
    return false;
  }

  /**
   * Appends <code>url</code> to the existing url path with respect to leading
   * and trailing slashes.
   * 
   * @param path
   *          the path to append
   */
  public String append(String path) {
    if (path == null)
      throw new IllegalArgumentException("Cannot append null string!");
    path_ = concat(path_, trim(path));
    return path_;
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
    return path_.indexOf(path) > -1;
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
  public boolean isPrefix(Url url) {
    if (url == null)
      throw new IllegalArgumentException("Cannot be prepended by null!");
    return url.getPath().indexOf(path_) == 0;
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
  public boolean isExtension(Url url) {
    return path_.indexOf(url.getPath()) == 0;
  }

  /**
   * Returns the string representation of this object, which is equal to the url
   * path.
   * 
   * @return the path
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return path_;
  }

  /**
   * Concatenates the two urls with respect to leading and trailing slashes.
   * 
   * @return the concatenated url of the two arguments
   */
  protected String concat(String prefix, String suffix) {
    if (prefix == null)
      throw new IllegalArgumentException("Argument prefix is null, suffix is " + suffix);
    if (suffix == null)
      throw new IllegalArgumentException("Argument suffix is null");

    if (!prefix.endsWith(separator_) && !suffix.startsWith(separator_))
      prefix += separator_;
    if (prefix.endsWith(separator_) && suffix.startsWith(separator_))
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
      throw new IllegalArgumentException("Argument url is null");
    url.trim();
    if (url.endsWith(separator_) || (url.length() == 1))
      return url;

    int index = url.lastIndexOf(separator_);
    index = url.indexOf(".", index);
    if (index < 0)
      url += separator_;
    return url;
  }

  /**
   * Returns the flavor or <code>null</code> if there is no flavor.
   * 
   * @param path
   *          the url path
   * @return the flavor
   */
  protected String extractFlavor(String path) {
    if (path == null)
      return null;
    if (separator <= 0)
      return null;
    int dotSeparator = path.lastIndexOf('.');
    if (dotSeparator > path.lastIndexOf(separator))
      return path.substring(dotSeparator + 1);
    return null;
  }

}