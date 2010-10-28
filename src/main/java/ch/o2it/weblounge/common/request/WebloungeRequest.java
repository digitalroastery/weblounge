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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is a wrapper to the <code>HttpServletRequest</code> with weblounge
 * specific functionality enhancements, e. g. to get access to the requested
 * site or language.
 */
public interface WebloungeRequest extends HttpServletRequest {

  /** The attribute name used to store the {@link User} in the session */
  String USER = "weblounge-user";

  /** The attribute name used to store the {@link Language} in the session */
  String LANGUAGE = "weblounge-language";

  /** The attribute name used to store the {@link Site} in the session */
  String SITE = "weblounge-site";

  /** The attribute name used to store the {@link URL} in the session */
  String URL = "weblounge-url";

  /** The attribute name used to store the {@link Page} in the request */
  String PAGE = "weblounge-page";

  /** The attribute name used to store the {@link Action} in the request */
  String ACTION = "weblounge-action";

  /** The attribute name used to store the {@link Renderer} in the request */
  String TEMPLATE = "weblounge-template";

  /** The attribute name used to store the {@link Composer} in the request */
  String COMPOSER = "weblounge-composer";

  /** The attribute name used to store the {@link Pagelet} in the request */
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
   * Returns the current user.
   * 
   * @return the user
   */
  User getUser();

  /**
   * Returns the requested version, which is one of
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.content.Resource#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.content.Resource#WORK}</li>
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