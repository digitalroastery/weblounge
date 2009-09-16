/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The <code>UrlManager</code> is used to handle the creation and removal of new
 * partitions and urls.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since WebLounge 1.0
 */

public class UrlManager {

  // /** Query to load the pages containing a pagelet of a given type */
  // private static XQuery getUrlsForPageletQuery_;
  //
  // /** Query to load the pages containing a pagelet of a given type and a
  // property value */
  // private static XQuery getUrlsForPageletAndPropertyQuery_;
  //
  // /** Query to load the pages containing a pagelet of a given type and a text
  // value */
  // private static XQuery getUrlsForPageletAndTextQuery_;
  //
  // /** Query to load the pages rendered by a given template */
  // private static XQuery getUrlsForTemplateQuery_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = UrlManager.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  static {
    // TODO: Implement using jcr repository
    String src = null;

    // src = "/ch/o2it/weblounge/core/url/xq/GetUrlsForPagelet.xq";
    // getUrlsForPageletQuery_ = XQuery.load(UrlManager.class, src);
    //
    // src = "/ch/o2it/weblounge/core/url/xq/GetUrlsForPageletAndProperty.xq";
    // getUrlsForPageletAndPropertyQuery_ = XQuery.load(UrlManager.class, src);
    //
    // src = "/ch/o2it/weblounge/core/url/xq/GetUrlsForPageletAndText.xq";
    // getUrlsForPageletAndTextQuery_ = XQuery.load(UrlManager.class, src);
    //
    // src = "/ch/o2it/weblounge/core/url/xq/GetUrlsForTemplate.xq";
    // getUrlsForTemplateQuery_ = XQuery.load(UrlManager.class, src);
  }

  /**
   * Checks whether the given path exists in the database. <code>path</code> is
   * relative to the site root collection, so it starts with the partition name,
   * followed by the path within the partition.
   * 
   * @param path
   *          the database path relative to the site root folder
   * @return <code>true</code> if the path exists
   */
  public static boolean exists(Site site, String path) {
    // TODO: Implement using jcr repository
    // DBXMLDatabase db =
    // (DBXMLDatabase)ServiceManager.getEnabledSystemService(DBXMLDatabase.ID);
    // if (db != null) {
    // Collection c = null;
    // try {
    // c = site.getCollection(UrlSupport.concat("/site", path), false);
    // return (c != null);
    // } finally {
    // site.returnCollection(c);
    // }
    // }
    return false;
  }

  /**
   * Creates the page with the given name.
   * 
   * @param path
   *          the partition name
   */
  public static void createPage(Site site, String path) throws Exception {
    // TODO: Implement using jcr repository
    // long id = ((SiteImpl)site).createPageId();
    // PageURI uri = new PageURIImpl(site, id, Page.LIVE);
    // User user = site.getAdministrator();
    // Language language = site.getDefaultLanguage();
    // String renderer = site.getDefaultTemplate();
    // String type = "default";
    // String title = site.getDescription(language);
    // String[] keywords = new String[] {};
    // try {
    // if (site.getPages().create(uri, user, language, renderer, title, type,
    // keywords) != null) {
    // log_.info("Partition '" + path + "' created");
    // } else {
    // log_.debug("Partition '" + path + "' existst");
    // }
    // } catch (Exception e) {
    // String msg = "Unable to create default page for partition '" + path +
    // "'!";
    // log_.warn(msg);
    // throw e;
    // }
  }

  /**
   * Returns the urls rendered using the given template.
   * 
   * @param site
   *          the site context
   * @param template
   *          the template to look up
   * @return the urls
   */
  public static PageURI[] getUrls(Site site, Renderer template) {
    // TODO: Implement using jcr repository
    // try {
    // ResourceSet result = getUrlsForTemplateQuery_.execute(new Object[][] {
    // { "collection", DBXMLCollectionSupport.getDatabaseURI(site) },
    // { "template", template.getIdentifier() },
    // { "version", "live" }
    // });
    // UrlReader urlreader = new UrlReader();
    // PageURI[] urls = urlreader.read((XMLResource)result.getResource(0),
    // site);
    // log_.debug("Found " + urls.length + " documents in " +
    // getUrlsForTemplateQuery_.getExecutionTime() + " ms");
    // return urls;
    // } catch (Exception e) {
    // log_.warn("Unable to load urls for template '" + template + "'!: " +
    // e.getMessage() + "\rQuery:\r" + getUrlsForTemplateQuery_.getQuery());
    // log_.warn("Database error:", e);
    // return new PageURI[] {};
    // }
    return null;
  }

  /**
   * Returns the urls containing pagelets of the given renderer.
   * 
   * @param site
   *          the site context
   * @param renderer
   *          the renderer to look up
   * @return the urls
   */
  public static PageURI[] getUrls(Site site, String renderer, String module) {
    // TODO: Implement using jcr repository
    // try {
    // ResourceSet result = getUrlsForPageletQuery_.execute(new Object[][] {
    // { "collection", DBXMLCollectionSupport.getDatabaseURI(site) },
    // { "module", module },
    // { "renderer", renderer },
    // { "version", "live" }
    // });
    // UrlReader urlreader = new UrlReader();
    // PageURI[] urls = urlreader.read((XMLResource)result.getResource(0),
    // site);
    // log_.debug("Found " + urls.length + " documents in " +
    // getUrlsForPageletQuery_.getExecutionTime() + " ms");
    // return urls;
    // } catch (Exception e) {
    // log_.warn("Unable to load urls for pagelet '" + renderer + "'!: " +
    // e.getMessage() + "\rQuery:\r" + getUrlsForPageletQuery_.getQuery());
    // log_.warn("Database error:", e);
    // return new PageURI[] {};
    // }
    return null;
  }

  /**
   * Returns the urls containing pagelets of the given renderer containing the
   * specified property value <code>property</code>.
   * 
   * @param site
   *          the site context
   * @param renderer
   *          the renderer to look up
   * @param module
   *          the renderer's module
   * @param property
   *          the property value to match
   * @return the urls
   */
  public static PageURI[] getUrls(Site site, String renderer, String module,
      String[][] properties) {
    // TODO: Implement using jcr repository
    // try {
    // ResourceSet result = getUrlsForPageletAndPropertyQuery_.execute(new
    // Object[][] {
    // { "collection", DBXMLCollectionSupport.getDatabaseURI(site) },
    // { "module", module },
    // { "renderer", renderer },
    // { "version", "live" },
    // { "properties", propertiesToXml(properties) }
    // });
    // UrlReader urlreader = new UrlReader();
    // PageURI[] urls = urlreader.read((XMLResource)result.getResource(0),
    // site);
    // log_.debug("Found " + urls.length + " documents in " +
    // getUrlsForPageletAndPropertyQuery_.getExecutionTime() + " ms");
    // return urls;
    // } catch (Exception e) {
    // log_.warn("Unable to load urls for pagelet '" + renderer + "'!: " +
    // e.getMessage() + "\rQuery:\r" + getUrlsForPageletQuery_.getQuery());
    // log_.warn("Database error:", e);
    // return new PageURI[] {};
    // }
    return null;
  }

  /**
   * Returns the urls containing pagelets of the given renderer containing the
   * specified property value <code>property</code>.
   * 
   * @param site
   *          the site context
   * @param renderer
   *          the renderer to look up
   * @param module
   *          the renderer's module
   * @param property
   *          the property value to match
   * @return the urls
   */
  public static PageURI[] getUrls(Site site, Renderer renderer, String module,
      String text, Language language) {
    // TODO: Implement using jcr repository
    // try {
    // ResourceSet result = getUrlsForPageletAndTextQuery_.execute(new
    // Object[][] {
    // { "collection", DBXMLCollectionSupport.getDatabaseURI(site) },
    // { "module", module },
    // { "renderer", renderer.getIdentifier() },
    // { "version", "live" },
    // { "text", text },
    // { "language", language.getIdentifier() }
    // });
    // UrlReader urlreader = new UrlReader();
    // PageURI[] urls = urlreader.read((XMLResource)result.getResource(0),
    // site);
    // log_.debug("Found " + urls.length + " documents in " +
    // getUrlsForPageletAndTextQuery_.getExecutionTime() + " ms");
    // return urls;
    // } catch (Exception e) {
    // log_.warn("Unable to load urls for pagelet '" + renderer + "'!: " +
    // e.getMessage() + "\rQuery:\r" + getUrlsForPageletQuery_.getQuery());
    // log_.warn("Database error:", e);
    // return new PageURI[] {};
    // }
    return null;
  }

  /**
   * Returns an xml representation of the properties.
   * 
   * @param properties
   *          the properties in name - value pairs
   * @return the xml representation of these properties
   * @throws ParserConfigurationException
   */
  private static Node propertiesToXml(String[][] properties)
      throws ParserConfigurationException {
    DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder();
    Document doc = docBuilder.newDocument();
    Element root = doc.createElement("properties");
    doc.appendChild(root);
    for (int i = 0; i < properties.length; i++) {
      Element property = doc.createElement("property");
      property.setAttribute("name", properties[i][0]);
      property.setNodeValue(properties[i][1]);
      root.appendChild(property);
    }
    return root;
  }

}