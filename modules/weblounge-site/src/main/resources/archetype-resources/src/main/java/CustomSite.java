/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ${groupId};

import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.site.SiteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom implementation of the <code>Site</code> API.
 */
public class CustomSite extends SiteImpl {

  /** Serial version UID */
  private static final long serialVersionUID = -241760236588222514L;
  
  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(CustomSite.class);

  /**
   * {@inheritDoc}
   */
  public synchronized void start() throws SiteException, IllegalStateException {
    logger.info("Site '${artifactId}' is about to start");
    super.start();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void stop() throws IllegalStateException {
    logger.info("Site '${artifactId}' is about to stop");
    super.stop();
  }
  
}
