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

package ch.o2it.weblounge.dispatcher.impl.handler;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationBase;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.request.RequestHandlerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;

/**
 * RequestHandlerConfigurationImpl
 * 
 * @version $Revision: 1.9 $ $Date: 2005/11/29 23:28:40 $
 * @author Daniel Steiner
 */
public class RequestHandlerConfigurationImpl extends ConfigurationBase implements RequestHandlerConfiguration {

	/** Service identifier */
	String identifier;
	
	/** Service name */
	String name;
	
	/** Service description */
	String description;

	/** Service description */
	String className;

	/** Java system properties  */
	Map env;
	
	/** the logger */
	Logger log = LoggerFactory.getLogger(RequestHandlerConfigurationImpl.class);

	/**
	 * Reads the handler configuration from the given xml configuration node.
	 * 
	 * @param config the configuration node
	 * @param path the XPath object used to parse the configuration
	 * @throws ConfigurationException if there are errors in the configuration
	 */
	public void init(XPath path, Node config) throws ConfigurationException {
		Arguments.checkNull(config, "config");
		try {
			readMainSettings(path, config);
			readEnv(path, config);
			super.init(path, config);
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error when reading request handler configuration!", e);
			throw new ConfigurationException("Error when reading request handler configuration!", e);
		}
	}

	/**
	 * Reads the handler properties from the handler configuration. These properties will
	 * be set using <code>System.setProperty()</code> at handler configuration time.
	 * 
	 * @param config handler configuration node
	 * @param path the XPath object used to parse the configuration
	 * @throws TransformerException on XPathAPI errors
	 */
	private void readEnv(XPath path, Node config) throws TransformerException {
		env = new HashMap();
		NodeList nodes = XPathHelper.selectList(path, config, "properties/env");
		for (int i=0; i < nodes.getLength(); i++) {
			Node property = nodes.item(i);
			String name = XPathHelper.valueOf(path, property, "name/text()");
			String value =  XPathHelper.valueOf(path, property, "value/text()");
			if (env.get(name) != null) {
				Object o = env.get(name);
				if (o instanceof List) {
					((List)o).add(value);
				} else {
					List values = new ArrayList();
					values.add(o);
					values.add(value);
					env.remove(name);
					env.put(name, values);
				}
			} else {
				env.put(name, value);
			}
			log.debug("Java property '" + name + "' has been set to '" + value + "'");
		}
	}

	/**
	 * Reads the main handler settings like identifier, name and description
	 * from the handler configuration.
	 * 
	 * @param config handler configuration node
	 * @throws DOMException
	 * @throws TransformerException
	 * @throws ConfigurationException
	 */
	private void readMainSettings(XPath path, Node config) throws DOMException, TransformerException, ConfigurationException {
		identifier = XPathHelper.valueOf(path, config, "@id");
		if (identifier == null || identifier.equals(""))
			throw new ConfigurationException("handler identifier must be specified");
		name = XPathHelper.valueOf(path, config, "name/text()");
		if (name == null || name.equals(""))
			throw new ConfigurationException("handler name must be specified");
		description = XPathHelper.valueOf(path, config, "description/text()");
		className = XPathHelper.valueOf(path, config, "class/text()");
		if (className == null || className.equals(""))
			throw new ConfigurationException("handler class must be specified");
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getIdentifier()
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#properties()
	 */
	public Iterator properties() {
		return env.keySet().iterator();
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#hasProperty(java.lang.String)
	 */
	public boolean hasProperty(String name) {
		return env.containsKey(name);
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		Object property = env.get(name);
		if (property instanceof ArrayList) {
			return (String)((ArrayList) property).get(0);
		} else {
			return (String) property;
		}
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getProperty(java.lang.String, java.lang.String)
	 */
	public String getProperty(String name, String defaultValue) {
		String value = getProperty(name);
		return (value != null) ? value : defaultValue;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getProperties(java.lang.String)
	 */
	public String[] getProperties(String name) {
		Object property = env.get(name);
		if (property instanceof ArrayList) {
			return (String[]) ((ArrayList) property).toArray(new String[] {});
		} else if (property instanceof String){
			return new String[] { (String )property };
		}
		return null;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.RequestHandlerConfiguration#getClassName()
	 */
	public String getClassName() {
		return className;
	}
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RequestHandlerConfigurationImpl) {
			RequestHandlerConfigurationImpl s = (RequestHandlerConfigurationImpl) obj;
			return
				identifier.equals(s.identifier) &&
				name.equals(s.name) &&
				description.equals(s.description) &&
				className.equals(s.className) &&
				options.entrySet().containsAll(s.options.entrySet()) &&
				s.options.entrySet().containsAll(options.entrySet());			
		}
		return false;
	}
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return identifier;
	}
}
