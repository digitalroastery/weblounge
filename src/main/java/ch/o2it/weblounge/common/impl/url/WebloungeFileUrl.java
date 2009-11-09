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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * File url encodes url information to locate a file through the web
 * application, so it is not a normal file path but a webapp-path
 */
public class WebloungeFileUrl extends UrlImpl {

  /** The preferred language */
  private Language language_;

  /** The default language */
  private Language defaultLanguage_;

  /** The multilingual path version */
  private String multilingualPath_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = WebloungeFileUrl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new weblounge specific url object. The url should be passed
   * relative to the weblounge root folder, e. g.
   * <code>/shared/modules/home/index.jsp</code>.
   * 
   * @param path
   *          the weblounge relative url.
   */
  public WebloungeFileUrl(String path) {
    this(path, null, null);
  }

  /**
   * Creates a new weblounge specific url object. The url should be passed
   * relative to the weblounge root folder, e. g.
   * <code>/shared/modules/home/index.jsp</code>.
   * 
   * @param path
   *          the weblounge relative url.
   * @param language
   *          the requested language
   */
  public WebloungeFileUrl(String path, Language language) {
    this(path, null, null);
  }

  /**
   * Creates a new weblounge specific url object. The url should be passed
   * relative to the weblounge root folder, e. g.
   * <code>/shared/modules/home/index.jsp</code>.
   * 
   * @param path
   *          the weblounge relative url.
   * @param language
   *          the requested language
   * @param defaultLanguage
   *          the default language
   */
  public WebloungeFileUrl(File path, Language language, Language defaultLanguage) {
    this(path.getAbsolutePath().substring(Env.getRealPath("/").length() - 1), language, defaultLanguage);
  }

  /**
   * Creates a new weblounge specific url object. The url should be passed
   * relative to the weblounge root folder, e. g.
   * <code>/shared/modules/home/index.jsp</code>.
   * 
   * @param path
   *          the weblounge relative url.
   * @param language
   *          the requested language
   * @param defaultLanguage
   *          the default language
   */
  public WebloungeFileUrl(String path, Language language,
      Language defaultLanguage) {
    super(path, File.separatorChar);
    language_ = language;
    defaultLanguage_ = defaultLanguage;
  }

  /**
   * Returns the url encoded and ready to be sent as a request to the
   * application server, relative to the web application mountpoint.
   * <p>
   * For example, if your weblounge instance can be reached at
   * <code>http://localhost/weblounge/cms</code> this method will return for a
   * jsp renderer located in the site's <code>renderer</code> directory the path
   * <code>/sites/mysite/renderer/myjrenderer.jsp
	 * </code>
   * 
   * @return the encoded, ready to use url
   */
  public String getVirtualPath() {
    String uriReal = Env.getRealPath("/");
    String path = getMultilingualPath();
    if (path.startsWith(uriReal))
      path = path.substring(uriReal.length() - 1);
    return UrlSupport.trim(path);
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
    String path = PathSupport.trim(getMultilingualPath());
    return Env.getRealPath(path);
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
   * Returns the language specific version of the url path, which means that for
   * example the path <tt>/news/index.jsp</tt> is encoded as
   * <tt>/news/index_de.jsp</tt>.
   * 
   * @return the encoded url path
   */
  protected String getMultilingualPath() {
    if (multilingualPath_ != null) {
      return multilingualPath_;
    }
    if (language_ == null) {
      return getPath();
    }

    String path = LanguageSupport.getLanguageVariant(getPath(), language_);
    File file = new File(Env.getRealPath(path));
    if (file.exists() && file.canRead()) {
      multilingualPath_ = path;
      return path;
    }

    if (defaultLanguage_ != null) {
      log_.info("Lookup for file '" + file.getAbsolutePath() + "' failed. Lookup up default...");
      path = LanguageSupport.getLanguageVariant(getPath(), defaultLanguage_);
      file = new File(Env.getRealPath(path));
      if (file.exists() && file.canRead()) {
        multilingualPath_ = path;
        return path;
      }
    }

    log_.info("Lookup for file '" + file.getAbsolutePath() + "' failed. Looking up international version...");
    path = getPath();
    file = new File(Env.getRealPath(path));
    if (!file.exists() || !file.canRead()) {
      log_.error("Lookup for any version of file '" + file.getAbsolutePath() + "' failed.");
    }
    multilingualPath_ = path;
    return path;
  }

}