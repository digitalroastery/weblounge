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

package ch.entwine.weblounge.common.impl.content.image;

import static ch.entwine.weblounge.common.site.ImageScalingMode.Box;
import static ch.entwine.weblounge.common.site.ImageScalingMode.Cover;
import static ch.entwine.weblounge.common.site.ImageScalingMode.Fill;
import static ch.entwine.weblounge.common.site.ImageScalingMode.Height;
import static ch.entwine.weblounge.common.site.ImageScalingMode.None;
import static ch.entwine.weblounge.common.site.ImageScalingMode.Width;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.GeneralComposeable;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathNamespaceContext;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * The <code>ImageStyleImpl</code> class defines a presentation style for images
 * of a certain size as well as the method to get from the original image
 * version to scaled ones.
 */
public class ImageStyleImpl extends GeneralComposeable implements ImageStyle {

  /** Identifier for the global context */
  public static final String GLOBAL_CONTEXT = "all";

  /** the image width */
  protected int width = -1;

  /** the image height */
  protected int height = -1;

  /** the scaling mode */
  protected ImageScalingMode scalingMode = null;

  /** The styling context */
  protected List<Context> contexts = null;

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
   *          <code>true</code> if the image style is composeable
   * @param preview
   *          <code>true</code> if the image style is automatically created
   * @throws IllegalArgumentException
   *           if id parameter is blank
   * @throws IllegalArgumentException
   *           if width and height parameter are zero or negative
   */
  public ImageStyleImpl(String id, int width, int height,
      ImageScalingMode scaling, boolean composeable, boolean preview)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("Identifier cannot be null");
    this.identifier = id;
    this.width = width;
    this.height = height;
    this.scalingMode = scaling;
    this.composeable = composeable;
    switch (scaling) {
      case Box:
      case Cover:
      case Fill:
        if (width <= 0)
          throw new IllegalArgumentException("Image width (" + width + ") must be a positive, non-zero number");
        if (height <= 0)
          throw new IllegalArgumentException("Image height (" + height + ") must be a positive, non-zero number");
        break;
      case Width:
        if (width <= 0)
          throw new IllegalArgumentException("Image width (" + width + ") must be a positive, non-zero number");
        break;
      case Height:
        if (height <= 0)
          throw new IllegalArgumentException("Image height (" + height + ") must be a positive, non-zero number");
        break;
      case None:
        break;
      default:
        throw new IllegalArgumentException("Scaling mode '" + scaling.toString().toLowerCase() + "' is unsupported");
    }
  }

  /**
   * Creates a new composeable image style with width, height and a default
   * scaling mode of {@link ImageScalingMode#None}.
   * 
   * @param id
   *          the style identifier
   * @param width
   *          the image width
   * @param height
   *          the image height
   */
  public ImageStyleImpl(String id, int width, int height) {
    this(id, width, height, None, true, false);
  }

  /**
   * Creates a new composeable image style.
   * 
   * @param id
   *          the style identifier
   * @param mode
   *          the scaling mode
   */
  public ImageStyleImpl(String id, ImageScalingMode mode) {
    this(id, -1, -1, mode, true, false);
  }

  /**
   * Creates a new composeable image style.
   * 
   * @param id
   *          the style identifier
   */
  public ImageStyleImpl(String id) {
    this(id, -1, -1, None, true, false);
  }

  /**
   * Adds a new context to this image style.
   * 
   * @param type
   *          the context
   * @param preview
   *          <code>true</code> to create previews
   * @throws IllegalArgumentException
   *           if <code>type</code> is blank
   */
  public void addContext(String type, boolean preview)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(type))
      throw new IllegalArgumentException("Type must not be blank");
    if (contexts == null)
      contexts = new ArrayList<Context>();
    if (GLOBAL_CONTEXT.equals(type))
      contexts.clear();
    contexts.add(new Context(type, preview));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#getContexts()
   */
  @Override
  public List<String> getContexts() {
    if (contexts == null || contexts.isEmpty())
      return Collections.emptyList();
    if (GLOBAL_CONTEXT.equals(contexts.get(0).getResource()))
      return Collections.emptyList();
    List<String> result = new ArrayList<String>(contexts.size());
    for (Context ctx : contexts) {
      result.add(ctx.getResource());
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#createPreview(java.lang.String)
   */
  @Override
  public boolean createPreview(String context) throws IllegalArgumentException {
    if (StringUtils.isBlank(context))
      throw new IllegalArgumentException("Context must not be blank");
    if (contexts == null || contexts.isEmpty())
      return false;
    if ("all".equals(contexts.get(0).getResource()))
      return contexts.get(0).createPreview();
    for (Context ctx : contexts) {
      if (context.equals(ctx.getResource()))
        return ctx.createPreview();
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#scale(int, int)
   */
  public float scale(int width, int height) {
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float scale = 1.0f;
    switch (scalingMode) {
      case Box:
        scaleX = this.width / width;
        scaleY = this.height / height;
        if (scaleX < 1.0f || scaleY < 1.0f) {
          scale = Math.min(scaleX, scaleY);
        } else {
          scale = Math.max(scaleX, scaleY);
        }
        break;
      case Cover:
        scaleX = this.width / width;
        scaleY = this.height / height;
        if (scaleX > 1.0f || scaleY > 1.0f) {
          scale = Math.max(scaleX, scaleY);
        } else {
          scale = Math.min(scaleX, scaleY);
        }
        break;
      case Fill:
        // TODO: Implement
        break;
      case Height:
        if (height != this.height) {
          scale = this.height / height;
        }
        break;
      case Width:
        if (width != this.width) {
          scale = this.width / width;
        }
        break;
      default:
        scale = 1.0f;
        break;
    }
    return scale;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#setScalingMode(ch.entwine.weblounge.common.site.ImageScalingMode)
   */
  public void setScalingMode(ImageScalingMode mode) {
    this.scalingMode = mode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#getScalingMode()
   */
  public ImageScalingMode getScalingMode() {
    return scalingMode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#setHeight(int)
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
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#setWidth(int)
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#equals(java.lang.Object)
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
    xpath.setNamespaceContext(new XPathNamespaceContext(true));
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
      if (XPathHelper.valueOf(node, "m:width", xpath) != null)
        width = Integer.parseInt(XPathHelper.valueOf(node, "m:width", xpath));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Missing width in image style definition");
    }

    // Height
    int height = -1;
    try {
      if (XPathHelper.valueOf(node, "m:height", xpath) != null)
        height = Integer.parseInt(XPathHelper.valueOf(node, "m:height", xpath));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Missing height in image style definition");
    }

    // Composeable
    boolean composeable = ConfigurationUtils.isTrue(XPathHelper.valueOf(node, "@composeable", xpath), true);

    // Preview
    boolean preview = ConfigurationUtils.isTrue(XPathHelper.valueOf(node, "@preview", xpath), false);

    // Scaling mode
    String mode = XPathHelper.valueOf(node, "m:scalingmode", xpath);
    ImageScalingMode scalingMode = mode == null ? None : ImageScalingMode.parseString(mode);

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
    ImageStyleImpl imageStyle = new ImageStyleImpl(id, width, height, scalingMode, composeable, preview);

    // Names
    String name = XPathHelper.valueOf(node, "m:name", xpath);
    imageStyle.setName(name);

    // Contexts
    NodeList contexts = XPathHelper.selectList(node, "m:context/m:resource", xpath);
    if (contexts != null) {
      for (int i = 0; i < contexts.getLength(); i++) {
        Node n = contexts.item(i);
        boolean createPreviews = ConfigurationUtils.isTrue(n.getAttributes().getNamedItem("preview"));
        String resource = n.getFirstChild().getNodeValue();
        imageStyle.addContext(resource, createPreviews);
      }
    }

    return imageStyle;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageStyle#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<imagestyle");

    // id
    buf.append(" id=\"").append(identifier).append("\"");

    // composeable
    buf.append(" composeable=\"").append(composeable).append("\"");

    buf.append(">");

    // name
    if (StringUtils.isNotBlank(name)) {
      buf.append("<name><![CDATA[");
      buf.append(name);
      buf.append("]]></name>");
    }

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

    // contexts
    if (contexts != null) {
      buf.append("<context>");
      for (Context ctx : contexts) {
        buf.append("<resource");
        if (ctx.createPreview())
          buf.append(" preview=\"true\"");
        buf.append(">").append(ctx.getResource());
        buf.append("</resource>");
      }
      buf.append("</context>");
    }

    buf.append("</imagestyle>");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.language.LocalizableObject#toString()
   */
  @Override
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

  /**
   * The image style context.
   */
  private static final class Context {

    /** The resource type */
    private String resource = null;

    /** Whether to render previews for the resource */
    private boolean preview = false;

    /**
     * Creates a new context for the given resource and defines whether previews
     * should be created automatically for this resource.
     * 
     * @param resource
     *          the resource
     * @param preview
     *          <code>true</code> to automatically render previews
     */
    public Context(String resource, boolean preview) {
      this.resource = resource;
      this.preview = preview;
    }

    /**
     * Returns the resource type.
     * 
     * @return the resource
     */
    public String getResource() {
      return resource;
    }

    /**
     * Returns <code>true</code> if previews need to be created for the
     * resource.
     * 
     * @return the preview the preview
     */
    public boolean createPreview() {
      return preview;
    }

  }

}