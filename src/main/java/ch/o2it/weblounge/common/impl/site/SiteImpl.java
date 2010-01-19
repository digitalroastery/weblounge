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
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.common.user.WebloungeUser;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Default implementation of a site.
 */
public class SiteImpl implements Site {

  /** Serial version uid */
  private static final long serialVersionUID = 5544198303137698222L;

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(SiteImpl.class);
  
  /** Bundle property name of the site identifier */
  public static final String PROP_IDENTIFIER = "site.identifier";

  /** Regular expression to test the validity of a site identifier */
  private static final String SITE_IDENTIFIER_REGEX = "^[a-zA-Z0-9-_.]*$";

  /** The site identifier */
  protected String identifier = null;

  /** Site enabled state */
  protected boolean enabled = false;

  /** Site description */
  protected String description = null;

  /** Site administrator */
  protected WebloungeUser administrator = null;

  /** Page languages */
  protected Map<String, Language> languages = null;

  /** The default language */
  protected Language defaultLanguage = null;

  /** Page templates */
  protected Map<String, PageTemplate> templates = null;

  /** The default page template */
  protected PageTemplate defaultTemplate = null;

  /** Page layouts */
  protected Map<String, PageLayout> layouts = null;

  /** The default page template */
  protected PageLayout defaultLayout = null;

  /** Request listeners */
  private List<RequestListener> requestListeners = null;

  /** Site listeners */
  private List<SiteListener> siteListeners = null;

  /** User listeners */
  private List<UserListener> userListeners = null;

  /** The site dispatcher */
  private SiteDispatcher dispatcher = null;

  /** OSGi cron service tracker */
  private CronServiceTracker cronServiceTracker = null;
  
  /** Quartz cron scheduler */
  private Scheduler scheduler = null;

