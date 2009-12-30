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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageLayout;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.common.site.SiteLogger;
import ch.o2it.weblounge.common.user.WebloungeUser;

import java.io.File;
import java.io.IOException;

/**
 * TODO: Comment SiteImpl
 */
public class SiteImpl implements Site {

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#addRequestListener(ch.o2it.weblounge.common.request.RequestListener)
   */
  public void addRequestListener(RequestListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#addSiteListener(ch.o2it.weblounge.common.site.SiteListener)
   */
  public void addSiteListener(SiteListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#addUserListener(ch.o2it.weblounge.common.security.UserListener)
   */
  public void addUserListener(UserListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#dispatch(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getAdministrator()
   */
  public WebloungeUser getAdministrator() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getAuthenticationModules()
   */
  public AuthenticationModule[] getAuthenticationModules() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getCollectionPath(java.lang.String)
   */
  public String getCollectionPath(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getDefaultLanguage()
   */
  public Language getDefaultLanguage() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getDefaultTemplate()
   */
  public String getDefaultTemplate() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getDescription(ch.o2it.weblounge.common.language.Language)
   */
  public String getDescription(Language l) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getGroup(java.lang.String, java.lang.String)
   */
  public Group getGroup(String group, String context) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getHistorySize()
   */
  public int getHistorySize() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getIdentifier()
   */
  public String getIdentifier() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getImageStyle(java.lang.String)
   */
  public ImageStyle getImageStyle(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getImageStyles()
   */
  public ImageStyle[] getImageStyles() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLanguage(java.lang.String)
   */
  public Language getLanguage(String languageId) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLanguages()
   */
  public Language[] getLanguages() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLayout(java.lang.String)
   */
  public PageLayout getLayout(String layoutId) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLayouts()
   */
  public PageLayout[] getLayouts() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLink()
   */
  public String getLink() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getLogger()
   */
  public SiteLogger getLogger() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getModule(java.lang.String)
   */
  public Module getModule(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getModules()
   */
  public Module[] getModules() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getPage(ch.o2it.weblounge.common.page.PageURI)
   */
  public Page getPage(PageURI uri) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getPhysicalPath(java.lang.String)
   */
  public String getPhysicalPath(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getRole(java.lang.String, java.lang.String)
   */
  public Role getRole(String role, String context) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getServerNames()
   */
  public String[] getServerNames() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getServername()
   */
  public String getServername() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getTemplate(java.lang.String)
   */
  public PageTemplate getTemplate(String template) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getTemplates()
   */
  public PageTemplate[] getTemplates() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getUser(java.lang.String)
   */
  public WebloungeUser getUser(String login) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getVirtualPath(java.lang.String, boolean)
   */
  public String getVirtualPath(String path, boolean webapp) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#getWorkDirectory()
   */
  public File getWorkDirectory() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#isEnabled()
   */
  public boolean isEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#removeRequestListener(ch.o2it.weblounge.common.request.RequestListener)
   */
  public void removeRequestListener(RequestListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#removeSiteListener(ch.o2it.weblounge.common.site.SiteListener)
   */
  public void removeSiteListener(SiteListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#removeUserListener(ch.o2it.weblounge.common.security.UserListener)
   */
  public void removeUserListener(UserListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#start()
   */
  public void start() {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#stop()
   */
  public void stop() {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Site#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.ModuleListener#moduleStarted(ch.o2it.weblounge.common.site.Module)
   */
  public void moduleStarted(Module module) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.ModuleListener#moduleStopped(ch.o2it.weblounge.common.site.Module)
   */
  public void moduleStopped(Module module) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.RequestListener#requestDelivered(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void requestDelivered(WebloungeRequest request,
      WebloungeResponse response) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.RequestListener#requestFailed(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse, int)
   */
  public void requestFailed(WebloungeRequest request,
      WebloungeResponse response, int reason) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.RequestListener#requestStarted(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void requestStarted(WebloungeRequest request,
      WebloungeResponse response) {
    // TODO Auto-generated method stub

  }

}
