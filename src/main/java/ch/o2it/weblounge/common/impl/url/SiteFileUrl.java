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
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * File url encodes url information to locate a file through the web
 * application, so it is not a normal file path but a webapp-path
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */

public class SiteFileUrl extends UrlImpl {

  /** The associated site */
  private Site site_;

  /** The preferred language */
  private Language language_;

  /** The multilingual path version */
  private String multilingualPath_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = SiteFileUrl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new site specific url object. The url should be passed raltive to
   * the site root folder, e. g.<code>/templates/home.jsp</code>.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the site relative url.
   */
  public SiteFileUrl(Site site, String path) {
    super(path, File.separatorChar);
    site_ = site;
  }

  /**
   * Creates a new site specific url object. The url should be passed relative
   * to the site root folder, e. g.<code>/templates/home.jsp</code>. The
   * language object is used to determine the preferred language version of the
   * file, e. g. <code>myjsp_de.jsp</code> for the German version.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the site relative url.
   * @param language
   *          the language version
   */
  public SiteFileUrl(Site site, String path, Language language) {
    this(site, path);
    language_ = language;
  }

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return site_;
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
    return site_.getVirtualPath(getMultilingualPath(), webapp);
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
   * @return the encoded, ready to use url
   * @see #getVirtualPath(boolean)
   */
  public String getVirtualPath() {
    return site_.getVirtualPath(getMultilingualPath(), false);
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
    return site_.getPhysicalPath(getMultilingualPath());
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
    File file = new File(site_.getPhysicalPath(getMultilingualPath()));
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
    File file = new File(site_.getPhysicalPath(path));
    if (!file.exists() || !file.canRead()) {
      log_.info("Lookup for file '" + file.getAbsolutePath() + "' failed. Lookup up site default...");
      path = LanguageSupport.getLanguageVariant(getPath(), site_.getDefaultLanguage());
      file = new File(site_.getPhysicalPath(path));
      if (!file.exists() || !file.canRead()) {
        log_.info("Lookup for file '" + file.getAbsolutePath() + "' failed. Looking up international version...");
        path = getPath();
        file = new File(site_.getPhysicalPath(path));
        if (!file.exists() || !file.canRead()) {
          log_.error("Lookup for any version of file '" + file.getAbsolutePath() + "' failed.");
        }
      }
    }
    multilingualPath_ = path;
    return path;
  }

}