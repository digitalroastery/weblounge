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
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.User;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * TODO: Comment AbstractJCRResourceSerializer
 */
public abstract class AbstractJCRResourceSerializer implements JCRResourceSerializer {

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.jcr.JCRResourceSerializer#store(javax.jcr.Node, ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  public void store(Node node, Resource<?> resource) {

    try {
      // Set properties
      node.setProperty("resource-type", ""); // FIXME Set resource type like page, file, image, ...
      node.setProperty("type", resource.getType());
      node.setProperty("promoted", resource.isPromoted());

      // Save owner
      if (resource.getOwner() != null) {
        Node owner = node.addNode("webl:owner");
        // owner.addMixin("webl:user");
        owner.setProperty("realm", resource.getOwner().getRealm());
        owner.setProperty("login", resource.getOwner().getLogin());
        owner.setProperty("name", resource.getOwner().getName());
      }

      // Save lock owner
      if (resource.isLocked() && resource.getLockOwner() != null) {
        Node lockOwner = node.addNode("webl:lockowner");
        lockOwner.setProperty("realm", resource.getLockOwner().getRealm());
        lockOwner.setProperty("login", resource.getLockOwner().getLogin());
        lockOwner.setProperty("name", resource.getLockOwner().getName());
      }

      // Save subjects
      if (resource.getSubjects().length > 0) {
        Node subjects = node.addNode("webl:subjects");

        for (String subject : resource.getSubjects()) {
          subjects.addNode(subject);
        }
      }

      // Save series
      if (resource.getSeries().length > 0) {
        Node series = node.addNode("webl:series");

        for (String serie : resource.getSeries()) {
          series.addNode(serie);
        }
      }

      // TODO Finish implementation

    } catch (ValueFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (VersionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (LockException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ConstraintViolationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.jcr.JCRResourceSerializer#read(javax.jcr.Node, ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  public void read(Node node, Resource<?> resource) {

    try {
      // Set resource properties
      resource.setIdentifier(node.getIdentifier());
      resource.setPath(node.getPath()); // FIXME Set path relative to resources
                                        // node
      resource.setType(node.getProperty("type").getString());
      resource.setPromoted(node.getProperty("promoted").getBoolean());

      // Set owner
      if (node.hasNode("webl:owner")) {
        Node ownerNode = node.getNode("webl:owner");
        User owner = new UserImpl(ownerNode.getProperty("login").getString(), ownerNode.getProperty("realm").getString(), ownerNode.getProperty("name").getString());
        resource.setOwner(owner);
      }

      // Set lock owner
      if (node.hasNode("webl:lockowner")) {
        Node lockOwnerNode = node.getNode("webl:lockowner");
        User lockOwner = new UserImpl(lockOwnerNode.getProperty("login").getString(), lockOwnerNode.getProperty("realm").getString(), lockOwnerNode.getProperty("name").getString());
        resource.lock(lockOwner);
      }

      // Set subjects
      if (node.hasNode("webl:subjects")) {
        Node subjects = node.getNode("webl:subject");
        NodeIterator subjectsIterator = subjects.getNodes();
        while (subjectsIterator.hasNext()) {
          resource.addSubject(subjectsIterator.nextNode().getName());
        }
      }

      // Set series
      if (node.hasNode("series")) {
        Node series = node.getNode("webl:series");
        NodeIterator seriesIterator = series.getNodes();
        while (seriesIterator.hasNext()) {
          resource.addSeries(seriesIterator.nextNode().getName());
        }
      }

      // TODO Finish implementation

    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