  /**
   * Creates a new site that is initially disabled. Use {@link #setEnabled()} to
   * enable the site.
   */
  public SiteImpl() {
    languages = new HashMap<String, Language>();
    templates = new HashMap<String, PageTemplate>();
    dispatcher = new SiteDispatcher(this, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Site identifier must not be null");
    else if (!Pattern.matches(SITE_IDENTIFIER_REGEX, identifier))
      throw new IllegalArgumentException("Site identifier '" + identifier + "' is malformed");
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled)
      dispatcher.startDispatching();
    else
      dispatcher.stopDispatching();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setAdministrator(ch.o2it.weblounge.common.user.WebloungeUser)
   */
  public void setAdministrator(WebloungeUser administrator) {
    if (administrator != null)
      log_.debug("Site administrator is {}", administrator);
    else
      log_.debug("Site administrator is now undefined");
    this.administrator = administrator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getAdministrator()
   */
  public WebloungeUser getAdministrator() {
    return administrator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addTemplate(ch.o2it.weblounge.common.site.PageTemplate)
   */
  public void addTemplate(PageTemplate template) {
    templates.put(template.getIdentifier(), template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeTemplate(ch.o2it.weblounge.common.site.PageTemplate)
   */
  public void removeTemplate(PageTemplate template) {
    if (template == null)
      throw new IllegalArgumentException("Template must not be null");
    log_.debug("Removing page template '{}'", template.getIdentifier());
    templates.remove(template.getIdentifier());
    if (template.equals(defaultTemplate)) {
      defaultTemplate = null;
      log_.debug("Default template is now undefined");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getTemplate(java.lang.String)
   */
  public PageTemplate getTemplate(String template) {
    return templates.get(template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getTemplates()
   */
  public PageTemplate[] getTemplates() {
    return templates.values().toArray(new PageTemplate[templates.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setDefaultTemplate(ch.o2it.weblounge.common.site.PageTemplate)
   */
  public void setDefaultTemplate(PageTemplate template) {
    if (template != null) {
      templates.put(template.getIdentifier(), template);
      log_.debug("Default page template is '{}'", template.getIdentifier());
    } else
      log_.debug("Default template is now undefined");
    this.defaultTemplate = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getDefaultTemplate()
   */
  public PageTemplate getDefaultTemplate() {
    return defaultTemplate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void addLanguage(Language language) {
    if (language != null)
      languages.put(language.getIdentifier(), language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void removeLanguage(Language language) {
    if (language != null) {
      languages.remove(language.getIdentifier());
      if (language.equals(defaultLanguage))
        defaultLanguage = null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getLanguage(java.lang.String)
   */
  public Language getLanguage(String languageId) {
    return languages.get(languageId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getLanguages()
   */
  public Language[] getLanguages() {
    return languages.values().toArray(new Language[languages.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return languages.values().contains(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#setDefaultLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setDefaultLanguage(Language language) {
    if (language != null)
      languages.put(language.getIdentifier(), language);
    defaultLanguage = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getDefaultLanguage()
   */
  public Language getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#dispatch(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response)
      throws IOException {
    dispatcher.dispatch(request, response);
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
   * @see ch.o2it.weblounge.common.site.Site#getGroup(java.lang.String,
   *      java.lang.String)
   */
  public Group getGroup(String group, String context) {
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
   * @see ch.o2it.weblounge.common.site.Site#getRole(java.lang.String,
   *      java.lang.String)
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
   * @see ch.o2it.weblounge.common.site.Site#getUser(java.lang.String)
   */
  public WebloungeUser getUser(String login) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getVirtualPath(java.lang.String,
   *      boolean)
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
   * @see ch.o2it.weblounge.common.site.Site#addRequestListener(ch.o2it.weblounge.common.request.RequestListener)
   */
  public void addRequestListener(RequestListener listener) {
    if (requestListeners == null)
      requestListeners = new ArrayList<RequestListener>();
    synchronized (requestListeners) {
      requestListeners.add(listener);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeRequestListener(ch.o2it.weblounge.common.request.RequestListener)
   */
  public void removeRequestListener(RequestListener listener) {
    if (requestListeners != null)
      requestListeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addSiteListener(ch.o2it.weblounge.common.site.SiteListener)
   */
  public void addSiteListener(SiteListener listener) {
    if (siteListeners == null)
      siteListeners = new ArrayList<SiteListener>();
    synchronized (siteListeners) {
      siteListeners.add(listener);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeSiteListener(ch.o2it.weblounge.common.site.SiteListener)
   */
  public void removeSiteListener(SiteListener listener) {
    if (siteListeners != null)
      siteListeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addUserListener(ch.o2it.weblounge.common.security.UserListener)
   */
  public void addUserListener(UserListener listener) {
    if (userListeners == null)
      userListeners = new ArrayList<UserListener>();
    synchronized (userListeners) {
      userListeners.add(listener);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeUserListener(ch.o2it.weblounge.common.security.UserListener)
   */
  public void removeUserListener(UserListener listener) {
    if (userListeners != null)
      userListeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#start()
   */
  public void start() {
    dispatcher.startDispatching();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#stop()
   */
  public void stop() {
    dispatcher.stopDispatching();
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
   * @see ch.o2it.weblounge.common.request.RequestListener#requestStarted(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void requestStarted(WebloungeRequest request,
      WebloungeResponse response) {
    // TODO: Remove
    fireRequestStarted(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.RequestListener#requestDelivered(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void requestDelivered(WebloungeRequest request,
      WebloungeResponse response) {
    // TODO: Remove
    fireRequestDelivered(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.RequestListener#requestFailed(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse, int)
   */
  public void requestFailed(WebloungeRequest request,
      WebloungeResponse response, int reason) {
    // TODO: Remove
    fireRequestFailed(request, response, reason);
  }

  /**
   * Method to fire a <code>requestStarted()</code> message to all registered
   * <code>RequestListener</code>s.
   * 
   * @param request
   *          the started request
   * @param response
   *          the response
   */
  protected void fireRequestStarted(WebloungeRequest request,
      WebloungeResponse response) {
    if (requestListeners == null)
      return;
    synchronized (requestListeners) {
      for (RequestListener listener : requestListeners) {
        listener.requestStarted(request, response);
      }
    }
  }

  /**
   * Method to fire a <code>requestDelivered()</code> message to all registered
   * <code>RequestListener</code>s.
   * 
   * @param request
   *          the delivered request
   * @param response
   *          the response
   */
  protected void fireRequestDelivered(WebloungeRequest request,
      WebloungeResponse response) {
    if (requestListeners == null)
      return;
    synchronized (requestListeners) {
      for (RequestListener listener : requestListeners) {
        listener.requestDelivered(request, response);
      }
    }
  }

  /**
   * Method to fire a <code>requestFailed()</code> message to all registered
   * <code>RequestListener</code>s.
   * 
   * @param request
   *          the failed request
   * @param response
   *          the response
   * @param error
   *          the error code
   */
  protected void fireRequestFailed(WebloungeRequest request,
      WebloungeResponse response, int error) {
    if (requestListeners == null)
      return;
    synchronized (requestListeners) {
      for (RequestListener listener : requestListeners) {
        listener.requestFailed(request, response, error);
      }
    }
  }

  /**
   * This method is called if a user is logged in.
   * 
   * @param user
   *          the user that logged in
   */
  protected void fireUserLoggedIn(User user) {
    if (userListeners == null)
      return;
    synchronized (userListeners) {
      for (UserListener listener : userListeners) {
        listener.userLoggedIn(user);
      }
    }
  }

  /**
   * This method is called if a user is logged out.
   * 
   * @param user
   *          the user that logged out
   */
  protected void fireUserLoggedOut(User user) {
    if (userListeners == null)
      return;
    synchronized (userListeners) {
      for (UserListener listener : userListeners) {
        listener.userLoggedOut(user);
      }
    }
  }

  /**
   * This method is a callback from the service tracker that is started when
   * this site is started. It is looking for an implementation of the Quartz
   * scheduler.
   * 
   * @param daemon
   */
  void setScheduler(Scheduler scheduler) {
    if (scheduler == null) {
      // TODO: remove registered tasks
    } else {
      // TODO: add registered tasks
    }
    this.scheduler = scheduler;
  }

  /**
   * Callback from the OSGi environment to activate the site. Subclasses should
   * make sure to call this super implementation as it will assist in correctly
   * setting up the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    
    // Fix the site identifier
    if (getIdentifier() == null) {
      String identifier = (String)context.getProperties().get(PROP_IDENTIFIER);
      if (identifier == null)
        throw new IllegalStateException("Site needs an identifier");
      setIdentifier(identifier);
    }

    log_.info("Site {} is starting", this);
    log_.debug("Getting in line for cron services");

    // Connect to the 
    cronServiceTracker = new CronServiceTracker(bundleContext, this);
    cronServiceTracker.open();
  }

  /**
   * Callback from the OSGi environment to deactivate the site. Subclasses
   * should make sure to call this super implementation as it will assist in
   * correctly shutting down the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) {
    log_.info("Site {} is stopping", this);
    log_.debug("Stopped looking for cron services");
    cronServiceTracker.close();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return identifier;
  }

}
