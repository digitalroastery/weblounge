/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.workbench;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * Implementation of a weblounge workbench. The workbench provides support for
 * management applications and the page editor.
 */
public class WorkbenchService implements ManagedService {

  /** The logging facility */
  private static Logger logger = LoggerFactory.getLogger(WorkbenchService.class);

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    // TODO Auto-generated method stub

  }

  /**
   * Returns the pagelet editor or <code>null</code> if either one of the page,
   * the composer or the is not available.
   * 
   * @param site
   *          the site
   * @param pageURI
   *          the page uri
   * @param composerId
   *          the composer id
   * @param pageletIndex
   *          the pagelet index
   * @return the pagelet editor
   */
  public PageletEditor getEditor(Site site, ResourceURI pageURI,
      String composerId, int pageletIndex) {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (composerId == null)
      throw new IllegalArgumentException("Composer must not be null");
    if (pageletIndex < 0)
      throw new IllegalArgumentException("Pagelet index must be a positive integer");

    // Get hold of the site's content repository
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return null;
    }

    // Find the pagelet
    Page page = null;

    try {
      page = (Page) contentRepository.get(pageURI);
      if (page == null) {
        logger.warn("Client requested pagelet editor for non existing page {}", pageURI);
        return null;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to access content repository {}: {}", contentRepository, e);
      return null;
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      logger.warn("Client requested pagelet editor for non existing composer {} on page {}", composerId, pageURI);
      return null;
    }

    // Get the pagelet
    if (composer.getPagelets().length < pageletIndex || composer.size() < pageletIndex) {
      logger.warn("Client requested pagelet editor for non existing pagelet on page {}", pageURI);
      return null;

    }
    Pagelet pagelet = composer.getPagelet(pageletIndex);

    return new PageletEditor(pagelet);
  }

}
