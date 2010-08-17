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

package ch.o2it.weblounge.common.content.file;

import ch.o2it.weblounge.common.content.ResourceContent;

import java.io.IOException;
import java.io.InputStream;

/**
 * File content represents an actual download for a {@link FileResource}. It
 * contains information about the file's mime type, size and extension. In
 * addition, it is designed to be extended by subclasses like images.
 */
public interface FileContent extends ResourceContent {

  /**
   * Sets the content mime type.
   * 
   * @param mimetype
   *          the mime type
   */
  void setMimetype(String mimetype);

  /**
   * Returns the content type.
   * 
   * @return the mime type
   */
  String getMimetype();

  /**
   * Sets the file size in bytes.
   * 
   * @param size
   *          the size
   */
  void setSize(long size);

  /**
   * Returns the file size in bytes.
   * 
   * @return the file size
   */
  long getSize();

  /**
   * Opens a stream to the file's content.
   * 
   * @return the content
   * @throws IOException
   *           if reading the resource content fails
   */
  InputStream openStream() throws IOException;

}
