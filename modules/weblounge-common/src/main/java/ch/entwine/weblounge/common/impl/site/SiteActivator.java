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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.util.classloader.BundleClassLoader;
import ch.entwine.weblounge.common.impl.util.classloader.ContextClassLoaderUtils;
import ch.entwine.weblounge.common.impl.util.xml.ValidationErrorHandler;
import ch.entwine.weblounge.common.security.SystemDirectory;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * The <code>SiteActivator</code> is used to load a site from the enclosing
 * bundle and is meant to be used with either the
 * <code>OSGi Declarative Services</code> or
 * <code>OSGi Blueprint Services</code> facility.
 * <p>
 * It will scan the bundle resources for a file called <code>site.xml</code>
 * located at <code>site</code> directory relative to the bundle root.
 */
public class SiteActivator {

  /** Site identifier OSGi service property */
  private static final String SITE_IDENTIFIER_SERVICE_PROPERTY = "site.identifier";

  /** The logging facility */
  static final Logger logger = LoggerFactory.getLogger(SiteActivator.class);

  /** Tests activation/deactivation option */
  public static final String OPT_TESTS = "tests";

  /** The site service registration */
  protected ServiceRegistration siteService = null;

  /** The site */
  protected Site site = null;

  /** The bundle context */
  protected BundleContext bundleContext = null;

  /** The component properties */
  protected Map<String, String> properties = new HashMap<String, String>();

  /**
   * Callback from the OSGi Blueprint Services to set the bundle context.
   * <p>
   * This method should be configured as the <code>bundleContext</code> property
   * in the <tt>Blueprint Services</tt> section of your bundle using
   * <code>blueprintBundleContext</code> as the value.
   * 
   * @param ctx
   *          the bundle context
   */
  protected void setBundleContext(final BundleContext ctx) {
    logger.debug("Bundle context reference received");
    this.bundleContext = ctx;
  }

  /**
   * Callback from the OSGi Declarative Services environment to activate a site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the OSGi component context
   * @throws Exception
   *           if the site activation fails
   */
  protected void activate(final ComponentContext context) throws Exception {
    bundleContext = context.getBundleContext();

    // Extract the component properties
    this.properties.clear();
    if (context.getProperties() != null) {
      Enumeration<?> ke = context.getProperties().keys();
      while (ke.hasMoreElements()) {
        Object key = ke.nextElement();
        properties.put(ke.toString(), context.getProperties().get(key).toString());
      }
    }

    // Call the one-in-all activator
    activate(bundleContext, properties);
  }

  /**
   * Callback from the OSGi Declarative Services environment to deactivate a
   * site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the OSGi component context
   * @throws Exception
   *           if the site activation fails
   */
  protected void deactivate(final ComponentContext context) throws Exception {
    if (bundleContext == null)
      throw new IllegalStateException("Bundle context has not been set");
    deactivate(bundleContext, properties);
  }

  /**
   * Callback from the OSGi Blueprint Services environment to activate a site.
   * <p>
   * This method should be configured in the <tt>Blueprint Services</tt> section
   * of your bundle.
   * 
   * @param service
   *          the service object
   * @param properties
   *          the component properties
   * @param throws Exception if component activation fails
   */
  protected void activate(Object service, Map<?, ?> properties)
      throws Exception {
    if (bundleContext == null)
      throw new IllegalStateException("Bundle context has not been set");

    // Extract the component properties
    this.properties.clear();
    if (properties != null) {
      for (Map.Entry<?, ?> entry : properties.entrySet()) {
        this.properties.put(entry.getKey().toString(), entry.getValue().toString());
      }
    }

    activate(bundleContext, this.properties);
  }

  /**
   * Callback from the OSGi Blueprint Services environment to deactivate a site.
   * <p>
   * This method should be configured in the <tt>Blueprint Services</tt> section
   * of your bundle.
   * 
   * @param service
   *          the service object
   * @param properties
   *          the component properties
   * @param throws Exception if component activation fails
   */
  protected void deactivate(Object service, Map<?, ?> properties)
      throws Exception {
    if (bundleContext == null)
      throw new IllegalStateException("Bundle context has not been set");
    deactivate(bundleContext, this.properties);
  }

  /**
   * Callback from the OSGi environment to activate a site.
   * <p>
   * Subclasses that are looking to modify the startup behavior of a site are
   * advised to overwrite {@link #beforeInactivation(Site, BundleContext, Map)}.
   * 
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if the site activation fails
   */
  @SuppressWarnings("unchecked")
  private void activate(final BundleContext bundleContext,
      final Map<String, String> properties) throws Exception {

    logger.debug("Scanning bundle '{}' for site.xml", bundleContext.getBundle().getSymbolicName());

    // Prepare schema validator
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/site.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);

