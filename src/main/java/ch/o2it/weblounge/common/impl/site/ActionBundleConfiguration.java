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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.SecuredObject;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.ModuleConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;

/**
 * The action bundle configuration stores configuration information about an action
 * handler bundle. The configuration data is being read from the
 * <code>&lt;action&gt;</code> section of the <code>module.xml</code>.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class ActionBundleConfiguration extends SecuredObject implements Localizable {

	/** The default valid time for a renderer */
	final static long VALID_TIME_DEFAULT = Times.MS_PER_DAY;

	/** The default recheck time for a renderer */
	final static long RECHECK_TIME_DEFAULT = Times.MS_PER_HOUR;

	/** Renderer identifier */
	String identifier;
	
	/** The concrete action url */
	String mountpoint;
	
	/** Mounpoint extension */
	String extension;
	
	/** The default target url */
	String target;

	/** The action handler configurations */
	Map configurations;
	
	/** The multilingual descriptions */
	LocalizableObject description;
	
	/** Amount of time until the content is considered to be invalid */
	long validTime;

	/** Amount of time where the content is considered to be valid */
	long recheckTime;
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(ActionBundleConfiguration.class.getName());
	
	/**
	 * Creates a new action configuration.
	 * 
	 * @param identifier the action identifier
	 * @param config the module configuration
	 */
	public ActionBundleConfiguration(String identifier, ModuleConfiguration config) {
		this.identifier = identifier;
		this.validTime = VALID_TIME_DEFAULT;
		this.recheckTime = RECHECK_TIME_DEFAULT;
		this.target = null;
		this.configurations = new HashMap();
		this.description = new LocalizableObject();
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
	 * Returns the multilingual action description.
	 * 
	 * @return the description
	 */
	public LocalizableObject getDescription() {
		return description;
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
	 * @return the mountpoint extension
	 */
	public String getExtension() {
		return extension;
	}
	
	/**
	 * Returns the url that is being targeted by default when executing
	 * this action. You may always overwrite this setting by passing a
	 * request parameter of name <tt>url</tt>.
	 * 
	 * @return the target url
	 */
	public String getTarget() {
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
	 * Returns the action identifier.
	 * 
	 * @return the action identifier
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return identifier;
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#getLanguage()
	 */
	public Language getLanguage() {
		return description.getLanguage();
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#setLanguage(ch.o2it.weblounge.api.language.Language)
	 */
	public void setLanguage(Language language) {
		description.setLanguage(language);
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#supportsLanguage(ch.o2it.weblounge.api.language.Language)
	 */
	public boolean supportsLanguage(Language language) {
		return description.supportsLanguage(language);
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#toString(ch.o2it.weblounge.api.language.Language)
	 */
	public String toString(Language language) {
		return description.toString(language);
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#toString(ch.o2it.weblounge.api.language.Language, boolean)
	 */
	public String toString(Language language, boolean force) {
		return description.toString(language, force);
	}

	/**
	 * @see ch.o2it.weblounge.Localizable.language.Multilingual#compareTo(ch.o2it.weblounge.Localizable.language.Multilingual, ch.o2it.weblounge.api.language.Language)
	 */
	public int compareTo(Localizable o, Language language) {
		return description.compareTo(o, language);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return description.compareTo(o);
	}

	/**
	 * Adds the action definition to this action bundle. The <code>
	 * action</code> argument is used to instantiate actions on
	 * demand, the <code>config</code> is then used to configure the
	 * newly created actions.
	 * 
	 * @param actionClass the action class
	 * @param config the action configuration
	 */
	public void define(Class actionClass, ActionConfiguration config) {
		configurations.put(actionClass, config);
	}

	/**
	 * Returns the action handler configurations.
	 * 
	 * @return the configurations
	 */
	Map getConfigurations() {
		return configurations;
	}
	
	/**
	 * Reads the action bundle configuration from the given xml configuration node.
	 * 
	 * @param config the configuration node
	 * @param path the XPath object used to parse the configuration
	 * @throws ConfigurationException if there are errors in the configuration
	 */
	public void read(XPath path, Node config) throws ConfigurationException {
		Arguments.checkNull(config, "config");
		try {
			readMainSettings(path, config);
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			log_.error("Error when reading action bundle configuration!");
			log_.debug("Error when reading action bundle configuration!", e);
			throw new ConfigurationException("Error when reading action bundle configuration!", e);
		}
	}

	/**
	 * Reads the main action bundle settings like identifier, name and description
	 * from the action configuration.
	 * 
	 * @param config action configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readMainSettings(XPath path, Node config) throws DOMException, TransformerException, ConfigurationException {
		mountpoint = XPathHelper.valueOf(path, config, "mountpoint/text()");
		if (mountpoint != null) {
			mountpoint = UrlSupport.concat("/", mountpoint);
			extension = UrlSupport.getExtension(mountpoint);
			mountpoint = UrlSupport.stripExtension(mountpoint);
		}
		
		target = XPathHelper.valueOf(path, config, "target/text()");
		
		try {
			validTime = Long.parseLong(XPathHelper.valueOf(path, config, "valid/text()"));
		} catch (NumberFormatException e) {
			validTime = VALID_TIME_DEFAULT;
		}
		
		try {
			recheckTime = Long.parseLong(XPathHelper.valueOf(path, config, "recheck/text()"));
		} catch (NumberFormatException e) {
			recheckTime = RECHECK_TIME_DEFAULT;
		}
	}

}