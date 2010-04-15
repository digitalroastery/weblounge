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

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageLayout;
import ch.o2it.weblounge.common.content.PageTemplate;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.WebloungeUser;

import java.io.IOException;
import java.io.Serializable;

/**
 * The site interface defines the method that may be called on weblounge site
 * objects.
 */
public interface Site extends Customizable, RequestListener, Serializable {

  /** Site descriptor */
  static final String CONFIG_FILE = "site.xml";

  /** The modules folder */
  static final String MODULE_DIR = "module";

  /**
   * Sets the site identifier.
   * <p>
   * <b>Note:</b> the identifier may be used in file paths, database table names
   * and the like, so make sure it does not contain spaces or weird characters,
   * i. e. it matches this regular expression: <code>^[a-zA-Z0-9-_.]*$</code>.
   * 
   * @param identifier
   *          the site identifier
   */
  void setIdentifier(String identifier);

  /**
   * Returns the site identifier.
   * 
   * @return the site identifier
   */
  String getIdentifier();

  /**
   * Sets the site name.
   * 
   * @param name
   *          the name
   */
  void setName(String name);

  /**
   * Returns the site name.
   * 
   * @return the site name
   */
  String getName();

  /**
   * Set to <code>true</code> to automatically start this site,
   * <code>false</code> otherwise.
   * 
   * @param autostart
   *          <code>true</code> to automatically start the site
   */
  void setAutoStart(boolean autostart);

  /**
   * Returns <code>true</code> if the site is automatically started at system
   * startup.
   * 
   * @return <code>true</code> if the site is automatically started
   */
  boolean isStartedAutomatically();

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
   * Adds <code>module</code> to the set of modules.
   * 
   * @param module
   *          the module
   * @throws ModuleException
   *           if the module cannot be properly initialized inside the site
   */
  void addModule(Module module) throws ModuleException;

  /**
   * Removes the module identified by <code>module</code> from the list of
   * modules and returns it. If no such module was found, this method returns
   * <code>null</code>.
   * 
   * @param module
   *          the module identifier
   * @return the module
   * @throws ModuleException
   *           if module cleanup fails
   */
  Module removeModule(String module) throws ModuleException;

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
   * Adds <code>template</code> to the list of templates.
   * 
   * @param template
   *          the template to add
   */
  void addTemplate(PageTemplate template);

  /**
   * Removes <code>template</code> from the list of templates.
   * 
   * @param template
   *          the template to remove
   */
  void removeTemplate(PageTemplate template);

  /**
   * Returns this site's templates which keeps track of the defined renderer
   * bundles.
   * 
   * @return the templates
   */
  PageTemplate[] getTemplates();

  /**
   * Returns the template with the given identifier or <code>null</code> if no
   * such template is defined.
   * 
   * @param template
   * @return the template
   */
  PageTemplate getTemplate(String template);

  /**
   * Sets the default template.
   * 
   * @param template
   *          the template
   */
  void setDefaultTemplate(PageTemplate template);

  /**
   * Returns the default template for this site.
   * 
   * @return the default template
   */
  PageTemplate getDefaultTemplate();

  /**
   * Adds <code>layout</code> to the set of page layouts.
   * 
   * @param layout
   *          the new layout
   */
  void addLayout(PageLayout layout);

  /**
   * Removes the page layout with identifier <code>layout</code> from the set of
   * layout and returns it. If no such layout was in the set, then this method
   * returns <code>null</code>.
   * 
   * @param layout
   *          the layout identifier
   * @return the layout or <code>null</code>
   */
  PageLayout removeLayout(String layout);

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
   * Adds <code>module</code> to the ordered set of authentication modules.
   * 
   * @param login
   *          module the new authentication module
   */
  void addAuthenticationModule(AuthenticationModule module);

  /**
   * Removes the login module from the set of authentication modules.
   * 
   * @param module
   *          the authentication module
   */
  void removeAuthenticationModule(AuthenticationModule module);

