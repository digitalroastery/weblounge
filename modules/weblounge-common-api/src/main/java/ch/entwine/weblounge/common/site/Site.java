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

package ch.entwine.weblounge.common.site;

import ch.entwine.weblounge.common.Customizable;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.PageLayout;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.RequestListener;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.UserListener;
import ch.entwine.weblounge.common.security.WebloungeUser;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * The site interface defines the method that may be called on weblounge site
 * objects.
 */
public interface Site extends Customizable, RequestListener, Serializable {

  /** Site descriptor */
  String CONFIG_FILE = "site.xml";

  /** The modules folder */
  String MODULE_DIR = "module";

  /** Identifier of the special weblounge site module */
  String WEBLOUNGE_MODULE = "weblounge";

  /** Default value for a site's path inside a bundle */
  String BUNDLE_PATH = "/site";

  /**
   * Initializes the site with the system environment.
   *
   * @param environment
   *          the system environment
   * @throws Exception
   *           if the site initialization fails
   */
  void initialize(Environment environment) throws Exception;

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
   * Returns the <code>i18n</code> dictionary.
   *
   * @return the dictionary
   */
  I18nDictionary getI18n();

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
   * Adds <code>url</code> to the list of site urls. Note that the url that is
   * added first will be considered the default hostname for this site unless
   * one of them is explicitly marked as such.
   *
   * @param url
   *          the url to add
   */
  void addHostname(SiteURL url);

  /**
   * Returns the primary url used to reach this site. This method will return
   * the default url as found in the <code>&lt;url&gt;</code> section of
   * <code>site.xml</code>.
   *
   * @return the site's primary url
   */
  SiteURL getHostname();

  /**
   * Returns the primary url used to reach this site in the given environment.
   * This method will return the default url as found in the
   * <code>&lt;url&gt;</code> section of <code>site.xml</code>.
   *
   * @return the site's primary url for the given environment
   */
  SiteURL getHostname(Environment environment);

  /**
   * Returns the urls that will lead to this site.
   *
   * @return the registered site urls
   */
  SiteURL[] getHostnames();

  /**
   * Removes <code>hostname</code> from the list of hostnames. The method
   * returns <code>true</code> if the hostname was found and removed,
   * <code>false</code> otherwise.
   *
   * @param url
   *          the hostname to remove
   * @return <code>true</code> if the hostname was removed
   */
  boolean removeHostname(SiteURL url);

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
   * Returns the URL to the security configuration or <code>null</code> if this
   * site doesn't define its own security rules.
   *
   * @return the security
   */
  URL getSecurity();

  /**
   * Sets the site's security configuration.
   *
   * @param url
   *          URL pointing to the security configuration
   */
  void setSecurity(URL url);

  /**
   * Returns this type's digest policy, which needs to match with what is
   * configured in Spring Security.
   *
   * @return the digest type
   */
  DigestType getDigestType();

  /**
   * Sets the digest type.
   *
   * @param digest
   *          the digest
   */
  void setDigestType(DigestType digest);

  /**
   * Returns the error page which should be displayed in case no other resource
   * nor an action with the given path could be found.
   * 
   * @param path
   *          the requested path
   * @return the uri of the error page or {@code null} if there is no error
   *         page
   */
  ResourceURI getErrorPage(String path);

  /**
   * Starts this site.
   * <p>
   * Starting the site is only allowed if there is a content repository
   * connected.
   *
   * @throws SiteException
   *           if the site cannot be started properly
   * @throws IllegalStateException
   *           if the site is already running or if no content repository is
   *           connected
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
   * @return <code>true</code> if the site has been started
   */
  boolean isStarted();

  /**
   * Returns the site's content repository or <code>null</code> if no repository
   * has connected to the site.
   *
   * @return the content repository
   */
  ContentRepository getContentRepository();

  /**
   * Suggests a maximum of <code>count</code> entries using <code>seed</code>
   * from the specified dictionary.
   *
   * @param dictionary
   *          the dictionary
   * @param seed
   *          the seed
   * @param count
   *          the maximum number of suggestions
   * @return the suggestions
   * @throws ContentRepositoryException
   *           if creating the suggestions fails
   */
  List<String> suggest(String dictionary, String seed, int count)
      throws ContentRepositoryException;

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

  /**
   * Sets the site's content repository.
   * <p>
   * This method will be called with <code>null</code> as the content repository
   * if the repository was taken down, and the site implementation is required
   * to take appropriate measures, such as switching into <tt>offline</tt> mode.
   *
   * @param repository
   *          the content repository
   */
  void setContentRepository(ContentRepository repository);

  /**
   * Adds a local role definition which will be used to map weblounge roles to
   * local roles. The mapping will be used if a system components needs to add a
   * default set of permissions.
   *
   * @param sytemRole
   *          the system role name, e. g. <code>editor</code>
   * @param localRole
   *          the local equivalent
   */
  void addLocalRole(String sytemRole, String localRole);

  /**
   * Returns the local role name for each system role as defined by
   * {@link ch.entwine.weblounge.common.security.Security}.
   * <p>
   * This method is used to translate roles that are referred to by weblounge
   * (e. g. <code>editor</code>) to each individual site, since these roles will
   * have different names depending on the site's directory.
   *
   * @param role
   *          the system role name
   * @return the local role
   */
  String getLocalRole(String role);

}
