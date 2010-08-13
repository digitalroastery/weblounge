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
import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.ResourceImpl;
import ch.o2it.weblounge.common.language.Language;

/**
 * Default implementation of a file resource.
 */
public class FileResourceImpl extends ResourceImpl<FileContent> implements FileResource {
  
  /**
   * Creates a new file with the given uri and the data at
   * <code>contentUrl</code>.
   * 
   * @param uri
   *          the file uri
   */
  public FileResourceImpl(ResourceURI uri) {
    super(uri);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#addContent(ch.o2it.weblounge.common.content.ResourceContent)
   */
  @Override
  public void addContent(FileContent content) {
    if (content == null)
      throw new IllegalArgumentException("Content must not be null");
    super.addContent(content);
    enableLanguage(content.getLanguage());
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#removeContent(ch.o2it.weblounge.common.language.Language)
   */
  @Override
  public FileContent removeContent(Language language) {
    if (content == null)
      throw new IllegalArgumentException("Content must not be null");
    disableLanguage(language);
    return super.removeContent(language);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlRootTag()
   */
  @Override
  protected String toXmlRootTag() {
    return TYPE;
  }
  
}
