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

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.UrlMatcher;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * Simple implementation of a <code>UrlMatcher</code> which compares a given
 * <code>url</code> with a fixed mountpoint and an optional mountpoint
 * extension.
 */
public class UrlMatcherImpl implements UrlMatcher {

  /** The site */
  protected Site site = null;
  
  /** The mountpoint */
  protected String url = null;

  /** The url extension */
  protected String extension = null;

  /**
   * Creates a matcher for the given action by taking it's mountpoint and path
   * extension.
   * 
   * @param action
   *          the action
   */
  public UrlMatcherImpl(Action action) {
    site = action.getSite();
    url = UrlSupport.trim(UrlSupport.concat("/", action.getPath()));
    // TODO: take extension into account
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.UrlMatcher#matches(ch.o2it.weblounge.common.url.WebUrl)
   */
  public boolean matches(WebUrl url) {
    if (!site.equals(url.getSite()))
      return false;
    if (!url.getPath().startsWith(this.url))
      return false;
    return true;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return url.hashCode();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UrlMatcherImpl) {
      UrlMatcherImpl m = (UrlMatcherImpl)obj;
      // TODO: Check extension
      return site.equals(m.site) && url.equals(m.url);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(site.getHostName());
    buf.append("/").append(url);
    if (extension != null)
      buf.append(extension);
    return buf.toString();
  }
  
}
