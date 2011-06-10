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

package ch.entwine.weblounge.contentrepository;

import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.site.Site;

/**
 * The <code>ContentRepositoryService</code> provides access to the content
 * repository of a given site.
 */
public interface ContentRepositoryService {

  /**
   * Returns the content repository for the given site or <code>null</code> if
   * no repository exists.
   * 
   * @param site
   *          the site
   * @return the site's content repository
   */
  ContentRepository getRepository(Site site);

}
