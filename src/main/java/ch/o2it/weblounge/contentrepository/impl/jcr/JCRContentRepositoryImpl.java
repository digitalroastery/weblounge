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

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import java.io.IOException;

/**
 * JRC implementation of the <code>WritableContentRepository</code> interface.
 */
public class JCRContentRepositoryImpl extends AbstractWritableContentRepository {

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deletePage(ch.o2it.weblounge.common.content.PageURI, long[])
   */
  @Override
  protected void deletePage(PageURI uri, long[] revisions) throws IOException {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storePage(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.content.Page)
   */
  @Override
  protected void storePage(PageURI uri, Page page) throws IOException {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#updatePage(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.content.Page)
   */
  @Override
  protected void updatePage(PageURI uri, Page page) throws IOException {
    // TODO Auto-generated method stub
    
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
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadPage(ch.o2it.weblounge.common.content.PageURI)
   */
  protected Page loadPage(PageURI uri) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {
    // TODO Auto-generated method stub
    
  }

}
