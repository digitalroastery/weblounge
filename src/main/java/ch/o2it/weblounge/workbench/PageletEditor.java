/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.workbench;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.json.JSONException;
import org.json.XML;

import java.net.URL;

/**
 * Class containing all the information that is needed to edit a certain
 * pagelet.
 */
public class PageletEditor {

  /** Possible flavors for the pagelet editor's data part */
  public enum DataFlavor {
    Xml, Json
  };

  /** The pagelet */
  protected Pagelet pagelet = null;

  /** The page's resource uri */
  protected ResourceURI uri = null;

  /** The pagelet's composer identifier */
  protected String composerId = null;

  /** The pagelet's position within the composer */
  protected int pageletIndex = -1;

  /** The data flavor */
  protected DataFlavor dataFlavor = DataFlavor.Json;

  /** The pagelet renderer */
  protected PageletRenderer renderer = null;

  /** The pagelet's renderer content */
  protected String rendererContents = null;

  /** The pagelet's editor content */
  protected String editorContents = null;

  /**
   * Creates a new pagelet editor for the given pagelet.
   * 
   * @param pagelet
   *          the pagelet
   */
  public PageletEditor(Pagelet pagelet, ResourceURI uri, String composer,
      int pageletIndex) {
    if (pagelet == null)
      throw new IllegalArgumentException("Pagelet cannot be null");
    if (uri == null)
      throw new IllegalArgumentException("Page uri cannot be null");
    if (composer == null)
      throw new IllegalArgumentException("Composer cannot be null");
    if (pageletIndex < 0)
      throw new IllegalArgumentException("Pagelet index must be a positive integer");

    this.pagelet = pagelet;
    this.uri = uri;
    this.composerId = composer;
    this.pageletIndex = pageletIndex;

    Site site = uri.getSite();
    Module module = site.getModule(pagelet.getModule());
    if (module != null) {
      renderer = module.getRenderer(pagelet.getIdentifier());
    }
  }

  /**
   * Returns the pagelet.
   * 
   * @return the pagelet
   */
  public Pagelet getPagelet() {
    return pagelet;
  }

  /**
   * Returns the resource uri.
   * 
   * @return the uri
   */
  public ResourceURI getURI() {
    return uri;
  }

  /**
   * Returns the composer identifier.
   * 
   * @return the composer
   */
  public String getComposer() {
    return composerId;
  }

  /**
   * Returns the (zero-based) pagelet's index inside the composer.
   * 
   * @return the pagelet's position
   */
  public int getPageletIndex() {
    return pageletIndex;
  }

  /**
   * Sets the flavor that the pagelet data is returned in.
   * 
   * @param flavor
   *          the flavor
   */
  public void setDataFlavor(DataFlavor flavor) {
    this.dataFlavor = flavor;
  }

  /**
   * Returns the flavor that the pagelet data is returned in. By default,
   * {@link DataFlavor#Json} is used.
   * 
   * @return the pagelet data flavor
   */
  public DataFlavor getDataFlavor() {
    return dataFlavor;
  }

  /**
   * Returns <code>true</code> if the pagelet has an renderer associated.
   * 
   * @return <code>true</code> if there is a renderer
   */
  public boolean hasRenderer() {
    return renderer != null && renderer.getRenderer() != null;
  }

  /**
   * Returns the URL to the pagelet's renderer.
   * 
   * @return the renderer URL
   */
  public URL getRenderer() {
    return renderer != null ? renderer.getRenderer() : null;
  }

  /**
   * Returns <code>true</code> if the pagelet has an editor associated.
   * 
   * @return <code>true</code> if there is an editor
   */
  public boolean hasEditor() {
    return renderer != null && renderer.getEditor() != null;
  }

  /**
   * Returns the URL to the editor or <code>null</code> if no editor is
   * available.
   * 
   * @return the url to the editor
   */
  public URL getEditorURL() {
    return renderer != null ? renderer.getEditor() : null;
  }

  /**
   * Sets the renderer contents.
   * 
   * @param contents
   *          the renderer contents
   */
  public void setRenderer(String contents) {
    rendererContents = contents;
  }

  /**
   * Sets the renderer contents.
   * 
   * @param contents
   *          the renderer contents
   */
  public void setEditor(String contents) {
    editorContents = contents;
  }

  /**
   * Returns the <code>XML</code> representation of this pagelet.
   * 
   * @return the pagelet
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();

    // head
    buf.append("<pageleteditor ");
    buf.append("uri=\"").append(uri.getIdentifier()).append("\" ");
    buf.append("composer=\"").append(composerId).append("\" ");
    buf.append("index=\"").append(pageletIndex).append("\">");

    // the pagelet
    buf.append("<data flavor=\"json\"><![CDATA[");
    String data = null;
    switch (dataFlavor) {
      case Json:
        try {
          data = XML.toJSONObject(pagelet.toXml()).toString();
        } catch (JSONException e) {
          throw new IllegalStateException("Pagelet xml can't be converted to json: " + e.getMessage(), e);
        }
        break;
      case Xml:
        data = pagelet.toXml();
        break;
      default:
        throw new IllegalStateException("An unhandled flavor was found: " + dataFlavor);
    }
    buf.append(data);
    buf.append("]]></data>");

    // the renderer
    if (rendererContents != null) {
      buf.append("<renderer type=\"xhtml\"><![CDATA[");
      buf.append(rendererContents);
      buf.append("<renderer>");
      buf.append("]]></renderer>");
    }

    // the editor
    if (editorContents != null) {
      buf.append("<editor type=\"xhtml\"><![CDATA[");
      buf.append(editorContents);
      buf.append("<editor>");
      buf.append("]]></editor>");
    }

    buf.append("</pageleteditor>");

    return buf.toString();
  }

}
