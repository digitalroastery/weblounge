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

package ch.entwine.weblounge.common.impl.content.movie;

import ch.entwine.weblounge.common.content.movie.ScanType;
import ch.entwine.weblounge.common.content.movie.VideoStream;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * Implementation of an audio stream representation.
 */
public class VideoStreamImpl implements VideoStream {

  /** The stream identifier */
  protected String identifier = null;

  /** The frame rate */
  protected float frameRate = -1;

  /** The bit rate */
  protected float bitRate = -1;

  /** The frame width */
  protected int frameWidth = -1;

  /** The frame height */
  protected int frameHeight = -1;

  /** The scan type */
  protected ScanType scanType = null;

  /** The format */
  protected String format = null;

  /**
   * Creates a new video stream object.
   */
  public VideoStreamImpl() {
    identifier = UUID.randomUUID().toString();
  }

  /**
   * Sets the stream identifier.
   * 
   * @param identifier
   *          the identifier
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.Stream#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the bit rate.
   * 
   * @param bitRate
   *          the bit rate
   */
  public void setBitRate(float bitRate) {
    this.bitRate = bitRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.VideoStream#getBitRate()
   */
  public Float getBitRate() {
    return bitRate;
  }

  /**
   * Sets the frame rate.
   * 
   * @param frameRate
   *          the frame rate
   */
  public void setFrameRate(float frameRate) {
    this.frameRate = frameRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.VideoStream#getFrameRate()
   */
  public Float getFrameRate() {
    return frameRate;
  }

  /**
   * Sets the frame width.
   * 
   * @param width
   *          the frame width
   */
  public void setFrameWidth(int width) {
    this.frameWidth = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.VideoStream#getFrameWidth()
   */
  public Integer getFrameWidth() {
    return frameWidth;
  }

  /**
   * Sets the frame height.
   * 
   * @param height
   *          the frame height
   */
  public void setFrameHeight(int height) {
    this.frameHeight = height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.VideoStream#getFrameHeight()
   */
  public Integer getFrameHeight() {
    return frameHeight;
  }

  /**
   * Sets the scan type.
   * 
   * @param scanType
   *          the scan type
   */
  public void setScanType(ScanType scanType) {
    this.scanType = scanType;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.VideoStream#getScanType()
   */
  public ScanType getScanType() {
    return scanType;
  }

  /**
   * Sets the encoding format.
   * 
   * @param format
   *          the format
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.Stream#getFormat()
   */
  public String getFormat() {
    return format;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (identifier != null)
      return identifier.hashCode();
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VideoStream))
      return false;
    VideoStream stream = (VideoStream) obj;
    if (identifier != null)
      return identifier.equals(stream.getIdentifier());
    return super.equals(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (format != null)
      buf.append(format).append(" ");
    buf.append("video");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.Stream#toXml()
   */
  public String toXml() {

    StringBuffer buf = new StringBuffer("<video>");

    // bit rate
    if (bitRate != -1) {
      buf.append("<bitrate>").append(bitRate).append("</bitrate>");
    }

    // format
    if (StringUtils.isNotBlank(format)) {
      buf.append("<format>").append(format).append("</format>");
    }

    // frame rate
    if (frameRate != -1) {
      buf.append("<framerate>").append(frameRate).append("</framerate>");
    }

    // resolution
    if (frameWidth != -1 && frameHeight != -1) {
      buf.append("<resolution>").append(frameWidth).append("x").append(frameHeight).append("</resolution>");
    }

    // scan type
    if (scanType != null) {
      buf.append("<scantype>").append(scanType.toString().toLowerCase()).append("</scantype>");
    }

    buf.append("</video>");

    return buf.toString();
  }

}
