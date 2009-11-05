/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.classloader.SiteClassLoader;
import ch.o2it.weblounge.common.impl.util.classloader.WebloungeClassLoader;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationBase;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Job;
import ch.o2it.weblounge.common.site.ModuleConfiguration;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * <code>ModuleConfiguration</code> represents the contents of the
 * <code>&lt;module&gt;</code> node from a module configuration file
 * <code>module.xml</code>.
 * <p>
 * A module configuration looks like this:
 * <p>
 * 
 * <pre>
 * 	&lt;module id=&quot;edit&quot;&gt;
 * 		&lt;enable&gt;true&lt;/enable&gt;
 * 		&lt;description&gt;Main site&lt;/description&gt;
 * 		.
 * 		.
 * 		.
 * 	&lt;/module&gt;
 * </pre>
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class ModuleConfigurationImpl extends ConfigurationBase implements ModuleConfiguration {

  /** Module identifier */
  String identifier = null;

  /** The configuration file */
  File file = null;

  /** Module description */
  LocalizableContent<String> descriptions = null;

  /** True if the module is enabled */
  boolean isEnabled = true;

  /** The default language */
  Language defaultLanguage = null;

  /** The renderers */
  Map<String, Renderer> renderers = null;

  /** The action handlers */
  Map<String, Action> actions = null;

  /** Module jobs */
  Map<String, Job> jobs = null;

  /** The image styles */
  Map<String, ImageStyle> imagestyles = null;

  /** The module load factor */
  int loadfactor = 1;

  /** The module class implementation to load */
  String moduleClass = null;

  /** The module class loader */
  ClassLoader classLoader = null;

  // Logging

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ModuleConfigurationImpl.class.getName());

  /**
   * Creates a new module configuration.
   * 
   * @param file
   *          the configuration file
   * @param defaultLanguage
   *          the default Language for the module description
   */
  public ModuleConfigurationImpl(File file, Language defaultLanguage) {
    this.file = file;
    this.defaultLanguage = defaultLanguage;
    actions = new HashMap<String, Action>();
    imagestyles = new HashMap<String, ImageStyle>();
    renderers = new HashMap<String, Renderer>();
    jobs = new HashMap<String, Job>();
    classLoader = Thread.currentThread().getContextClassLoader();
  }

  /**
   * Returns the module's document base.
   * 
   * @return the module's document base
   */
  public String getPath() {
    return file.getParentFile().getPath();
  }

  /**
   * Reads the module configuration from the given xml configuration node.
   * 
   * @param config
   *          the configuration node
   * @throws ConfigurationException
   *           if there are errors in the configuration
   */
  public void configure(Document config) throws ConfigurationException {
    if (config == null)
      throw new IllegalArgumentException("Configuration node is null");
    try {
      XPath path = XMLUtilities.getXPath();
      readMainSettings(path, XPathHelper.select(config, "/module", path));
      readOptions(path, XPathHelper.select(config, "/module/options", path));
      readPerformanceSettings(path, XPathHelper.select(config, "/module/performance", path));
      readRenderers(path, XPathHelper.select(config, "/module/renderers", path));
      readActions(path, XPathHelper.select(config, "/module/actions", path));
      readImagestyles(path, XPathHelper.select(config, "/module/imagestyles", path));
      readJobs(path, XPathHelper.select(config, "/module/jobs", path));
      super.init(path, XPathHelper.select(config, "/module", path));
    } catch (ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      log_.error("Error when reading module configuration '" + file + "':" + e.getMessage(), e);
      throw new ConfigurationException("Error when reading module configuration!", e);
    }
  }

  /**
   * Returns the module identifier, e. g. <tt>forum</tt>.
   * 
   * @return the module identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the module description, e. g. <tt>Forum</tt>.
   * 
   * @param l
   *          the language used to return the description
   * @return the module description
   */
  public String getDescription(Language l) {
    return descriptions.toString(l);
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>ModuleConfiguration
	 * </code> and matches this configuration in every aspect.
   * 
   * @return <code>true</code> if o is equal to this configuration
   */
  public boolean equals(Object o) {
    if (o instanceof ModuleConfiguration) {
      ModuleConfigurationImpl s = (ModuleConfigurationImpl) o;
      return (identifier.equals(s.identifier) && descriptions.equals(s.descriptions) && isEnabled == s.isEnabled);
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the module is configured to be enabled.
   * 
   * @return <code>true</code> if the module is enabled
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Returns the module load factor. The factor denotes the load that is
   * expected to be put on the module.
   * 
   * @return the module load factor
   */
  public int getLoadFactor() {
    return loadfactor;
  }

  /**
   * Returns the renderers that are defined for the module.
   * 
   * @return the module renderer
   */
  public Collection<Renderer> getRenderers() {
    return renderers.values();
  }

  /**
   * Returns the actions containing all registered actions.
   * 
   * @return the actions
   */
  public Collection<Action> getActions() {
    return actions.values();
  }

  /**
   * Returns the image styles containing all registered styles.
   * 
   * @return the image styles
   */
  public Collection<ImageStyle> getImageStyles() {
    return imagestyles.values();
  }

  /**
   * Returns the module jobs.
   * 
   * @return the module jobs
   */
  public Collection<Job> getJobs() {
    return jobs.values();
  }

  /**
   * Reads the main module settings like identifier, name and description from
   * the module configuration.
   * 
   * @param config
   *          module configuration node
   */
  private void readMainSettings(XPath path, Node config)
      throws ConfigurationException {
    identifier = XPathHelper.valueOf(config, "@id", path);
    WebloungeClassLoader classLoader = WebloungeClassLoader.getInstance();
    classLoader.addExtendedClassPath(file.getParentFile().getAbsolutePath());
    moduleClass = XPathHelper.valueOf(config, "class", path);
    if (moduleClass == null)
      moduleClass = "ch.o2it.weblounge.core.module.ModuleImpl";
    descriptions = new LocalizableContent<String>();
    LanguageSupport.addDescriptions(path, config, descriptions, true);
    String enable = XPathHelper.valueOf(config, "enable", path);
    isEnabled = (enable == null || "true".equals(enable.toLowerCase()));
  }

  /**
   * Reads the performance settings from the module configuration.
   * 
   * @param config
   *          performance configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readPerformanceSettings(XPath path, Node config)
      throws ConfigurationException {
    loadfactor = 1;
    if (config == null) {
      log_.debug("No performance settings found for module '" + identifier + "'");
      return;
    }
    String factor = XPathHelper.valueOf(config, "loadfactor", path);
    if (factor != null) {
      try {
        loadfactor = Integer.parseInt(factor);
        if (loadfactor < 1) {
          String msg = "Error when reading action configuration: Loadfactor must be >= 1!";
          log_.error(msg);
        }
      } catch (NumberFormatException e) {
        log_.warn("Loadfactor " + factor + " is not a number! Adjusting to 1");
        loadfactor = -1;
      }
    }
  }

  /**
   * Reads the renderer definitions.
   * 
   * @param config
   *          renderer configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readRenderers(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      log_.debug("No renderer definitions found for module '" + identifier + "'");
      return;
    }
    NodeList bundleNodes = XPathHelper.selectList(config, "renderer", path);
    for (int i = 0; i < bundleNodes.getLength(); i++) {
      Node node = bundleNodes.item(i);
      String id = null;
      try {
        id = XPathHelper.valueOf(node, "@id", path);
        log_.debug("Reading renderer bundle '" + id + "'");
        RendererBundleConfiguration bundleConfig = new RendererBundleConfiguration(id, getFile());
        bundleConfig.read(path, node);
        LanguageSupport.addDescriptions(path, node, null, bundleConfig.getDescriptions());

        // Read jsp renderer definitions
        NodeList jspRenderers = XPathHelper.selectList(node, "jsp", path);
        for (int j = 0; j < jspRenderers.getLength(); j++) {
          Node jspNode = jspRenderers.item(j);
          RendererConfigurationImpl rendererConfig = new RendererConfigurationImpl(bundleConfig);
          rendererConfig.init(path, jspNode);
          bundleConfig.define(JSPRenderer.class, rendererConfig);
        }

        // Read xsl renderer definitions
        NodeList xslRenderers = XPathHelper.selectList(node, "xsl", path);
        for (int j = 0; j < xslRenderers.getLength(); j++) {
          Node xslNode = xslRenderers.item(j);
          RendererConfigurationImpl rendererConfig = new RendererConfigurationImpl(bundleConfig);
          rendererConfig.init(path, xslNode);
          bundleConfig.define(XSLRenderer.class, rendererConfig);
        }

        // Read custom renderer definitions
        NodeList customRenderers = XPathHelper.selectList(node, "custom", path);
        for (int j = 0; j < customRenderers.getLength(); j++) {
          Node customNode = customRenderers.item(j);
          RendererConfigurationImpl rendererConfig = new RendererConfigurationImpl(bundleConfig);
          rendererConfig.init(path, customNode);
          try {
            Class<?> clazz = classLoader.loadClass(rendererConfig.getClassName());
            bundleConfig.define(clazz, rendererConfig);
          } catch (ClassNotFoundException e) {
            log_.error("Unable to load custom renderer, since class " + rendererConfig.getClassName() + " was not found!", e);
          }
        }
        renderers.put(id, bundleConfig);
      } catch (Exception e) {
        log_.warn("Error when reading renderer bundle '" + id + "': " + e.getMessage());
      }
    }
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
    for (int j = 0; j < styleNodes.getLength(); j++) {
      Node styleNode = styleNodes.item(j);
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
        LanguageSupport.addDescriptions(path, styleNode, null, style);
        imagestyles.put(id, style);
      } catch (ConfigurationException e) {
        String msg = "Configuration error when reading imagestyle '" + id + "': ";
        log_.warn(msg + e.getCause());
      } catch (Exception e) {
        String msg = "Error reading imagestyle '" + id + "': ";
        log_.warn(msg, e);
      }
    }
    log_.debug("Imagestyles configured");
  }

  /**
   * Reads the action definitions.
   * 
   * @param config
   *          renderer configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readActions(XPath path, Node config)
      throws ConfigurationException, ConfigurationException {
    if (config == null) {
      log_.debug("No action definitions found for module '" + identifier + "'");
      return;
    }
    NodeList actionNodes = XPathHelper.selectList(config, "action", path);
    for (int i = 0; i < actionNodes.getLength(); i++) {
      Node node = actionNodes.item(i);
      String id = null;
      try {
        id = XPathHelper.valueOf(node, "@id", path);
        log_.debug("Reading action bundle '" + id + "'");
        ActionBundleConfiguration bundleConfig = new ActionBundleConfiguration(id, this);
        LanguageSupport.addDescriptions(path, node, site.getLanguages(), site.getDefaultLanguage(), bundleConfig.getDescription());
        bundleConfig.read(path, node);

        // Read handler definitions
        NodeList handlerNodes = XPathHelper.selectList(node, "handler", path);
        for (int j = 0; j < handlerNodes.getLength(); j++) {
          Node handlerNode = handlerNodes.item(j);
          ActionConfigurationImpl actionConfig = new ActionConfigurationImpl(bundleConfig);
          actionConfig.init(path, handlerNode);
          try {
            Class handlerClass = classLoader.loadClass(actionConfig.getClassName());
            handlerClass.newInstance();
            bundleConfig.define(handlerClass, actionConfig);
          } catch (InstantiationException e) {
            log_.error("Error instantiating action handler of type '" + id + "'");
            log_.error("InstatiationException when instantiating action '" + id + "' of module '" + identifier + "': " + e.getMessage());
            continue;
          } catch (IllegalAccessException e) {
            log_.error("Access violation when instantiating action '" + id + "' of module '" + identifier + "': " + e.getMessage());
            log_.error("Error when reading wizard '" + id + "' of module '" + identifier + "': " + e.getMessage());
            continue;
          } catch (NoClassDefFoundError e) {
            log_.error("Class '" + e.getMessage() + "' cannot be found but is required by " + actionConfig.getClassName() + "' in action '" + id + "' of module '" + identifier + "'!");
            continue;
          } catch (ClassNotFoundException e) {
            log_.error("Handler class '" + actionConfig.getClassName() + "' for action '" + id + "' of module '" + identifier + "' not found!");
            continue;
          } catch (Throwable e) {
            if (e.getCause() != null)
              e = e.getCause();
            log_.error("Error creating action handler '" + id + "' of module '" + identifier + "': " + e.getMessage());
            continue;
          }
        }
        this.actions.put(id, bundleConfig);
      } catch (Exception e) {
        log_.error("Error when reading action bundle '" + id + "' of module '" + identifier + "': " + e.getMessage());
        log_.debug("Error when reading action bundle '" + id + "'!", e);
      }
    }
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
    log_.debug("Configuring cron jobs");

    NodeList jobNodes = XPathHelper.selectList(config, "job", path);
    for (int i = 0; i < jobNodes.getLength(); i++) {
      Node jobNode = jobNodes.item(i);
      ModuleJob job = null;

      // See if job is enabled
      String enabled = XPathHelper.valueOf(jobNode, "@enabled", path);
      if (enabled != null && !"true".equals(enabled))
        continue;

      String className = XPathHelper.valueOf(jobNode, "class", path);
      try {
        job = (ModuleJob) classLoader.loadClass(className).newInstance();
        job.init(path, jobNode);
        jobs.put(job.getIdentifier(), job);
      } catch (ConfigurationException e) {
        log_.debug("Error configuring service!", e.getCause());
        log_.error("Error configuring cronjob '" + job.getName() + "' of module '" + identifier + "': " + e.getMessage());
      } catch (InstantiationException e) {
        log_.error("Error instantiating cronjob '" + className + "' of module '" + identifier + "': " + e.getMessage());
      } catch (IllegalAccessException e) {
        log_.error("Access error instantiating cronjob '" + className + "' of module '" + identifier + "': " + e.getMessage());
      } catch (ClassNotFoundException e) {
        log_.error("Class '" + className + "' for cronjob of module '" + identifier + "' not found: " + e.getMessage());
      } catch (NoClassDefFoundError e) {
        log_.error("Required class '" + className + "' for cronjob of module '" + identifier + "' not found: " + e.getMessage());
      } catch (Exception e) {
        log_.debug("Error configuring job!", e);
        log_.error("Error configuring job '" + ((job != null) ? job.getIdentifier() : "?") + "' of module '" + identifier + "': " + e.getMessage());
      }
    }
    log_.debug("Jobs configured");
  }

  /**
   * Returns the module identifier.
   * 
   * @return the module identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return identifier;
  }

}