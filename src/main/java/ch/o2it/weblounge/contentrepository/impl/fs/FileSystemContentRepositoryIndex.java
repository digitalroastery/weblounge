/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository.impl.fs;

import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import java.io.File;
import java.io.IOException;

/**
 * This index implementation is suited for filesystem based content
 * repositories. Given a root directory, the index will maintain it's index
 * files inside of that directory.
 */
public class FileSystemContentRepositoryIndex extends ContentRepositoryIndex {

  /** Name for the uri index file */
  public static final String URI_IDX_NAME = "uri.idx";

  /** Name for the id index file */
  public static final String ID_IDX_NAME = "id.idx";

  /** Name for the path index file */
  public static final String PATH_IDX_NAME = "path.idx";

  /** Name for the version index file */
  public static final String VERSION_IDX_NAME = "version.idx";

  /**
   * Creates a new content repository index that lives inside the given
   * directory.
   * 
   * @param directory
   *          the index root directory
   * @throws IOException
   *           if an existing index cannot be read of if creating new index
   *           files fails
   */
  public FileSystemContentRepositoryIndex(File directory) throws IOException {
    super(directory, false);
  }

}
