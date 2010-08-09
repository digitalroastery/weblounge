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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.ResourceImpl;
import ch.o2it.weblounge.common.language.Language;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Default implementation of a file resource.
 */
public class FileResourceImpl extends ResourceImpl implements FileResource {

  /** URL to the file's */
  protected URL contentUrl = null;
  
  /** The file size */
  protected long size = -1;
  
  /** The mime type */
  protected String mimeType = null;

  /**
   * Creates a new file with the given uri and the data at
   * <code>contentUrl</code>.
   * 
   * @param uri
   *          the file uri
   */
  public FileResourceImpl(ResourceURI uri) {
    this(uri, null);
  }

  /**
   * Creates a new file with the given uri and the data at
   * <code>contentUrl</code>.
   * 
   * @param uri
   *          the file uri
   * @param contentUrl
   *          <code>URL</code> to the file's content
   */
  public FileResourceImpl(ResourceURI uri, URL contentUrl) {
    super(uri);
    this.contentUrl = contentUrl;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.file.FileResource#setSize(long)
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.file.FileResource#getSize(ch.o2it.weblounge.common.language.Language)
   */
  public long getSize(Language language) {
    return size;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.file.FileResource#setMimeType(java.lang.String)
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.file.FileResource#getMimeType()
   */
  public String getMimeType() {
    return mimeType;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlHead(java.lang.StringBuffer)
   */
  @Override
  protected void toXmlHead(StringBuffer buffer) {
    super.toXmlHead(buffer);
    
    // Mime type
    if (mimeType != null) {
      buffer.append("<mimetype>");
      buffer.append(mimeType);
      buffer.append("</mimetype>");
    }

    // File size
    if (size > 0) {
      buffer.append("<size>");
      buffer.append(size);
      buffer.append("</size>");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileResource#openStream()
   */
  public InputStream openStream() throws IOException {
    return contentUrl.openStream();
  }

}
