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

package ch.entwine.weblounge.contentrepository.impl.bundle;

import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import java.io.File;
import java.io.IOException;

/**
 * Index implementation for a content repository located inside an OSGi bundle.
 */
public class BundleContentRepositoryIndex extends ContentRepositoryIndex {

  /**
   * Creates a new repository index at the specified location.
   * 
   * @param site
   *          the site
   * @param rootDir
   *          the index root directory
   * @param serializer
   *          the resource serializer
   * @throws IOException
   *           if the index cannot be created at the given location
   */
  public BundleContentRepositoryIndex(Site site, File rootDir,
      ResourceSerializerService serializer) throws IOException {
    super(site, rootDir, serializer, false);
  }

}
