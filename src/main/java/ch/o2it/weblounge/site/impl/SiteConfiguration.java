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
import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.security.jaas.AdminLoginModule;
import ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl;
import ch.o2it.weblounge.common.impl.user.SiteAdminImpl;
import ch.o2it.weblounge.common.impl.user.WebloungeAdminImpl;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.config.Options;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.dispatcher.RequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * <code>SiteConfiguration</code> represents the contents of the
 * <code>&lt;site&gt;</code> node from a site configuration file
 * <code>site.xml</code>.
 * <p>
 * A site configuration looks like this:
 * <p>
 * 
 * <pre>
 * 	&lt;site id=&quot;www&quot;&gt;
 * 		&lt;enable&gt;true&lt;/enable&gt;
 * 		&lt;description&gt;Main site&lt;/description&gt;
 * 		.
 * 		.
 * 		.
 * 	&lt;/site&gt;
 * </pre>
 */
public class SiteConfiguration implements Customizable {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(SiteConfiguration.class);

  /** The default load factor */
  public static final int DEFAULT_LOADFACTOR = 1;

  /** The default history size */
  public static final int DEFAULT_HISTORYSIZE = 1;

  /** Site identifier */
  protected String identifier = null;

  /** Site description */
  protected LocalizableContent<String> description = null;

  /** True if the site is enabled */
  protected boolean isEnabled = false;

  /** True if this is the system default site */
  protected boolean isDefault = false;

  /** Site implementation class */
  protected String siteClass = null;

  /** Server names that match to this site */
  protected List<URL> urls = null;

  /** JAAS authentication modules */
  protected List<AuthenticationModule> authenticationModules = null;

  /** The default language */
  protected Language defaultLanguage = null;

  /** Site languages */
  protected List<Language> languages = null;

  /** Site templates */
  protected List<PageTemplate> templates = null;

  /** Image styles */
  protected List<ImageStyle> imagestyles = null;

  /** Site cron jobs */
  protected List<SiteJob> jobs = null;

  /** Site options */
  protected Options options = null;

  /** Site load factor */
  protected int loadfactor = DEFAULT_LOADFACTOR;

  /** Number of versions to keep */
  protected int historysize = DEFAULT_HISTORYSIZE;

  /** the site administrator */
  protected SiteAdminImpl admin = null;

  /**
   * Creates a configuration for the site that is located at
   * <code>siteRoot</code>.
   * 
   * @param siteRoot
   *          url pointing to the site root
   */
  public SiteConfiguration(URL siteRoot) {
    authenticationModules = new ArrayList<AuthenticationModule>();
    imagestyles = new ArrayList<ImageStyle>();
    templates = new ArrayList<PageTemplate>();
    urls = new ArrayList<URL>();
    jobs = new ArrayList<SiteJob>();
  }

  /**
   * Reads the site configuration.
   * 
   * @param config
   *          the configuration
   * @throws ConfigurationException
   *           if there are errors in the configuration
   */
  public void configure(Document config) throws ConfigurationException {
    if (config == null)
      throw new IllegalArgumentException("Site configuration cannot be null");
    try {
      XPath path = XPathFactory.newInstance().newXPath();
      readMainSettings(path, XPathHelper.select(config, "/site", path));
      readAdmin(path, XPathHelper.select(config, "/site/admin", path));
      readUrls(path, XPathHelper.select(config, "/site/urls", path));
      readAuthenticationModules(path, XPathHelper.select(config, "/site/authentication", path));
      readLanguages(path, XPathHelper.select(config, "/site/languages", path));
      readPerformanceSettings(path, XPathHelper.select(config, "/site/performance", path));
      readLayouts(path, XPathHelper.select(config, "/site/layouts", path));
      readRenderers(path, XPathHelper.select(config, "/site/templates", path));
      readImagestyles(path, XPathHelper.select(config, "/site/imagestyles", path));
      readJobs(path, XPathHelper.select(config, "/site/jobs", path));
    } catch (Throwable t) {
      throw new ConfigurationException("Error when reading site configuration", t);
    }
  }

  /**
   * Returns the site identifier, e. g. <tt>o2it.ch</tt>.
   * 
   * @return the site identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the site description, e. g. <tt>World Championships 2004</tt>.
   * 
   * @param l
   *          the language used to return the description
   * @return the site description
   */
  public String getDescription(Language l) {
    return description.toString(l);
  }

