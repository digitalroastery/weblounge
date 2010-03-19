/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.page.LinkImpl;
import ch.o2it.weblounge.common.impl.page.ScriptImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.HTMLHeadElement;
import ch.o2it.weblounge.common.page.HTMLInclude;
import ch.o2it.weblounge.common.page.Link;
import ch.o2it.weblounge.common.page.Script;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionConfiguration;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Default implementation of an action configuration.
 */
public class ActionConfigurationImpl implements ActionConfiguration {

  /** The identifier */
  private String identifier = null;

  /** The action implementation */
  private Class<? extends Action> actionClass = null;

  /** The action mountpoint */
  private String mountpoint = null;

  /** The target url */
  private String pageURI = null;

  /** The target template */
  private String template = null;

  /** The recheck time */
  private long recheckTime = Action.DEFAULT_RECHECK_TIME;

  /** The valid time */
  private long validTime = Action.DEFAULT_VALID_TIME;

  /** The list of includes */
  private Set<HTMLInclude> includes = new HashSet<HTMLInclude>();;

  /** The list of flavors */
  private Set<RequestFlavor> flavors = new HashSet<RequestFlavor>();

  /** The action name */
  private LocalizableContent<String> name = new LocalizableContent<String>();

  /** Options support */
  private OptionsHelper options = new OptionsHelper();

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getActionClass()
   */
  public Class<? extends Action> getActionClass() {
    return actionClass;
  }

