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

import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.Url;
import ch.o2it.weblounge.common.url.WebUrl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * A web url represents a url that is used to address locations within the
 * webapp, such as html pages or module actions.
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since Weblounge 1.0
 */

public class WebUrlImpl extends BaseUrl implements WebUrl {

  /** The associated site */
  private Site site_;

  /** The title in the currently active language */
  private LocalizableObject<String> titles_;

  /** The url version */
  private long version_;

  /** The link */
  private String link_;

  /**
   * Constructor for a url. Since this constructor has package access, use
   * <code>getUrl(<i>type</i>)</code> from the site navigation to obtain a
   * reference to a concrete url instance.
   * <p>
   * This object supports multilingual titles. The default title language is set
   * to the site default language and the version of this url is
   * <code>LIVE</code>.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   */
  public WebUrlImpl(Site site, String path) {
    this(site, path, Page.LIVE);
  }

  /**
   * Constructor for a url. Since this constructor has package access, use
   * <code>getUrl(<i>type</i>)</code> from the site navigation to obtain a
   * reference to a concrete url instance.
   * <p>
   * This object supports multilingual titles. The default title language is set
   * to the site default language and the version of this url is
   * <code>LIVE</code>.
   * 
   * @param site
   *          the associated site
   * @param url
   *          the url
   */
  public WebUrlImpl(Site site, Url url) {
    this(site, url.getPath(), Page.LIVE);
  }

  /**
   * Constructor for a url. Since this constructor has package access, use
   * <code>getUrl(<i>type</i>)</code> from the site navigation to obtain a
   * reference to a concrete url instance.
   * <p>
   * This object supports multilingual titles. The default title language is set
   * to the site default language.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the required version
   */
  public WebUrlImpl(Site site, String path, long version) {
    super(path, '/');
    site_ = site;
    titles_ = new LocalizableObject<String>();
    version_ = version;
  }

  /**
   * Constructor for a url based on the tail component. Since this constructor
   * has package access, use <code>getUrl(<i>type</i>)</code> from the site
   * navigation to obtain a reference to a concrete url instance.
   * <p>
   * This object supports multilingual titles. The default title language is set
   * to the site default language, and the version of this url is
   * <code>LIVE</code>.
   * 
   * @param site
   *          the associated site
   * @param c
   *          the url tail component
   */
  WebUrlImpl(Site site, UrlComponent c) {
    this(site, c, Page.LIVE);
  }

  /**
   * Constructor for a url based on the tail component. Since this constructor
   * has package access, use <code>getUrl(<i>type</i>)</code> from the site
   * navigation to obtain a reference to a concrete url instance.
   * <p>
   * This object supports multilingual titles. The default title language is set
   * to the site default language.
   * 
   * @param site
   *          the associated site
   * @param c
   *          the url tail component
   * @param version
   *          the required version
   */
  WebUrlImpl(Site site, UrlComponent c, long version) {
    super(c, '/');
    site_ = site;
    titles_ = new LocalizableObject<String>();
    titles_.setDefaultLanguage(site_.getDefaultLanguage());
    version_ = version;
  }

