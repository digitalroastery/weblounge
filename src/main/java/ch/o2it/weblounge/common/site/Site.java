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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageLayout;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.repository.RepositoryManager;
import ch.o2it.weblounge.common.request.RequestHandler;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.SecurityManager;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.user.SiteAdmin;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.common.user.WebloungeUser;

import java.io.File;
import java.io.Serializable;

/**
 * The site interface defines the method that may be called on weblounge site
 * objects.
 */
public interface Site extends ModuleListener, RequestListener, Serializable {

  /** Site descriptor */
  static final String CONFIG_FILE = "site.xml";

  /** The modules folder */
  static final String MODULE_DIR = "module";

  /**
   * Returns the site identifier.
   * 
   * @return the site identifier
   */
  String getIdentifier();

  /**
   * Returns the description of this site.
   * 
   * @param l
   *          the description language
   * @return the site description
   */
  String getDescription(Language l);

  /**
   * Returns <code>true</code> if the site is enabled.
   * 
   * @return <code>true</code> if the site is enabled
   */
  boolean isEnabled();

  /**
   * Returns the site's logging facility.
   * 
   * @return the site logger
   */
  SiteLogger getLogger();

  /**
   * Adds <code>listener</code> to the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  void addSiteListener(SiteListener listener);

  /**
   * Removes <code>listener</code> from the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  void removeSiteListener(SiteListener listener);

  /**
   * Adds <code>listener</code> to the list of request listeners if it has not
   * already been registered.
   * 
   * @param listener
   *          the lister
   */
  void addRequestListener(RequestListener listener);

  /**
   * Removes the listener from the list of request listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeRequestListener(RequestListener listener);

  /**
   * Adds <code>handler</code> to the list of request handlers.
   * 
   * @param handler
   *          the request handler
   */
  void addRequestHandler(RequestHandler handler);

  /**
   * Removes the request handler from the list of request handlers.
   * 
   * @param handler
   *          the request handler to remove
   */
  void removeRequestHandler(RequestHandler handler);

  /**
   * Returns the site's security manager.
   * 
   * @return the security manager
   */
  SecurityManager getSecurityManager();

  /**
   * Returns the site's page manager. TODO: Rename
   * 
   * @return
   */
  PageManager getPages();

  /**
   * Returns the repository associated with this site. TODO: Rename
   * 
   * @return the site repository
   */
  RepositoryManager getRepository();

  /**
   * Returns the keyword manager for this site. TODO: Rename
   * 
   * @return the keyword manager
   */
  KeywordManager getKeywords();

  /**
   * Returns the site module with the given identifier or <code>null</code> if
   * no such module can be found.
   * 
   * @param id
   *          the module identifier
   * @return the module
   */
  Module getModule(String id);

  /**
   * Returns the site modules that are currently associated with this site. Note
   * that the modules may be active or inactive.
   * 
   * @return the site modules
   */
  Module[] getModules();

  /**
   * Returns this site's renderers which keeps track of the defined renderer
   * bundles.
   * 
   * @return the renderers
   */
  Renderer[] getRenderers();

  /**
   * Returns the renderer with the specified id or <code>null</code> if no such
   * renderer is defined.
   * 
   * @param rendererId
   *          the renderer identifier
   * 
   * @return the renderer
   */
  Renderer getRenderer(String rendererId);

  /**
   * Returns the renderer with the specified id that is suited for the given
   * rendering method or <code>null</code> if no such renderer is defined.
   * 
   * @param rendererId
   *          the renderer identifier
   * @param method
   *          the rendering method
   * 
   * @return the renderer
   */
  Renderer getRenderer(String rendererId, String method);

  /**
   * Returns the identifier of the default template for this site.
   * 
   * @return the site default template
   */
  String getDefaultTemplate();

  /**
   * Returns this site's layouts which keeps track of the defined layouts.
   * 
   * @return the layouts
   */
  PageLayout[] getLayouts();

  /**
   * Returns the layout with the given identifier or <code>null</code> if no
   * such layout is defined.
   * 
   * @param layoutId
   *          layout identifier
   * @return the layout
   */
  PageLayout getLayout(String layoutId);

  /**
   * Returns this site's request handlers which keeps track of the defined
   * request handler for servlets etc.
   * 
   * @return the request handlers
   */
  RequestHandler[] getRequestHandler();

  /**
   * Returns the image styles.
   * 
   * @return the image styles
   */
  ImageStyle[] getImageStyles();

  /**
   * Returns the image style with the given identifier or <code>null</code> if
   * no such style is found.
   * 
   * @return the image style
   */
  ImageStyle getImageStyle(String id);

