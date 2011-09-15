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

package ch.entwine.weblounge.common.request;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is a wrapper to the <code>HttpServletRequest</code> with weblounge
 * specific functionality enhancements, e. g. to get access to the requested
 * site or language.
 */
public interface WebloungeRequest extends HttpServletRequest {

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.security.User} in the session
   */
  String USER = "weblounge-user";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.language.Language} in the session
   */
  String LANGUAGE = "weblounge-language";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.site.Site} in the session
   */
  String SITE = "weblounge-site";

  /** The attribute name used to store the {@link java.net.UR} in the session */
  String URL = "weblounge-url";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.content.page.Page} in the request
   */
  String PAGE = "weblounge-page";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.site.Action} in the request
   */
  String ACTION = "weblounge-action";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.content.Renderer} in the request
   */
  String TEMPLATE = "weblounge-template";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.content.page.Composer} in the request
   */
  String COMPOSER = "weblounge-composer";

  /**
   * The attribute name used to store the
   * {@link ch.entwine.weblounge.common.content.page.Pagelet} in the request
   */
  String PAGELET = "weblounge-pagelet";

  /**
   * Returns the requested language. The language is determined by evaluating
   * the request header fields.
   * 
   * @return the requested language
   */
  Language getLanguage();

  /**
   * Returns the site that is serving this request.
   * 
   * @return the requested site
   */
  Site getSite();

  /**
   * Returns the url that is used to render the content for this request. Note
   * that due to redirection, this might not be the url that the client
   * originally requested.
   * 
   * @return the url
   * @see #getRequestedUrl()
   */
  WebUrl getUrl();

  /**
   * Returns the originally requested url.
   * 
   * @return the requested url
   * @see #getUrl()
   */
  WebUrl getRequestedUrl();

  /**
   * Returns the request environment as determined by the hostname and the site
   * settings.
   * 
   * @return the request environment
   */
  Environment getEnvironment();

  /**
   * Returns the current user.
   * 
   * @return the user
   */
  User getUser();

  /**
   * Returns the requested version, which is one of
   * <ul>
   * <li>{@link ch.entwine.weblounge.common.content.Resource#LIVE}</li>
   * <li>{@link ch.entwine.weblounge.common.content.Resource#WORK}</li>
   * </ul>
   * 
   * @return the requested version
   */
  long getVersion();

  /**
   * Returns the requested content flavor. The default output method is
   * <code>HTML</code>, but <code>XML</code> and <code>JSON</code> are supported
   * as well.
   * 
   * @return the requested output method
   */
  RequestFlavor getFlavor();

}