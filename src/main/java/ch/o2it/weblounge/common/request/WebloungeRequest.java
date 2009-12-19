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
  public static final String SESSION_USER = "weblounge-user";
  /** The attribute name used to store the {@link Language} in the session */
  public static final String SESSION_LANGUAGE = "weblounge-language";
  /** The attribute name used to store the {@link Site} in the session */
  public static final String SESSION_SITE = "weblounge-site";
  /** The attribute name used to store the {@link URL} in the session */
  public static final String SESSION_URL = "weblounge-url";
  /** The attribute name used to store the last {@link URL} in the session */
  public static final String SESSION_PREVIOUS_URL = "weblounge-lasturl";

  /**
   * Returns the requested language. The language is determined by evaluating
   * the respective request header fields.
   * 
   * @return the requested language
   */
  Language getLanguage();

  /**
   * Returns the requested site.
   * 
   * @return the requested site
   */
  Site getSite();

  /**
   * Returns the requested url.
   * 
   * @return the requested url
   */
  WebUrl getUrl();

  /**
   * Returns the originally requested url.
   * 
   * @return the requested url
   */
  WebUrl getRequestedUrl();

  /**
   * Returns the url that was called before this request.
   * 
   * @return the previous url
   */
  WebUrl getPreviousUrl();

  /**
   * Returns the current user.
   * 
   * @return the user
   */
  User getUser();

  /**
   * Returns the requested version, which is one of
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.page.Page#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#WORK}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#ORIGINAL}</li>
   * </ul>
   * 
   * @return the requested version
   */
  long getVersion();

  /**
   * Returns the requested output method.
   * 
   * @return the requested output method
   */
  String getOutputMethod();

}