    // Set up the document builder
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // Load the site
    Enumeration<URL> e = bundleContext.getBundle().findEntries("site", "site.xml", false);
    if (e != null && e.hasMoreElements()) {
      final String bundleName = bundleContext.getBundle().getSymbolicName();
      URL siteUrl = e.nextElement();

      logger.debug("Parsing site definition from bundle '{}'", bundleName);

      // Load and validate the site descriptor
      ValidationErrorHandler errorHandler = new ValidationErrorHandler(siteUrl);
      docBuilder.setErrorHandler(errorHandler);
      final Document siteXml = docBuilder.parse(siteUrl.openStream());
      if (errorHandler.hasErrors()) {
        logger.error("Errors found while validating site descriptor in bundle '{}'. Site is not loaded", bundleName);
        return;
      }

      logger.info("Loading site from bundle '{}'", bundleName);

          final BundleClassLoader bundleClassLoader = new BundleClassLoader(bundleContext.getBundle());
            ContextClassLoaderUtils.doWithClassLoader(bundleClassLoader, new Callable<Void>() {
              public Void call() throws Exception {
                site = SiteImpl.fromXml(siteXml.getFirstChild());

                // Make sure the system admin account is not shadowed
                if (site.getAdministrator() != null) {
                  ServiceReference userDirectoryRef = bundleContext.getServiceReference(SystemDirectory.class.getName());
                  if (userDirectoryRef != null) {
                    SystemDirectory systemDirectory = (SystemDirectory) bundleContext.getService(userDirectoryRef);
                    User siteAdmin = site.getAdministrator();
                    if (siteAdmin != null) {
                      logger.debug("Checking site '{}' admin user '{}' for shadowing of system account");
                      User shadowedUser = systemDirectory.loadUser(siteAdmin.getLogin(), site);
                      if (shadowedUser != null && SecurityUtils.userHasRole(shadowedUser, SystemRole.SYSTEMADMIN)) {
                        throw new IllegalStateException("Site '" + site.getIdentifier() + "' administrative account '" + siteAdmin.getLogin() + "' is shadowing the system account");
                      }
                    }
                  } else {
                    logger.warn("Directory service not found, site '{}' admin user cannot be checked for user shadowing", site.getIdentifier());
                  }
                } else {
                  logger.info("Site '{}' does not specify an administrative account", site.getIdentifier());
                }

                if (site instanceof SiteImpl) {
                  beforeActivation(site, bundleContext, properties);
                  ((SiteImpl) site).activate(bundleContext, properties);
                  afterActivation(site, bundleContext, properties);
                }

                // Register the site as a service
                logger.debug("Registering site '{}' in the service registry", site);
                Dictionary<String, String> serviceProperties = new Hashtable<String, String>();
                serviceProperties.put(SITE_IDENTIFIER_SERVICE_PROPERTY, site.getIdentifier());
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                  serviceProperties.put(entry.getKey(), entry.getValue());
                }
                siteService = bundleContext.registerService(Site.class.getName(), site, serviceProperties);

                logger.debug("Site '{}' loaded", site);

                return null;
              }
            });

    } else {
      logger.warn("Site activator was unable to locate site.xml");
    }
  }

  /**
   * Callback from the OSGi environment to deactivate a site.
   * <p>
   * Subclasses that are looking to modify the shutdown behavior of a site are
   * advised to overwrite {@link #beforeInactivation(Site, BundleContext, Map)}.
   * 
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if the site inactivation fails
   */
  private void deactivate(BundleContext bundleContext,
      Map<String, String> properties) throws Exception {
    if (site != null && site instanceof SiteImpl) {
      try {
        beforeInactivation(site, bundleContext, properties);
      } catch (Throwable t) {
        logger.error("Error during site activator cleanup: {}", t.getMessage(), t);
      }
      ((SiteImpl) site).deactivate(bundleContext, properties);
      try {
        afterInactivation(site, bundleContext, properties);
      } catch (Throwable t) {
        logger.error("Error during site activator cleanup: {}", t.getMessage(), t);
      }
    }

    if (siteService != null) {
      try {
        siteService.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering site failed: {}", t.getMessage());
      }
    }
  }

  /**
   * This method is called right before the site is initialized and registered
   * in the OSGi service registry.
   * <p>
   * Subclasses that need to do additional initialization work should override
   * this method.
   * 
   * @param site
   *          the site
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if initialization fails
   */
  protected void beforeActivation(Site site, BundleContext context,
      Map<String, String> properties) throws Exception {
  }

  /**
   * This method is called after the site has been successfully initialized and
   * before it is registered in the OSGi service registry.
   * <p>
   * Subclasses that need to do additional initialization work should override
   * this method.
   * 
   * @param site
   *          the site
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if initialization fails
   */
  protected void afterActivation(Site site, BundleContext context,
      Map<String, String> properties) throws Exception {
  }

  /**
   * This method is called right before the site will be deactivated and pulled
   * from the OSGi the service registry.
   * <p>
   * Subclasses that need to do cleanup work should override this method.
   * 
   * @param site
   *          the site
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if cleanup fails
   */
  protected void beforeInactivation(Site site, BundleContext context,
      Map<String, String> properties) {
  }

  /**
   * This method is called right after the site has been deactivated and pulled
   * from the OSGi the service registry.
   * <p>
   * Subclasses that need to do cleanup work should override this method.
   * 
   * @param site
   *          the site
   * @param context
   *          the OSGi bundle context
   * @param properties
   *          the component properties
   * @throws Exception
   *           if cleanup fails
   */
  protected void afterInactivation(Site site, BundleContext context,
      Map<String, String> properties) {
  }

}
