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
package ch.o2it.weblounge.common.impl.content.image;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class holds metadata information for an image. See class
 * {@link ImageMetadataUtils} for extracting the metadata out of an image.
 */
public class ImageMetadata {

  /** image caption */
  private String caption;

  /** image legend */
  private String legend;

  /** photographer of the picture */
  private String photographer;

  /** copyright of the picture */
  private String copyright;

  /** date the picture was taken */
  private Date dateTaken;

  /** keywords */
  private List<String> keywords = new ArrayList<String>();

  /** location where the picture was taken */
  private String location;

  /** GPS latitude of the place where the picture was taken */
  private String gpsLat;

  /** GPS longitude of the place where the picture was taken */
  private String gpsLong;

  /** film speed with which the picture was taken */
  private int filmspeed;

  /** f-number with which the picture was taken */
  private float fnumber;

  /** focal width with which the picture was taken */
  private int focalWidth;

  /** exposure time used while taking the picture */
  private float exposureTime;

  /**
   * Returns the picture caption/title
   * 
   * @return picture caption
   */
  public String getCaption() {
    return caption;
  }

  /**
   * Sets the picture caption/title
   * 
   * @param caption
   *          the caption to set
   */
  public void setCaption(String caption) {
    this.caption = caption;
  }

  /**
   * Returns the picture legend
   * 
   * @return the legend
   */
  public String getLegend() {
    return legend;
  }

  /**
   * Sets the picture legend
   * 
   * @param legend
   *          the legend to set
   */
  public void setLegend(String legend) {
    this.legend = legend;
  }

  /**
   * Returns the name of the photographer of this picture
   * 
   * @return the photographer's name
   */
  public String getPhotographer() {
    return photographer;
  }

  /**
   * Set the name of the photographer
   * 
   * @param photographer
   *          the photographer to set
   */
  public void setPhotographer(String photographer) {
    this.photographer = photographer;
  }

  /**
   * Returns the copyright information for the picture.
   * 
   * @return the copyright information
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * Sets the copyright information
   * 
   * @param copyright
   *          the copyright information to set
   */
  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  /**
   * Returns the date the picture was taken
   * 
   * @return the dateTaken
   */
  public Date getDateTaken() {
    return dateTaken;
  }

  /**
   * Sets the date the picture was taken
   * 
   * @param dateTaken
   *          the dateTaken to set
   */
  public void setDateTaken(Date dateTaken) {
    this.dateTaken = dateTaken;
  }

  /**
   * Returns a <code>java.util.List</code> with keywords for the picture
   * 
   * @return list of keywords
   */
  public List<String> getKeywords() {
    return keywords;
  }

  /**
   * Adds a keyword to the list of keywords.
   * 
   * @param keyword
   *          the keyword to add
   */
  public void addKeyword(String keyword) {
    keywords.add(keyword);
  }

  /**
   * Returns the shooting location
   * 
   * @return the shooting location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the shooting location
   * 
   * @param location
   *          the shooting location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * 
   * @return the gpsLat
   */
  public String getGpsLat() {
    return gpsLat;
  }

  /**
   * @param gpsLat
   *          the gpsLat to set
   */
  public void setGpsLat(String gpsLat) {
    this.gpsLat = gpsLat;
  }

  /**
   * @return the gpsLong
   */
  public String getGpsLong() {
    return gpsLong;
  }

  /**
   * @param gpsLong
   *          the gpsLong to set
   */
  public void setGpsLong(String gpsLong) {
    this.gpsLong = gpsLong;
  }

  /**
   * Returns the film speed used for this picture
   * 
   * @return the filmspeed
   */
  public int getFilmspeed() {
    return filmspeed;
  }

  /**
   * Sets the film speed used for this picture
   * 
   * @param filmspeed
   *          the filmspeed to set
   */
  public void setFilmspeed(int filmspeed) {
    this.filmspeed = filmspeed;
  }

  /**
   * Returns the f-number used for this picture
   * 
   * @return the fnumber
   */
  public float getFNumber() {
    return fnumber;
  }

  /**
   * Sets the f-number used for this picture
   * 
   * @param fnumber
   *          the f-number to set
   */
  public void setFNumber(float fnumber) {
    this.fnumber = fnumber;
  }

  /**
   * Returns the focal width used for this picture
   * 
   * @return the focal width
   */
  public int getFocalWidth() {
    return focalWidth;
  }

  /**
   * Sets the focal width used for this picture
   * 
   * @param focalWidth
   *          the focal width to set
   */
  public void setFocalWidth(int focalWidth) {
    this.focalWidth = focalWidth;
  }

  /**
   * Returns the exposure time used for this picture
   * 
   * @return the exposure time
   */
  public float getExposureTime() {
    return exposureTime;
  }

  /**
   * Sets the exposure time used for this picture
   * 
   * @param exposureTime
   *          the exposure time to set
   */
  public void setExposureTime(float exposureTime) {
    this.exposureTime = exposureTime;
  }

}
