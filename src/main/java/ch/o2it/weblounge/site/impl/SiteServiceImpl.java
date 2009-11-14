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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.PageRepository;
import ch.o2it.weblounge.contentrepository.ResourceRepository;
import ch.o2it.weblounge.site.SiteService;

/**
 * Default implementation of the site service.
 */
public class SiteServiceImpl implements SiteService {

  /** The page repository */
  protected PageRepository pageRepository = null;

  /** The resource repository */
  protected ResourceRepository resourceRepository = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.site.SiteService#getSite()
   */
  public Site getSite() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sets the page repository service as soon as it is brought up in the
   * <code>OSGi</code> container.
   * 
   * @param repository
   *          the page repository
   */
  public void setPageRepository(PageRepository repository) {
    this.pageRepository = repository;
  }

  /**
   * Sets the resource repository service as soon as it is brought up in the
   * <code>OSGi</code> container.
   * 
   * @param repository
   *          the resource repository
   */
  public void setResourceRepository(ResourceRepository repository) {
    this.resourceRepository = repository;
  }

}
