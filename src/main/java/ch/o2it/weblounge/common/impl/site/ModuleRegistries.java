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

import ch.o2it.weblounge.common.impl.util.registry.Registry;
import ch.o2it.weblounge.common.site.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides the various registries for modules, such as the renderer registry,
 * service registry etc.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class ModuleRegistries {
	
	/** Modules */
	private static Map<Module, Map> modules_ = new HashMap<Module, Map>();
	
	/**
	 * This constructor is private to prevent instantiation of this class, since
	 * it is meant to be purely static.
	 */	
	private ModuleRegistries() { }

	/**
	 * Adds the given module registry to the list of registries using the given key.
	 * If there is a registry already registered with this key, then it is replaced
	 * by the new one.
	 * 
	 * @param key the registry key
	 * @param module the associated module
	 * @param registry the registry
	 * @see #remove(String, Module)
	 */
	public static void add(String key, Module module, Registry registry) {
		Map<String, Registry> registries = modules_.get(module);
		if (registries == null) {
			registries = new HashMap<String, Registry>();
			modules_.put(module, registries);
		}
		registries.put(key, registry);
	}
	
	/**
	 * Removes the module registry with the given key.
	 * 
	 * @param key the registry key
	 * @param module the associated site
	 * @see #add(String, Module, Registry)
	 * @see #get(String, Module)
	 */
	public static void remove(String key, Module module) {
		Map registries = modules_.get(module);
		if (registries != null)
			registries.remove(key);
	}
	
	/**
	 * Returns the m,odule registry with the given name or <code>null</code> if no registry
	 * with this name can be found.
	 * 
	 * @param key the registry key
	 * @param module the associated module
	 * @return the registry or <code>null</code>.
	 * @see #add(String, Module, Registry)
	 * @see #remove(String, Module)
	 */
	public static Registry get(String key, Module module) {
		Map registries = modules_.get(module);
		if (registries != null)
			return (Registry)registries.get(key);
		return null;
	}

}