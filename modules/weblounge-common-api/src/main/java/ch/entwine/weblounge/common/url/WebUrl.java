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

package ch.entwine.weblounge.common.url;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

/**
 * A web url represents a url that is used to address locations within the web
 * application such as pages, binary resources or module actions. It not only
 * contains information about the path but also the version of the page or
 * resource it is pointing to as well as the flavor.
 * <p>
 * Following is a list of supported urls:
 * <ul>
 * <li><code>http://www.example.org/</code></li>
 * <li><code>http://www.example.org/test</code></li>
 * <li><code>http://www.example.org/test/index.(html|json|xml)</code></li>
 * <li><code>http://www.example.org/test/index_de.(html|json|xml)</code></li>
 * <li><code>http://www.example.org/test/127.(html|json|xml)</code></li>
 * </ul>
 */
public interface WebUrl extends Path {

  /** The url separator character */
  char separatorChar = '/';

  /** The url separator as a string */
  String separator = "/";

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the encoded url.
   * 
   * @return the encoded url
   */
  String getLink();

  /**
   * Returns a link to the indicated version of this url.
   * 
   * @param version
   *          the requested version
   * @return the encoded url
   * @see #getVersion()
   */
  String getLink(long version);

  /**
   * Returns a link to the indicated version of this url.
   * 
   * @param language
   *          the requested language
   * @return the encoded url
   * @see #getLanguage()
   */
  String getLink(Language language);

  /**
   * Returns a link to the indicated localized and flavored version of this url.
   * 
   * @param version
   *          the requested version
   * @param language
   *          the requested language
   * @param flavor
   *          the requested flavor
   * @return the encoded url
   */
  String getLink(long version, Language langugae, String flavor);

  /**
   * Returns a link to the indicated flavored live version of this url.
   * 
   * @param flavor
   *          the requested flavor
   * @return the encoded url
   */
  String getLink(String flavor);

  /**
   * Returns a normalized version of this url. A normalized version is defined
   * by these properties:
   * <ul>
   * <li>It does not contain a protocol</li>
   * <li>It starts with the site's main hostname as returned by
   * {@link ch.entwine.weblounge.common.site.Site#getHostname()}</li>
   * <li>It contains the requested version as a url extension</li>
   * <li>It contains the request flavor as a url extension</li>
   * </ul>
   * For example, a normalized version of the url defined by
   * <code>http://www.test.com/foo/work.html</code> would look like
   * <code>www.test.com/foo/work/html</code>
   * 
   * @return the normalized version of the url
   */
  String normalize();

  /**
   * Returns a normalized version of this url.
   * <p>
   * By specifying the parameters <code>includeVersion</code>,
   * <code>includeLanguage</code> and <code>includeFlavor</code>, the version,
   * language and flavor of that normalization can be left out.
   * 
   * @param includeHost
   *          <code>true</code> to include the hostname
   * @param includeVersion
   *          <code>true</code> to include the version
   * @param includeLanguage
   *          <code>true</code> to include the language
   * @param includeFlavor
   *          <code>true</code> to include the flavor
   * 
   * @return the normalized version of the url
   * @see #normalize()
   */
  String normalize(boolean includeHost, boolean includeVersion,
      boolean includeLanguage, boolean includeFlavor);

  /**
   * Returns <code>true</code> if the language had originally been encoded into
   * the url, as in <code>/my/path/en</code>.
   * 
   * @return <code>true</code> if the language was encoded in the path
   */
  boolean hasLanguagePathSegment();

}