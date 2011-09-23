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

import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.impl.content.file.FileContentImpl;
import ch.entwine.weblounge.common.language.Language;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;


/**
 * Default implementation of an image resource content.
 */
public class ImageContentImpl extends FileContentImpl implements ImageContent {

  /** The image width in pixels */
  protected int width = -1;

  /** The image height in pixels */
  protected int height = -1;
  
  /** location where the picture was taken */
  protected String location = null;

  /** GPS latitude of the place where the picture was taken */
  protected double gpsLat = -1;

  /** GPS longitude of the place where the picture was taken */
  protected double gpsLong = -1;

  /** film speed with which the picture was taken */
  protected int filmspeed = -1;

  /** f-number with which the picture was taken */
  protected float fnumber = -1;

  /** focal width with which the picture was taken */
  protected int focalWidth = -1;

  /** exposure time used while taking the picture */
  protected float exposureTime = -1;

  /**
   * Creates a new image content representation.
   */
  public ImageContentImpl() {
    super();
  }

  /**
   * Creates a new image content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the image's mime type
   */
  public ImageContentImpl(String filename, Language language, String mimetype) {
    this(filename, language, mimetype, -1, -1, -1);
  }

  /**
   * Creates a new image content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the image's mime type
   * @param width
   *          the image width in pixels
   * @param height
   *          the image height in pixels
   */
  public ImageContentImpl(String filename, Language language, String mimetype,
      int width, int height) {
    this(filename, language, mimetype, width, height, -1);
  }

  /**
   * Creates a new image content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the image's mime type
   * @param width
   *          the image width in pixels
   * @param height
   *          the image height in pixels
   * @param filesize
   *          the file size in bytes
   */
  public ImageContentImpl(String filename, Language language, String mimetype,
      int width, int height, long filesize) {
    super(filename, language, mimetype, filesize);
    this.width = width;
    this.height = height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setWidth(int)
   */
  public void setWidth(int width) {
    if (width <= 0)
      throw new IllegalArgumentException("Image must be wider than 0 pixels");
    this.width = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setHeight(int)
   */
  public void setHeight(int height) {
    if (height <= 0)
      throw new IllegalArgumentException("Image must be taller than 0 pixels");
    this.height = height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getHeight()
   */
  public int getHeight() {
    return height;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getLocation()
   */
  public String getLocation() {
    return location;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setLocation(java.lang.String)
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getGpsLat()
   */
  public double getGpsLat() {
    return gpsLat;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getGpsLong()
   */
  public double getGpsLong() {
    return gpsLong;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setGpsPosition(double, double)
   */
  public void setGpsPosition(double gpsLat, double gpsLong) {
    this.gpsLat = gpsLat;
    this.gpsLong = gpsLong;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getFilmspeed()
   */
  public int getFilmspeed() {
    return filmspeed;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setFilmspeed(int)
   */
  public void setFilmspeed(int filmspeed) {
    this.filmspeed = filmspeed;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getFNumber()
   */
  public float getFNumber() {
    return fnumber;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setFNumber(float)
   */
  public void setFNumber(float fnumber) {
    this.fnumber = fnumber;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getFocalWidth()
   */
  public int getFocalWidth() {
    return focalWidth;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setFocalWidth(int)
   */
  public void setFocalWidth(int focalWidth) {
    this.focalWidth = focalWidth;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#getExposureTime()
   */
  public float getExposureTime() {
    return exposureTime;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.image.ImageContent#setExposureTime(float)
   */
  public void setExposureTime(float exposureTime) {
    this.exposureTime = exposureTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentImpl#extendXml(java.lang.StringBuffer)
   */
  @Override
  protected StringBuffer extendXml(StringBuffer xml) {
    xml = super.extendXml(xml);
    if (width <= 0)
      throw new IllegalArgumentException("Image must be wider than 0 pixels");
    if (height <= 0)
      throw new IllegalArgumentException("Image must be taller than 0 pixels");
    
    DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    formatSymbols.setDecimalSeparator('.');
    NumberFormat nf = new DecimalFormat("0.000000", formatSymbols);
    
    xml.append("<width>").append(width).append("</width>");
    xml.append("<height>").append(height).append("</height>");
    xml.append("<location><![CDATA[").append(location).append("]]></location>");
    xml.append("<gps lat=\"").append(nf.format(gpsLat)).append("\" lng=\"").append(nf.format(gpsLong)).append("\" />");
    xml.append("<filmspeed>").append(filmspeed).append("</filmspeed>");
    xml.append("<fnumber>").append(fnumber).append("</fnumber>");
    xml.append("<focalwidth>").append(focalWidth).append("</focalwidth>");
    xml.append("<exposuretime>").append(exposureTime).append("</exposuretime>");
    return xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.file.FileContentImpl#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentImpl#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ImageContent) {
      ImageContent content = (ImageContent) obj;
      if (width != content.getWidth())
        return false;
      if (height != content.getHeight())
        return false;
      return super.equals(content);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentImpl#toString()
   */
  @Override
  public String toString() {
    return filename != null ? filename : super.toString();
  }

}
