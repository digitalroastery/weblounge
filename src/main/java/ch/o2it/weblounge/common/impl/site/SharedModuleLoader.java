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

import ch.o2it.weblounge.common.impl.util.PathSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Module watcher for shared modules located in the directory
 * <code>$WEBLOUNGE_HOME/shared/modlule</code>.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

final class SharedModuleLoader extends ModuleLoader {
	
	/** Relative path to the shared module directory */
	public static final String REL_PATH = PathSupport.concat(Directories.SHARED_DIR, "module");
	
	/** Absolute path to the shared module directory */
	public static final String ABS_PATH = Directories.toAbsolute(REL_PATH);
	
	/** List of processed module directories */
	private List configurations = new ArrayList();
	
	/**
	 * Creates a new shared module loader for the given site.
	 * 
	 * @param site the site
	 */
	SharedModuleLoader(ModuleManager manager) {
		super(new File(ABS_PATH), null, manager);
		File[] modules = directory.listFiles();
		if (modules != null) {
			for (int i=0; i < modules.length; i++) {
				if (modules[i].isDirectory()) {
					File modulexml = new File(modules[i], "module.xml");
					if (modulexml.exists() && modulexml.canRead()) {
						configurations.add(modules[i]);
					}
				}
			}
		}
	}
	
	/**
	 * Returns <code>true</code> while the shared modules are being loaded.
	 * 
	 * @return <code>true</code> while the modules are being loaded
	 */
	synchronized boolean isLoading() {
		return configurations != null && configurations.size() > 0;
	}

	/**
	 * Overwritten to be able to check for loaded modules.
	 * 
	 * @see ch.o2it.weblounge.core.util.config.DirectoryWatchdog#fileAppeared(java.io.File)
	 */
	protected synchronized void fileAppeared(File file) {
		if (configurations != null) {
			configurations.remove(file);
			if (configurations.isEmpty())
				configurations = null;
		}
		super.fileAppeared(file);
	}
	
}