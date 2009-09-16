/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.security.WebloungeUser;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.Date;
import java.util.Set;

/**
 * This class is used as a proxy for {@link WebloungeUser} objects.
 * 
 * @author Tobias Wunden
 */
public class WebloungeUserProxy extends UserProxy implements WebloungeUser {

  /** The user's enabled flag */
  protected boolean enabled = false;

  /**
   * Creates a new weblounge user proxy.
   * 
   * @param login
   *          the login
   * @param site
   *          the associated site
   */
  public WebloungeUserProxy(String login, Site site) {
    super(login, site);
  }

  /**
   * @see ch.o2it.weblounge.common.security.WebloungeUser#isEnabled()
   */
  public boolean isEnabled() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).isEnabled();
  }

  /**
   * @see ch.o2it.weblounge.common.security.WebloungeUser#getLastLogin()
   */
  public Date getLastLogin() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getLastLogin();
  }

  /**
   * @see ch.o2it.weblounge.common.security.WebloungeUser#getLastLoginSource()
   */
  public String getLastLoginSource() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getLastLoginSource();
  }

  /**
   * @see ch.o2it.weblounge.common.security.WebloungeUser#toXml()
   */
  public Node toXml() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).toXml();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#setLoginContext(ch.o2it.weblounge.LoginContextImpl.security.jaas.LoginContext)
   */
  public void setLoginContext(LoginContext context) {
    if (!initialized && login != null)
      init();
    ((WebloungeUser) stub).setLoginContext(context);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPassword()
   */
  public String getPassword() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPassword();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPasswordType()
   */
  public int getPasswordType() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPasswordType();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#checkPassword(java.lang.String)
   */
  public boolean checkPassword(String password) {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).checkPassword(password);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#isAuthenticated()
   */
  public boolean isAuthenticated() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).isAuthenticated();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#addPublicCredential(java.lang.Object)
   */
  public void addPublicCredential(Object credential) {
    if (!initialized && login != null)
      init();
    ((WebloungeUser) stub).addPublicCredential(credential);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPublicCredentials()
   */
  public Set getPublicCredentials() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPublicCredentials();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPublicCredentials(java.lang.Class)
   */
  public Set getPublicCredentials(Class type) {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPublicCredentials(type);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#addPrivateCredential(java.lang.Object)
   */
  public void addPrivateCredential(Object credential) {
    if (!initialized && login != null)
      init();
    ((WebloungeUser) stub).addPrivateCredential(credential);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPrivateCredentials()
   */
  public Set getPrivateCredentials() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPrivateCredentials();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getPrivateCredentials(java.lang.Class)
   */
  public Set getPrivateCredentials(Class type) {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getPrivateCredentials(type);
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#getLoginContext()
   */
  public LoginContext getLoginContext() {
    if (!initialized && login != null)
      init();
    return ((WebloungeUser) stub).getLoginContext();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#logout()
   */
  public void logout() {
    if (!initialized && login != null)
      init();
    ((WebloungeUser) stub).logout();
  }

}