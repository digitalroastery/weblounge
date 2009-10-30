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
 * An extension url represents a url that is used to express extensible url
 * domains like <tt>/news/*</tt> (all direct suburls) or <tt>/news/**</tt> (all
 * suburls).
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */
public class ExtensionUrl extends UrlImpl {

  /** Indicates that the url is not extended */
  public final static int EXTENSION_NONE = 0;

  /**
   * Extends the url by everything that is included in the current path, e. g.
   * with this extension set, the url <code>/news/index.jsp</code> is included
   * in the url <code>/news/</code><br>
   * A common representation of this kind of url extension is
   * <code>/news/*</code>.
   */
  public final static int EXTENSION_SUBDIRECTORY = 1;

  /**
   * Extends the url by everything that is included in the current path and all
   * subdirectories, e. g. with this extension set, the url
   * <code>/news/index. jsp</code> is included as well as
   * <code>/news/sports/index.jsp</code> in the url <code>/news/</code>.<br>
   * A common representation of this kind of url extension is
   * <code>/news/**</code>.
   */
  public final static int EXTENSION_SUBDIRECTORIES = 2;

  /** type of url extension */
  private int extension_;

  /**
   * Constructor for a url. Since this constructor has package access, use
   * <code>getUrl(<i>type</i>)</code> from the site navigation to obtain a
   * reference to a concrete url instance.
   * 
   * @param path
   *          the url path
   */
  ExtensionUrl(String path) {
    super(path, '/');
    extension_ = EXTENSION_NONE;
  }

  /**
   * Returns the webapp-relativ url for this page, e. g.
   * <code>/news/articles/</code>.
   * 
   * @return the path
   */
  public String getPath() {
    return super.getPath() + getExtension();
  }

  /**
   * Returns the encoded url to be used when calling the url through the web
   * application.
   * <p>
   * What this method does in detail is adding the extension to the path.
   * 
   * @return the encoded url
   */
  public String encode() {
    String encUrl = concat(getPath(), getExtension());
    return encUrl;
  }

  /**
   * Returns the hash code for this url. The method includes the
   * superimplementation and adds sensitivity for the site and the url
   * extension.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return super.hashCode() | extension_;
  }

  /**
   * Returns true if the given object is a url itself and describes the same url
   * than this object, including the associated site and possible url
   * extensions.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object object) {
    if (object instanceof Url) {
      return super.equals(object);
    } else if (object instanceof ExtensionUrl) {
      ExtensionUrl url = (ExtensionUrl) object;
      return (super.equals(object) && (extension_ == url.extension_));
    }
    return false;
  }

  /**
   * Extends the url by the given extension, replacing any previously added
   * extension. By extending a url it is possible to find prefixes of this url
   * and the like. <br>
   * Possible extensions are
   * <ul>
   * <li>{@link #EXTENSION_NONE}</li>
   * <li>{@link #EXTENSION_SUBDIRECTORY}</li>
   * <li>{@link #EXTENSION_SUBDIRECTORIES}</li>
   * </ul>
   * 
   * @param extension
   *          the url extesion
   */
  public void extend(int extension) {
    extension_ = extension;
  }

  /**
   * Returns true if the given url is included. This is the case if both urls
   * are equal or if the given url is included in this one by means of a url
   * extension.
   * 
   * @param url
   *          the url to test for inclusion
   * @return true if the given url is included
   */
  public boolean includes(ExtensionUrl url) {

    // if both urls are equal, then it depends on both extension types
    // wheter this url includes the other or not
    if (getPath().equals(url.getPath())) {
      switch (extension_) {
      case EXTENSION_NONE:
        return (url.extension_ == EXTENSION_NONE);
      case EXTENSION_SUBDIRECTORY:
        return (url.extension_ < EXTENSION_SUBDIRECTORIES);
      case EXTENSION_SUBDIRECTORIES:
        return true;
      }
    } else if (url.getPath().startsWith(getPath())) {
      switch (extension_) {
      case EXTENSION_NONE:
        break;
      case EXTENSION_SUBDIRECTORY:
        int start = getPath().length();
        if (url.getPath().indexOf("/", start) == -1)
          return true;
        break;
      case EXTENSION_SUBDIRECTORIES:
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the extension type, wich is one of the following:
   * <ul>
   * <li>{@link #EXTENSION_NONE}</li>
   * <li>{@link #EXTENSION_SUBDIRECTORY}</li>
   * <li>{@link #EXTENSION_SUBDIRECTORIES}</li>
   * </ul>
   * 
   * @return the extension type
   */
  public int getExtensionType() {
    return extension_;
  }

  /**
   * Returns a string representation of the url extension. Depending on the
   * extension this is: the empty string, <code>*</code> or <code>**</code>.
   * 
   * @return the url extension
   */
  public String getExtension() {
    switch (extension_) {
    case EXTENSION_NONE:
      return "";
    case EXTENSION_SUBDIRECTORY:
      return "*";
    case EXTENSION_SUBDIRECTORIES:
      return "**";
    }
    return "";
  }

}