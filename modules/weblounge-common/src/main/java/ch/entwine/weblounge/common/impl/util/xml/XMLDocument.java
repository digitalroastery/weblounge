/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;

/**
 * General class dealing with an xml document.
 */
public class XMLDocument {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(XMLDocument.class);

  /** The cache file */
  private File file = null;

  /** The xml document node */
  protected Document document = null;

  /** the XPath engine used by this document */
  protected XPath engine = XMLUtils.getXPath();

  /** Dirty flag for the document */
  protected boolean isDirty = false;

  /**
   * Creates a new <code>XMLDocument</code>. Calling <code>save</code> on a
   * document created this way will eventually result in an
   * <code>IllegalStateException</code> if you didn't provide a file name using
   * <code>setFileName</code>.
   * 
   */
  public XMLDocument() {
    file = null;
    isDirty = false;
  }

  /**
   * Creates a new <code>XMLDocument</code>. If the file exists, it is read in,
   * otherwise it is going to be created.
   * 
   * @param file
   *          the file
   */
  public XMLDocument(File file) {
    this.file = file;
    load();
    isDirty = false;
  }

  /**
   * Returns the file object that was used to read the xml document from. Note
   * that the returned object may be <code>null</code>.
   * 
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns the document which is needed to create new nodes in the DOM tree.
   * 
   * @return the document
   */
  public Document getDocument() {
    return document;
  }

  /**
   * Sets the file used to store this document.
   * 
   * @param file
   *          the file
   */
  public void setFile(File file) {
    this.file = file;
    isDirty = true;
  }

  /**
   * Returns <code>true</code> if the file is dirty and needs to be saved.
   * 
   * @return <code>true</code> if the file is dirty
   */
  public boolean isDirty() {
    return isDirty;
  }

  /**
   * Sets this file to dirty, which will lead to a save operation sooner or
   * later.
   */
  public void setDirty() {
    isDirty = true;
  }

  /**
   * Saves the document using the given output format.
   * 
   * @return <code>true</code> if the document could be stored
   */
  public boolean save() {
    if (!isDirty)
      return true;

    if (file == null) {
      throw new IllegalStateException("No filename specified!");
    }
    if (!file.exists() || !file.canRead()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        logger.error("Unable to create file {}", file);
        return false;
      }
    }
    try {
      FileOutputStream fos = new FileOutputStream(file);
      DOMSource domSource = new DOMSource(document);
      StreamResult streamResult = new StreamResult(fos);
      Transformer serializer = XMLUtils.getTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      // serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.transform(domSource, streamResult);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      logger.error("File {} was not found!", file);
      return false;
    } catch (IOException e) {
      logger.error("Error when serializing xml document to {}", file);
      return false;
    } catch (TransformerConfigurationException e) {
      logger.error("Transformer configuration error when serializing xml document to {}", file);
      return false;
    } catch (TransformerException e) {
      logger.error("Error when serializing xml document to {}", file);
      return false;
    }
    isDirty = false;
    return true;
  }

  /**
   * Returns the first entry that matches the xpath query or <code>null</code>
   * if the query didn't return a result.
   * 
   * @param xpath
   *          the xpath query
   * @return the first resulting node or <code>null</code>
   */
  public Node getNode(String xpath) {
    return XPathHelper.select(document, xpath, engine);
  }

  /**
   * Returns all entries that match the xpath query or <code>null</code> if the
   * query didn't return any result.
   * 
   * @param xpath
   *          the xpath query
   * @return the resulting nodelist or <code>null</code>
   */
  public NodeList getNodes(String xpath) {
    return XPathHelper.selectList(document, xpath, engine);
  }

  /**
   * Removes the nodes that satisfy the xpath query.
   * 
   * @param xpath
   *          the xpath query
   * @return the number of elements removed
   */
  public int removeNode(String xpath) {
    return removeNode(XPathHelper.select(document, xpath, engine));
  }

  /**
   * Removes the node.
   * 
   * @param node
   *          the node to remove
   * @return the number of elements removed
   */
  public int removeNode(Node node) {
    if (node == null)
      return 0;
    Node parent = node.getParentNode();
    if (parent == null)
      return 0;
    Node removed = parent.removeChild(node);
    int count = 0;
    if (removed != null) {
      isDirty = true;
      count++;
    }
    return count;
  }

  /**
   * Removes all nodes that satisfy the xpath query.
   * 
   * @param xpath
   *          the xpath query
   * @return the number of elements removed
   */
  public int removeNodes(String xpath) {
    return removeNodes(XPathHelper.selectList(document, xpath, engine));
  }

  /**
   * Removes all nodes in the node list.
   * 
   * @param nodes
   *          the list of nodes to remove
   * @return the number of elements removed
   */
  public int removeNodes(NodeList nodes) {
    int count = 0;
    if (nodes != null) {
      for (int i = 0; i < nodes.getLength(); i++) {
        count += removeNode(nodes.item(i));
      }
    }
    if (count > 0)
      isDirty = true;
    return count;
  }

  /**
   * Adds the node to the parent node.
   * 
   * @param node
   *          the node to add
   * @param parent
   *          the parent node
   */
  public Node addNode(Node node, Node parent) {
    if (node == null)
      throw new IllegalArgumentException("Node cannot be null");
    if (parent == null)
      throw new IllegalArgumentException("parent cannot be null");
    isDirty = true;
    return parent.appendChild(node);
  }

  /**
   * Adds the node to the parent node specified by the xpath expression and
   * return the node if the operation was successful, <code>null</code>
   * otherwise.
   * 
   * @param node
   *          the node to add
   * @param xpath
   *          query leading to the new parent node
   * @return the node that has been added
   */
  public Node addNode(Node node, String xpath) {
    Node parent = getNode(xpath);
    if (parent != null) {
      parent.appendChild(node);
      isDirty = true;
      return node;
    }
    return null;
  }

  /**
   * Adds the node to the parent node.
   * 
   * @param nodes
   *          the nodes to add
   * @param parent
   *          the parent node
   */
  public int addNodes(NodeList nodes, Node parent) {
    int count = 0;
    for (int i = 0; i < nodes.getLength(); i++) {
      if (parent.appendChild(nodes.item(i)) != null) {
        count++;
      }
    }
    if (count > 0)
      isDirty = true;
    return count;
  }

  /**
   * Adds the node to the parent node specified by the xpath expression and
   * return the node if the operation was successful, <code>null</code>
   * otherwise.
   * 
   * @param nodes
   *          the nodes to add
   * @param xpath
   *          query leading to the new parent node
   * @return the node that has been added
   */
  public int addNodes(NodeList nodes, String xpath) {
    int count = 0;
    Node parent = getNode(xpath);
    if (parent != null) {
      count = addNodes(nodes, parent);
    }
    if (count > 0)
      isDirty = true;
    return count;
  }

  /**
   * Loads the cache information from the file <code>CACHE_FILE</code> in the
   * sites work directory.
   */
  private void load() {
    DocumentBuilder docBuilder;
    try {
      docBuilder = XMLUtils.getDocumentBuilder();
      if (!file.exists() || !file.canRead()) {
        document = docBuilder.newDocument();
      } else {
        try {
          document = docBuilder.parse(file);
        } catch (IOException e) {
          logger.error("Unable to create file {}", file);
          document = docBuilder.newDocument();
        } catch (SAXException e) {
          logger.error("SAX error when parsing file {}", file);
          document = docBuilder.newDocument();
        }
      }
    } catch (ParserConfigurationException e) {
      logger.error("Unable to create XML document builder! Check your xml settings!");
    }
    isDirty = false;
  }

}