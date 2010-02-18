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

import ch.o2it.weblounge.common.impl.scheduler.FireOnceJobTrigger;
import ch.o2it.weblounge.common.impl.scheduler.QuartzJob;
import ch.o2it.weblounge.common.impl.scheduler.QuartzJobWorker;
import ch.o2it.weblounge.common.impl.scheduler.QuartzJobTrigger;
import ch.o2it.weblounge.common.impl.scheduler.QuartzTriggerListener;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageLayout;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobTrigger;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ModuleException;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteException;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.common.user.WebloungeUser;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
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
  protected boolean enabled = true;

  /** Site running state */
  private boolean running = false;

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

  /** Image styles */
  protected Map<String, ImageStyle> imagestyles = null;

  /** Modules */
  protected Map<String, Module> modules = null;

  /** Ordered list of site urls */
  protected List<String> hostnames = null;

  /** Jobs */
  protected Map<String, QuartzJob> jobs = null;

  /** Option handling support */
  protected OptionsHelper options = null;

  /** Request listeners */
  private List<RequestListener> requestListeners = null;

  /** Site listeners */
  private List<SiteListener> siteListeners = null;

  /** User listeners */
  private List<UserListener> userListeners = null;

  /** Root url to static content */
  protected URL staticContentRoot = null;

  /** OSGi cron service tracker */
  private CronServiceTracker cronServiceTracker = null;

  /** Quartz cron scheduler */
  private Scheduler scheduler = null;
  
  /** Listener for the quartz scheduler */
  private TriggerListener quartzTriggerListener = null;

  /**
   * Creates a new site that is initially disabled. Use {@link #setEnabled()} to
   * enable the site.
   */
  public SiteImpl() {
    languages = new HashMap<String, Language>();
    templates = new HashMap<String, PageTemplate>();
    layouts = new HashMap<String, PageLayout>();
    imagestyles = new HashMap<String, ImageStyle>();
    modules = new HashMap<String, Module>();
    hostnames = new ArrayList<String>();
    jobs = new HashMap<String, QuartzJob>();
    options = new OptionsHelper();
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
    if (running)
      stop();
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
   * @see ch.o2it.weblounge.common.site.Site#setStaticContentRoot(java.net.URL)
   */
  public void setStaticContentRoot(URL root) {
    if (root == null)
      throw new IllegalStateException("Content root url must not be null");
    this.staticContentRoot = root;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getStaticContentRoot()
   */
  public URL getStaticContentRoot() {
    return staticContentRoot;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addImageStyle(ch.o2it.weblounge.common.site.ImageStyle)
   */
  public void addImageStyle(ImageStyle imagestyle) {
    if (imagestyle == null)
      throw new IllegalStateException("Imagestyle must not be null");
    imagestyles.put(imagestyle.getIdentifier(), imagestyle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeImageStyle(java.lang.String)
   */
  public ImageStyle removeImageStyle(String imagestyle) {
    if (imagestyle == null)
      throw new IllegalStateException("Imagestyle must not be null");
    return imagestyles.remove(imagestyle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getImageStyle(java.lang.String)
   */
  public ImageStyle getImageStyle(String imagestyle) {
    return imagestyles.get(imagestyle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getImageStyles()
   */
  public ImageStyle[] getImageStyles() {
    return imagestyles.values().toArray(new ImageStyle[imagestyles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addLayout(ch.o2it.weblounge.common.page.PageLayout)
   */
  public void addLayout(PageLayout layout) {
    if (layout == null)
      throw new IllegalStateException("Layout must not be null");
    layouts.put(layout.getIdentifier(), layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeLayout(java.lang.String)
   */
  public PageLayout removeLayout(String layout) {
    if (layout == null)
      throw new IllegalStateException("Layout must not be null");
    return layouts.remove(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getLayout(java.lang.String)
   */
  public PageLayout getLayout(String layout) {
    return layouts.get(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getLayouts()
   */
  public PageLayout[] getLayouts() {
    return layouts.values().toArray(new PageLayout[layouts.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addHostName(java.lang.String)
   */
  public void addHostName(String hostname) {
    if (hostname == null)
      throw new IllegalArgumentException("Hostname must not be null");
    hostnames.add(hostname);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeHostname(java.lang.String)
   */
  public boolean removeHostname(String hostname) {
    if (hostname == null)
      throw new IllegalArgumentException("Hostname must not be null");
    return hostnames.remove(hostname);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getHostNames()
   */
  public String[] getHostNames() {
    return hostnames.toArray(new String[hostnames.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getHostName()
   */
  public String getHostName() {
    return hostnames.size() > 0 ? hostnames.get(0) : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getLink()
   */
  public String getLink() {
    String hostname = getHostName();
    return hostname != null ? hostname : "/";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addModule(ch.o2it.weblounge.common.site.Module)
   */
  public void addModule(Module module) throws ModuleException {
    if (module == null)
      throw new IllegalArgumentException("Module must not be null");
    module.init(this);
    modules.put(module.getIdentifier(), module);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeModule(java.lang.String)
   */
  public Module removeModule(String module) throws ModuleException {
    if (module == null)
      throw new IllegalArgumentException("Module must not be null");
    Module m = modules.remove(module);
    if (m != null)
      m.destroy();
    return m;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getModule(java.lang.String)
   */
  public Module getModule(String module) {
    if (module == null)
      throw new IllegalArgumentException("Module must not be null");
    return modules.get(module);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#getModules()
   */
  public Module[] getModules() {
    return modules.values().toArray(new Module[modules.size()]);
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
    if (requestListeners != null) {
      synchronized (requestListeners) {
        requestListeners.remove(listener);
      }
    }
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
    if (siteListeners != null) {
      synchronized (siteListeners) {
        siteListeners.remove(listener);
      }
    }
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
    if (userListeners != null) {
      synchronized (userListeners) {
        userListeners.remove(listener);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#start()
   */
  public void start() throws SiteException, IllegalStateException {
    log_.debug("Starting site {}", this);
    if (running)
      throw new IllegalStateException("Site is already running");
    if (!enabled)
      throw new IllegalStateException("Cannot start a disabled site");

    // Start the site modules
    synchronized (modules) {
      List<Module> started = new ArrayList<Module>(modules.size());
      for (Module module : modules.values()) {
        try {
          module.start();
          started.add(module);
        } catch (Exception e) {
          for (Module m : started) {
            try {
              log_.debug("Halting module {}", m);
              m.stop();
            } catch (Exception e2) {
              log_.error("Error stopping module {}", m, e2);
            }
          }
          throw new SiteException(this, "Error starting module " + module, e);
        }
      }
    }
    
    // Register jobs
    synchronized (jobs) {
      for (QuartzJob job : jobs.values()) {
        scheduleJob(job);
      }
    }

    // Finally, mark this site as running
    running = true;
    log_.info("Site {} started", this);

    // Tell listeners
    fireSiteStarted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#stop()
   */
  public void stop() throws IllegalStateException {
    log_.debug("Stopping site {}", this);
    if (!running)
      throw new IllegalStateException("Site is not running");

    // Stop jobs
    synchronized (jobs) {
      for (QuartzJob job : jobs.values()) {
        unscheduleJob(job);
      }
    }
    
    // Shutdown all of the modules
    synchronized (modules) {
      for (Module module : modules.values()) {
        try {
          log_.debug("Stopping module {}", module);
          module.stop();
        } catch (Exception e) {
          log_.error("Error stopping module {}", module, e);
        }
      }
    }

    // Finally, mark this site as stopped
    running = false;
    log_.info("Site {} stopped", this);

    // Tell listeners
    fireSiteStopped();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#isRunning()
   */
  public boolean isRunning() {
    return running;
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
   * Method to fire a <code>siteStarted()</code> message to all registered
   * <code>SiteListener</code>s.
   */
  protected void fireSiteStarted() {
    if (siteListeners == null)
      return;
    synchronized (siteListeners) {
      for (SiteListener listener : siteListeners) {
        listener.siteStarted(this);
      }
    }
  }

  /**
   * Method to fire a <code>siteStopped()</code> message to all registered
   * <code>SiteListener</code>s.
   */
  protected void fireSiteStopped() {
    if (siteListeners == null)
      return;
    synchronized (siteListeners) {
      for (SiteListener listener : siteListeners) {
        listener.siteStopped(this);
      }
    }
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

  /* -------------------------------- OSGi -------------------------------- */

  /**
   * This method is a callback from the service tracker that is started when
   * this site is started. It is looking for an implementation of the Quartz
   * scheduler. The configuration is expected to be <code>0..1</code>, so there
   * should only be one scheduler instance at any given moment.
   * 
   * @param scheduler
   *          the quartz scheduler
   */
  void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
    this.quartzTriggerListener = new QuartzTriggerListener(this);
    try {
      this.scheduler.addTriggerListener(quartzTriggerListener);
      if (running) {
        synchronized (jobs) {
          for (QuartzJob job : jobs.values()) {
            scheduleJob(job);
          }
        }
      }
    } catch (SchedulerException e) {
      log_.error("Error adding trigger listener to quartz scheduler", e);
    }
  }

  /**
   * This method is a callback from the service tracker that is started when
   * this site is started, indicating that the scheduler service is no longer
   * available.
   */
  void removeScheduler() {
    log_.info("Site " + this + " can no longer execute jobs (scheduler was taken down)");
    this.quartzTriggerListener = null;
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
      String identifier = (String) context.getProperties().get(PROP_IDENTIFIER);
      if (identifier == null)
        throw new IllegalStateException("Site needs an identifier");
      setIdentifier(identifier);
    }

    log_.debug("Initializing site {}", this);
    log_.debug("Signing up for cron services");

    // Connect to the
    cronServiceTracker = new CronServiceTracker(bundleContext, this);
    cronServiceTracker.open();

    log_.info("Site {} initialized", this);
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
    log_.debug("Taking down site {}", this);
    log_.debug("Stopped looking for cron services");
    cronServiceTracker.close();
    log_.info("Site {} deactivated", this);
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
   * @see ch.o2it.weblounge.common.site.Site#getUser(java.lang.String)
   */
  public WebloungeUser getUser(String login) {
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
   * @see ch.o2it.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    options.setOption(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptions()
   */
  public Map<String, List<String>> getOptions() {
    return options.getOptions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addJob(java.lang.String,
   *      ch.o2it.weblounge.common.scheduler.Job, java.util.Map)
   */
  public void addJob(String name, Class<? extends Job> job,
      Dictionary<String, Serializable> config) {
    addJob(name, job, config, new FireOnceJobTrigger());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#addJob(java.lang.String,
   *      ch.o2it.weblounge.common.scheduler.Job, java.util.Map,
   *      ch.o2it.weblounge.common.scheduler.JobTrigger)
   */
  public void addJob(String name, Class<? extends Job> job,
      Dictionary<String, Serializable> config, JobTrigger trigger) {

    QuartzJob jobDetail = new QuartzJob(name, job, config, trigger);
    
    // Register the job
    synchronized (jobs) {
      jobs.put(job.getName(), jobDetail);
    }
    
    // Schedule it if this site is running
    if (running) {
      scheduleJob(jobDetail);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Site#removeJob(java.lang.String)
   */
  public void removeJob(String name) {
    synchronized (jobs) {
      jobs.remove(name);
    }
  }

  /**
   * Schedules the job with the Quartz job scheduler.
   * 
   * @param job
   *          the job
   */
  private void scheduleJob(QuartzJob job) {
    if (scheduler == null)
      return;
    
    // Throw the job at quartz
    String groupName = "site " + this.getIdentifier();
    String jobName = job.getName();
    Class<?> jobClass = job.getJob();
    JobTrigger trigger = job.getTrigger();
    
    synchronized (jobs) {
      
      // Set up the job detail
      JobDataMap jobData = new JobDataMap();
      jobData.put(QuartzJobWorker.CLASS, jobClass);
      jobData.put(QuartzJobWorker.CONTEXT, job.getContext());
      JobDetail quartzJob = new JobDetail(jobName, groupName, QuartzJobWorker.class);
      quartzJob.setJobDataMap(jobData);
      
      // Define the trigger
      Trigger quartzTrigger = new QuartzJobTrigger(jobName, groupName, trigger);
      quartzTrigger.addTriggerListener(quartzTriggerListener.getName());
      
      // Schedule
      try {
        Date date = scheduler.scheduleJob(quartzJob, quartzTrigger);
        log_.info("Job '{}' scheduled, first execution at {}", jobName, date);
      } catch (SchedulerException e) {
        log_.error("Error trying to schedule job {}: {}", new Object[] {
            jobName,
            e.getMessage(),
            e });
      }
    }
  }

  /**
   * Removes the job from the Quartz job scheduler.
   * 
   * @param job
   *          the job
   */
  private void unscheduleJob(QuartzJob job) {
    if (scheduler == null)
      return;

    String groupName = "site " + this.getIdentifier();
    String jobName = job.getName();
    try {
      if (scheduler.unscheduleJob(jobName, groupName))
        log_.info("Unscheduled job {}", jobName);
    } catch (SchedulerException e) {
      log_.error("Error trying to schedule job {}: {}", new Object[] {
          jobName,
          e.getMessage(),
          e });
    }
  }

}
