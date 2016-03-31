/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.contentrepository.index;

import ch.entwine.weblounge.search.impl.SearchIndexImpl;

import java.io.IOException;

/**
 * SearchIndexImplStub
 */
public final class SearchIndexImplStub extends SearchIndexImpl {

  public static SearchIndexImplStub mkSearchIndexImplStub() throws IOException {
    SearchIndexImplStub si = new SearchIndexImplStub();
    si.init();
    return si;
  }

  private SearchIndexImplStub() {

  }

  @Override
  public void init() throws IOException {
    super.init();
  }



  /**
   * Calls the underlying deactivate-method.
   * 
   * @throws IOException
   */
  public void close() throws IOException {
    super.close();
  }

}
