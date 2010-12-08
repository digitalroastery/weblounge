/*
 * PageImporterCallback.java
 *
 * Copyright 2007 by O2 IT Engineering
 * Zurich, Switzerland (CH)
 * All rights reserved.
 *
 * This software is confidential and proprietary information ("Confidential
 * Information").  You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into.
 */

package ch.o2it.weblounge.tools.importer;

import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class ResourceImporterCallback extends AbstractImporterCallback {

	/**
	 * Creates a new callback for page imports.
	 * 
	 * @param src the source root directory
	 * @param dest the destination root directory
	 */
	ResourceImporterCallback(String src, String dest, Map<String,Collection<?>> clipboard) {
		super(src, dest);
	}

	/**
	 * This method is called if a resource file has been found.
	 * 
	 * @see ch.o2it.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
	 */
	public boolean fileImported(File f) {
	  // load collection information
	  File collXml = new File(FilenameUtils.getFullPath(f.getPath()) + "repository.xml");
	  
	  // paht relative to the src directory
	  String relpath = f.getPath().replaceAll(this.srcDir, "");
	  	  
	  UUID uuid = UUID.randomUUID();
    File resourceXml = null;
    BufferedImage img = null;
    try {
      // try loading file as image
      img = ImageIO.read(f);
      // create temporary resource descriptor file
      resourceXml = File.createTempFile(uuid.toString(), ".xml");
    } catch (IOException e) {
      System.err.println("Error processing file '" + relpath + "/" + f.getName() + "': " + e.getMessage());
      return false;
    }

    // log_.info("Processing file " + file.getName());
    String relfilepath = relpath.concat("/").concat(f.getName());
    String path = ImporterUtils.normalizePath(relfilepath);
    ImporterState.getInstance().putUUID(relfilepath, uuid);

    // concat filename ('de' + original's file extension, ex 'de.pdf')
    String filename = "de.".concat(FilenameUtils.getExtension(f.getName()));

    // check, if file is an image resource
    String subfolder = null;
    if (img != null) {
      subfolder = "images";
      Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/convert-image.xsl"));
      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer trans;
      try {
        trans = transFact.newTransformer(xsl);
        trans.setParameter("fileid", relfilepath);
        trans.setParameter("uuid", uuid.toString());
        trans.setParameter("path", path);
        trans.setParameter("imgwidth", img.getWidth());
        trans.setParameter("imgheight", img.getHeight());
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
        trans.setParameter("fileid", relfilepath);
        trans.setParameter("uuid", uuid.toString());
        trans.setParameter("path", path);
        this.transformXml(collXml, resourceXml, trans);
      } catch (TransformerConfigurationException e) {
        System.err.println(e.getMessage());
        return false;
      }
    }

    File destfolder = new File(this.destDir);
    this.storeFile(f, destfolder, subfolder, filename, uuid, 0);
    this.storeFile(resourceXml, destfolder, subfolder, "index.xml", uuid, 0);
    System.out.println("Imported resource " + f.getName());
		return true;
	}

  public boolean folderImported(File f) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

}