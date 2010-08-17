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
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceContentReader;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.contentrepository.impl.index.solr.PageInputDocument;

import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Resource serializer for pages.
 */
public class PageSerializer extends AbstractResourceSerializer<ResourceContent, Page> {

  /**
   * Creates a new page serializer.
   */
  public PageSerializer() {
    super(Page.TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected PageReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new PageReader();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializer#getInputDocument(ch.o2it.weblounge.common.content.Resource)
   */
  public SolrInputDocument getInputDocument(Resource<?> page) {
    return new PageInputDocument((Page)page);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializer#getContentReader()
   */
  public ResourceContentReader<ResourceContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return null;
  }

}
