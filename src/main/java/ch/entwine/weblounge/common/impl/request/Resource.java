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

package ch.entwine.weblounge.common.impl.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A <code>Resource</code> represents a file or a directory in a web
 * application.
 */
public class Resource {

  /**
   * Returns <code>true</code> if the resource exists.
   * 
   * @return <code>true</code> if the resource exists
   */
  public boolean exists() {
    // TODO: Implement
    return false;
  }

  /**
   * Returns a <code>File</code> object for this resource.
   * 
   * @return the file
   * @throws IOException
   *           if the file cannot be accessed
   */
  public File getFile() throws IOException {
    // TODO: Implement
    return null;
  }

  /**
   * Returns a <code>URL</code> object for this resource.
   * 
   * @return the url
   * @throws IOException
   *           if the resource cannot be accessed
   */
  public URL getURL() throws IOException {
    // TODO: Implement
    return null;
  }

  /**
   * Returns an input stream to the resource.
   * 
   * @return the input stream
   * @throws IOException
   *           if the resource cannot be accessed
   */
  public InputStream getInputStream() throws IOException {
    // TODO: Implement
    return null;
  }

}
