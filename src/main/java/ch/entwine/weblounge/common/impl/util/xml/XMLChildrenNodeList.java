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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to return all direct children of an xml node with a given local
 * tag name as a node list.
 */
public class XMLChildrenNodeList implements NodeList {

  /** The nodes */
  private List<Node> nodes = null;

  public XMLChildrenNodeList(Element root, String tagname) {
    nodes = new ArrayList<Node>();
    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      String localName = child.getLocalName();
      if (localName != null && localName.equals(tagname)) {
        nodes.add(child);
      }
    }
  }

  /**
   * @see org.w3c.dom.NodeList#getLength()
   */
  public int getLength() {
    return nodes.size();
  }

  /**
   * @see org.w3c.dom.NodeList#item(int)
   */
  public Node item(int index) {
    return nodes.get(index);
  }

}