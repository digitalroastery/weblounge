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

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a logging interface for site related objects.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public final class SiteLoggerImpl implements SiteLogger {

	/** The site prefix for the log entries */
	private String sitePrefix_;
	
	/** the class name, used for the loggin facility */
	private final static String className = SiteLoggerImpl.class.getName();
	
	/** Logging facility */
	final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * Creates a new logger for the given site.
	 * 
	 * @param site the site
	 */
	public SiteLoggerImpl(Site site) {
		sitePrefix_ = createSitePrefix(site);
	}

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.site.SiteLogger#trace(java.lang.String)
   */
  public void trace(String msg) {
    log_.trace(format(msg));
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.site.SiteLogger#trace(java.lang.String, java.lang.Throwable)
   */
  public void trace(String msg, Throwable t) {
    log_.trace(format(msg), t);
  }
		
	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#debug(java.lang.String)
	 */
	public void debug(String msg) {
		log_.debug(format(msg));
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String msg, Throwable t) {
		log_.info(format(msg), t);
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#info(java.lang.String)
	 */
	public void info(String msg) {
		log_.info(format(msg));
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String msg, Throwable t) {
		log_.info(format(msg), t);
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#warn(java.lang.String)
	 */
	public void warn(String msg) {
		log_.warn(format(msg));
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String msg, Throwable t) {
		log_.warn(format(msg), t);
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#error(java.lang.String)
	 */
	public void error(String msg) {
		log_.error(format(msg));
	}

	/**
	 * @see ch.o2it.weblounge.api.site.SiteLogger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String msg, Throwable t) {
		log_.error(format(msg), t);
	}

	/**
	 * Returns the formatted message ready to be sent to the logging
	 * facility.
	 * 
	 * @param msg the message
	 * @return the formatted message
	 */
	private String format(String msg) {
		StringBuffer b = new StringBuffer();
		b.append(sitePrefix_);
		b.append(msg);
		return b.toString();
	}
	
	/**
	 * Returns the prefix for the given site. The prefix looks like
	 * <code>[<site identifier]</code>.
	 * 
	 * @param site the site
	 * @return the site prefix
	 */
	private static String createSitePrefix(Site site) {
		StringBuffer b = new StringBuffer();
		b.append("[");
		b.append(site.getIdentifier());
		b.append("] ");
		return b.toString();
	}

}