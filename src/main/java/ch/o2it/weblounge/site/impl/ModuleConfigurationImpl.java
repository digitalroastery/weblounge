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
import ch.o2it.weblounge.common.impl.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.page.PageletRendererImpl;
import ch.o2it.weblounge.common.impl.util.config.OptionsSupport;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.ModuleConfiguration;
import ch.o2it.weblounge.common.site.PageletRenderer;
import ch.o2it.weblounge.common.site.Renderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 */
public final class ModuleConfigurationImpl extends OptionsSupport implements ModuleConfiguration {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ModuleConfigurationImpl.class.getName());

  /** Module identifier */
  String identifier = null;

  /** Module description */
  protected LocalizableContent<String> description = null;

  /** The configuration file */
  File file = null;

  /** True if the module is enabled */
  boolean isEnabled = true;

  /** The default language */
  Language defaultLanguage = null;

  /** The renderers */
  Map<String, Renderer> renderers = null;

  /** The action handlers */
  Map<String, Action> actions = null;

  /** The image styles */
  List<ImageStyle> imagestyles = null;

  /** The module load factor */
  int loadfactor = 1;

  /** The module class implementation to load */
  String moduleClass = null;

  /** The module class loader */
  ClassLoader classLoader = null;

  /** Site options */
  protected OptionsSupport options = null;

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
    imagestyles = new ArrayList<ImageStyle>();
    renderers = new HashMap<String, Renderer>();
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
      super.load(path, XPathHelper.select(config, "/module", path));
    } catch (ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      log_.error("Error when reading module configuration '{}': {}", new Object[] {file, e.getMessage(), e});
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
    return description.toString(l);
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
    return imagestyles;
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
    description = LanguageSupport.addDescriptions(config, "description", defaultLanguage, null, false);
    isEnabled = "true".equalsIgnoreCase(XPathHelper.valueOf(config, "enable", path));
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
      log_.debug("No performance settings found for module '{}'", identifier);
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
        log_.warn("Loadfactor {} is not a number! Adjusting to 1", factor);
        loadfactor = -1;
      }
    }
  }

  /**
   * Reads the template definitions.
   * 
   * @param config
   *          template configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readRenderers(XPath path, Node config)
      throws ConfigurationException {
    if (config == null) {
      log_.debug("No renderer definitions found");
      return;
    }
    log_.debug("Configuring renderers");
    try {
      NodeList templateNodes = XPathHelper.selectList(config, "renderer", path);
      for (int i = 0; i < templateNodes.getLength(); i++) {
        Node templateNode = templateNodes.item(i);
        PageletRenderer renderer = PageletRendererImpl.fromXml(templateNode, path);
        renderers.put(renderer.getIdentifier(), renderer);
      }
    } catch (Exception e) {
      log_.error("Configuration error when reading renderers: {}", e.getMessage(), e);
    }
    log_.debug("Renderers configured");
  }

  /**
   * Configures the image styles.
   * 
   * @param config
   *          the image styles node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readImagestyles(XPath path, Node config) {
    if (config == null) {
      log_.debug("No image styles found");
      return;
    }
    log_.debug("Configuring image styles");
    try {
      NodeList styleNodes = XPathHelper.selectList(config, "imagestyle", path);
      for (int i = 0; i < styleNodes.getLength(); i++) {
        Node styleNode = styleNodes.item(i);
        ImageStyle style = ImageStyleImpl.fromXml(styleNode, path);
        imagestyles.add(style);
      }
    } catch (Exception e) {
      log_.error("Configuration error when reading image styles: {}", e.getMessage(), e);
    }
    log_.debug("Image styles configured");
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
      log_.debug("No action definitions found for module '{}'", identifier);
      return;
    }
    NodeList actionNodes = XPathHelper.selectList(config, "action", path);
    for (int i = 0; i < actionNodes.getLength(); i++) {
      Node node = actionNodes.item(i);
      String id = null;
      try {
        id = XPathHelper.valueOf(node, "@id", path);
        log_.debug("Reading action bundle '{}'", id);
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
            log_.error("Error instantiating action of type '{}' in modue {}: {}", new Object[] {id, identifier, e.getMessage(), e});
            continue;
          } catch (IllegalAccessException e) {
            log_.error("Access violation when instantiating action of type '{}' in modue {}: {}", new Object[] {id, identifier, e.getMessage(), e});
            continue;
          } catch (NoClassDefFoundError e) {
            log_.error("Class required by action {} of module '{}' not found", actionConfig.getClassName(), identifier);
            continue;
          } catch (ClassNotFoundException e) {
            log_.error("Class {} for action '{}' of module '{}' not found!", new Object[] {actionConfig.getClassName(), id, identifier});
            continue;
          } catch (Throwable e) {
            if (e.getCause() != null)
              e = e.getCause();
            log_.error("Error creating action handler '{}' of module '{}': {}", new Object[] {id, identifier, e.getMessage(), e});
            continue;
          }
        }
        this.actions.put(id, bundleConfig);
      } catch (Exception e) {
        log_.error("Error when reading action bundle '{}' of module '{}': {}", new Object[] {id, identifier, e.getMessage(), e});
      }
    }
  }

  /**
   * Reads the module options.
   * 
   * @param config
   *          options configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readOptions(XPath path, Node config) {
    options = OptionsSupport.load(path, XPathHelper.select(config, "/site", path));
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return identifier.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>ModuleConfiguration
   * </code> and matches this
   * configuration in every aspect.
   * 
   * @return <code>true</code> if o is equal to this configuration
   */
  public boolean equals(Object o) {
    if (o instanceof ModuleConfiguration) {
      ModuleConfigurationImpl s = (ModuleConfigurationImpl) o;
      return (identifier.equals(s.identifier));
    }
    return false;
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