  /**
   * Returns this site's authentication modules which define who can log into
   * this site.
   * 
   * @return the authentication modules
   */
  AuthenticationModule[] getAuthenticationModules();

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
   * Sets a default hostname for the site. This hostname is used when links are
   * being generated for the site.
   * 
   * @param hostname
   *          the default hostname
   */
  void setDefaultHostname(String hostname);

  /**
   * Adds <code>hostname</code> to the list of hostnames. Note that the hostname
   * that is added first will be considered the default hostname for this site.
   * 
   * @param hostname
   *          the hostname to add
   */
  void addHostName(String hostname);

  /**
   * Removes <code>hostname</code> from the list of hostnames. The method
   * returns <code>true</code> if the hostname was found and removed,
   * <code>false</code> otherwise.
   * 
   * @param hostname
   *          the hostname to remove
   * @return <code>true</code> if the hostname was removed
   */
  boolean removeHostname(String hostname);

  /**
   * Returns the default hostname used to reach this site. This method will
   * return the complete hostname as found in the <code>&lt;name&gt;</code>
   * section of <code>site.xml</code>.
   * 
   * @return the site's server name
   */
  String getHostName();

  /**
   * Returns the server names that will lead to this site. A server name is the
   * first part of a url. For example, in <tt>http://www.o2it.ch/weblounge</tt>,
   * <tt>www.o2it.ch</code> is the server name.
   * 
   * @return the registered server names
   */
  String[] getHostNames();

  /**
   * Returns the absolute link which can be used to reach this site. This method
   * will return the complete hostname as found in the <code>&lt;name&gt;</code>
   * section of <code>site.xml</code>
   * <p>
   * If the site is mounted to the server's root, then the output is equivalent
   * to {@link #getHostName()}. If no hostnames have been configured, this
   * method returns <code>/</code> to indicate the root url of the current
   * server.
   * 
   * @return the absolute link to this site
   */
  WebUrl getUrl();

  /**
   * Adds <code>language</code> to the site languages.
   * 
   * @param language
   *          the language to add
   */
  void addLanguage(Language language);

  /**
   * Removes <code>language</code> from the site languages.
   * 
   * @param language
   *          the language to remove
   */
  void removeLanguage(Language language);

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
   * Sets the default language.
   * 
   * @param language
   *          the default language
   */
  void setDefaultLanguage(Language language);

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
   * Starts this site.
   * 
   * @throws SiteException
   *           if the site cannot be started properly
   * @throws IllegalStateException
   *           if the site is already running
   */
  void start() throws SiteException, IllegalStateException;

  /**
   * Stops this site.
   * 
   * @throws IllegalStateException
   *           if the site is already stopped
   */
  void stop() throws IllegalStateException;

  /**
   * Returns <code>true</code> if the site has been started.
   * 
   * @return <code>true</code> if the site is running
   */
  boolean isRunning();

  /**
   * Sets the site administrator.
   * 
   * @param administrator
   *          the site administrator
   */
  void setAdministrator(WebloungeUser administrator);

  /**
   * Returns the administrator user.
   * 
   * @return the site administrator user
   */
  WebloungeUser getAdministrator();

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
   * Returns the role with the given identifier, defined in the specified
   * context or <code>null</code> if no such role was found.
   * 
   * TODO: Remove
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
   * TODO: Remove
   * 
   * @param group
   *          the group identifier
   * @param context
   *          the group domain
   * @return the role
   */
  Group getGroup(String group, String context);

  /**
   * Returns the page identified by the <code>uri</code>.
   * 
   * TODO: Remove
   * 
   * @param uri
   *          the page uri
   * @return the page or <code>null</code> if the page doesn't exist
   * @throws IOException
   *           if the page cannot be read from its source
   */
  Page getPage(PageURI uri) throws IOException;

  /**
   * Returns an <code>XML</code> representation of the site, which will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;site id="mysite"&gt;
   * TODO: Finish example
   * &lt;/site&gt;
   * </pre>
   * 
   * @return the <code>XML</code> representation of the site
   */
  String toXml();

}