  /**
   * Reads the main site settings like identifier, name and description from the
   * site configuration.
   * 
   * @param config
   *          site configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readMainSettings(XPath path, Node config)
      throws ConfigurationException {
    identifier = XPathHelper.valueOf(config, "@id", path);
    isDefault = "true".equals(XPathHelper.valueOf(config, "@default", path));
    siteClass = XPathHelper.valueOf(config, "class", path);
    if (siteClass == null)
      siteClass = SiteImpl.class.getCanonicalName();
    description = LanguageSupport.addDescriptions(config, "description", defaultLanguage, null, false);
    isEnabled = "true".equalsIgnoreCase(XPathHelper.valueOf(config, "enable", false, path));
    options = Options.load(path, XPathHelper.select(config, "/site", path));
  }

  /**
   * Reads the site's contact information.
   * 
   * @param config
   *          contact configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readAdmin(XPath path, Node config) {
    if (config != null) {
      String login = XPathHelper.valueOf(config, "login", path);     
      if (login.equals(WebloungeAdminImpl.getInstance().getLogin())) {
        throw new ConfigurationException("Site administrator login '" + login + "' is not allowed. Login is taken by system administrator");
      }
      String password = XPathHelper.valueOf(config, "password", path);
      String digest = XPathHelper.valueOf(config, "password/@type", path);
      String email = XPathHelper.valueOf(config, "email", path);
      String firstname = XPathHelper.valueOf(config, "firstname", path);
      String lastname = XPathHelper.valueOf(config, "lastname", path);
      DigestType digestType = DigestType.plain;
      if (digest != null)
        digestType = DigestType.valueOf(digest);
      admin = new SiteAdminImpl(login);
      admin.setPassword(password.getBytes(), digestType);
      admin.setEmail(email);
      admin.setFirstName(firstname);
      admin.setLastName(lastname);
    } else {
      throw new ConfigurationException("Site administrator definition missing!");
    }
  }

  /**
   * Reads the urls that lead to this site from the configuration.
   * 
   * @param config
   *          urls configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readUrls(XPath path, Node config) {
    NodeList urlNodes = XPathHelper.selectList(config, "url", path);
    try {
      for (int i = 0; i < urlNodes.getLength(); i++) {
        Node node = urlNodes.item(i);
        String url = node.getFirstChild().getNodeValue();
        urls.add(new URL(url));
        log_.debug("Found site url {}", url);
      }
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Site url " + e.getMessage() + " is malformed");
    }
  }

  /**
   * Reads the JAAS authentication module definitions.
   * 
   * @param config
   *          authentication configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readAuthenticationModules(XPath path, Node config) {
    if (config == null) {
      log_.debug("No authentication modules found");
      return;
    }
    NodeList moduleNodes = XPathHelper.selectList(config, "module", path);
    for (int i = 0; i < moduleNodes.getLength(); i++) {
      Node node = moduleNodes.item(i);
      try {
        AuthenticationModule module = new AuthenticationModuleImpl(path, node);
        authenticationModules.add(module);
        log_.debug("Login module {} registered", module.getClass());
      } catch (Exception e) {
        String msg = "Error reading authentication module: " + e.getMessage();
        log_.warn(msg);
      }
    }
    // By default, add authentication for superuser login
    authenticationModules.add(new AuthenticationModuleImpl(AdminLoginModule.class.getName(), "sufficient"));
  }

  /**
   * Reads the site languages from the site configuration and checks whether the
   * languages have been defined in the system settings.
   * 
   * @param config
   *          languages configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readLanguages(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      throw new ConfigurationException("A site must at least have one language!");
    }

    // Site languages
    String allLanguages = XPathHelper.valueOf(config, "all", path);
    String[] languageIds = ConfigurationUtils.getMultiOptionValues(allLanguages);
    for (String lId : languageIds) {
      Language l = LanguageSupport.getLanguage(lId);
      if (l != null)
        languages.add(l);
      else
        throw new ConfigurationException(lId + " is not a valid language identifier");
    }

    // Default language
    String defaultLanguageId = XPathHelper.valueOf(config, "default", path);
    if (defaultLanguageId == null)
      throw new ConfigurationException("No default language has been specified");

    Language l = LanguageSupport.getLanguage(defaultLanguageId);
    if (l != null) {
      defaultLanguage = l;
    } else {
      throw new ConfigurationException("The default language " + defaultLanguage + " is not a system language");
    }
  }

  /**
   * Reads the performance settings from the site configuration.
   * 
   * @param config
   *          performance configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readPerformanceSettings(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      log_.debug("No performance settings found");
      return;
    }

    // Load factor
    String factor = XPathHelper.valueOf(config, "loadfactor", path);
    try {
      loadfactor = Integer.parseInt(factor);
      if (loadfactor < 1) {
        String msg = "Error in site configuration: Loadfactor must be >= 1!";
        log_.error(msg);
        throw new ConfigurationException(msg);
      }
    } catch (NumberFormatException e) {
      String msg = "Error in site configuration: Loadfactor must be a number >= 1!";
      log_.error(msg);
      throw new ConfigurationException(msg);
    }

    // History size
    String size = XPathHelper.valueOf(config, "history", path);
    if (size != null) {
      try {
        historysize = Integer.parseInt(size);
        if (historysize < 0) {
          String msg = "Error in site configuration: History size must be >= 0!";
          log_.error(msg);
          throw new ConfigurationException(msg);
        }
      } catch (NumberFormatException e) {
        String msg = "Error in site configuration: Historysize must be a number >= 0!";
        log_.error(msg);
        throw new ConfigurationException(msg);
      }
    } else {
      historysize = DEFAULT_HISTORYSIZE;
    }
  }

  /**
   * Reads the urls that lead to this site from the configuration.
   * 
   * @param config
   *          urls configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readLayouts(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      log_.debug("No layout definitions found");
      return;
    }
  }

  /**
   * Reads the renderer definitions.
   * 
   * @param config
   *          renderers configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readRenderers(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      log_.debug("No renderer definitions found");
      return;
    }
    RendererBundle defaultRenderer = null;
    NodeList rendererNodes = XPathHelper.selectList(config, "renderer", path);
    for (int i = 0; i < rendererNodes.getLength(); i++) {
      Node node = rendererNodes.item(i);
      String id = null;
      RendererBundle bundle = null;
      try {
        id = XPathHelper.valueOf(node, "@id", path);
        log_.debug("Reading renderer bundle '{}'", id);
        RendererBundleConfiguration bundleConfig = new RendererBundleConfiguration(id, getFile());
        bundleConfig.read(path, node);
        bundle = new RendererBundle(id);
        bundle.init(bundleConfig);
        LanguageSupport.addDescriptions(path, node, languages.getDefaultLanguage(), bundle);
        if (!bundle.supportsLanguage(languages.getDefaultLanguage()))
          throw new ConfigurationException("Default language description is missing for renderer bundle '" + id + "'");

        // Handle default renderer
        if (defaultRenderer == null || bundleConfig.isDefault()) {
          defaultRenderer = bundle;
        }

        // Read jsp renderer definitions
        NodeList jspRenderers = XPathHelper.selectList(node, "jsp", path);
        for (int j = 0; j < jspRenderers.getLength(); j++) {
          Node jspNode = jspRenderers.item(j);
          TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
          rendererConfig.load(path, jspNode);
          bundle.define(JSPRenderer.class, rendererConfig);
        }

        // Read xsl renderer definitions
        NodeList xslRenderers = XPathHelper.selectList(node, "xsl", path);
        for (int j = 0; j < xslRenderers.getLength(); j++) {
          Node xslNode = xslRenderers.item(j);
          TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
          rendererConfig.load(path, xslNode);
          bundle.define(XSLRenderer.class, rendererConfig);
        }

        // Read custom renderer definitions
        NodeList customRenderers = XPathHelper.selectList(node, "custom", path);
        for (int j = 0; j < customRenderers.getLength(); j++) {
          Node customNode = customRenderers.item(j);
          TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
          rendererConfig.load(path, customNode);
          try {
            Class clazz = classLoader.loadClass(rendererConfig.getClassName());
            bundle.define(clazz, rendererConfig);
          } catch (ClassNotFoundException e) {
            log_.error("Unable to load custom renderer, since class {} was not found", rendererConfig.getClassName(), e);
          }
        }
        this.templates.put(id, bundle);
      } catch (ConfigurationException e) {
        log_.warn("Error when reading renderer bundle '{}'", id, e);
      }
    }

    // Check if there is at least one renderer defined
    if (defaultRenderer == null) {
      String msg = "Site '" + identifier + "' does not define a default renderer!";
      log_.warn(msg);
      throw new ConfigurationException(msg);
    }
    this.templates.setDefault(defaultRenderer);
    log_.info("Renderer '{}' is the default renderer", defaultRenderer);
  }

  /**
   * Configures the job settings.
   * 
   * @param config
   *          the jobs node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readJobs(XPath path, Node config) {
    if (config == null) {
      log_.debug("No job definitions found");
      return;
    }
    log_.debug("Configuring jobs");
    NodeList jobNodes = XPathHelper.selectList(config, "job", path);
    for (int i = 0; i < jobNodes.getLength(); i++) {
      Node jobNode = jobNodes.item(i);
      SiteJob job = null;

      // See if job is enabled
      String enabled = XPathHelper.valueOf(jobNode, "@enabled", path);
      if (enabled != null && !"true".equals(enabled))
        continue;

      String id = XPathHelper.valueOf(jobNode, "@id", path);
      String className = XPathHelper.valueOf(jobNode, "class", path);
      try {
        Class<?> jobClass = Class.forName(className);
        job = (SiteJob) jobClass.newInstance();
        job.load(path, jobNode);
        jobs.add(job);
      } catch (ConfigurationException e) {
        log_.debug("Error configuring service!", e.getCause());
        log_.error("Error configuring cronjob '{}' of site '{}': {}", new Object[] {
            job.getName(),
            identifier,
            e.getMessage(),
            e });
      } catch (InstantiationException e) {
        log_.error("Error instantiating cronjob '{}' of site '{}': {}", new Object[] {
            className,
            identifier,
            e.getMessage(),
            e });
      } catch (IllegalAccessException e) {
        log_.error("Access error instantiating cronjob '{}' of site '{}': {}", new Object[] {
            className,
            identifier,
            e.getMessage(),
            e });
      } catch (ClassNotFoundException e) {
        log_.error("Class '{}' for cronjob of site '{}' not found: {}", new Object[] {
            className,
            identifier,
            e.getMessage(),
            e });
      } catch (NoClassDefFoundError e) {
        log_.error("Required class '{}' for cronjob of site '{}' not found: {}", new Object[] {
            className,
            identifier,
            e.getMessage(),
            e });
      } catch (Exception e) {
        log_.error("Error configuring job '{}' of site '{}': {}", new Object[] {
            id,
            identifier,
            e.getMessage() });
      }
    }
    log_.debug("Jobs configured");
  }

  /**
   * Configures the image styles.
   * 
   * @param config
   *          the services node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readImagestyles(XPath path, Node config) {
    if (config == null) {
      log_.debug("No image styles found");
      return;
    }
    log_.debug("Configuring imagestyles");
    NodeList styleNodes = XPathHelper.selectList(config, "imagestyle", path);
    for (int i = 0; i < styleNodes.getLength(); i++) {
      Node styleNode = styleNodes.item(i);
      String id = XPathHelper.valueOf(styleNode, "@id", path);
      String composeableAttribute = XPathHelper.valueOf(styleNode, "@composeable", path);
      boolean composeable = composeableAttribute == null || "true".equalsIgnoreCase(composeableAttribute);
      try {
        String mode = XPathHelper.valueOf(styleNode, "scalingmode", path);
        String width = XPathHelper.valueOf(styleNode, "width", path);
        String height = XPathHelper.valueOf(styleNode, "height", path);
        int m = ImageStyle.SCALE_NONE;
        int h = -1;
        int w = -1;
        if (mode != null && !mode.equals("none")) {
          w = Integer.parseInt(width);
          h = Integer.parseInt(height);
          if ((mode != null) && mode.equals("fit")) {
            m = ImageStyle.SCALE_TO_FIT;
          } else if ((mode != null) && mode.equals("fill")) {
            m = ImageStyle.SCALE_TO_FILL;
          } else {
            String msg = "Found unknown scalingmode for imagestyle '" + id + "': " + mode;
            log_.warn(msg);
            throw new ConfigurationException(msg);
          }
        }

        ImageStyleImpl style = new ImageStyleImpl(id, w, h, composeable);
        style.setScalingMode(m);
        LanguageSupport.addDescriptions(path, styleNode, languages.getDefaultLanguage(), style);
        imagestyles.addStyle(style);
      } catch (ConfigurationException e) {
        log_.error("Configuration error when reading imagestyle '{}': {}", new Object[] {
            id,
            e.getCause().getMessage(),
            e.getCause() });
      } catch (Exception e) {
        log_.error("Configuration error when reading imagestyle '{}': {}", new Object[] {
            id,
            e.getMessage(),
            e });
      }
    }
    log_.debug("Imagestyles configured");
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o instanceof SiteConfiguration) {
      SiteConfiguration s = (SiteConfiguration) o;
      return (identifier.equals(s.identifier) && description.equals(s.description) && isEnabled == s.isEnabled);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return identifier.hashCode();
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
   * @see ch.o2it.weblounge.common.Customizable#options()
   */
  public Map<String, List<String>> options() {
    return options.options();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    // TODO Auto-generated method stub
    return null;
  }

}