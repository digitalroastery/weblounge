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

import ch.entwine.weblounge.common.repository.ContentRepositoryException;

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Base class for JCR related tests. Sets up a JCR repository.
 */
public class WebloungeJCRTestBase {

  /** The temporary folder for the transient JCR repository */
  @ClassRule
  // CHECKSTYLE:OFF - field must be public, because it's a ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();
  // CHECKSTYLE:ON

  /** The JCR repository */
  private static TransientRepository repository = null;

  /** Session to the JCR repository */
  private static Session session = null;
  
  /** The logging facility */
  private static Logger log = LoggerFactory.getLogger(WebloungeJCRTestBase.class);

  @BeforeClass
  public static void setUpRepository() throws Exception {
    File dir = temp.newFolder("repository");
    File xml = new File(PageRepositoryTest.class.getResource("/repository.xml").toURI());
    repository = new TransientRepository(xml, dir);


    // Register Weblounge namespace
    try {
      NamespaceRegistry nsRegistry = getSession().getWorkspace().getNamespaceRegistry();
      nsRegistry.registerNamespace("webl", "http://entwine.ch/weblounge/jcr");
      session.save();
      log.info("Registered namespace '{}' with uri '{}'", "webl", "http://entwine.ch/weblounge/jcr");
    } catch (RepositoryException e) {
      log.warn("Error while trying to register namespace '{}': {}", "webl", e.getMessage());
      throw new ContentRepositoryException(e);
    }
  }

  @AfterClass
  public static void shutdownRepository() {
    repository.shutdown();
    repository = null;
    session = null;
  }

  public static Repository getRepository() {
    return repository;
  }

  public static Session getSession() throws RepositoryException {
    if (session == null)
      session = repository.login();
    return session;
  }

  public static Node getTestRootNode() throws RepositoryException {
    Node rootNode = getSession().getRootNode();
    return rootNode.addNode(UUID.randomUUID().toString());
  }

}
