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

package ch.entwine.weblounge.contentrepository.impl.fs;

import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import java.io.IOException;

/**
 * This index implementation is suited for filesystem based content
 * repositories. Given a root directory, the index will maintain it's index
 * files inside of that directory.
 */
public class FileSystemContentRepositoryIndex extends ContentRepositoryIndex {

  /**
   * Creates a new content repository index that lives inside the given
   * directory.
   * 
   * @param site
   *          the site
   * @param serializer
   *          the resource serializer service
   * @throws IOException
   *           if an existing index cannot be read of if creating new index
   *           files fails
   */
  public FileSystemContentRepositoryIndex(Site site,
      ResourceSerializerService serializer) throws IOException {
    super(site, serializer, false);
  }

}
