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

package ch.o2it.weblounge.contentrepository.impl.jcr;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import java.io.IOException;
import java.io.InputStream;

/**
 * JRC implementation of the <code>WritableContentRepository</code> interface.
 */
public class JCRContentRepositoryImpl extends AbstractWritableContentRepository {

  /** The repository type */
  public static final String TYPE = "ch.o2it.weblounge.contentrepository.jcr";

  /**
   * Creates a new instance of the jcr content repository service.
   */
  public JCRContentRepositoryImpl() {
    super(TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResource(ch.o2it.weblounge.common.content.ResourceURI,
   *      long[])
   */
  @Override
  protected void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.o2it.weblounge.common.content.Resource)
   */
  protected <T extends ResourceContent, R extends Resource<T>> R storeResource(
      R resource) throws IOException {
    // TODO Auto-generated method stub
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResource(ch.o2it.weblounge.common.content.ResourceURI)
   */
  protected InputStream openStreamToResource(ResourceURI uri)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.repository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResourceContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.language.Language)
   */
  @Override
  protected InputStream openStreamToResourceContent(ResourceURI uri,
      Language language) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResourceContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.ResourceContent, java.io.InputStream)
   */
  protected <T extends ResourceContent> T storeResourceContent(ResourceURI uri,
      T content, InputStream is) throws IOException {
    // TODO Auto-generated method stub
    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResourceContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.content.ResourceContent)
   */
  @Override
  protected <T extends ResourceContent> void deleteResourceContent(
      ResourceURI uri, T content) throws IOException {
    // TODO Auto-generated method stub

  }

}
