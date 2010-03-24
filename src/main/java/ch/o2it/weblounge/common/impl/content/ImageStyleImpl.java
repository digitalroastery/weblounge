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

package ch.o2it.weblounge.common.impl.content;

import static ch.o2it.weblounge.common.site.ScalingMode.Box;
import static ch.o2it.weblounge.common.site.ScalingMode.Cover;
import static ch.o2it.weblounge.common.site.ScalingMode.Fill;
import static ch.o2it.weblounge.common.site.ScalingMode.Height;
import static ch.o2it.weblounge.common.site.ScalingMode.None;
import static ch.o2it.weblounge.common.site.ScalingMode.Width;

import ch.o2it.weblounge.common.content.ImageStyle;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.ScalingMode;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * The <code>ImageStyleImpl</code> class defines a presentation style for images
 * of a certain size as well as the method to get from the original image
 * version to scaled ones.
 */
public class ImageStyleImpl extends GeneralComposeable implements ImageStyle {

  /** the image width */
  protected int width = -1;

  /** the image height */
  protected int height = -1;

  /** the scaling mode */
  protected ScalingMode scalingMode = null;

  /**
   * Creates a new image style with the name as its identifier, width and
   * height, the indicated scaling behavior. If <code>composeable</code> is set
   * to <code>true</code>, the image style will be available to the user when it
   * comes to adding scaled images to a page.
   * 
   * @param id
   *          the style identifier
   * @param width
   *          the image width
   * @param height
   *          the image height
   * @param scaling
   *          the scaling mode
   * @param composeable
   *          <code>true</code> if the image is composeable
   */
  public ImageStyleImpl(String id, int width, int height, ScalingMode scaling,
      boolean composeable) {
    this.identifier = id;
    this.width = width;
    this.height = height;
    this.scalingMode = scaling;
  }

  /**
   * Creates a new composeable image style with width, height and a default
   * scaling mode of {@link ScalingMode#None}.
   * 
   * @param id
   *          the style identifier
   * @param width
   *          the image width
   * @param height
   *          the image height
   */
  public ImageStyleImpl(String id, int width, int height) {
    this(id, width, height, None, true);
  }

  /**
   * Creates a new composeable image style.
   */
  public ImageStyleImpl() {
    this(null, -1, -1, None, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#setScalingMode(ch.o2it.weblounge.common.site.ScalingMode)
   */
  public void setScalingMode(ScalingMode mode) {
    this.scalingMode = mode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#getScalingMode()
   */
  public ScalingMode getScalingMode() {
    return scalingMode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#setHeight(int)
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Returns the image height.
   * 
   * @return the image height
   */
  public int getHeight() {
    return height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#setWidth(int)
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.GeneralComposeable#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.GeneralComposeable#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    // This is to indicate that using the super implementation is sufficient
    return super.equals(o);
  }

  /**
   * Initializes this image style from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param node
   *          the image style node
   * @throws IllegalStateException
   *           if the image style cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static ImageStyleImpl fromXml(Node node) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(node, xpath);
  }

  /**
   * Initializes this image style from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param node
   *          the image style node
   * @param xpath
   *          the xpath processor
   * @throws IllegalStateException
   *           if the image style cannot be parsed
   * @see #fromXml(Node)
   * @see #toXml()
   */
  public static ImageStyleImpl fromXml(Node node, XPath xpath)
      throws IllegalStateException {

    // Identifier
    String id = XPathHelper.valueOf(node, "@id", xpath);
    if (id == null)
      throw new IllegalStateException("Missing id in image style definition");

    // Width
    int width = -1;
    try {
      if (XPathHelper.valueOf(node, "width", xpath) != null)
        width = Integer.parseInt(XPathHelper.valueOf(node, "width", xpath));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Missing width in image style definition");
    }

    // Height
    int height = -1;
    try {
      if (XPathHelper.valueOf(node, "height", xpath) != null)
        height = Integer.parseInt(XPathHelper.valueOf(node, "height", xpath));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Missing height in image style definition");
    }

    // Composeable
    boolean composeable = "true".equals(XPathHelper.valueOf(node, "@composeable", xpath));

    // Scaling mode
    String mode = XPathHelper.valueOf(node, "scalingmode", xpath);
    ScalingMode scalingMode = mode == null ? None : ScalingMode.parseString(mode);

    if (Width.equals(scalingMode) && width <= 0)
      throw new IllegalStateException("Width scaling needs positive width");
    if (Height.equals(scalingMode) && height <= 0)
      throw new IllegalStateException("Height scaling needs positive height");
    if (Box.equals(scalingMode) && (height <= 0 || width <= 0))
      throw new IllegalStateException("Box scaling needs positive width and height");
    if (Cover.equals(scalingMode) && (width <= 0 || height <= 0))
      throw new IllegalStateException("Cover scaling needs positive width and height");
    if (Fill.equals(scalingMode) && (width <= 0 || height <= 0))
      throw new IllegalStateException("Fill scaling needs positive width and height");

    // Create the image style
    ImageStyleImpl imageStyle = new ImageStyleImpl(id, width, height, scalingMode, composeable);

    // Names
    LanguageSupport.addDescriptions(node, "name", null, imageStyle.name, false);

    return imageStyle;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ImageStyle#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<imagestyle");

    // id
    buf.append(" id=\"").append(identifier).append("\"");

    // composeable
    buf.append(" composeable=\"").append(composeable).append("\"");

    buf.append(">");

    // scaling mode
    buf.append("<scalingmode>");
    buf.append(scalingMode.toString().toLowerCase());
    buf.append("</scalingmode>");

    // width
    if (width > 0)
      buf.append("<width>").append(width).append("</width>");

    // height
    if (height > 0)
      buf.append("<height>").append(height).append("</height>");

    // name
    for (Language l : name.languages()) {
      buf.append("<name language=\"");
      buf.append(l.getIdentifier());
      buf.append("\">");
      buf.append(name.get(l));
      buf.append("</name>");
    }

    buf.append("</imagestyle>");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#toString()
   */
  public String toString() {
    StringBuffer buf = new StringBuffer(identifier);
    buf.append(" [scaling=");
    buf.append(scalingMode.toString());
    if (width > 0) {
      buf.append(";width=");
      buf.append(width);
    }
    if (height > 0) {
      buf.append(";height=");
      buf.append(height);
    }
    buf.append("]");
    return buf.toString();
  }

}