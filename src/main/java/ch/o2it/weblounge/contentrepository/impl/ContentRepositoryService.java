/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

import ch.o2it.weblounge.contentrepository.PageRepository;
import ch.o2it.weblounge.contentrepository.ResourceRepository;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * This class represents the default implementation of the
 * {@link PageRepository} and {@link ResourceRepository}, backed by the
 * <code>Jackrabbit content repository</code> implementation.
 */
public class ContentRepositoryService implements PageRepository, ResourceRepository, ManagedService {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(ContentRepositoryService.class);

  /** Weblounge node prefix */
  public static final String PREFIX = "wl:";

  /** Jackrabbit jcr repository instance */
  private Repository jcrRepository = null;

  public ContentRepositoryService() {
  }

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {
    log_.info("Java content repository service properties have been updated");
  }

  /**
   * Sets up the jackrabbit content repository.
   * 
   * @param context
   *          the bundle context
   * @throws Exception
   *           if starting the bundle fails
   */
  public void start(ComponentContext context) throws Exception {
    // Open the configuration template from within this bundle
    InputStream configTemplate = context.getBundleContext().getBundle().getEntry("/cluster-repository-template.xml").openStream();

    // Get the configuration settings from the framework or system properties
    String nodeId = context.getBundleContext().getProperty("weblounge.jcr.nodeId");
    String repoHome = context.getBundleContext().getProperty("weblounge.jcr.repoPath");
    String dbUrl = context.getBundleContext().getProperty("weblounge.jcr.db.url");
    String dbDriver = context.getBundleContext().getProperty("weblounge.jcr.db.driver");
    String dbUser = context.getBundleContext().getProperty("weblounge.jcr.db.user");
    String dbPass = context.getBundleContext().getProperty("weblounge.jcr.db.password");
    String pm = context.getBundleContext().getProperty("weblounge.jcr.persistence.manager");
    String dataStorePath = context.getBundleContext().getProperty("weblounge.jcr.datastore.path");

    // This is just a hack... better to use an xml parser
    BufferedReader br = new BufferedReader(new InputStreamReader(configTemplate));
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    configTemplate.close();
    String templateString = sb.toString();
    String configString = templateString.replaceAll("PERSISTENCE_MGR", pm).replaceAll("NODE_ID", nodeId).replaceAll("DB_DRIVER", dbDriver).replaceAll("DB_USER", dbUser).replaceAll("DB_PASS", dbPass).replaceAll("DB_URL", dbUrl).replaceAll("DATA_STORE_PATH", dataStorePath);

    InputStream config = new ByteArrayInputStream(configString.getBytes("UTF8"));

    log_.info(configString);

    // Build the repository
    RepositoryConfig rc = RepositoryConfig.create(config, repoHome);

    log_.info("Creating a new JCR instance with cluster id=" + rc.getClusterConfig().getId());

    jcrRepository = RepositoryImpl.create(rc);

    // TODO Remove jackrabbit-specific dependencies
    Session session = getSession();
    try {
      JackrabbitNodeTypeManager manager = (JackrabbitNodeTypeManager)
        session.getWorkspace().getNodeTypeManager();
      manager.registerNodeTypes(this.getClass().getResourceAsStream("/nodeTypes.cnd"),
          JackrabbitNodeTypeManager.TEXT_X_JCR_CND);
    } catch (Exception e) {
      e.printStackTrace();
      log_.error(e.getLocalizedMessage());
    }

  }

