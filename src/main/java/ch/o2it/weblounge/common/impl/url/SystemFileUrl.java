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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;

import java.io.File;

/**
 * File url encodes url information to locate a file through the web
 * application, so it is not a normal file path but a webapp-path
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */

public class SystemFileUrl extends BaseUrl {

  /**
   * Creates a new weblounge specific url object. The url should be passed
   * relative to the weblounge root folder, e. g.
   * <code>/shared/modules/home/index.jsp</code>.
   * 
   * @param path
   *          the weblounge relative url.
   */
  public SystemFileUrl(String path) {
    super(path, File.separatorChar);
  }

  /**
   * This constructor creates a system file url from a <code>File</code> object.
   * It is important that the file is located within the weblounge root folder,
   * since the constructor will try to cut off the path identified by
   * <code>Env.getRealPath("/")</code>.
   * 
   * @param file
   *          the file
   */
  public SystemFileUrl(File file) {
    super(file.getAbsolutePath().substring(Env.getRealPath("/").length()), File.separatorChar);
  }

  /**
   * Returns the url encoded and ready to be sent as a request to the
   * application server, relative to the server root url.
   * <p>
   * For example, if your weblounge instance can be reached at
   * <code>http://localhost/weblounge/cms</code> this method will return for a
   * jsp renderer located in the site's <code>renderer</code> directory the path
   * <code>/weblounge/sites/mysite/renderer/myjrenderer.jsp
	 * </code>
   * 
   * @param webapp
   *          <code>true</code> to prepend the webapp uri
   * @return the encoded, ready to use url
   * @see #getVirtualPath()
   */
  public String getVirtualPath(boolean webapp) {
    String url = getPath();
    if (webapp)
      url = UrlSupport.concat(Env.getURI(), url);
    return url;
  }

  /**
   * Returns the url encoded and ready to be sent as a request to the
   * application server, relative to the webapplication mountpoint.
   * <p>
   * For example, if your weblounge instance can be reached at
   * <code>http://localhost/weblounge/cms</code> this method will return for a
   * jsp renderer located in the site's <code>renderer</code> directory the path
   * <code>/sites/mysite/renderer/myjrenderer.jsp
	 * </code>
   * 
   * @return the encoded url
   * @see #getVirtualPath(boolean)
   */
  public String getVirtualPath() {
    return getVirtualPath(false);
  }

  /**
   * Returns the virtual path to the first existing language variant or
   * <code>null</code> if no such variant exists.
   * 
   * @param languages
   *          the prioritized languages
   * @return the first existing language variant
   */
  public String getVirtualPath(Language[] languages) {
    return getVirtualPath(languages, false);
  }

  /**
   * Returns the virtual path to the first existing language variant or
   * <code>null</code> if no such variant exists.
   * 
   * @param languages
   *          the prioritized languages
   * @param webapp
   *          <code>true</code> to prepend the webapp uri
   * @return the first existing language variant
   */
  public String getVirtualPath(Language[] languages, boolean webapp) {
    for (int i = 0; i < languages.length; i++) {
      if (exists(languages[i])) {
        return ((webapp) ? Env.getURI() : "") + getPath(languages[i]);
      }
    }
    if (exists()) {
      return ((webapp) ? Env.getURI() : "") + getPath();
    }
    return null;
  }

  /**
   * Returns the url encoded as an absolute path in the file system. The path
   * may either point to the requested language version of the file, the site
   * default language version or just to the language neutral file version,
   * depending on whether a language has been specified at construction time and
   * on the existance of the respective files.
   * <p>
   * <b>Note:</b> If a language had been specified, and the language neutral
   * version is returned, then you have to make sure that the file does exist on
   * the server. If a language version is returned, then it is for sure that the
   * file exists.
   * 
   * @return the encoded url as an absolute file path
   */
  public String getPhysicalPath() {
    return Env.getRealPath(getPath());
  }

  /**
   * Returns the physical path to the first existing language variant or
   * <code>null</code> if no such variant exists.
   * 
   * @param languages
   *          the prioritized languages
   * @return the first existing language variant
   */
  public String getPhysicalPath(Language[] languages) {
    String url = getVirtualPath(languages);
    if (url != null) {
      return Env.getRealPath(url);
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the url exists for the given language.
   * <p>
   * Assuming the path were <tt>/xyz/abc.jsp</tt> and the language of interes is
   * <code>deutsch</code>. Then this method returns <code>true</code> if a file
   * exists at <tt>/xyz/abc_de.jsp</tt>.
   * 
   * @return <code>true</code> if the file exists in the special language
   *         version
   */
  public boolean exists() {
    File file = new File(getPhysicalPath());
    return (file.exists() && file.canRead());
  }

  /**
   * Returns <code>true</code> if the url exists for the given language.
   * <p>
   * Assuming the path were <tt>/xyz/abc.jsp</tt> and the language of interest
   * is <code>deutsch</code>. Then this method returns <code>true</code> if a
   * file exists at <tt>/xyz/abc_de.jsp</tt>.
   * 
   * @param language
   *          the language of interest
   * @return <code>true</code> if the file exists in the special language
   *         version
   */
  public boolean exists(Language language) {
    File file = new File(Env.getRealPath(getPath(language)));
    return (file.exists() && file.canRead());
  }

  /**
   * Returns <code>true</code> if the url exists.
   * 
   * @param path
   *          the path relative to the weblounge root folder
   * @return <code>true</code> if the file exists
   */
  public boolean exists(String path) {
    File file = new File(Env.getRealPath(path));
    return (file.exists() && file.canRead());
  }

  /**
   * Returns the language variant of the path.
   * 
   * @param language
   *          the language of interest
   * @return the language variant
   */
  public String getPath(Language language) {
    return LanguageSupport.getLanguageVariant(getPath(), language);
  }

}