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

package ch.entwine.weblounge.common.impl.content.file;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.impl.content.ResourceImpl;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;


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
    super(new ResourceURIImpl(TYPE, uri.getSite(), uri.getPath(), uri.getIdentifier(), uri.getVersion()));
    setLanguageResolution(LanguageResolution.Original);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.impl.content.ResourceImpl#toXmlRootTag()
   */
  @Override
  protected String toXmlRootTag() {
    return TYPE;
  }
  
}
