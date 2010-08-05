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

package ch.o2it.weblounge.common.content.resource;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.language.Language;

import java.io.IOException;
import java.io.InputStream;

/**
 * This interface models a file resource.
 */
public interface File extends Resource {

  /**
   * Opens a stream to the file's content.
   * 
   * @return the content
   * @throws IOException
   *           if reading the resource content fails
   */
  InputStream openStream() throws IOException;

  /**
   * Sets the file size in bytes.
   * 
   * @param size the file size
   */
  void setSize(long size);
  
  /**
   * Returns the file size in bytes for the given language version.
   * 
   * @param language
   *          the language
   */
  long getSize(Language language);

  /**
   * Sets the mime type.
   * 
   * @param mimeType
   *          the mime type
   */
  void setMimeType(String mimeType);

  /**
   * Returns the resource's mime type or <code>null</code> if the mimetype is
   * unknown.
   * 
   * @return the mime type
   */
  String getMimeType();

}