  /**
   * Sets the action implementation.
   * 
   * @param actionClass
   *          the action class
   */
  public void setActionClass(Class<? extends Action> actionClass) {
    this.actionClass = actionClass;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the action identifier.
   * 
   * @param identifier
   *          the identifier
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getIncludes()
   */
  public Set<HTMLInclude> getIncludes() {
    return includes;
  }

  /**
   * Adds an include.
   * 
   * @param include
   *          the include
   */
  public void addInclude(HTMLInclude include) {
    includes.add(include);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getMountpoint()
   */
  public String getMountpoint() {
    return mountpoint;
  }

  /**
   * Sets the mountpoint.
   * 
   * @param mountpoint
   *          the mountpoint
   */
  public void setMountpoint(String mountpoint) {
    this.mountpoint = UrlSupport.trim(mountpoint);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getRecheckTime()
   */
  public long getRecheckTime() {
    return recheckTime;
  }

  /**
   * Sets the recheck time.
   * 
   * @param recheckTime
   *          the recheck time
   */
  public void setRecheckTime(long recheckTime) {
    this.recheckTime = recheckTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getPageURI()
   */
  public String getPageURI() {
    return pageURI;
  }

  /**
   * Sets the target url.
   * 
   * @param uri
   *          the target url
   */
  public void setPageURI(String uri) {
    this.pageURI = UrlSupport.trim(uri);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getTemplate()
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the target template.
   * 
   * @param template
   *          the template
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getValidTime()
   */
  public long getValidTime() {
    return validTime;
  }

  /**
   * Sets the valid time.
   * 
   * @param validTime
   *          the valid time
   */
  public void setValidTime(long validTime) {
    this.validTime = validTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getFlavors()
   */
  public Set<RequestFlavor> getFlavors() {
    return flavors;
  }

  /**
   * Adds a flavor.
   * 
   * @param flavor
   *          the flavor
   */
  public void addFlavor(RequestFlavor flavor) {
    flavors.add(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#getName()
   */
  public Localizable getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.ActionConfiguration#setName(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setName(String name, Language language) {
    this.name.put(name, language);
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
   * @see ch.o2it.weblounge.common.Customizable#getOptions()
   */
  public Map<String, List<String>> getOptions() {
    return options.getOptions();
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
   * @see ch.o2it.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
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
   * Initializes this action configuration from an XML node that was generated
   * using {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the action configuration node
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static ActionConfigurationImpl fromXml(Node config)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Initializes this action configuration from an XML node that was generated
   * using {@link #toXml()}.
   * 
   * @param config
   *          the action configuration node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static ActionConfigurationImpl fromXml(Node config,
      XPath xpathProcessor) throws IllegalStateException {

    ActionConfigurationImpl action = new ActionConfigurationImpl();

    // identifier
    String identifier = XPathHelper.valueOf(config, "@id", xpathProcessor);
    if (identifier == null)
      throw new IllegalStateException("Unable to create actions without identifier");
    action.setIdentifier(identifier);

    // class
    String className = XPathHelper.valueOf(config, "class", xpathProcessor);
    if (className == null)
      throw new IllegalStateException("Action '" + identifier + " has no implementation class");
    try {
      Class<? extends Action> c = (Class<? extends Action>) Class.forName(className);
      action.setActionClass(c);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to instantiate class " + className + " for action '" + identifier + ": " + e.getMessage(), e);
    }

    // mountpoint
    String mountpoint = XPathHelper.valueOf(config, "mountpoint", xpathProcessor);
    if (mountpoint == null)
      throw new IllegalStateException("Action '" + identifier + " has no mountpoint");
    action.setMountpoint(mountpoint);
    // TODO: handle /, /*

    // content url
    String targetUrl = XPathHelper.valueOf(config, "page", xpathProcessor);
    action.setPageURI(targetUrl);

    // template
    String targetTemplate = XPathHelper.valueOf(config, "template", xpathProcessor);
    action.setTemplate(targetTemplate);

    // recheck time
    String recheck = XPathHelper.valueOf(config, "recheck", xpathProcessor);
    if (recheck != null) {
      try {
        action.setRecheckTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The recheck time '" + recheck + "' is malformed", e);
      }
    }

    // valid time
    String valid = XPathHelper.valueOf(config, "valid", xpathProcessor);
    if (valid != null) {
      try {
        action.setValidTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The valid time '" + recheck + "' is malformed", e);
      }
    }

    // scripts
    NodeList scripts = XPathHelper.selectList(config, "includes/script", xpathProcessor);
    for (int i = 0; i < scripts.getLength(); i++) {
      action.addInclude(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(config, "includes/link", xpathProcessor);
    for (int i = 0; i < includes.getLength(); i++) {
      action.addInclude(LinkImpl.fromXml(includes.item(i)));
    }

    // name
    NodeList names = XPathHelper.selectList(config, "name", xpathProcessor);
    for (int i = 0; i < names.getLength(); i++) {
      Node localiziation = names.item(i);
      String language = XPathHelper.valueOf(localiziation, "@language", xpathProcessor);
      if (language == null)
        throw new IllegalStateException("Found action name without language");
      String name = XPathHelper.valueOf(localiziation, "text()", xpathProcessor);
      if (name == null)
        throw new IllegalStateException("Found empty action name");
      action.setName(name, LanguageSupport.getLanguage(language));
    }
    
    // flavor
    // TODO: Make this dynamic/configurable?
    action.addFlavor(RequestFlavor.HTML);
    
    // options
    Node optionsNode = XPathHelper.select(config, "options", xpathProcessor);
    action.options = OptionsHelper.fromXml(optionsNode, xpathProcessor);

    return action;
  }

  /**
   * Returns an <code>XML</code> representation of the action configuration,
   * that will look similar to the following example:
   * 
   * <pre>
   * &lt;action id="myaction"&gt;
   * TODO: Finish example
   * &lt;/action&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>ActionConfiguration</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the action configuration
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<action id=\"");
    b.append(identifier);
    b.append("\">");
    
    // class
    b.append("<class>").append(actionClass.getName()).append("</class>");

    // mountpoint
    b.append("<mountpoint>").append(mountpoint).append("</mountpoint>");

    // pageuri
    if (pageURI != null)
      b.append("<page>").append(pageURI).append("</page>");

    // template
    if (template != null)
      b.append("<template>").append(template).append("</template>");

    // Recheck time
    if (recheckTime >= 0) {
      b.append("<recheck>");
      b.append(ConfigurationUtils.toDuration(recheckTime));
      b.append("</recheck>");
    }

    // Valid time
    if (validTime >= 0) {
      b.append("<valid>");
      b.append(ConfigurationUtils.toDuration(validTime));
      b.append("</valid>");
    }

    // Names
    for (Language l : name.languages()) {
      b.append("<name language=\"").append(l.getIdentifier()).append("\">");
      b.append(name.get(l));
      b.append("</name>");
    }

    // Includes
    if (includes.size() > 0) {
      b.append("<includes>");
      for (HTMLHeadElement include : getIncludes()) {
        if (include instanceof Link)
          b.append(include.toXml());
      }
      for (HTMLHeadElement include : getIncludes()) {
        if (include instanceof Script)
          b.append(include.toXml());
      }
      b.append("</includes>");
    }
    
    // Options
    b.append(options.toXml());

    b.append("</action>");
    return b.toString();
  }

}
