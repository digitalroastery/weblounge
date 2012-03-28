/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.SiteURL;

import java.net.URL;

/**
 * Default implementation for the environment.
 */
public class SiteURLImpl implements SiteURL {

  /** The url */
  protected URL url = null;

  /** True if this is the default url for the environment */
  protected boolean isDefault = false;

  /** The environment */
  protected Environment environment = Environment.Production;

  /**
   * Creates a new site url.
   * 
   * @param url
   *          the url
   */
  public SiteURLImpl(URL url) {
    this(url, Environment.Production, false);
  }

  /**
   * Creates a new site url for the given environment.
   * 
   * @param url
   *          the url
   * @param environment
   *          the environment
   */
  public SiteURLImpl(URL url, Environment environment) {
    this(url, environment, false);
  }

  /**
   * Creates a new site url for the given environment.
   * 
   * @param url
   *          the url
   * @param isDefault
   *          <code>true</code> if this is the default url
   */
  public SiteURLImpl(URL url, boolean isDefault) {
    this(url, Environment.Production, false);
  }

  /**
   * Creates a new site url.
   * 
   * @param url
   *          the url
   * @param environment
   *          the environment
   * @param isDefault
   *          <code>true</code> if this is the default url
   */
  public SiteURLImpl(URL url, Environment environment, boolean isDefault) {
    this.url = url;
    this.url = url;
    this.environment = environment;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.SiteURL#getURL()
   */
  public URL getURL() {
    return url;
  }
  
  /**
   * Specifies whether this url is the default one for the given environment.
   * 
   * @param isDefault
   *          <code>true</code> if this is the default url
   */
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.SiteURL#isDefault()
   */
  public boolean isDefault() {
    return isDefault;
  }

  /**
   * Sets the url's environment.
   * 
   * @param environment
   *          the environment
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.SiteURL#getEnvironment()
   */
  public Environment getEnvironment() {
    return environment;
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
  public boolean equals(Object o) {
    if (! (o instanceof SiteURL))
      return false;
    SiteURL siteURL = (SiteURL)o;
    return url.equals(siteURL.getURL()) && environment.equals(siteURL.getEnvironment());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.site.SiteURL#toExternalForm()
   */
  public String toExternalForm() {
    return url.toExternalForm();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return url.toExternalForm();
  }
  
}