  /**
   * Stops the previously set up jackrabbit content repository.
   * 
   * @param context
   *          the bundle context
   * @throws Exception
   *           if shutdown fails
   */
  public void stop(ComponentContext context) throws Exception {
    if (jcrRepository != null) {
      ((JackrabbitRepository) jcrRepository).shutdown();
    }
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.repository.api.OpencastRepository#getObject(java.lang.Class, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <T> T getObject(Class<T> type, String path) {
    assertSupported(type);
    log_.debug("getting data from " + path);
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    } else {
      Node node = null;
      try {
        node = (Node)session.getItem(path + "/jcr:content");
        return (T) node.getProperty("jcr:data").getStream();
      } catch (PathNotFoundException e) {
        throw new RuntimeException(e);
      } catch (RepositoryException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected Session getSession() {
    Session session = null;
    try {
      // TODO Use authentication service to log in to the repo
      session = jcrRepository.login(new SimpleCredentials("foo", "bar".toCharArray()));
    } catch (LoginException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return session;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.repository.api.OpencastRepository#getSupportedTypes()
   */
  public Class<?>[] getSupportedTypes() {
    // TODO: Support MediaBundle
    return new Class<?>[] {InputStream.class};
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.repository.api.OpencastRepository#hasObject(java.lang.String)
   */
  public boolean hasObject(String path) {
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    } else {
      try {
        return session.itemExists(path + "/jcr:content");
      } catch (RepositoryException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected void assertSupported(Class<?> type) {
    Class<?>[] supportedTypes = getSupportedTypes();
    boolean objectClassSupported = false;
    for (int i=0; i<supportedTypes.length; i++) {
      Class<?> currentClass = supportedTypes[i];
      if (currentClass.isAssignableFrom(type)) {
        objectClassSupported = true;
        break;
      }
    }
    if( ! objectClassSupported) {
      throw new IllegalArgumentException("Class " + type + " is not a supported object type");
    }
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.repository.api.OpencastRepository#putObject(java.lang.Object, java.lang.String)
   */
  public String putObject(Object object, String path) {
    assertSupported(object.getClass());
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    }
    try {
      Node fileNode = null;
      if (session.itemExists(path)) {
        fileNode = (Node)session.getItem(path);
      } else {
        fileNode = buildPath(session, path);
      }
      log_.debug("fileNode path=" + fileNode.getPath());
      Node resNode;
      try {
        resNode = (Node) fileNode.getNode("jcr:content");
        log_.debug("resource node exists: " + resNode.getPath());
      } catch(PathNotFoundException e) {
        resNode = fileNode.addNode("jcr:content", "nt:resource");
        log_.debug("resource node created: " + resNode.getPath());
        resNode.addMixin("mix:referenceable");
        resNode.setProperty("jcr:mimeType", "application/octet-stream");
        resNode.setProperty("jcr:encoding", "");
      }
      resNode.setProperty("jcr:data", convertToStream(object));
      resNode.setProperty("jcr:lastModified", Calendar.getInstance());
      session.save();
      return fileNode.getUUID();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Build each "nt:unstructured" node as needed
   * @param root
   * @param path
   * @return The leaf nt:unstructured node
   */
  protected Node buildPath(Session session, String path) throws Exception {
    log_.debug("Building nodes for " + path);
    StringBuilder partialPath = new StringBuilder();
    Node currentNode = session.getRootNode();
    String[] sa = path.split("/");
    for(int i=0; i<sa.length; i++) {
      String pathElement = sa[i];
      if (pathElement == null || "".equals(pathElement))
        continue;
      partialPath.append("/");
      partialPath.append(pathElement);
      log_.debug("checking for node " + pathElement + " at path " + partialPath);
      if(session.itemExists(partialPath.toString())) {
        currentNode = currentNode.getNode(pathElement);
      } else {
        log_.debug("Adding node " + pathElement);
        currentNode = currentNode.addNode(pathElement, "nt:unstructured");
      }
      currentNode.addMixin("mix:referenceable");
    }
    return currentNode;
  }
  protected InputStream convertToStream(Object object) {
    // TODO Handle Media Bundles and anything else this should support
    return (InputStream)object;
  }

  public Map<String, String> getMetadata(String path) {
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    }
    try {
      Node node = null;
      if (session.itemExists(path)) {
        node = (Node)session.getItem(path);
        Map<String, String> map = new HashMap<String, String>();
        PropertyIterator iter = node.getProperties();
        while(iter.hasNext()) {
          Property prop = iter.nextProperty();
          if( ! prop.getName().startsWith(PREFIX)) continue;
          map.put(prop.getName().substring(PREFIX.length()), prop.getString());
        }
        return map;
      } else {
        throw new RuntimeException("no object exists at path " + path);
      }
    } catch(PathNotFoundException e) {
      throw new RuntimeException(e);
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public void putMetadata(String value, String key, String path) {
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    }
    try {
      Node node = null;
      if (session.itemExists(path)) {
        node = (Node)session.getItem(path);
        node.setProperty(PREFIX + key, value);
        session.save();
      } else {
        throw new RuntimeException("no object exists at path " + path);
      }
    } catch(PathNotFoundException e) {
      throw new RuntimeException(e);
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
