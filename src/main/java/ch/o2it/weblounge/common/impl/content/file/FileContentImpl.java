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
import ch.o2it.weblounge.common.language.Language;

/**
 * Default implementation of a file content.
 */
public class FileContentImpl implements FileContent {

  /** The content's language */
  protected Language language = null;

  /** The file's mimetype */
  protected String mimetype = null;

  /** The file's original name */
  protected String filename = null;

  /** The file size in bytes */
  protected long size = -1L;

  /**
   * Creates a new file content representation.
   * 
   * @param language
   *          the language
   */
  protected FileContentImpl(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    this.language = language;
  }
  
  /**
   * Creates a new file content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param filesize
   *          the file size in bytes
   */
  public FileContentImpl(String filename, Language language, long filesize) {
    if (filename == null)
      throw new IllegalArgumentException("Filename cannot be null");
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    if (filesize <= 0)
      throw new IllegalArgumentException("File size needs to be larger than 0 bytes");
    this.filename = filename;
    this.language = language;
    this.size = filesize;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#setLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#setMimetype(java.lang.String)
   */
  public void setMimetype(String mimetype) {
    if (mimetype == null)
      throw new IllegalArgumentException("Mimetype must not be null");
    this.mimetype = mimetype;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#getMimetype()
   */
  public String getMimetype() {
    return mimetype;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#setFilename(java.lang.String)
   */
  public void setFilename(String filename) {
    if (filename == null)
      throw new IllegalArgumentException("Filename must not be null");
    this.filename = filename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#getFilename()
   */
  public String getFilename() {
    return filename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#setSize(long)
   */
  public void setSize(long size) {
    if (size <= 0)
      throw new IllegalArgumentException("File size must be greater than 0 bytes");
    this.size = size;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#getSize()
   */
  public long getSize() {
    return size;
  }

  /**
   * Callback for subclasses that need to add additional information to the file
   * content representation. Implementations should append their data to the
   * <code>StringBuffer</code> and return it once they're done.
   * 
   * @param xml
   *          the string buffer
   * @return the modified string buffer
   */
  protected StringBuffer addXml(StringBuffer xml) {
    return xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.file.FileContent#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<content language=\"").append(language.getIdentifier()).append("\">");
    if (filename != null)
      buf.append("<filename><![CDATA[").append(filename).append("]]></filename>");
    if (mimetype != null)
      buf.append("<mimetype>").append(mimetype).append("</mimetype>");
    buf.append("<size>").append(size).append("</size>");
    addXml(buf);
    buf.append("</content>");
    return buf.toString();
  }

}
