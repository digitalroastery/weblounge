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

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

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

  @BeforeClass
  public static void setUpRepository() throws Exception {
    File dir = temp.newFolder("repository");
    File xml = new File(PageRepositoryTest.class.getResource("/repository.xml").toURI());
    repository = new TransientRepository(xml, dir);
  }

  @AfterClass
  public static void shutdownRepository() {
    repository.shutdown();
    repository = null;
  }
  
  public static Repository getRepository() {
    return repository;
  }
  
  public static Session getSession() throws RepositoryException {
    if (session == null)
      session = repository.login();
    return session;
  }

}
