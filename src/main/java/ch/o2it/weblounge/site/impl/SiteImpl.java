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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.security.DefaultAuthorizationProvider;
import ch.o2it.weblounge.common.impl.security.SecurityManagerImpl;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.impl.util.classloader.SiteClassLoader;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.resource.Collection;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.AuthorizationProvider;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.common.site.SiteLogger;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.SiteAdmin;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.common.user.WebloungeUser;
import ch.o2it.weblounge.contentrepository.PageListener;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.site.SiteService;

import com.sun.corba.se.impl.activation.RepositoryImpl;
import com.sun.corba.se.spi.activation.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quicktime.Errors;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

/**
 * The <code>Site</code> object represents one of multiple sites managed by the
 * weblounge system.
 */
public class SiteImpl implements Site {

  /** Site identifier */
  private String id_;

  /** Site xml configuration file */
  private SiteConfigurationImpl config_ = null;

  /** The site request dispatcher */
  private SiteDispatcher dispatcher_;

  /** The module manager */
  private ModuleManager moduleManager_;

  /** The site listeners */
  private List siteListeners;

  /** List of request listeners */
  private static List requestListeners;

  /** The user listeners */
  private List userListeners_;

  /** The page listeners */
  private List pageListeners_;

  /** The registered channels */
  private List channels_;

  /** Page registry */
  private PageCache pageCache_;

  /** Page registry */
  private PageHeaderCache pageHeaderCache_;

  /** Page registry */
  private PageHeaderListCache pageHeaderListCache_;

  /** the user registry for this site */
  private UserRegistryImpl users_;

  /** the registry for known groups */
  private GroupRegistryImpl groups_;

  /** the registry for known roles */
  private RoleRegistryImpl roles_;

  /** The site url registry */
  private UrlRegistryImpl urls_;

  /** The control panel registry */
  private ControlPanelRegistry controlPanels_;

  /** Loaded modules for this site */
  private ModuleRegistry modules_;

  /** Loaded image styles for this site */
  private ImageStyleRegistryImpl imageStyles_;

  /** the session tracker keeps track of user movements */
  private SessionTracker tracker_;

  /** the site file repository */
  private Repository repository_;

  /** Security manager */
  private SecurityManager securityManager_;

  /** the error handler */
  private Errors errorHandler_;

  /** The path pointing to the original sites directory */
  private String virtualPath_;

  /** The site's work directory */
  private File workdir_;

  /** The page listener channel provider */
  private PageListenerChannelProvider pageListenerChannelProvider_;

  /** The site logger */
  private SiteLogger logger_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = SiteImpl.class.getName();

  /** Logging facility */
  final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Constructor for class Site.
   * 
   * @param id
   *          the site identifier
   */
  public SiteImpl() {
  }

  /**
   * Initializer for class Site.
   * 
   * @param id
   *          the site identifier
   */
  protected void init(String id) {
    id_ = id;
    siteListeners = new ArrayList();
    requestListeners = new ArrayList();
    userListeners_ = new ArrayList();
    pageListeners_ = new ArrayList();
    channels_ = new ArrayList();
    controlPanels_ = new ControlPanelRegistry();
    securityManager_ = new SecurityManagerImpl();
    imageStyles_ = new ImageStyleRegistryImpl();
    modules_ = new ModuleRegistry();
    moduleManager_ = new ModuleManager(this);
    pageListenerChannelProvider_ = new PageListenerChannelProvider(this);
    urls_ = new UrlRegistryImpl(this);
    pageCache_ = new PageCache(this);
    pageHeaderCache_ = new PageHeaderCache(this);
    pageHeaderListCache_ = new PageHeaderListCache(this);
    users_ = new UserRegistryImpl(this);
    groups_ = new GroupRegistryImpl();
    roles_ = new RoleRegistryImpl();
    logger_ = new SiteLoggerImpl(this);
  }

  /**
   * Returns the site identifier.
   * 
   * @return the site identifier
   */
  public String getIdentifier() {
    return id_;
  }

  /**
   * Returns the description of this site, e.g.
   * "World Floorball Championships 2004".
   * 
   * @param l
   *          the language used to describe the site
   * @return the site description
   */
  public String getDescription(Language l) {
    return config_.getDescription(l);
  }

  /**
   * Returns <code>true</code> if the site is enabled.
   * 
   * @return <code>true</code> if the site is enabled
   */
  public boolean isEnabled() {
    return config_.isEnabled;
  }

  /**
   * Returns the site's logging facility.
   * 
   * @return the site logger
   */
  public SiteLogger getLogger() {
    return logger_;
  }

