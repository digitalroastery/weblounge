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

package ch.o2it.weblounge.contentrepository.jcr;

import ch.o2it.weblounge.common.impl.url.PathSupport;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.StringValue;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionHistory;

/**
 * This class represents the default implementation of the
 * {@link PageRepository} and {@link ResourceRepository}, backed by the
 * <code>Jackrabbit content repository</code> implementation.
 */
public class ContentRepositoryService implements ManagedService {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(ContentRepositoryService.class);

  /** Path to the jcr repository */
  private final static String jcrConfigPath = "/jackrabbit/repository.xml";

  /** Name of the jcr home configuration parameter */
  public static final String OPT_JCR_HOME = "weblounge.jcr.home";

  /** Name of the jcr database driver configuration parameter */
  public static final String OPT_JCR_DB_DRIVER = "weblounge.jcr.db.driver";

  /** Name of the jcr database url configuration parameter */
  public static final String OPT_JCR_DB_URL = "weblounge.jcr.db.url";

  /** Name of the jcr database user configuration parameter */
  public static final String OPT_JCR_DB_USER = "weblounge.jcr.db.user";

  /** Name of the jcr database password configuration parameter */
  public static final String OPT_JCR_DB_PASSWORD = "weblounge.jcr.db.password";

  /** Weblounge node prefix */
  public static final String PREFIX = "wl:";

  /** Path to the jcr home directory */
  private String jcrHome = null;

  /** Path to the jcr home directory */
  private String jcrDbDriver = null;

  /** Url to the jcr persistence database */
  private String jcrDbUrl = null;

  /** Username for the jcr persistence database */
  private String jcrDbUsername = null;

  /** Password for the jcr persistence database */
  private String jcrDbPassword = null;

  /** Jackrabbit jcr repository instance */
  private JackrabbitRepository jcr = null;

  /**
   * Creates a new content repository service instance.
   */
  public ContentRepositoryService() {
    initToDefaults();
  }

  /**
   * Initializes the jackrabbit jcr settings to their default values.
   */
  protected void initToDefaults() {
    jcrHome = PathSupport.concat(new String[] {
        System.getProperty("java.io.tmpdir"),
        "weblounge",
        "jackrabbit" });
    jcrDbDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    jcrDbUrl = "jdbc:derby:${wsp.home}/db;create=true";
    jcrDbUsername = null;
    jcrDbPassword = null;
  }

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {

    // jcrHome
    if (properties.get(OPT_JCR_HOME) != null) {
      jcrHome = properties.get(OPT_JCR_HOME).toString();
      log_.info("Jackrabbit home is now {}", jcrHome);
    }

    // jcrDbDriver
    if (properties.get(OPT_JCR_DB_DRIVER) != null) {
      jcrDbDriver = properties.get(OPT_JCR_DB_DRIVER).toString();
      log_.info("Driver for jackrabbit persistence database is now {}", jcrDbDriver);
    }

    // jcrDbUrl
    if (properties.get(OPT_JCR_DB_URL) != null) {
      jcrDbUrl = properties.get(OPT_JCR_DB_URL).toString();
      log_.info("Url to jackrabbit persistence database is now {}", jcrDbUrl);
    }

    // jcrDbUsername
    if (properties.get(OPT_JCR_DB_USER) != null) {
      jcrDbUsername = properties.get(OPT_JCR_DB_USER).toString();
      log_.info("Username for jackrabbit persistence database is now {}", jcrDbUsername);
    }

    // jcrDbPassword
    if (properties.get(OPT_JCR_DB_PASSWORD) != null) {
      jcrDbPassword = properties.get(OPT_JCR_DB_PASSWORD).toString();
      log_.info("Password for jackrabbit persistence database is now {}", jcrDbPassword);
    }

  }

