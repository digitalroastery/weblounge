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
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This watchdog keeps watching the <code>sites</code> folder to be
 * able to detect new or removed sites.
 *
 * @author Tobias Wunden
 * @version 1.0
 */

public final class SiteWatchdog extends DirectoryWatchdog {

	/** The thread group for the site loaders */
	public static ThreadGroup siteLoaderThreads = new ThreadGroup("Site loader");
	
	/** The active site loader counter */
	public static int siteLoaderCount = 0;
	
	/** The primary initialized state */
	public static boolean initialized = false;
	
	// Logging
	
	/** the class name, used for the loggin facility */
	private final static String className = SiteWatchdog.class.getName();
	
	/** Logging facility */
	final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * Creates a new SiteWatchdog which will immediately start searching
	 * for sites that have either been added or removed.
	 * 
	 * @param directory  the directory to watch
	 */
	public SiteWatchdog(File directory) {
		super(directory, "SiteWatcher", false);
		init();
	}

	/**
	 * Initializes the site watcher by making every site directory, that exists
	 * at the beginning, to appear as if it had been added.
	 */		
	public void init() {
		File baseDir = new File(getDirectoryPath());

		// Search the sites directory and go through all directories
		// located in there.
		
		if (baseDir.exists() && baseDir.canRead()) {
			File[] siteDirs = baseDir.listFiles();

			// Go through all potential site directories and check for the
			// site.xml configuration file

			for (int i=0; i < siteDirs.length; i++) {
				File site = siteDirs[i];
				fileAppeared(site);
			}
			
			// See if we found any valid site folder
			if (siteLoaderCount == 0) {
				log_.warn("No active sites found!");
			}
		} else {
			log_.warn("Unable to load sites because sites directory doesn't exist or is not readable!");
		}
	}
	
	/**
	 * Returns <code>true</code> if there are sites being loaded.
	 * 
	 * @return <code>true</code>if there are sites being loaded
	 */
	public static boolean sitesLoading() {
		synchronized (siteLoaderThreads) {
			return !initialized && siteLoaderCount > 0;
		}
	}
	
	/**
	 * Callback for siteloaders to state that the site has been loaded.
	 * 
	 * @param site the loaded site
	 */
	public static void siteLoaded(Site site) {
		synchronized (siteLoaderThreads) {
			siteLoaderCount --;
			initialized = true;
		}
	}
	
	/**
	 * Method is called if  a new file has been detected in the sites
	 * folder. It then check if it is a folder and has a site.xml file in it.
	 * If so, the new site is being configured and eventually started.
	 * 
	 * @see ch.o2it.weblounge.core.util.config.DirectoryWatchdog#fileAppeared(File)
	 */
	protected void fileAppeared(File siteDir) {
		File configFile = new File(siteDir, Site.CONFIG_FILE);
		if (configFile.exists()) {
			SiteLoader loader = new SiteLoader(configFile, siteLoaderThreads);
			siteLoaderCount ++;
			(new Thread(siteLoaderThreads, loader)).start();
			I18n.addDictionaries(new File(configFile.getParentFile(), PathSupport.concat("conf", "i18n")));
		} else {
			String filename = siteDir.getAbsolutePath();
			String sitename = filename.substring(filename.lastIndexOf(File.separator) + 1);
			log_.info("No site descriptor (" + Site.CONFIG_FILE + ") found for site '" + sitename + "'");
		}
	}
		
	/**
	 * @see ch.o2it.weblounge.core.util.config.DirectoryWatchdog#fileDisappeared(File)
	 */
	protected void fileDisappeared(File file) {
		// TODO Implement fileDisappeared()
	}

	/**
	 * @see ch.o2it.weblounge.core.util.config.Watchdog#fileChanged(File)
	 */
	protected void fileChanged(File file) {
		// TODO Implement fileChanged()
	}

}