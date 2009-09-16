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

package ch.o2it.weblounge.common.impl.util.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to return all direct children of an xml node with a given local
 * tag name as a node list.
 * 
 * @author Tobias Wunden
 * @version Feb 4, 2003
 */

public class XMLChildrenNodeList implements NodeList {

  public XMLChildrenNodeList(Element root, String tagname) {
    nodes_ = new ArrayList<Node>();
    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      String localName = child.getLocalName();
      if (localName != null && localName.equals(tagname)) {
        nodes_.add(child);
      }
    }
  }

  /**
   * @see org.w3c.dom.NodeList#getLength()
   */
  public int getLength() {
    return nodes_.size();
  }

  /**
   * @see org.w3c.dom.NodeList#item(int)
   */
  public Node item(int index) {
    return nodes_.get(index);
  }

  private List<Node> nodes_;

}