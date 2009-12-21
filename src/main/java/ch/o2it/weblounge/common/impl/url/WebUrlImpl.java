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

import static ch.o2it.weblounge.common.request.RequestFlavor.html;
import static ch.o2it.weblounge.common.request.RequestFlavor.json;
import static ch.o2it.weblounge.common.request.RequestFlavor.xml;

import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.Url;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A web url represents a url that is used to address locations within the web
 * application, such as HTML pages or module actions.
 */
public class WebUrlImpl extends UrlImpl implements WebUrl {

  /** Serial version uid */
  private static final long serialVersionUID = -5815146954734580746L;

  /** The logging facility */
  private static Logger log_ = LoggerFactory.getLogger(WebUrlImpl.class);

  /** Regular expression used to take urls apart */
  private final static Pattern inspector = Pattern.compile("^(.*)/(work|original|index|[0-9]*)(_[a-zA-Z0-9]+)?\\.([a-zA-Z0-9]+)$");

  /** The associated site */
  protected Site site = null;

  /** The url version */
  protected long version = -1;

  /** The link */
  private transient String link_ = null;

  /** The url flavor */
  protected String flavor = null;

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
    this(site, path, Page.LIVE, html.toString());
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
    this(site, url.getPath(), Page.LIVE, html.toString());
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
  public WebUrlImpl(Site site, Url url, String path) {
    this(site, concat(url.getPath(), path, '/'), Page.LIVE, html.toString());
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
    this(site, path, version, html.toString());
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
   * @param flavor
   *          the url flavor, e. g. <code>HTML</code>
   */
  public WebUrlImpl(Site site, String path, long version, String flavor) {
    super('/');
    this.site = site;
    this.path = inspect(path, '/');
    if (this.version < 0)
      this.version = version;
    if (this.flavor == null)
      this.flavor = flavor;
    if (this.flavor == null)
      throw new IllegalStateException("Flavor information missing from url");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink()
   */
  public String getLink() {
    if (link_ == null) {
      try {
        link_ = URLEncoder.encode(getLink(version, flavor), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        log_.error("Unexpected error while urlencoding link " + link_, e);
      }
      link_ = link_.replaceAll("%2F", "/");
    }
    return link_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(long)
   */
  public String getLink(long version) {
    return getLink(version, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(java.lang.String)
   */
  public String getLink(String flavor) {
    return getLink(version, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(long, java.lang.String)
   */
  public String getLink(long version, String flavor) {
    StringBuffer selector = new StringBuffer();
    if (version == Page.WORK) {
      selector.append("work");
    } else if (version == Page.LIVE) {
      selector.append("index");
    } else {
      selector.append(Long.toString(version));
    }
    selector.append(".").append(flavor);
    return UrlSupport.concat(new String[] {
        Env.getMountpoint(),
        Env.getServletPath(),
        path,
        selector.toString() });
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getFlavor()
   */
  public String getFlavor() {
    return flavor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getVersion()
   */
  public long getVersion() {
    return version;
  }

  /**
   * Returns the hash code for this url. The method includes the super
   * implementation and adds sensitivity for the site and the url extension.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return super.hashCode() | site.hashCode() >> 16;
  }

  /**
   * Returns true if the given object is a url itself and describes the same url
   * than this object, including the associated site and possible url
   * extensions.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object object) {
    if (object instanceof WebUrl) {
      WebUrl url = (WebUrl) object;
      return (super.equals(object) && version == url.getVersion() && flavor.equals(url.getFlavor()) && site.equals(url.getSite()));
    } else if (object instanceof Url) {
      return super.equals(object);
    }
    return false;
  }

  /**
   * Strips version and flavor from this url.
   * 
   * @param path
   *          the full path
   * @param separator
   *          path separator character
   * @return the directory path
   */
  protected String inspect(String path, char separator) {
    Matcher m = inspector.matcher(path);
    if (m.matches()) {
      String v = m.group(2);
      if ("".equals(v) || "index".equals(v)) {
        this.version = Page.LIVE;
      } else if ("work".equals(v)) {
        this.version = Page.WORK;
      } else {
        try {
          this.version = Long.parseLong(v);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unable to extract version from url " + path);
        }
      }
      String f = m.group(4);
      this.flavor = "".equals(f) ? html.toString() : f;
      return trim(m.group(1));
    }

    // Make sure we've not been fooled
    if (path.indexOf('.') > 0)
      throw new IllegalArgumentException("Path is malformed: " + path);

    // Test for well-known flavors in the last path element
    path = path.toLowerCase();
    if (path.endsWith(html.toString()))
      this.flavor = html.toString();
    else if (path.endsWith(xml.toString()))
      this.flavor = xml.toString();
    else if (path.endsWith(json.toString()))
      this.flavor = json.toString();
    return trim(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.url.UrlImpl#toString()
   */
  @Override
  public String toString() {
    return getLink();
  }

}