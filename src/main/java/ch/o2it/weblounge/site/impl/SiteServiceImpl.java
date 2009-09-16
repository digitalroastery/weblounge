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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.site.SiteService;

import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * TODO: Comment SiteServiceImpl
 */
public class SiteServiceImpl implements SiteService {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(SiteServiceImpl.class);

  /** The site */
  private Site site = null;

  /** Site identifier */
  /** TODO: Remove once site is there */
  private String id = null;

  /** Can people log into that site? */
  /** TODO: Remove once site is there */
  private boolean loginEnabled = true;

  /**
   * Creates a new site service.
   */
  SiteServiceImpl() {
    this.id = "n/a";
    this.loginEnabled = true;
  }

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {
    log_.info("Site service properties have been updated");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.site.SiteService#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.site.SiteService#getIdentifier()
   */
  public String getIdentifier() {
    return id;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.site.SiteService#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.site.SiteService#isLoginEnabled()
   */
  public boolean isLoginEnabled() {
    return loginEnabled;
  }

  /**
   * Enables or disables the login for this site.
   * 
   * @param enable
   *          <code>true</code> to enable login
   */
  public void setLoginEnabled(boolean enable) {
    this.loginEnabled = enable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.site.SiteService#start()
   */
  public void start() {
    log_.info("Starting site " + id);
    log_.info("Login to site " + id + " is " + (loginEnabled ? "enabled" : "disabled"));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.site.SiteService#stop()
   */
  public void stop() {
    log_.info("Stopping site " + id);
  }

}