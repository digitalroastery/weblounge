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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContentReader;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.impl.content.image.ImageContentReader;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceReader;
import ch.o2it.weblounge.contentrepository.impl.index.solr.ImageInputDocument;

import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of a serializer for image resources.
 */
public class ImageResourceSerializer extends AbstractResourceSerializer<ImageContent, ImageResource> {

  /**
   * this gets rid of exception for not using native acceleration
   */
  static {
    System.setProperty("com.sun.media.jai.disableMediaLib", "true");
  }

  /**
   * Creates a new image resource serializer.
   */
  public ImageResourceSerializer() {
    super(ImageResource.TYPE);

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected ImageResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new ImageResourceReader();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializer#getInputDocument(ch.o2it.weblounge.common.content.Resource)
   */
  public SolrInputDocument getInputDocument(Resource<?> resource) {
    return new ImageInputDocument((ImageResource) resource);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializer#getContentReader()
   */
  public ResourceContentReader<ImageContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new ImageContentReader();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Image serializer";
  }

}
