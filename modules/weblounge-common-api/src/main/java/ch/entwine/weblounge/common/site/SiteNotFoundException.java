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

package ch.entwine.weblounge.common.site;

/**
 * Exception to indicate that a site has not been found. This is the case if the
 * urls starting with e. g. <code>http://www.entwinemedia.com/weblounge/cms</code> have
 * been mapped to the weblounge content management system but no site has been
 * associated with this server name.
 */
public class SiteNotFoundException extends RuntimeException {

  /** The serial version id */
  private static final long serialVersionUID = 1L;

  /** Requested hostname */
  private String serverName = null;

  /**
   * Constructor for class SiteNotFoundException. The constructor takes the
   * requested hostname as an argument.
   * 
   * @param host
   *          the requested hostname
   */
  public SiteNotFoundException(String host) {
    super("Site not found for host '" + host + "'");
    serverName = host;
  }

  /**
   * Returns the hostname that was given in the request.
   * 
   * @return the hostname
   */
  public String getHost() {
    return serverName;
  }

}