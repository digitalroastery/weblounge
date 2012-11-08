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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.Path;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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
  private static Logger logger = LoggerFactory.getLogger(WebUrlImpl.class);

  /** Regular expression for /path/to/resource/work_de.html */
  private static final Pattern pathInspector = Pattern.compile("^(.*)/(work|index|live|[0-9]*)(_[a-zA-Z]+)?\\.([a-zA-Z0-9]+)$");

  /** Regular expression for /path/to/resource/de/html */
  private static final Pattern segmentInspector = Pattern.compile("^(/([a-zA-Z0-9\\-\\,\\.\\:\\;\\(\\)/_~!\\$&\\*'\\+=@%^#^\\?])*+)+$");

  /** The default request flavor */
  private final RequestFlavor defaultFlavor = RequestFlavor.ANY;

  /** The associated site */
  protected Site site = null;

  /** The url version */
  protected long version = -1;

  /** The language */
  protected Language language = null;

  /** True if the language is encoded on the path */
  protected boolean languageIsPathEncoded = false;

  /** The link */
  private transient String link = null;

  /** The url flavor */
  protected RequestFlavor flavor = null;

  /**
   * Constructor for a url with the given path, a version of <code>LIVE</code>,
   * a language matching the site default language and an <code>HTML</code>
   * flavor, unless version, flavor and language are encoded in the url using
   * either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, String path) throws IllegalArgumentException {
    super('/');
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (path == null)
      throw new IllegalArgumentException("Path must not be null");
    this.site = site;
    this.path = analyzePath(path, '/');
    version = Math.max(Resource.LIVE, version);
  }

  /**
   * Constructor for a url with the given path, a version of <code>LIVE</code>
   * and an <code>HTML</code> flavor, unless version, flavor or language are
   * encoded in the url using either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param url
   *          the url
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, Path url) throws IllegalArgumentException {
    this(site, url.getPath());
  }

  /**
   * Constructor for a url with the given path added to <code>url</code>, a
   * version of <code>LIVE</code> and an <code>HTML</code> flavor, unless
   * version and/or flavor are encoded in the url using either of these two
   * schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param url
   *          the url
   * @param path
   *          the path to append
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, Path url, String path)
      throws IllegalArgumentException {
    this(site, concat(url.getPath(), path, '/'));
  }

  /**
   * Constructor for a url with the given path and version and an
   * <code>HTML</code> flavor, unless the flavor is encoded in the url using
   * either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * <p>
   * Note that even if the version is encoded in the url path, the one passed as
   * the argument to this constructor will be used.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the url version
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, String path, long version)
      throws IllegalArgumentException {
    this(site, path);
    this.version = version;
  }

  /**
   * Constructor for a url with the given path, version and flavor.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the required version
   * @param flavor
   *          the url flavor
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, String path, long version, RequestFlavor flavor)
      throws IllegalArgumentException {
    this(site, path, version, flavor, null);
  }

  /**
   * Constructor for a url with the given path, version and flavor.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the required version
   * @param flavor
   *          the url flavor
   * @param language
   *          the language
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(Site site, String path, long version, RequestFlavor flavor,
      Language language) throws IllegalArgumentException {
    super(path, '/');
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    this.site = site;
    this.version = version;
    this.flavor = flavor;
    this.language = language;
  }

  /**
   * Creates a new url that has the same properties as <code>url</code> except
   * for the <code>path</code>. This constructor is intended to be used when
   * redirecting.
   * 
   * @param url
   *          the original url
   * @param path
   *          the new path
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>path</code> are
   *           <code>null</code> or the path is malformed
   */
  public WebUrlImpl(WebUrlImpl url, String path) throws IllegalArgumentException {
    super(path, url.getPathSeparator());
    this.site = url.getSite();
    this.version = url.getVersion();
    this.flavor = url.getFlavor();
    this.language = url.getLanguage();
    this.link = url.getLink();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLink()
   */
  public String getLink() {
    if (link == null) {
      try {
        link = URLEncoder.encode(getLink(-1, null, null), "utf-8");
      } catch (UnsupportedEncodingException e) {
        logger.error("Unexpected error while urlencoding link {}", link, e);
      }
      link = link.replaceAll("%2F", "/");
    }
    return link;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLink(long)
   */
  public String getLink(long version) {
    return getLink(version, null, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLink(ch.entwine.weblounge.common.language.Language)
   */
  public String getLink(Language language) {
    return getLink(-1, language, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLink(java.lang.String)
   */
  public String getLink(String flavor) {
    return getLink(-1, null, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLink(long,
   *      ch.entwine.weblounge.common.language.Language, java.lang.String)
   */
  public String getLink(long version, Language language, String flavor) {
    StringBuffer selector = new StringBuffer();
    boolean hasVersion = false;
    if (version >= 0 || this.version > 0 || language != null || this.language != null || flavor != null || this.flavor != null) {
      if (version < 0)
        version = this.version;
      if (version == Resource.LIVE)
        selector.append("index");
      else if (version == Resource.WORK) {
        selector.append("work");
      } else if (version >= 0) {
        selector.append(Long.toString(version));
      } else {
        selector.append("index");
      }
      hasVersion = true;
    }

    // Language
    if (language != null)
      selector.append("_").append(language.getIdentifier());
    else if (this.language != null) {
      selector.append("_").append(this.language.getIdentifier());
    }

    // Flavor
    if (flavor != null)
      selector.append(".").append(flavor.toLowerCase());
    else if (this.flavor != null) {
      selector.append(".").append(this.flavor.toExtension());
    } else if (hasVersion) {
      selector.append(".").append(RequestFlavor.HTML.toExtension());
    }

    if (selector.length() > 0)
      return UrlUtils.concat(path, selector.toString());
    else
      return path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#normalize()
   */
  public String normalize() {
    return normalize(true, true, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#normalize(boolean, boolean,
   *      boolean)
   */
  public String normalize(boolean includeVersion, boolean includeLanguage,
      boolean includeFlavor) {
    StringBuffer buf = new StringBuffer();

    // Path
    buf.append(pathElementSeparatorChar).append(path);

    // Version
    if (includeVersion && version > 0) {
      buf.append(pathElementSeparatorChar);
      if (version == Resource.WORK)
        buf.append("work");
      else if (version >= 0)
        buf.append(Long.toString(version));

      // Language
      if (includeLanguage && language != null) {
        buf.append("_").append(language.getIdentifier());
      }

      // Flavor
      if (includeFlavor && flavor != null) {
        buf.append(".").append(flavor.toExtension());
      } else {
        buf.append(".");
        buf.append(RequestFlavor.HTML.toExtension());
      }
    } else {
      // Language
      if (includeLanguage && language != null) {
        buf.append(pathElementSeparatorChar).append(language.getIdentifier());
      }

      // Flavor
      if (includeFlavor && flavor != null) {
        buf.append(pathElementSeparatorChar).append(flavor.toExtension());
      }
    }

    return trim(buf.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getFlavor()
   */
  public RequestFlavor getFlavor() {
    return flavor != null ? flavor : defaultFlavor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getVersion()
   */
  public long getVersion() {
    return version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.WebUrl#hasLanguagePathSegment()
   */
  public boolean hasLanguagePathSegment() {
    return languageIsPathEncoded;
  }

  /**
   * Returns the hash code for this url. The method includes the super
   * implementation and adds sensitivity for the site and the url extension.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
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
  @Override
  public boolean equals(Object object) {
    if (object instanceof WebUrlImpl) {
      WebUrlImpl url = (WebUrlImpl) object;
      if (!super.equals(object))
        return false;
      if (version != url.getVersion())
        return false;
      if (language == null && url.getLanguage() != null || (language != null && !language.equals(url.getLanguage())))
        return false;
      if (!getFlavor().equals(url.getFlavor()))
        return false;
      if (!site.equals(url.getSite()))
        return false;
      return true;
    } else if (object instanceof Path) {
      return super.equals(object);
    }
    return false;
  }

  /**
   * Strips version and flavor from this url. Version and flavor can either be
   * encoded as
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * or as <code>path/to/resource/version/language/flavor</code>.
   * 
   * @param path
   *          the full path
   * @param separator
   *          path separator character
   * @return the directory path
   * @throws IllegalArgumentException
   *           if an invalid path is given
   */
  protected String analyzePath(String path, char separator)
      throws IllegalArgumentException {
    if (path.contains(":/")) {
      try {
        URL u = new URL(path);
        path = u.getPath();
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Path " + path + " cannot be parsed");
      }
    }
    path = trim(path);

    // Make sure the path is absolute
    if (!path.startsWith(WebUrlImpl.separator))
      throw new IllegalArgumentException("Path must be absolute");

    Matcher pathMatcher = pathInspector.matcher(path);
    if (pathMatcher.matches()) {

      // Version
      String v = pathMatcher.group(2);
      if ("index".equals(v) || "live".equals(v)) {
        this.version = Resource.LIVE;
      } else if ("work".equals(v)) {
        this.version = Resource.WORK;
      } else if (v != null && !"".equals(v)) {
        try {
          this.version = Long.parseLong(v);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unable to extract version from url " + path);
        }
      }

      // Language (will be something like "_fr")
      String l = pathMatcher.group(3);
      if (l != null && !"".equals(l)) {
        l = l.substring(1);
        Language language = site.getLanguage(l);
        if (language == null) {
          logger.debug("Switching request language {} for {}", l, site.getDefaultLanguage().getIdentifier());
          this.language = site.getDefaultLanguage();
        }
        this.language = language;
      }

      // Flavor
      String f = pathMatcher.group(4);
      if (f != null && !"".equals(f))
        try {
          this.flavor = RequestFlavor.parseString(f);
        } catch (IllegalArgumentException e) {
          logger.debug("Found unknwon request flavor {}", f);
        }
      return trim(pathMatcher.group(1));
    }

    // Try the segmented approach for /path/to/resource/<language>/<flavor>
    Matcher segmentMatcher = segmentInspector.matcher(path);
    if (segmentMatcher.matches()) {

      // Extract flavor and language
      String url = trim(segmentMatcher.group(1));
      String[] segments = url.split(Character.toString(separator));
      for (int i = segments.length; i > 0; i--) {
        String segment = segments[i - 1].replaceAll(Character.toString(separator), "");
        boolean foundMetadata = false;

        // Test for flavor
        try {
          this.flavor = RequestFlavor.parseString(segment);
          url = url.substring(0, url.length() - segment.length() - 1);
          foundMetadata = true;
          continue;
        } catch (IllegalArgumentException e) {
          logger.debug("Found unknown request flavor {}", segment);
        }

        // Test group for language
        Language language = site.getLanguage(segment);
        if (language != null) {
          this.language = language;
          this.languageIsPathEncoded = true;
          url = url.substring(0, url.length() - segment.length() - 1);
          foundMetadata = true;
          continue;
        }

        if (!foundMetadata)
          break;
      }

      return trim(url);
    }

    throw new IllegalArgumentException("Invalid path provided");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.url.UrlImpl#toString()
   */
  @Override
  public String toString() {
    return getLink();
  }

}