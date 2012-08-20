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

import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlMatcher;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of a <code>UrlMatcher</code> which compares a given
 * <code>url</code> with a fixed mountpoint and an optional mountpoint
 * extension.
 */
public class UrlMatcherImpl implements UrlMatcher {

  /** The site */
  protected Site site = null;
  
  /** The mountpoint */
  protected String path = null;

  /** The url extension */
  protected String extension = null;
  
  /** The request flavors */
  protected Set<RequestFlavor> flavors = new HashSet<RequestFlavor>();
  
  /**
   * Creates a matcher for the given action by taking it's mountpoint and path
   * extension.
   * 
   * @param action
   *          the action
   */
  public UrlMatcherImpl(Action action) {
    site = action.getSite();
    path = UrlUtils.trim(UrlUtils.concat("/", action.getPath()));
    flavors.addAll(Arrays.asList(action.getFlavors()));
    // TODO: take extension into account
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.url.UrlMatcher#matches(ch.entwine.weblounge.common.url.WebUrl, RequestFlavor)
   */
  public boolean matches(WebUrl url, RequestFlavor flavor) {
    if (!site.equals(url.getSite()))
      return false;
    String path = url.normalize(false, false, false);
    String normalizedPath = url.normalize(false, false, true);
    if (!path.startsWith(this.path) && !normalizedPath.startsWith(this.path))
      return false;
    // TODO: check for extension
    if (!flavors.contains(flavor) && !flavor.equals(RequestFlavor.ANY))
      return false;
    return true;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.url.UrlMatcher#getMountpoint()
   */
  public String getMountpoint() {
    return path;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.url.UrlMatcher#getExtension()
   */
  public String getExtension() {
    return extension;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return path.hashCode();
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
      if (!flavors.equals(m.flavors))
        return false;
      if (!site.equals(m.site))
         return false;
      if (!path.equals(m.path))
        return false;
      // TODO: Check extension
      return true;
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
    StringBuffer buf = new StringBuffer(site.getHostname().getURL().getHost());
    buf.append("/").append(path);
    if (extension != null)
      buf.append(extension);
    return buf.toString();
  }
  
}
