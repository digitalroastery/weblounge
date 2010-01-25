/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

/**
 * This exception is thrown if a site cannot be properly started or shut down or
 * exhibits any other misbehavior.
 */
public class SiteException extends Exception {

  /**
   * Serial version uid
   */
  private static final long serialVersionUID = 5173735923703880699L;

  /**
   * The site in question
   */
  protected Site site = null;

  /**
   * Creates a new exception, caused by the given site.
   * 
   * @param site
   *          the site
   */
  public SiteException(Site site) {
    this.site = site;
  }

  /**
   * Creates a new exception, caused by the given site.
   * 
   * @param site
   *          the site
   * @param message
   *          the error message
   */
  public SiteException(Site site, String message) {
    super(message);
    this.site = site;
  }

  /**
   * Creates a new exception, caused by the given site.
   * 
   * @param site
   *          the site
   * @param cause
   *          the original cause
   */
  public SiteException(Site site, Throwable cause) {
    super(cause);
    this.site = site;
  }

  /**
   * Creates a new exception, caused by the given site.
   * 
   * @param site
   *          the site
   * @param message
   *          the error message
   * @param cause
   *          the original cause
   */
  public SiteException(Site site, String message, Throwable cause) {
    super(message, cause);
    this.site = site;
  }

  /**
   * Returns the site that caused the exception.
   * 
   * @return the site
   */
  public Site getSite() {
    return site;
  }

}
