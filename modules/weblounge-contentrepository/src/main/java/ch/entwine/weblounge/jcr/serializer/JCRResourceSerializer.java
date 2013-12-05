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
package ch.entwine.weblounge.jcr.serializer;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;

import javax.jcr.Node;

/**
 * A resource serializer knows the implementation details of storing and reading
 * a {@link Resource}e into/from a JCR {@link Node}.
 * 
 * @see Resource
 * @see Node
 */
public interface JCRResourceSerializer {

  /**
   * Returns a list of classes this resource serializer is capable to serialize.
   * 
   * @return a class list
   */
  <T extends Resource<?>> Class<T>[] getSerializableTypes();

  /**
   * This method serializes the given {@link Resource} into the given
   * {@link Node}.
   * <p>
   * If the given resource type is not supported by this serializer or if any
   * other error occurs while storing the resource in the node, a
   * {@link ContentRepositoryException} is thrown.
   * 
   * @param node
   *          the node to store the resource in
   * @param resource
   *          the resource to store
   * @throws ContentRepositoryException
   *           if the resource type is not supported or any other error occurs
   */
  // TODO Maybe we should throw something like a TypeNotSupportedException, if
  // the type is not supported by this serializer
  void store(Node node, Resource<?> resource) throws ContentRepositoryException;

  /**
   * Creates a {@link Resource} from its data stored in the given {@link Node}.
   * <p>
   * If any error occurs while reading the resource from the node, a
   * {@link ContentRepositoryException} is thrown.
   * 
   * @param node
   *          the node containing the resource's data
   * @param uri TODO
   * @return the resource created from the node's data
   * @throws ContentRepositoryException
   *           if there occurs any error while reading the resource
   */
  Resource<?> read(Node node, ResourceURI uri) throws ContentRepositoryException;

}