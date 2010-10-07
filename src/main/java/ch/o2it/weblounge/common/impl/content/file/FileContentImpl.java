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

package ch.o2it.weblounge.common.impl.content.file;

import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.impl.content.ResourceContentImpl;
import ch.o2it.weblounge.common.language.Language;

/**
 * Default implementation of a file content.
 */
public class FileContentImpl extends ResourceContentImpl implements FileContent {

  /**
   * Creates a new file content representation.
   */
  protected FileContentImpl() {
    super();
  }

  /**
   * Creates a new file content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the file's mimetype
   */
  public FileContentImpl(String filename, Language language, String mimetype) {
    super(language, filename, mimetype, -1);
  }

  /**
   * Creates a new file content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the file's mimetype
   * @param filesize
   *          the file size in bytes
   */
  public FileContentImpl(String filename, Language language, String mimetype,
      long filesize) {
    super(language, filename, mimetype, filesize);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.ResourceContentImpl#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.ResourceContentImpl#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileContent) {
      FileContent content = (FileContent) obj;
      if (size != content.getSize())
        return false;
      return super.equals(content);
    }
    return false;
  }

}
