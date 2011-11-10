/*
y * PageImporterCallback.java
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

import ch.entwine.weblounge.common.impl.util.xml.XMLDocument;

import org.apache.commons.io.FilenameUtils;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class ResourceImporterCallback extends AbstractImporterCallback {

  /**
   * Creates a new callback for page imports.
   * 
   * @param src
   *          the source root directory
   * @param dest
   *          the destination root directory
   */
  ResourceImporterCallback(File src, File dest,
      Map<String, Collection<?>> clipboard) {
    super(src, dest);
  }

  /**
   * This method is called if a resource file has been found.
   * 
   * @see ch.entwine.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
   */
  public boolean fileImported(File f) {
    // load collection information
    File collXml = new File(FilenameUtils.getFullPath(f.getPath()) + "repository.xml");
    if (!collXml.exists()) {
      System.err.println("No repository information (repository.xml) found for file: " + f.getName());
      return false;
    }

    // path relative to the src directory
    String relpath = f.getPath().replaceAll(this.srcDir.getAbsolutePath(), "");
    
    // Check if collXml contains information for this file
    XMLDocument repoXml = new XMLDocument(collXml);
    if (repoXml.getNode("/collection/entry[@id='" + relpath + "']") == null)
      return false;
    
    UUID uuid = UUID.randomUUID();
    File resourceXml = null;
    ImageInfo imageInfo = null;
    try {
      // create temporary resource descriptor file
      resourceXml = File.createTempFile(uuid.toString(), ".xml");

      // try loading file as image
      imageInfo = Sanselan.getImageInfo(f);
    } catch (IOException e) {
      System.err.println("Error processing file '" + relpath + "': " + e.getMessage());
      return false;
    } catch (ImageReadException e) {
      // file is not an image
    }

    ImporterState.getInstance().putUUID(relpath, uuid);

    String filename = "de.".concat(FilenameUtils.getExtension(f.getName()));

    // check, if file is an image resource
    String subfolder = null;
    if (imageInfo != null) {
      subfolder = "images";
      Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/convert-image.xsl"));
      // set the TransformFactory to use the Saxon TransformerFactoryImpl method
      System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer trans;
      try {
        trans = transFact.newTransformer(xsl);
        trans.setParameter("fileid", relpath);
        trans.setParameter("uuid", uuid.toString());
        trans.setParameter("path", relpath);
        trans.setParameter("imgwidth", imageInfo.getWidth());
        trans.setParameter("imgheight", imageInfo.getHeight());
        this.transformXml(collXml, resourceXml, trans);
      } catch (TransformerConfigurationException e) {
        System.err.println(e.getMessage());
        return false;
      }
    } else {
      subfolder = "files";
      Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/convert-file.xsl"));
      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer trans;
      try {
        trans = transFact.newTransformer(xsl);
        trans.setParameter("fileid", relpath);
        trans.setParameter("uuid", uuid.toString());
        trans.setParameter("path", relpath);
        this.transformXml(collXml, resourceXml, trans);
      } catch (TransformerConfigurationException e) {
        System.err.println(e.getMessage());
        return false;
      }
    }

    this.storeFile(f, destDir, subfolder, filename, uuid, 0);
    this.storeFile(resourceXml, destDir, subfolder, "index.xml", uuid, 0);
    System.out.println("Imported resource " + f.getName());
    return true;
  }

  public boolean folderImported(File f) throws Exception {
    return true;
  }

}