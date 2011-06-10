/*
 * PageImporterCallback.java
 *
 * Copyright 2007 by Entwine
 * Zurich, Switzerland (CH)
 * All rights reserved.
 *
 * This software is confidential and proprietary information ("Confidential
 * Information").  You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into.
 */

package ch.entwine.weblounge.tools.importer;

import ch.entwine.weblounge.common.impl.url.PathUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class PageImporterCallback extends AbstractImporterCallback {

  /**
   * defines root partition; content inside this partition will be put into the
   * root
   */
  public static final String ROOT_PARTITION = "/home";

  /** The page id */
  static long pageId = 1L;

  /**
   * Creates a new callback for page imports.
   * 
   * @param src
   *          the source root directory
   * @param dest
   *          the destination root directory
   */
  PageImporterCallback(File src, File dest, Map<String, Collection<?>> clipboard) {
    super(src, dest);
  }

  /**
   * This method is called if a page file has been found.
   * 
   * @see ch.entwine.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
   */
  public boolean fileImported(File f) {
    try {
      if ("index.xml".equals(f.getName())) {
        Node root = getXmlRoot(f);
        String path = getPath(XPathHelper.valueOf(root, "/page/@partition"), XPathHelper.valueOf(root, "/page/@path"));
        File dest = new File(destDir, PathUtils.concat("pages", uuidToPath(ImporterState.getInstance().getUUID(path)), "0", "index.xml"));
        dest.getParentFile().mkdirs();
        dest.createNewFile();
        // File dest = createDestination(f);
        Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/convert-page.xsl"));
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsl);
        trans.setParameter("uuid", ImporterState.getInstance().getUUID(path));
        trans.setParameter("path", path);
        this.transformXml(f, dest, trans);

        System.out.println("Imported page " + path);
        return true;
      }
      return false;
    } catch (IOException e) {
      System.err.println("Error creating target file for " + f + ": " + e.getMessage());
      return false;
    } catch (ParserConfigurationException e) {
      System.err.println("Error parsing " + f + ": " + e.getMessage());
      return false;
    } catch (SAXException e) {
      System.err.println("Error creating sax parser for  " + f + ": " + e.getMessage());
      return false;
    } catch (TransformerConfigurationException e) {
      System.err.println("Error transforming " + f + ": " + e.getMessage());
      return false;
    }
  }

  private String getPath(String partition, String path) {
    String path_ = getOrigPath(partition, path);
    if (path_.startsWith(ROOT_PARTITION)) {
      path_ = path_.substring(ROOT_PARTITION.length(), path_.length());
    }
    if ("".equals(path_)) {
      path_ = "/";
    }
    return path_.toLowerCase();
  }

  private String getOrigPath(String partition, String path) {
    if (!partition.startsWith("/")) {
      partition = "/".concat(partition);
    }
    if (path.length() == 1) {
      path = "";
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.length() > 1 && !path.startsWith("/")) {
      path = "/".concat(path);
    }
    return partition.concat(path).toLowerCase();
  }

  public boolean folderImported(File f) throws Exception {
    return true;
  }

}