  /**
   * Adds the listener to the list of user listeners.
   * 
   * @param listener
   *          the user listener to add
   */
  void addUserListener(UserListener listener);

  /**
   * Removes the listener from the list of user listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeUserListener(UserListener listener);

  /**
   * Returns the user with the given login name or <code>null</code> if no such
   * user exists.
   * 
   * @param login
   *          the user's login name
   * @return the user
   */
  WebloungeUser getUser(String login);

  /**
   * Returns the real path on the server for a given virtual path.
   * 
   * @param path
   *          the virtual (site-relative) path
   * @return the real (physical) path on the server
   */
  String getPhysicalPath(String path);

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
  String getVirtualPath(String path, boolean webapp);

  /**
   * Returns the servername of this page. This method will return the complete
   * hostname as found in the <code>&lt;name&gt;</code> section of
   * <code>site.xml</code>.
   * 
   * @return the site's server name
   */
  String getServername();

  /**
   * Returns the absolute link which can be used to reach this page. This method
   * will return the complete hostname as found in the <code>&lt;name&gt;</code>
   * section of <code>site.xml</code> concatenated with the weblounge
   * mountpoint.
   * 
   * @return the absolute link to this site
   */
  String getLink();

  /**
   * Returns the path to the database collection for <code>path</code>, which is
   * interpreted relative to the site's root collection.
   * <p>
   * For example, a service would request the collection path
   * <code>myservice</code> which returns the database collection
   * <code>/db/weblounge/sites/mysite/myservice</code>.
   * <p>
   * 
   * TODO: Remove
   * 
   * @param path
   *          the site relative path
   * @return the collection path
   */
  String getCollectionPath(String path);

  /**
   * Returns <code>true</code> <code>language</code> is supported by this site.
   * 
   * @param language
   *          the language
   * @return <code>true</code> if the language is supported
   */
  boolean supportsLanguage(Language language);

  /**
   * Returns the site's languages.
   * 
   * @return the site languages
   */
  Language[] getLanguages();

  /**
   * Returns the default language for this site.
   * 
   * @return the site default language
   */
  Language getDefaultLanguage();

  /**
   * Returns the language for the given identifier or <code>null</code> if that
   * language is unknown or not supported by this site.
   * 
   * @param languageId
   *          the language identifier
   * @return the site default language
   */
  Language getLanguage(String languageId);

  /**
   * Returns the server names that will lead to this site. A server name is the
   * first part of a url. For example, in <tt>http://www.o2it.ch/weblounge</tt>,
   * <tt>www.o2it.ch</code> is the server name.
   * 
   * @return the registered server names
   */
  String[] getServerNames();

  /**
   * Starts this site.
   */
  void start();

  /**
   * Stops this site.
   */
  void stop();

  /**
   * Returns the number of versions to keep of each page.
   * 
   * @return the history size
   */
  int getHistorySize();

  /**
   * Returns the registered JAAS authentication modules. Note that the list is
   * ordered according to the appearance of each authentication module in the
   * site configuration.
   * 
   * @return the authentication modules
   */
  AuthenticationModule[] getAuthenticationModules();

  /**
   * Returns the administrator user.
   * 
   * @return the site administrator user
   */
  SiteAdmin getAdministrator();

  /**
   * Returns a reference to the site's work directory. This is usually located
   * at <code>%WEBLOUNGE_HOME%/work/weblounge/sites/%sitename%</code>
   * 
   * TODO: Remove
   * 
   * @return the work directory
   */
  File getWorkDirectory();

  /**
   * Dispatches the given request and writes output to the response.
   * 
   * @param request
   *          the request to dispatch
   * @param response
   *          the response to deliver
   */
  void dispatch(WebloungeRequest request, WebloungeResponse response);

  /**
   * Returns the page identified by the <code>uri</code>.
   * 
   * @param uri
   *          the page uri
   * @param user
   *          the user requesting the page
   * @return the page or <code>null</code> if the page doesn't exist
   */
  Page getPage(PageURI uri, User user);

  /**
   * Returns the role with the given identifier, defined in the specified
   * context or <code>null</code> if no such role was found.
   * 
   * @param role
   *          the role identifier
   * @param context
   *          the role domain
   * @return the role
   */
  Role getRole(String role, String context);

  /**
   * Returns the group with the given identifier, defined in the specified
   * context or <code>null</code> if no such group was found.
   * 
   * @param group
   *          the group identifier
   * @param context
   *          the group domain
   * @return the role
   */
  Group getGroup(String group, String context);
  
}