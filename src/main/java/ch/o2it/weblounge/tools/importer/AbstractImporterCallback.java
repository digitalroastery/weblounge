/*
 * AbstractImporterCallback.java
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

import ch.o2it.weblounge.common.impl.url.PathUtils;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public abstract class AbstractImporterCallback implements ImporterCallback {

  /** site source directory */
  protected File srcDir = null;
  
  /** destination directory */
  protected File destDir = null;
  
  

  AbstractImporterCallback(File src, File dest) {
    this.srcDir = src;
    this.destDir = dest;
  }

  public abstract boolean fileImported(File f) throws Exception;


  /**
   * Takes the file argument, strips off the src directory prefix and creates
   * the remainder under the destination path.
   * 
   * @param f
   *          the file
   * @return the destination file
   */
  protected File createDestination(File f) throws IOException {
    String path = f.getAbsolutePath();
    path = path.substring(srcDir.getAbsolutePath().length());
    File dest = new File(destDir, path);
    if (f.isDirectory())
      return (dest.exists() || dest.mkdirs()) ? dest : null;
    else {
      if (dest.exists())
        dest.delete();
      return dest.createNewFile() ? dest : null;
    }
  }

  /**
   * Copies the contents of file <code>src</code> to <code>dest</code>.
   * 
   * @param f
   *          the file
   * @return the destination file
   */
  protected void copy(File src, File dest) throws IOException {
    FileInputStream is = null;
    FileOutputStream os = null;
    try {
      is = new FileInputStream(src);
      os = new FileOutputStream(dest);
      byte buf[] = new byte[1024];
      int i;
      while ((i = is.read(buf)) != -1)
        os.write(buf, 0, i);
      os.flush();
    } catch (IOException e) {
      System.err.println("Unable to copy " + src.getPath() + " to " + dest.getPath() + ": " + e.getMessage());
      dest.delete();
    } finally {
      if (is != null)
        try {
          is.close();
        } catch (IOException e) { /* ignore */
        }
      if (os != null)
        try {
          os.close();
        } catch (IOException e) { /* ignore */
        }
    }
  }

  /**
   * Parses file <code>f</code> and returns the document root node.
   * 
   * @param f
   *          the file to parse
   * @return the root node
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  protected Node getXmlRoot(File f) throws ParserConfigurationException,
      SAXException, IOException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    return docBuilder.parse(f);
  }

  protected boolean transformXml(File srcFile, File resultFile,
      Transformer trans) {
    Source src = new StreamSource(srcFile);
    Result result = new StreamResult(resultFile);

    try {
      trans.transform(src, result);
    } catch (TransformerException e) {
      System.err.println("Error transforming file '" + srcFile.getPath() + "': " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * 
   * @param file
   * @param dest
   * @param folder
   * @param filename
   * @param uuid
   * @param version
   * @return
   */
  protected boolean storeFile(File file, File dest, String folder, String filename, UUID uuid,
      int version) {
    String path = PathUtils.concat(dest.getAbsolutePath(), folder, uuidToPath(uuid.toString()), String.valueOf(version), filename);
    File newfile = new File(path);
    if (!newfile.getParentFile().exists()) {
      newfile.getParentFile().mkdirs();
    }
    try {
      FileUtils.copyFile(file, newfile);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Returns the identifier of the form <code>x-y-z-u-v</code> as a path as in
   * <code>/&lt;int&gt;/&lt;int&gt;/id</code>, with the "/" being the platform's
   * file separator.
   * 
   * @param uuid
   *          the universal unique identifier
   * @return the path
   */
  protected String uuidToPath(String uuid) {
    if (uuid == null)
      throw new IllegalArgumentException("Identifier must not be null");
    String[] elements = uuid.split("-");
    StringBuffer path = new StringBuffer();

    // convert first part of uuid to long and apply modulo 100
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[0], 16) % 100));

    // convert second part of uuid to long and apply modulo 10
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[1], 16) % 10));

    // append the full uuid as the actual directory
    path.append(File.separatorChar);
    path.append(uuid);

    return path.toString();
  }

}