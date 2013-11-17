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
package ch.entwine.weblounge.jcr.impl.serializer;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer;

import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * This abstract class of the {@link JCRPageResourceSerializer} interface
 * implements some basic functionality used by each concrete implementation of
 * {@link JCRResourceSerializer}
 */
public abstract class AbstractJCRResourceSerializer implements JCRResourceSerializer {

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(AbstractJCRResourceSerializer.class);

  /**
   * Stores data hold by the given {@link Resource} into the JCR {@link Node}.
   * <p>
   * Some special kind of resource-related information is not updated, because
   * it should only be changed by calling the appropriate method on the
   * repository. This behavior applies to the following information:
   * <ul>
   * <li>Identifier (not changeable)
   * <li>Path (see {@link ResourceRepository#move})
   * <li>Lock-information
   * <li>Creation date/creator
   * <li>Modification date/modifier
   * <li>Publication range/publisher
   * <li>Owner
   * <li>Security
   * </ul>
   * TODO Add links to repository methods
   * 
   * @param node
   *          node to store the data in
   * @param resource
   *          resource to store
   * @throws ContentRepositoryException
   *           if there was any error while storing the resource
   */
  protected void storeResource(Node node, Resource<?> resource)
      throws ContentRepositoryException {

    try {
      // Set properties
      // FIXME Set resource type like page, file, image, ...
      node.setProperty("resource-type", "");
      node.setProperty("type", resource.getType());
      node.setProperty("promoted", resource.isPromoted());

      // Save subjects
      if (resource.getSubjects().length > 0) {
        Node subjects = JcrUtils.getOrAddNode(node, "webl:subjects");

        for (String subject : resource.getSubjects()) {
          subjects.addNode(subject);
        }
      }

      // Save series
      if (resource.getSeries().length > 0) {
        Node series = JcrUtils.getOrAddNode(node, "webl:series");

        for (String serie : resource.getSeries()) {
          series.addNode(serie);
        }
      }

      // TODO Finish implementation (language dependent metadata, see ticket
      // #304)

      // TODO Move to repository methods
      // // Save owner
      // if (resource.getOwner() != null) {
      // Node owner = JcrUtils.getOrAddNode(node, "webl:owner");
      // storeUser(owner, resource.getOwner());
      // }
      //
      // // Save lock owner
      // if (resource.isLocked() && resource.getLockOwner() != null) {
      // Node lockOwner = JcrUtils.getOrAddNode(node, "webl:lockowner");
      // storeUser(lockOwner, resource.getLockOwner());
      // }
      // // Save creation date & creator
      // if (resource.getCreationDate() != null) {
      // Node created = JcrUtils.getOrAddNode(node, "webl:created");
      // Calendar cal = Calendar.getInstance();
      // cal.setTime(resource.getCreationDate());
      // created.setProperty("date", cal);
      // }
      // if (resource.getCreator() != null) {
      // Node created = JcrUtils.getOrAddNode(node, "webl:created");
      // storeUser(created, resource.getCreator());
      // }
      //
      // // Save modification date & modifier
      // if (resource.getModificationDate() != null) {
      // Node modified = JcrUtils.getOrAddNode(node, "webl:modified");
      // Calendar cal = Calendar.getInstance();
      // cal.setTime(resource.getModificationDate());
      // modified.setProperty("date", cal);
      // }
      // if (resource.getModifier() != null) {
      // Node modified = JcrUtils.getOrAddNode(node, "webl:modified");
      // storeUser(modified, resource.getModifier());
      // }

    } catch (RepositoryException e) {
      log.warn("Error while storing a the resource '{}' in the given JCR node '{}'", resource, node);
      throw new ContentRepositoryException("Error while storing the resource in the JCR node", e);
    }

  }

  /**
   * Reads the resource data stored in the given node and puts it into the given
   * resource object.
   * 
   * @param node
   *          node with the resource data
   * @param resource
   *          resource to fill
   * @throws ContentRepositoryException
   *           if there was any error while reading the resource data
   */
  protected void readResource(Node node, Resource<?> resource)
      throws ContentRepositoryException {

    try {
      // Read properties
      resource.setIdentifier(node.getIdentifier());
      // FIXME Set path relative to resources node
      resource.setPath(node.getPath());
      resource.setType(node.getProperty("type").getString());
      resource.setPromoted(node.getProperty("promoted").getBoolean());

      // Read subjects
      if (node.hasNode("webl:subjects")) {
        Node subjects = node.getNode("webl:subject");
        NodeIterator subjectsIterator = subjects.getNodes();
        while (subjectsIterator.hasNext()) {
          resource.addSubject(subjectsIterator.nextNode().getName());
        }
      }

      // Read series
      if (node.hasNode("webl:series")) {
        Node series = node.getNode("webl:series");
        NodeIterator seriesIterator = series.getNodes();
        while (seriesIterator.hasNext()) {
          resource.addSeries(seriesIterator.nextNode().getName());
        }
      }

      // Read owner
      if (node.hasNode("webl:owner")) {
        Node ownerNode = node.getNode("webl:owner");
        resource.setOwner(readUser(ownerNode));
      }

      // Read lock owner
      if (resource.isLocked())
        throw new IllegalArgumentException("The given resource is already locked by a user!");
      if (node.hasNode("webl:lockowner")) {
        Node lockOwnerNode = node.getNode("webl:lockowner");
        resource.lock(readUser(lockOwnerNode));
      }

      // Read creation date & creator
      if (node.hasNode("webl:created")) {
        Node created = node.getNode("webl:created");
        resource.setCreated(readUser(created), created.getProperty("date").getDate().getTime());
      }

      // Read modification date & modifier
      if (node.hasNode("webl:modified")) {
        Node modified = node.getNode("webl:modified");
        resource.setModified(readUser(modified), modified.getProperty("date").getDate().getTime());
      }

      // Read publication range & user
      if (node.hasNode("webl:published")) {
        Node publication = node.getNode("webl:published");
        resource.setPublished(readUser(publication), publication.getProperty("from").getDate().getTime(), publication.getProperty("to").getDate().getTime());
      }

      // Read owner
      if (node.hasNode("webl:owner")) {
        Node owner = node.getNode("webl:owner");
        resource.setOwner(readUser(owner));
      }

      // TODO Finish implementation (language dependent metadata, see ticket
      // #304)

    } catch (RepositoryException e) {
      log.warn("Error while reading resource data from JCR node '{}'", node);
      throw new ContentRepositoryException("Error while reading resource data from JCR node", e);
    }

  }

  /**
   * Stores the data of a user object in the given node.
   * 
   * @param node
   *          the node to store the data
   * @param user
   *          the user to store
   * @throws RepositoryException
   *           if storing fails
   */
  protected void storeUser(Node node, User user) throws RepositoryException {
    node.setProperty("realm", user.getRealm());
    node.setProperty("login", user.getLogin());
    node.setProperty("name", user.getName());
  }

  /**
   * Reads all user information from the given node and returns it as a
   * {@link User} object.
   * 
   * @param node
   *          node with the user information
   * @return the user
   * @throws RepositoryException
   *           if reading fails
   */
  protected User readUser(Node node) throws RepositoryException {
    return new UserImpl(node.getProperty("login").getString(), node.getProperty("realm").getString(), node.getProperty("name").getString());
  }
}