  /**
   * Returns <code>true</code> if this is the default site.
   * 
   * @return <code>true</code> if this is the default site
   */
  public boolean isDefault() {
    return SiteRegistry.getInstance().getDefault() == this;
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#getOption(java.lang.String)
   */
  public String getOption(String name) {
    return config_.getOption(name);
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#getOption(java.lang.String,
   *      java.lang.String)
   */
  public String getOption(String name, String defaultValue) {
    return config_.getOption(name, defaultValue);
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#getOptions(java.lang.String)
   */
  public String[] getOptions(String name) {
    return config_.getOptions(name);
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#options()
   */
  public Map getOptions() {
    return config_.options();
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return config_.hasOption(name);
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#options()
   */
  public Iterator options() {
    return config_.options();
  }

  /**
   * Adds <code>listener</code> to the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  public void addSiteListener(SiteListener listener) {
    siteListeners.add(listener);
  }

  /**
   * Removes <code>listener</code> from the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  public void removeSiteListener(SiteListener listener) {
    siteListeners.remove(listener);
  }

  /**
   * Adds <code>listener</code> to the list of request listeners if it has not
   * already been registered.
   * 
   * @param listener
   *          the lister
   */
  public void addRequestListener(RequestListener listener) {
    if (!requestListeners.contains(listener))
      requestListeners.add(listener);
  }

  /**
   * Removes the listener from the list of request listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeRequestListener(RequestListener listener) {
    requestListeners.remove(listener);
  }

  /**
   * Adds the listener to the list of user listeners.
   * 
   * @param listener
   *          the user listener to add
   */
  public void addUserListener(UserListener listener) {
    userListeners_.add(listener);
  }

  /**
   * Removes the listener from the list of user listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeUserListener(UserListener listener) {
    userListeners_.remove(listener);
  }

  /**
   * Adds a <code>PageListener</code> to the page registry, who will be notified
   * about new, moved, deleted or altered pages.
   * 
   * @param listener
   *          the new page listener
   */
  public void addPageListener(PageListener listener) {
    pageListeners_.add(listener);
  }

  /**
   * Removes a <code>PageListener</code> from the page registry.
   * 
   * @param listener
   *          the page listener
   */
  public void removePageListener(PageListener listener) {
    pageListeners_.remove(listener);
  }

  /**
   * Adds <code>handler</code> to the list of request handlers.
   * 
   * @param handler
   *          the request handler
   */
  public void addRequestHandler(RequestHandler handler) {
    dispatcher_.addRequestHandler(handler);
  }

  /**
   * Removes the request handler from the list of request handlers.
   * 
   * @param handler
   *          the request handler to remove
   */
  public void removeRequestHandler(RequestHandler handler) {
    dispatcher_.removeRequestHandler(handler);
  }

  /**
   * Announces the site channel <code>channel</code> for subscription.
   * 
   * @param channel
   *          the new channel
   */
  public void addChannel(SiteChannel channel) {
    channels_.add(channel);
  }

  /**
   * Removes the site channel <code>channel</code> from the subscription list.
   * 
   * @param channel
   *          the channel to remove
   */
  public void removeChannel(SiteChannel channel) {
    channels_.remove(channel);
  }

  /**
   * Returns the available channels.
   * 
   * @return the channels
   */
  public SiteChannel[] getChannels() {
    SiteChannel[] channels = new SiteChannel[channels_.size()];
    return (SiteChannel[]) channels_.toArray(channels);
  }

  /**
   * Returns the real path on the server for a given virtual path.
   * 
   * @param path
   *          the virtual (site-relative) path
   * @return the real (physical) path on the server
   */
  public String getPhysicalPath(String path) {
    return Env.getRealPath(PathSupport.concat("sites/" + this + "/", path));
  }

  /**
   * Returns the virtual path on the server relative to the web application.
   * Using this path e. g. for a renderer <code>renderer/myjsp.jsp</code> will
   * produce </code>/sites/mysite/renderer/myjsp.jsp</code>.
   * 
   * @param path
   *          the virtual path relative to the site
   * @param webapp
   *          <code>true</code> to preprend the webapp url
   * @return the virtual work path relative to the webapp
   */
  public String getVirtualPath(String path, boolean webapp) {
    String url = UrlSupport.concat(virtualPath_, path);
    if (webapp)
      url = UrlSupport.concat(Env.getURI(), url);
    return url;
  }

  /**
   * Returns the servername of this page. This method will return the complete
   * hostname as found in the <code>&lt;name&gt;</code> section of
   * <code>site.xml</code>.
   * 
   * @return the site's server name
   */
  public String getServername() {
    String servername = getServerNames()[0];
    if (servername.startsWith("*")) {
      servername = "www" + servername.substring(1);
    }
    return servername;
  }

  /**
   * Returns the absolute link which can be used to reach this page. This method
   * will return the complete hostname as found in the <code>&lt;name&gt;</code>
   * section of <code>site.xml</code> concatenated with the weblounge
   * mountpoint.
   * 
   * @return the absolute link to this site
   */
  public String getLink() {
    String link = UrlSupport.concat(new String[] {
        getServername(),
        Env.getMountpoint() });
    if (link.endsWith("/")) {
      link = link.substring(0, link.length() - 1);
    }
    return link;
  }

  /**
   * Returns the registered JAAS authentication modules.
   * 
   * @return the authentication modules
   */
  public AuthenticationModule[] getAuthenticationModules() {
    AuthenticationModule[] modules = new AuthenticationModule[config_.authenticationModules.size()];
    return (AuthenticationModule[]) config_.authenticationModules.toArray(modules);
  }

  /**
   * Returns the database collection for <code>path</code>, which is interpreted
   * relative to the site's root collection.
   * <p>
   * For example, a service would request the collection path
   * <code>myservice</code> which returns the database collection
   * <code>weblounge/sites/mysite/myservice</code>.
   * <p>
   * The method returns the collection either if it exists or can be created (
   * <code>
	 * create</code> is <code>true</code>.
   * 
   * @param path
   *          the site relative path
   * @param create
   *          <code>true</code> to create the collection
   * @return the collection or <code>null</code> if the collection does not
   *         exist
   */
  public Collection getCollection(String path, boolean create) {
    DBXMLDatabase db = (DBXMLDatabase) ServiceManager.getEnabledSystemService(DBXMLDatabase.ID);
    if (db != null) {
      return db.getCollection(getCollectionPath(path), create);
    }
    return null;
  }

  /**
   * Returns the database collection to the database.
   * 
   * @param c
   *          the collection to be returned
   */
  public void returnCollection(Collection c) {
    DBXMLDatabase db = (DBXMLDatabase) ServiceManager.getEnabledSystemService(DBXMLDatabase.ID);
    db.returnCollection(c);
  }

  /**
   * Returns the path to the database collection for <code>path</code>, which is
   * interpreted relative to the site's root collection.
   * <p>
   * For example, a service would request the collection path
   * <code>myservice</code> which returns the database collection
   * <code>/db/weblounge/sites/mysite/myservice</code>.
   * <p>
   * 
   * @param path
   *          the site relative path
   * @return the collection path
   */
  public String getCollectionPath(String path) {
    String collectionPath = "/db/weblounge/sites/" + this + "/";
    collectionPath = UrlSupport.concat(collectionPath, path);
    return collectionPath;
  }

  /**
   * Returns the site's security manager.
   * 
   * @return the security manager
   */
  public SecurityManager getSecurityManager() {
    return securityManager_;
  }

  /**
   * Returns the repository associated with this site.
   * 
   * @return the site repository
   */
  public Repository getRepository() {
    return repository_;
  }

  /**
   * Returns the site's load factor, which is <code>1</code> for normal sites
   * with up to 10'000 hits /s. This factor can be configured in the
   * <code>&lt;performance&gt;</code> section of the site configuration.
   * 
   * @return the site's load factor
   */
  public int getLoadFactor() {
    return config_.loadfactor;
  }

  /**
   * Returns the number of versions to keep of each page.
   * 
   * @return the history size
   */
  public int getHistorySize() {
    return config_.historysize;
  }

  /**
   * Returns <code>true</code> if pages should be stored in an internal page
   * cache. Using the cache results in better overall performance since multiple
   * database lookups for a page may be avoided.
   * 
   * @return <code>true</code> if the page cache is enabled
   */
  public boolean isPageCacheEnabled() {
    return config_.usePageCache;
  }

  /**
   * Returns <code>true</code> <code>language</code> is supported by this site.
   * 
   * @param language
   *          the language
   * @return <code>true</code> if the language is supported
   */
  public boolean supportsLanguage(Language language) {
    if (language == null)
      return false;
    return config_.languages.containsValue(language);
  }

  /**
   * Returns the default language for this site.
   * 
   * @return the site default language
   */
  public Language getDefaultLanguage() {
    return config_.languages.getDefaultLanguage();
  }

  /**
   * Returns the site's language registry.
   * 
   * @return the site languages
   */
  public LanguageRegistry getLanguages() {
    return config_.languages;
  }

  /**
   * Returns the control panel registry containing all registered panels.
   * 
   * @return the control panels
   */
  public ControlPanelRegistry getControlPanels() {
    return controlPanels_;
  }

  /**
   * Returns the server names that will lead to this site. A servername is the
   * first part of a ur. For example, in <tt>http://www.o2it.ch/weblounge</tt>,
   * <tt>www.o2it.ch</code> is the servername.
   * 
   * @return the registered server names
   */
  public String[] getServerNames() {
    Object[] serverNames = new String[config_.urls.size()];
    return (String[]) config_.urls.toArray(serverNames);
  }

  /**
   * Returns the mountpoints of this site. Mountpoints define a partition and a
   * url, where this partition is mounted.
   * 
   * @return the registered mountpoints
   */
  public Mountpoint[] getMountpoints() {
    Object[] mountpoints = new Mountpoint[config_.mountpoints.size()];
    return (Mountpoint[]) config_.mountpoints.toArray(mountpoints);
  }

  /**
   * Returns the site modules that are currently associated with this site. Note
   * that the modules may be active or inactive.
   * 
   * @return the site modules
   */
  public ModuleRegistry getModules() {
    return modules_;
  }

  /**
   * Returns the site module with the given identifier or <code>null</code> if
   * no such module can be found.
   * 
   * @param id
   *          the module identifier
   * @return the module
   */
  public Module getModule(String id) {
    return (Module) modules_.get(id);
  }

  /**
   * Returns this site's navigation facility, which keeps track of all links.
   * 
   * @return the navigation facility
   */
  public UrlRegistry getNavigation() {
    return urls_;
  }

  /**
   * Returns this site's renderer registry which keeps track of the defined
   * renderer bundles.
   * 
   * @return the renderer registry
   */
  public RendererRegistry getRenderers() {
    return config_.templates;
  }

  /**
   * Returns the identifier of the default template for this site.
   * 
   * @return the site default template
   */
  public String getDefaultTemplate() {
    return config_.templates.getDefault("html").getIdentifier();
  }

  /**
   * Returns this site's request handler registry which keeps track of the
   * defined request handler vor servlets etc.
   * 
   * @return the request handler registry
   */
  public RequestHandlerRegistry getRequestHandler() {
    return config_.handlers;
  }

  /**
   * Returns the <code>UserRegistry</code> of this site where all users are
   * stored. The user registry may also be used to verify that a user has
   * certain roles.
   * 
   * @return the site user registry
   */
  public WebloungeUser getUser(String string) {
    // TODO: See if user has been loaded before. If so, return it
    // TODO: Check login modules for user
  }

  /**
   * Returns the site administrator.
   * 
   * @return the site admin
   * @see ch.o2it.weblounge.api.site.Site#getAdministrator()
   */
  public SiteAdmin getAdministrator() {
    return config_.admin;
  }

  /**
   * Returns the <code>GroupRegistry</code> of this site where all groups are
   * stored.
   * 
   * @return the site group registry
   */
  public GroupRegistry getGroups() {
    return groups_;
  }

  /**
   * Returns the <code>RoleRegistry</code> of this site where all roles are
   * stored.
   * 
   * @return the site role registry
   */
  public RoleRegistry getRoles() {
    return roles_;
  }

  /**
   * Returns the site specific error handler which is used to display error
   * messages in pages and pagelets.
   * 
   * @return the site error handler
   */
  public Errors getErrorHandler() {
    return errorHandler_;
  }

  /**
   * Returns the session tracker for this site.
   * 
   * @return this site's session tracker
   */
  public SessionTracker getSessionTracker() {
    return tracker_;
  }

  /**
   * Returns the image style registry.
   * 
   * @return the image style registry
   */
  public ImageStyleRegistry getImageStyles() {
    return imageStyles_;
  }

  /**
   * Returns the image style with the given identifier or <code>null</code> if
   * no such style is found.
   * 
   * @return the image style
   */
  public ImageStyle getImageStyle(String id) {
    return imageStyles_.getStyle(id);
  }

  /**
   * Returns a class loader suitable for loading module classes and libraries.
   * 
   * @return the site specific module class loader
   */
  public SiteClassLoader getClassLoader() {
    return config_.classLoader;
  }

  /**
   * Returns the site service identified by <code>service</code> or
   * <code>null</code> if no such service is available.
   * 
   * @param service
   *          the service identifier
   * @return the service
   */
  public SiteService getService(String service) {
    return (SiteService) config_.services.get(service);
  }

  /**
   * Returns the requested service if it can be found in the registry (and it is
   * enabled, in case that <code>enabledOnly</code> is <code>true</code>).
   * 
   * @param id
   *          the service identifier
   * @param enabledOnly
   *          <code>true</code> to return enabled services only
   * @return the requested service
   */
  public SiteService getService(String id, boolean enabledOnly) {
    SiteService s = getService(id);
    if (s != null && (!enabledOnly || s.isEnabled()))
      return s;
    return null;
  }

  /**
   * Returns a reference to the site's work directory. This is usually located
   * at <code>%WEBLOUNGE_HOME%/work/weblounge/sites/%sitename%</code>
   * 
   * @return the work directory
   */
  public File getWorkDirectory() {
    if (workdir_ == null)
      workdir_ = new File(PathSupport.concat(new String[] {
          Env.getRealPath("/"),
          Directories.WORK_DIR,
          Directories.SITES_DIR,
          getIdentifier() }));
    return workdir_;
  }

  /**
   * Returns a hashcode for this site object, which is the unique key taken from
   * the community database.
   */
  public int hashCode() {
    return id_.hashCode();
  }

  /**
   * Returns true if the given object represents the same site.
   * 
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object object) {
    if (object instanceof SiteImpl) {
      SiteImpl site = (SiteImpl) object;
      return id_.equals(site.id_);
    }
    return false;
  }

  /**
   * Returns a string representation of this site.
   * 
   * @return a string representation of this site
   */
  public String toString() {
    return getIdentifier();
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E ModuleListener
   * -------------------------------------------------------------
   */

  /**
   * Callback to notify this site that an associated module has been started.
   * 
   * @param module
   *          the started module
   */
  public void moduleStarted(Module module) {
    ImageStyleRegistry styles = module.getImageStyles();
    Iterator si = styles.styles();
    while (si.hasNext()) {
      ImageStyle style = (ImageStyle) si.next();
      if (!config_.imagestyles.containsStyle(style)) {
        imageStyles_.addStyle(style);
      }
    }
  }

  /**
   * Callback to notify this site that an associated module has been stopped.
   * 
   * @param module
   *          the stopped module
   */
  public void moduleStopped(Module module) {
    ImageStyleRegistry styles = module.getImageStyles();
    Iterator si = styles.styles();
    while (si.hasNext()) {
      ImageStyle style = (ImageStyle) si.next();
      if (!config_.imagestyles.containsStyle(style)) {
        imageStyles_.removeStyle(style);
      }
    }
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E RequestListener
   * -------------------------------------------------------------
   */

  /**
   * Called when a request is about to be processed.
   * 
   * @param request
   *          the incoming request
   * @param response
   *          the servlet response
   */
  public void requestStarted(WebloungeRequest request,
      WebloungeResponse response) {
    fireStartRequest(request, response);
  }

  /**
   * This method is called when the request has been successfully delivered to
   * the client.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   */
  public void requestDelivered(WebloungeRequest request,
      WebloungeResponse response) {
    fireRequestDelivered(request, response);
  }

  /**
   * Callback for requests that raise an exception. The <code>reason</code>
   * denotes the <code>HTTP/1.1</code> error response code.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @param reason
   *          the reason of failure
   */
  public void requestFailed(WebloungeRequest request,
      WebloungeResponse response, int reason) {
    fireRequestFailed(request, response, reason);
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E UserListener
   * -------------------------------------------------------------
   */

  /**
   * This method is called if the user moves from one url to another. Note that
   * moving does not include calling actions. Only movements that are detected
   * by the <code>SimpleRequestHandler</code> are noted.
   * 
   * @param user
   *          the moving user
   * @param url
   *          the url that the user moved to
   */
  public void userMoved(User user, WebUrl url) {
    fireUserMoved(user, url);
  }

  /**
   * This method is called if a user logs in.
   * 
   * @param user
   *          the user that logged in
   */
  public void userLoggedIn(User user) {
    fireUserLoggedIn(user);
  }

  /**
   * This method is called if a user logs out.
   * 
   * @param user
   *          the user that logged out
   */
  public void userLoggedOut(User user) {
    fireUserLoggedOut(user);
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E PageListener
   * -------------------------------------------------------------
   */

  /**
   * This method is called if the page at location <code>url</code> has been
   * created by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the creating user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageCreated(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageCreated(WebUrl url, User user) {
    firePageCreated(url, user);
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * removed by user <code>user</code>.
   * 
   * @param url
   *          the page's former location
   * @param user
   *          the removing user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageRemoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageRemoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageRemoved(WebUrl url, User user) {
    firePageRemoved(url, user);
  }

  /**
   * This method is called if the page at location <code>from</code> has been
   * moved to <code>to</code> by user <code>user</code>.
   * 
   * @param from
   *          the page's former location
   * @param to
   *          the page's new location
   * @param user
   *          the user moving the page
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageMoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageMoved(WebUrl from, WebUrl to, User user) {
    firePageMoved(from, to, user);
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * published by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user publishing the page
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pagePublished(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pagePublished(WebUrl url, User user) {
    firePagePublished(url, user);
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * unpublished by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user unpublishing the page
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageUnpublished(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageUnpublished(WebUrl url, User user) {
    firePageUnlocked(url, user);
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * locked by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user locking the page
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageLocked(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageLocked(WebUrl url, User user) {
    firePageLocked(url, user);
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * released by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user releasing the page lock
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageUnlocked(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageUnlocked(WebUrl url, User user) {
    firePageUnlocked(url, user);
  }

  /**
   * Notifies the listener about a new page renderer at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newRenderer
   *          the new renderer
   * @param oldRenderer
   *          the former renderer
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageRendererChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.common.site.api.renderer.Renderer,
   *      ch.o2it.weblounge.common.site.api.renderer.Renderer,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageRendererChanged(WebUrl url, Renderer newRenderer,
      Renderer oldRenderer, User user) {
    firePageRendererChanged(url, newRenderer, oldRenderer, user);
  }

  /**
   * Notifies the listener about a new page layout at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newLayout
   *          the new layout
   * @param oldLayout
   *          the former layout
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageLayoutChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.core.content.Layout,
   *      ch.o2it.weblounge.core.content.Layout,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageLayoutChanged(WebUrl url, Layout newLayout, Layout oldLayout,
      User user) {
    firePageLayoutChanged(url, newLayout, oldLayout, user);
  }

  /**
   * Notifies the listener about a new page type at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newType
   *          the new page type
   * @param oldType
   *          the former page type
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageTypeChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      java.lang.String, java.lang.String,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageTypeChanged(WebUrl url, String newType, String oldType,
      User user) {
    firePageTypeChanged(url, newType, oldType, user);
  }

  /**
   * Notifies the listener about a change in the list of keywords at url
   * <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newKeywords
   *          the new keywords
   * @param oldKeywords
   *          the old keywords
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.contentrepository.api.content.PageListener#pageKeywordsChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      java.lang.String[], java.lang.String[],
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public void pageKeywordsChanged(WebUrl url, String[] newKeywords,
      String[] oldKeywords, User user) {
    firePageKeywordsChanged(url, newKeywords, oldKeywords, user);
  }

  /**
   * The dispatching method, which takes a <code>HttpServletRequest</code> and
   * dispatches it to the site (which will in turn pass it on to a jsp or any
   * other suitable renderer).
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response) {
    dispatcher_.dispatch(request, response);
  }

  /**
   * This method is called by the <code>SiteManager</code> to start configuring
   * the site. This method then starts a new <code>XMLConfigurator</code> on the
   * configuration file to stay updated about configuration changes.
   * 
   * @param config
   *          the site configuration
   */
  void configure(SiteConfigurationImpl config) throws ConfigurationException {
    config_ = config;
    virtualPath_ = UrlSupport.trim("/sites/" + config.identifier);
    dispatcher_ = new SiteDispatcher(this, config_.isEnabled);
    config_.admin.init(this);
  }

  /**
   * Initializes and starts the site. This is done after configuration and
   * mainly registers and initializes the various site registries.
   */
  public void start() {
    repository_ = new RepositoryImpl(this);

    urls_.clear();
    SiteRegistries.add(UrlRegistry.ID, this, urls_);
    for (int i = 0; i < config_.mountpoints.size(); i++) {
      urls_.addMountpoint((Mountpoint) config_.mountpoints.get(i));
    }

    pageCache_.clear();
    SiteRegistries.add(PageCache.ID, this, pageCache_);
    pageCache_.setMaximumCacheSize(config_.loadfactor * 20 + 10);

    pageHeaderCache_.clear();
    SiteRegistries.add(PageHeaderCache.ID, this, pageHeaderCache_);
    pageHeaderCache_.setMaximumCacheSize(config_.loadfactor * 20 + 10);

    pageHeaderListCache_.clear();
    SiteRegistries.add(PageHeaderListCache.ID, this, pageHeaderListCache_);
    pageHeaderListCache_.setMaximumCacheSize(config_.loadfactor * 20 + 10);

    // Listeners
    pageListenerChannelProvider_.start();

    // Handlers

    SiteRegistries.add(RequestHandlerRegistry.ID, this, new RequestHandlerRegistry());

    // Languages

    SiteRegistries.add(LanguageRegistry.ID, this, config_.languages);

    // Imagestyles

    imageStyles_.clear();
    SiteRegistries.add(ImageStyleRegistry.ID, this, imageStyles_);
    ImageStyleRegistry styles = config_.imagestyles;
    Iterator si = styles.styles();
    while (si.hasNext()) {
      ImageStyle style = (ImageStyle) si.next();
      imageStyles_.addStyle(style);
    }

    // Control Panels

    controlPanels_.clear();
    SiteRegistries.add(ControlPanelRegistry.ID, this, controlPanels_);

    // Renderers

    SiteRegistries.add(RendererRegistry.ID, this, config_.templates);
    Iterator renderers = config_.templates.values().iterator();
    while (renderers.hasNext()) {
      ((RendererBundle) renderers.next()).setSite(this);
    }

    // Users

    SiteRegistries.add(UserRegistry.ID, this, users_);

    // Groups

    SiteRegistries.add(GroupRegistry.ID, this, groups_);

    // Roles

    SiteRegistries.add(RoleRegistry.ID, this, roles_);

    // Security

    AuthorizationProvider ap = new DefaultAuthorizationProvider(this);
    securityManager_.addProvider(Role.class.getName(), ap);
    securityManager_.addProvider(User.class.getName(), ap);

    // Services

    SiteRegistries.add(ServiceRegistry.ID, this, config_.services);
    Iterator services = config_.services.values().iterator();
    while (services.hasNext()) {
      SiteService service = (SiteService) services.next();
      service.setSite(this);
      try {
        ServiceManager.startService(service);
      } catch (ServiceException e) {
        log_.error("Unable to start service '{}' of site '{}': {}", new Object[] {service, this, e.getMessage()});
      } catch (ServiceDependencyException e) {
        log_.error("Unable to start service '{}' of site '{}' due to circular dependencies", service, this);
      }
    }

    // Handlers

    SiteRegistries.add(RequestHandlerRegistry.ID, this, config_.handlers);
    for (Iterator i = config_.handlers.values().iterator(); i.hasNext();) {
      dispatcher_.addRequestHandler((RequestHandler) i.next());
    }

    // Modules

    moduleManager_.init();
    moduleManager_.startAllModules();

    // Jobs

    Iterator jobs = config_.jobs.iterator();
    while (jobs.hasNext()) {
      SiteJob job = (SiteJob) jobs.next();
      job.setSite(this);
      Daemon.getInstance().addJob(job);
    }

    // Enable the site to deliver content
    dispatcher_.startDispatching();
  }

  /**
   * Method to shut down this site. This includes unloading all modules,
   * stopping the site services and sending a <code>shudown</code> event to
   * registered site listeners.
   */
  public void stop() {

    // Disable dispatcher
    dispatcher_.setEnabled(false);
    dispatcher_.stopDispatching();

    // Modules

    moduleManager_.destroy();
    moduleManager_.stopAllModules();

    // Services

    ServiceManager.stopAllServices(config_.services);

    // Jobs

    Iterator jobs = config_.jobs.iterator();
    while (jobs.hasNext()) {
      SiteJob job = (SiteJob) jobs.next();
      Daemon.getInstance().removeJob(job);
    }

    // Handlers

    SiteRegistries.remove(RequestHandlerRegistry.ID, this);
    for (Iterator i = config_.handlers.values().iterator(); i.hasNext();)
      dispatcher_.removeRequestHandler((RequestHandler) i.next());

    // Listeners
    pageListenerChannelProvider_.stop();

    // Tell the others
    fireSiteShutdown();
  }

  /**
   * Method to restart this site. This includes restarting all modules and
   * services and sending a <code>restart</code> event to registered site
   * listeners.
   */
  public void restart() {

    // Clear page cache
    PageCache pageCache = (PageCache) SiteRegistries.get(PageCache.ID, this);
    pageCache.clear();

    // Clear page header cache
    PageHeaderCache pageHeaderCache = (PageHeaderCache) SiteRegistries.get(PageHeaderCache.ID, this);
    pageHeaderCache.clear();

    // Clear user registry
    users_.clear();

    // try {
    // dispatcher_.stopDispatching();
    //
    // // Restart services
    //			
    // Iterator services = config_.services.values().iterator();
    // while (services.hasNext()) {
    // SiteServiceImpl service = (SiteServiceImpl)services.next();
    // service.setSite(this);
    // try {
    // ServiceManager.startService(service);
    // } catch (ServiceException e) {
    // log_.error("Unable to start service '" + service + "' of site '" + this +
    // "':" + e.getMessage());
    // } catch (ServiceDependencyException e) {
    // log_.error("Unable to start service '" + service + "' of site '" + this +
    // "' due to cirular dependencies.");
    // }
    // }
    //			
    // // Restart Modules
    //
    // ModuleManager.destroy(this);
    // ModuleManager.stopAllModules(this);
    // ModuleManager.init(this);
    // ModuleManager.startAllModules(this);
    //			
    // dispatcher_.startDispatching();
    // } catch (Exception e) {
    // dispatcher_.setEnabled(false);
    // }
  }

  /**
   * This method notifies the listeners of a site shutdown.
   */
  protected void fireSiteShutdown() {
    for (int i = 0; i < siteListeners.size(); i++) {
      ((SiteListener) siteListeners.get(i)).shutdown(this);
    }
  }

  /**
   * Method to fire a <code>startRequest()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the starting request
   * @param response
   *          the request response
   */
  protected void fireStartRequest(WebloungeRequest request,
      WebloungeResponse response) {
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = (RequestListener) requestListeners.get(i);
      listener.requestStarted(request, response);
    }
  }

  /**
   * Method to fire a <code>requestDelivered()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the delivered request
   * @param response
   *          the request response
   */
  protected void fireRequestDelivered(WebloungeRequest request,
      WebloungeResponse response) {
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = (RequestListener) requestListeners.get(i);
      listener.requestDelivered(request, response);
    }
  }

  /**
   * Method to fire a <code>requestFailed()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the failing request
   * @param error
   *          the error code
   */
  protected void fireRequestFailed(WebloungeRequest request,
      WebloungeResponse response, int error) {
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = (RequestListener) requestListeners.get(i);
      listener.requestFailed(request, response, error);
    }
  }

  /**
   * This method is called if a user moved around.
   * 
   * @param user
   *          the user that moved
   * @param url
   *          the url where the user moved to
   */
  protected void fireUserMoved(User user, WebUrl url) {
    for (int i = 0; i < userListeners_.size(); i++) {
      ((UserListener) userListeners_.get(i)).userMoved(user, url);
    }
  }

  /**
   * This method is called if a user is logged in.
   * 
   * @param user
   *          the user that logged in
   */
  protected void fireUserLoggedIn(User user) {
    for (int i = 0; i < userListeners_.size(); i++) {
      ((UserListener) userListeners_.get(i)).userLoggedIn(user);
    }
  }

  /**
   * This method is called if a user is logged out.
   * 
   * @param user
   *          the user that logged out
   */
  protected void fireUserLoggedOut(User user) {
    for (int i = 0; i < userListeners_.size(); i++) {
      ((UserListener) userListeners_.get(i)).userLoggedOut(user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * created by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the creating user
   */
  protected void firePageCreated(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageCreated(url, user);
    }
  }

  /**
   * This method is called if the page at location <code>from</code> has been
   * moved to <code>to</code> by user <code>user</code>.
   * 
   * @param from
   *          the page's former location
   * @param to
   *          the page's new location
   * @param user
   *          the user moving the page
   */
  protected void firePageMoved(WebUrl from, WebUrl to, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageMoved(from, to, user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * removed by user <code>user</code>.
   * 
   * @param url
   *          the page's former location
   * @param user
   *          the removing user
   */
  protected void firePageRemoved(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageRemoved(url, user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * published by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user publishing the page
   */
  protected void firePagePublished(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pagePublished(url, user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * unpublished by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user unpublishing the page
   */
  protected void firePageUnpublished(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageUnpublished(url, user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * locked by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user locking the page
   */
  protected void firePageLocked(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageLocked(url, user);
    }
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * released by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user releasing the page lock
   */
  protected void firePageUnlocked(WebUrl url, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageUnlocked(url, user);
    }
  }

  /**
   * Notifies the listener about a new page renderer at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newRenderer
   *          the new renderer
   * @param oldRenderer
   *          the former renderer
   * @param user
   *          the editing user
   */
  protected void firePageRendererChanged(WebUrl url, Renderer newRenderer,
      Renderer oldRenderer, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageRendererChanged(url, newRenderer, oldRenderer, user);
    }
  }

  /**
   * Notifies the listener about a new page type at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newType
   *          the new page type
   * @param oldType
   *          the former page type
   * @param user
   *          the editing user
   */
  protected void firePageTypeChanged(WebUrl url, String newType,
      String oldType, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageTypeChanged(url, newType, oldType, user);
    }
  }

  /**
   * Notifies the listener about a new page layout at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newLayout
   *          the new layout
   * @param oldLayout
   *          the former layout
   * @param user
   *          the editing user
   */
  protected void firePageLayoutChanged(WebUrl url, Layout newLayout,
      Layout oldLayout, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageLayoutChanged(url, newLayout, oldLayout, user);
    }
  }

  /**
   * Notifies the listener about a change in the list of keywords at url
   * <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newKeywords
   *          the new keywords
   * @param oldKeywords
   *          the old keywords
   * @param user
   *          the editing user
   */
  protected void firePageKeywordsChanged(WebUrl url, String[] newKeywords,
      String[] oldKeywords, User user) {
    for (int i = 0; i < pageListeners_.size(); i++) {
      ((PageListener) pageListeners_.get(i)).pageKeywordsChanged(url, newKeywords, oldKeywords, user);
    }
  }

}