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

package ch.o2it.weblounge.common.impl.util.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of endpoints used to group endpoints into a read and write
 * section.
 */
public class EndpointCollection {

  /** Collection name */
  private String name = null;

  /** Collection title */
  private String title = null;

  /** List of endpoints */
  private List<Endpoint> endpoints = null;

  /**
   * Creates a new collection for endpoints.
   * 
   * @param name
   *          the collection name
   * @param title
   *          the collection title
   */
  public EndpointCollection(String name, String title) {
    if (!EndpointDocumentation.isValid(name))
      throw new IllegalArgumentException("Name must not be null and must be alphanumeric");
    if (title == null)
      throw new IllegalArgumentException("Title must not be null");

    this.name = name;
    this.title = title;
  }

  /**
   * Adds an endpoint to this collection.
   * 
   * @param endpoint
   *          the new endpoint
   */
  public void addEndPoint(Endpoint endpoint) {
    if (endpoint != null) {
      if (this.endpoints == null) {
        this.endpoints = new ArrayList<Endpoint>();
      }
      this.endpoints.add(endpoint);
    }
  }

  /**
   * Returns the collection name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the collection title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the endpoints.
   * 
   * @return the endpoints
   */
  public List<Endpoint> getEndpoints() {
    if (endpoints == null) {
      endpoints = new ArrayList<Endpoint>(0);
    }
    return endpoints;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new EndpointCollection(name, title);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}
