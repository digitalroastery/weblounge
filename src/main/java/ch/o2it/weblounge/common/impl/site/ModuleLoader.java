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

import ch.o2it.weblounge.common.impl.util.I18n;
import ch.o2it.weblounge.common.impl.util.PathSupport;
import ch.o2it.weblounge.common.impl.util.config.DirectoryWatchdog;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This watchdog keeps watching the <code>modules</code> folder to be
 * able to detect new or removed modules.
 *
 * @author Tobias Wunden
 * @version 1.0
 */

class ModuleLoader extends DirectoryWatchdog {

	/** The associated site */
	private Site site_;

	/** The module manager */
	private ModuleManager manager_;
	
	// Logging
	
	/** the class name, used for the loggin facility */
	private final static String className = ModuleLoader.class.getName();
	
	/** Logging facility */
	final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * Creates a new ModuleLoader which will immediately start searching
	 * for modules that have either been added or removed.
	 * 
	 * @param directory the modules directory
	 * @param site the associated site
	 * @param manager the module manager
	 */
	ModuleLoader(File directory, Site site, ModuleManager manager) {
		super(directory, "ModuleWatcher", false);
		site_ = site;
		manager_ = manager;
	}

	/**
	 * Initializes the module watcher by making every module directory, that exists
	 * at the beginning, to appear as if it had been added.
	 */		
	void init() {
		File baseDir = new File(getDirectoryPath());

		// Search the modules directory and go through all directories
		// located in there.
		
		if (baseDir.exists() && baseDir.canRead()) {
			File[] moduleDirs = baseDir.listFiles();

			// Go through all potential module directories and check for the
			// module.xml configuration file

			for (int i=0; i < moduleDirs.length; i++) {
				File module = moduleDirs[i];
				fileAppeared(module);
			}
		} else {
			log_.warn("Unable to load modules because modules directory doesn't exist or is not readable!");
		}
	}
	
	/**
	 * Method is called if  a new file has been detected in the modules
	 * folder. It then checks if it is a folder and has a module.xml file in it.
	 * If so, the new module is being configured and eventually started.
	 * 
	 * @param moduleDir the module directory
	 * @see ch.o2it.weblounge.core.util.config.DirectoryWatchdog#fileAppeared(File)
	 */
	protected void fileAppeared(File moduleDir) {
		File configFile = new File(moduleDir, Module.CONFIG_FILE);
		if (configFile.exists() && configFile.canRead()) {
			ModuleConfigurationImpl config = new ModuleConfigurationImpl(configFile, site_, shared);
			config.configure();
			manager_.configureModule(config);
			I18n.addDictionaries(new File(configFile.getParentFile(), PathSupport.concat("conf", "i18n")));
		} else {
			String filename = moduleDir.getAbsolutePath();
			String modulename = filename.substring(filename.lastIndexOf(File.separator) + 1);
			log_.info("No module descriptor (" + Module.CONFIG_FILE + ") found for module '" + modulename + "'");
		}
	}

	/**
	 * This method is called if a watched file disappeared.
	 * 
	 * @param file the disappearing file
	 * @see ch.o2it.weblounge.core.util.config.DirectoryWatchdog#fileDisappeared(File)
	 */
	protected void fileDisappeared(File file) {
		// TODO Implement fileDisappeared()
	}

	/**
	 * This method is called if a watched file changes.
	 * 
	 * @param file the changed file
	 * @see ch.o2it.weblounge.core.util.config.Watchdog#fileChanged(File)
	 */
	protected void fileChanged(File file) {
		// TODO Implement fileChanged()
	}

}