  /**
   * Callback from the OSGi environment to activate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    InputStream is = bundleContext.getBundle().getEntry(jcrConfigPath).openStream();
    RepositoryConfig jcrConfig = RepositoryConfig.create(is, jcrHome);
    jcr = RepositoryImpl.create(jcrConfig);

    // Test
    Session session = jcr.login(new SimpleCredentials("userid", "".toCharArray()), null);

    // Obtain the root node
    Node rn = session.getRootNode();

    // Create and add a new blog node. The node's type will be "blog".
    Node blogNode = rn.addNode("blog");
    blogNode.addMixin("mix:versionable");
    blogNode.setProperty("blogtitle", new StringValue("Chasing Jackrabbit article"));
    blogNode.setProperty("blogauthor", new StringValue("Joe Blogger"));
    blogNode.setProperty("blogdate", new DateValue(Calendar.getInstance()));
    blogNode.setProperty("blogtext", new StringValue("JCR is an interesting API to lo learn."));
    session.save();

    log_.debug("Blog node has been saved to jcr");

    // See if the node can be retrieved
    Workspace ws = session.getWorkspace();
    QueryManager qm = ws.getQueryManager();

    // Specify a query using the XPATH query language
    Query q = qm.createQuery("//blog[@blogauthor = 'Joe Blogger']", Query.XPATH);
    QueryResult res = q.execute();
    NodeIterator it = res.getNodes();
    while (it.hasNext()) {
      Node n = it.nextNode();
      Property prop = n.getProperty("blogtitle");
      log_.debug("Found blog entry with title: {}", prop.getString());

      VersionHistory history = n.getVersionHistory();
      if (history.getAllVersions().hasNext()) {
        log_.debug("Blog entry is versioned");
      }
    }
  }

  /**
   * Callback from the OSGi environment to deactivate the service.
   * <p>
   * This method is be configured in the <tt>Dynamic Services</tt> section of
   * the bundle.
   * 
   * @param context
   *          the component context
   */
  public void stop(ComponentContext context) {
    if (jcr != null) {
      jcr.shutdown();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.repository.api.OpencastRepository#getObject(java.lang.Class,
   *      java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <T> T getObject(Class<T> type, String path) {
    assertSupported(type);
    log_.debug("getting data from {}", path);
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    } else {
      Node node = null;
      try {
        node = (Node) session.getItem(path + "/jcr:content");
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
      session = jcr.login(new SimpleCredentials("foo", "bar".toCharArray()));
    } catch (LoginException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return session;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.repository.api.OpencastRepository#getSupportedTypes()
   */
  public Class<?>[] getSupportedTypes() {
    // TODO: Support MediaBundle
    return new Class<?>[] { InputStream.class };
  }

  /**
   * {@inheritDoc}
   * 
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
    for (int i = 0; i < supportedTypes.length; i++) {
      Class<?> currentClass = supportedTypes[i];
      if (currentClass.isAssignableFrom(type)) {
        objectClassSupported = true;
        break;
      }
    }
    if (!objectClassSupported) {
      throw new IllegalArgumentException("Class " + type + " is not a supported object type");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.repository.api.OpencastRepository#putObject(java.lang.Object,
   *      java.lang.String)
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
        fileNode = (Node) session.getItem(path);
      } else {
        fileNode = buildPath(session, path);
      }
      log_.debug("fileNode path={}", fileNode.getPath());
      Node resNode;
      try {
        resNode = fileNode.getNode("jcr:content");
        log_.debug("resource node exists: {}", resNode.getPath());
      } catch (PathNotFoundException e) {
        resNode = fileNode.addNode("jcr:content", "nt:resource");
        log_.debug("resource node created: {}", resNode.getPath());
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
   * 
   * @param root
   * @param path
   * @return The leaf nt:unstructured node
   */
  protected Node buildPath(Session session, String path) throws Exception {
    log_.debug("Building nodes for {}", path);
    StringBuilder partialPath = new StringBuilder();
    Node currentNode = session.getRootNode();
    String[] sa = path.split("/");
    for (int i = 0; i < sa.length; i++) {
      String pathElement = sa[i];
      if (pathElement == null || "".equals(pathElement))
        continue;
      partialPath.append("/");
      partialPath.append(pathElement);
      log_.debug("checking for node {} at path {}", pathElement, partialPath);
      if (session.itemExists(partialPath.toString())) {
        currentNode = currentNode.getNode(pathElement);
      } else {
        log_.debug("Adding node {}", pathElement);
        currentNode = currentNode.addNode(pathElement, "nt:unstructured");
      }
      currentNode.addMixin("mix:referenceable");
    }
    return currentNode;
  }

  protected InputStream convertToStream(Object object) {
    // TODO Handle Media Bundles and anything else this should support
    return (InputStream) object;
  }

  public Map<String, String> getMetadata(String path) {
    Session session = getSession();
    if (session == null) {
      throw new RuntimeException("Couldn't log in to the repo");
    }
    try {
      Node node = null;
      if (session.itemExists(path)) {
        node = (Node) session.getItem(path);
        Map<String, String> map = new HashMap<String, String>();
        PropertyIterator iter = node.getProperties();
        while (iter.hasNext()) {
          Property prop = iter.nextProperty();
          if (!prop.getName().startsWith(PREFIX))
            continue;
          map.put(prop.getName().substring(PREFIX.length()), prop.getString());
        }
        return map;
      } else {
        throw new RuntimeException("no object exists at path " + path);
      }
    } catch (PathNotFoundException e) {
      throw new RuntimeException(e);
    } catch (RepositoryException e) {
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
        node = (Node) session.getItem(path);
        node.setProperty(PREFIX + key, value);
        session.save();
      } else {
        throw new RuntimeException("no object exists at path " + path);
      }
    } catch (PathNotFoundException e) {
      throw new RuntimeException(e);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