  /**
   * Extends the current url by <code>path</code>.
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#extend(java.lang.String)
   */
  public WebUrl extend(String path) {
    WebUrlImpl url = new WebUrlImpl(getSite(), this);
    url.append(path);
    return url;
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
   * Returns the encoded url to be used when calling the url through the web
   * application. <br>
   * What this method does in detail is prepending the mount point of the
   * webapplication and the servlet path obtained via <code>Env.getURI()</code>
   * and <code>getServletPath()</code> to the url.
   * <p>
   * The generated link is free from version information. It just points to the
   * correct database collection.
   * 
   * @return the encoded url
   */
  public String getLink() {
    if (link_ == null) {
      link_ = UrlSupport.concat(new String[] { Env.getURI(), Env.getServletPath(), getPath() });
      try {
        link_ = URLEncoder.encode(link_, "UTF-8");
      } catch (UnsupportedEncodingException e) {
      }
      link_ = link_.replaceAll("%2F", "/");
    }
    return link_;
  }

  /**
   * Returns the encoded url to be used when calling the url through the web
   * application. <br>
   * What this method does in detail is prepending the mount point of the
   * webapplication and the servlet path obtained via <code>Env.getURI()</code>
   * and <code>getServletPath()</code> to the url.
   * <p>
   * The parameter version is used to create includes to special versions of a
   * given page. Possible values are:
   * <ul>
   * <li>live</li>
   * <li>work</li>
   * <li>original</li>
   * </ul>
   * 
   * @param version
   *          the requested version
   * @return the encoded url
   */
  public String getLink(String version) {
    String selector = null;
    if (version != null && version.equals("work")) {
      selector = "work.xml";
      version_ = Page.WORK;
    } else if (version != null && version.equals("original")) {
      selector = "original.xml";
      version_ = Page.ORIGINAL;
    } else {
      selector = "index.xml";
      version_ = Page.LIVE;
    }
    return UrlSupport.concat(new String[] { getLink(), selector });
  }

  /**
   * Returns the version of this url. Possible versions are:
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.page.Page#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#WORK}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#ORIGINAL}</li>
   * </ul>
   * 
   * @return the url version
   */
  public long getVersion() {
    return version_;
  }

  /**
   * Sets the corresponding language version of the url title if that language
   * is supported by the associated site.
   * 
   * @param title
   *          the url title
   * @param language
   *          the language
   */
  public void setTitle(String title, Language language) {
    if (site_.supportsLanguage(language)) {
      titles_.put(title, language);
    }
  }

  /**
   * Returns the url title in the currently active language or <code>null</code>
   * if no such title exists.
   * 
   * @return the url title in the currently active language
   * @see ch.o2it.weblounge.common.language.Localizable#toString()
   */
  public String getTitle() {
    return titles_.get();
  }

  /**
   * Returns the url title in the requested language or <code>null</code> if the
   * title didn't exist in that language. Call
   * {@link #supportsLanguage(Language)} to find out about supported languages.
   * 
   * @param language
   *          the requested language
   * @return the title
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    Arguments.checkNull(language, "language");
    return titles_.get(language);
  }

  /**
   * Returns the hash code for this url. The method includes the
   * superimplementation and adds sensitivity for the site and the url
   * extension.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return super.hashCode() | site_.hashCode();
  }

  /**
   * Returns true if the given object is a url itself and describes the same url
   * than this object, including the associated site and possible url
   * extensions.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object object) {
    if (object instanceof WebUrlImpl) {
      WebUrlImpl url = (WebUrlImpl) object;
      return (super.equals(object) && site_.equals(url.site_));
    } else if (object instanceof Url) {
      return super.equals(object);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.language.Localizable#languages()
   */
  public Iterator<Language> languages() {
    return titles_.languages();
  }

  /**
   * Returns <code>true</code> if the url title has been provided in the given
   * language.
   * 
   * @param language
   *          the language
   * @return <code>true</code> if a title has been provided in that language
   * @see ch.o2it.weblounge.common.language.Localizable#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return titles_.supportsLanguage(language);
  }

  /**
   * Returns the url title in the currently active language or the output from
   * the superimplementation (which returns the url path) if no such title
   * exists.
   * 
   * @return the url title in the currently active language
   * @see ch.o2it.weblounge.common.language.Localizable#toString()
   */
  public String toString() {
    return titles_.toString();
  }

  /**
   * Returns the url title in the requested language or <code>null</code> if the
   * title didn't exist in that language. Call
   * {@link #supportsLanguage(Language)} to find out about supported languages.
   * 
   * @param language
   *          the requested language
   * @return the title
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    return titles_.toString(language);
  }

  /**
   * Returns the url title in the requested language or <code>null</code> if the
   * title didn't exist in that language. Call
   * {@link #supportsLanguage(Language)} to find out about supported languages.
   * 
   * @param language
   *          the requested language
   * @param force
   *          to force the given language
   * @return the title
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    return titles_.toString(language, force);
  }

  /**
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language language) {
    return titles_.compareTo(o, language);
  }

}