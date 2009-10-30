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
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationBase;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.Include;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ScriptInclude;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;

/**
 * The action configuration stores configuration information about an action handler
 * instance. The configuration data is being read from the <code>&lt;action&gt;</code>
 * section of the <code>module.xml</code>.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class ActionConfigurationImpl extends ConfigurationBase implements ActionConfiguration {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionConfigurationImpl.class);

	/** the default valid time is 60 minutes */
	public static final long DEFAULT_VALID_TIME = 60L * 60L * 1000L;

	/** the default recheck time is 1 minute */
	public static final long DEFAULT_RECHECK_TIME = 60L * 1000L;

	/** Renderer identifier */
	String identifier = null;
	
	/** The action's site */
	Site site = null;
	
	/** The action's module */
	Module module = null;

	/** The concrete action url */
	String mountpoint = null;
	
	/** Mounpoint extension */
	String extension = null;
	
	/** Loadfactor */
	int loadfactor = null;
	
	/** The concrete action class */
	String className = null;
	
	/** The supported action methods */
	List<String> methods = null;

	/** The default target url */
	String target = null;
	
	/** Amount of time until the content is considered to be invalid */
	long validTime = -1;

	/** Amount of time where the content is considered to be valid */
	long recheckTime = -1;
	
	/** The links that have been defined for this action */
	List<Include> includes = null;
	
	/** The scripts that have been defined for this action */
	List<ScriptInclude> scripts = null;
	
	/**
	 * Creates a new action configuration.
	 * 
	 * @param config the bundle configuration
	 */
	public ActionConfigurationImpl(ActionBundleConfiguration config) {
		identifier = config.identifier;
		mountpoint = config.mountpoint;
		extension = config.extension;
		target = config.target;
		recheckTime = config.recheckTime;
		validTime = config.validTime;
		methods = new ArrayList<String>();
		includes = new ArrayList<Include>();
		scripts = new ArrayList<ScriptInclude>();
	}

	/**
	 * Returns the action identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the associated site or <code>null</code> if no site has been
	 * set.
	 * 
	 * @return the site
	 */	
	public Site getSite() {
		return site;
	}
	
	/**
	 * Returns the associated module or <code>null</code> if no module has been
	 * set.
	 * 
	 * @return the module
	 */	
	public Module getModule() {
		return module;
	}

	/**
	 * Returns the supported output methods. The meaning of methods is
	 * the possible output format of an action. The methods usually include
	 * <tt>html</tt>, <tt>pdf</tt> and so on.
	 * 
	 * @return the supported methods
	 */
	public String[] methods() {
		String[] result = new String[methods.size()];
		return methods.toArray(result);
	}

	/**
	 * Returns <code>true</code> if the given method is supported by the action.
	 * The method is used to lookup a method for a given action id.
	 * 
	 * @param method the method name
	 * @return <code>true</code> if the action supports the output method
	 */
	public boolean provides(String method) {
		return methods.contains(method) || methods.contains("*");
	}

	/**
	 * Returns the mountpoint used to call the action. The mountpoint is
	 * interpreted relative to the site root.
	 * 
	 * @return the action mountpoint
	 */
	public String getMountpoint() {
		return mountpoint;
	}
	
	/**
	 * Returns the mountpoint extension. The extension is either <code>null</code>,
	 * <code>/*</code> or <code>/**</code>, depending on whether to match only
	 * the mountpoint (e. g. <code>/news</code>), to match the mountpoint and any
	 * direct children (e. g. <code>/news/today</code>) or the mountpoint and any
	 * subsequent urls.
	 * 
	 * @return <code>true</code> if this action has an extended mountpoint
	 */
	public String getExtension() {
		return extension;
	}
	
	/**
	 * Returns the class name in case of a custom action.
	 * 
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the url that is being targeted by default when executing
	 * this action. You may always overwrite this setting by passing a
	 * request parameter of name <tt>url</tt>.
	 * 
	 * @return the target url
	 */
	public String getTargetUrl() {
		return target;
	}
	
	/**
	 * Returns the amount of time in milliseconds that the output of this action
	 * will be considered valid. This value is used by the cache to throw away
	 * outdated content.
	 * 
	 * @return the valid time of the action output in miliseconds.
	 */
	public long getValidTime() {
		return validTime;
	}

	/**
	 * Returns the amount of time in milliseconds that the output of this action
	 * will be considered valid. After this time, clients will be advised to recheck
	 * whether the output they may have cached is still valid.
	 * 
	 * @return the recheck time of the action output in miliseconds.
	 */
	public long getRecheckTime() {
		return recheckTime;
	}

	/**
	 * Returns the &lt;link&gt; elements that have been defined for this renderer.
	 * 
	 * @return the links
	 */
	public Include[] getLinks() {
		return includes.toArray(new Include[includes.size()]);
	}
	
	/**
	 * Returns the &lt;script&gt; elements that have been defined for this renderer.
	 * 
	 * @return the scripts
	 */
	public ScriptInclude[] getScripts() {
		return scripts.toArray(new ScriptInclude[scripts.size()]);
	}

	/**
	 * Sets the associated site if this is a site related renderer configuration.
	 * 
	 * @param site the associated site
	 */
	public void setSite(Site site) {
		this.site = site;
	}

	/**
	 * Sets the associated module if this is a module renderer configuration.
	 * 
	 * @param module the associated module
	 */
	public void setModule(Module module) {
		this.module = module;
		for (Include include : includes)
		  if (include instanceof IncludeImpl)
		    ((IncludeImpl)include).setModule(module);
		for (ScriptInclude script : scripts)
		  if (script instanceof ScriptIncludeImpl)
		    ((ScriptIncludeImpl)script).setModule(module);
	}

	/**
	 * Returns the action identifier.
	 * 
	 * @return the action identifier
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return identifier;
	}

	/**
	 * Reads the action handler configuration from the given xml configuration node.
	 * 
	 * @param config the handler configuration node
	 * @param path the XPath object used to parse the configuration
	 * @throws ConfigurationException if there are errors in the configuration
	 */
	public void init(XPath path, Node config) throws ConfigurationException {
		Arguments.checkNull(config, "config");
		try {
			readHandler(path, config);
			readLinks(path, config);
			readScripts(path, config);
			super.init(path, config);
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Error when reading configuration for action '" + identifier + "'";
			log_.error(msg, e);
			throw new ConfigurationException(msg, e);
		}
	}

	/**
	 * Reads the methods that are supported by this action.
	 * 
	 * @param config action configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readHandler(XPath path, Node config) throws TransformerException, ConfigurationException {
		String methods = XPathHelper.valueOf(path, config, "methods/text()");
		if (methods == null || methods.length() == 0) {
			String msg = "Error when reading action configuration: No methods defined!";
			log_.error(msg);
			throw new ConfigurationException(msg);
		}		
		StringTokenizer tok = new StringTokenizer(methods, " ");
		while (tok.hasMoreTokens()) {
			this.methods.add(tok.nextToken());
		}
		className = XPathHelper.valueOf(path, config, "class/text()");
	}

	/**
	 * Reads the links that are defined for this renderer.
	 * 
	 * @param config renderer configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readLinks(XPath path, Node config) throws ConfigurationException {
		NodeList links = XPathHelper.selectList(path, config, "link");
		if (links != null) {
			for (int i=0; i < links.getLength(); i++) {
				Node link = links.item(i);
				this.includes.add(new IncludeImpl(link, path));
			}
		}
	}

	/**
	 * Reads the scripts that are defined for this renderer.
	 * 
	 * @param config renderer configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readScripts(XPath path, Node config) throws ConfigurationException {
		NodeList scripts = XPathHelper.selectList(path, config, "script");
		if (scripts != null) {
			for (int i=0; i < scripts.getLength(); i++) {
				Node script = scripts.item(i);
				this.scripts.add(new ScriptIncludeImpl(script, path));
			}
		}
	}	
	
}