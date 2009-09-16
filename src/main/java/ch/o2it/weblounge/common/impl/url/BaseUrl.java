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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The class <code>BaseUrl</code> implements common url behaviour with just the
 * <code>encode</code> method missing.
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */

public abstract class BaseUrl implements Url {

  /** The url that is represented by this site object */
  private String path_;

  /** The url flavor */
  private String flavor_;

  /** The separator character */
  protected char separator;

  /** The path separator */
  private String separator_;

  /** The tail url component */
  private UrlComponent tail_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = BaseUrl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new url from a given path. Since this constructor has package
   * access, use <code>getUrl(<i>type</i>)</code> from the site navigation to
   * obtain a reference to a certain url.
   * 
   * @param path
   *          the url path
   * @param separator
   *          the path separator
   */
  BaseUrl(String path, char separator) {
    this(separator);
    initFromString(path);
  }

  /**
   * Creates a new url given its tail component.
   * 
   * @param tail
   *          the url tail component
   * @param separator
   *          the path separator
   */
  BaseUrl(UrlComponent tail, char separator) {
    this(separator);
    initFromTail(tail);
  }

  /**
   * This private constructor serves to properly initialize any url, whether it
   * has been created from a string or a tail component.
   */
  private BaseUrl(char separator) {
    this.separator = separator;
    separator_ = Character.toString(separator);
    path_ = null;
  }

  /**
   * Returns the url separator character, like '/' in web urls.
   * 
   * @return the separator character
   */
  public char getSeparator() {
    return separator;
  }

  /*
   * ------------------------------------------------------------- I M P L E M E
   * N T A T I O N O F Url
   * -------------------------------------------------------------
   */

  /**
   * Returns url path, e. g.<code>/news/articles/</code>.
   * 
   * @return the url path
   * @see ch.o2it.weblounge.common.url.Url#getPath()
   */
  public String getPath() {
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
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
   * <b>Note:</b> This hash code is not site dependant. Overwrite this method if
   * you want to be able to distinguish between two urls with the same paths but
   * from different sites.
   * 
   * @return the url hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
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
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
    if (obj != null && obj instanceof BaseUrl) {
      BaseUrl url = (BaseUrl) obj;
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
  public void append(String path) {
    if (path == null)
      throw new IllegalArgumentException("Cannot append null string!");
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
    path_ = concat(path_, trim(path));
  }

  /**
   * Returns <code>true</code> if the url contains the given path.
   * 
   * @param path
   *          the path
   * @return <code>true</code> if the path is contained in this url
   */
  public boolean contains(String path) {
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
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
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
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
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
    return path_.indexOf(url.getPath()) == 0;
  }

  /**
   * Returns <code>true</code> if the url is pointing to a file rather than to a
   * directory node.
   * 
   * @return <code>true</code> if this url points to a file
   * @see #isDirectory()
   */
  public boolean isFile() {
    return (tail_.getPath().indexOf('.') >= 0);
  }

  /**
   * Returns <code>true</code> if the url is pointing to a directory rather than
   * to a file node.
   * 
   * @return <code>true</code> if this url points to a directory
   * @see #isFile()
   */
  public boolean isDirectory() {
    return !isFile();
  }

  /**
   * Returns the string representation of this object, which is equal to the url
   * path.
   * 
   * @return the path
   * @see java.lang.Object#toString()
   */
  public String toString() {
    path_ = (path_ == null) ? buildPathFromTail(tail_) : path_;
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
   * Initializes this url given its tail.
   */
  private void initFromTail(UrlComponent tail) {
    tail_ = tail;
    path_ = "";
    UrlComponent c = UrlComponent.getLease(tail.getPath(), tail.getPrevious());
    List<UrlComponent> components = new ArrayList<UrlComponent>();
    while (c != null) {
      path_ = separator_ + c.getPath() + path_;
      components.add(0, UrlComponent.getLease(c.getPath(), c.getPrevious()));
      c = c.getPrevious();
    }
    tail_.setUrl(this);
    log_.debug("Url [tail=" + tail_ + "] initialized");
  }

  /**
   * Initializes the url from the given String by taking it apart into its
   * components.
   */
  private void initFromString(String path) {
    path_ = trim(path);

    // Check for leading separator
    if (!path_.startsWith(separator_)) {
      path_ = separator_ + path_;
    }

    // Check if the url was the home url "/"

    if (path_.equals(separator_)) {
      UrlComponent c = UrlComponent.getLease(separator_, null);
      tail_ = c;
    } else {
      UrlTokenizer tok = new UrlTokenizer(path_, separator_);
      UrlComponent previous = null;
      UrlComponent c = null;
      while (tok.hasMoreElements()) {
        if (c == null) {
          c = UrlComponent.getLease(tok.nextElement().toString(), previous);
        } else {
          c.setPath(tok.nextElement().toString());
          c.setPrevious(previous);
        }
        previous = c;
      }
      tail_ = c;
    }

    tail_.setUrl(this);
  }

  /**
   * Returns the path that is formed by the url with <code>tail</code> as its
   * tail component. The path is derived by traversing the component chain.
   * 
   * @param tail
   *          the tail component
   * @return the url path
   */
  protected String buildPathFromTail(UrlComponent tail) {
    String path = "";
    UrlComponent c = tail;
    while (c != null) {
      path = separator_ + c.getPath() + path;
      c = c.getPrevious();
    }
    return path;
  }

  /**
   * Returns a url dump.
   * 
   * @return a url dump
   */
  public String dump() {
    StringBuffer result = new StringBuffer();
    UrlTokenizer components = new UrlTokenizer(this);
    while (components.hasMoreElements()) {
      if (result.length() > 0)
        result.append("->");
      result.append(components.nextElement().toString());
    }
    return result.toString();
  }

  /**
   * This method is called if this object is about to be collected by the
   * garbage collector.
   * 
   * @see java.lang.Object#finalize()
   */
  protected void finalize() {
    UrlComponent.returnLease(tail_);
  }

}