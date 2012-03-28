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

import ch.entwine.weblounge.common.content.movie.AudioStream;
import ch.entwine.weblounge.common.content.movie.VideoStream;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * Implementation of an audio stream representation.
 */
public class AudioStreamImpl implements AudioStream {

  /** The stream identifier */
  protected String identifier = null;

  /** The bit depth */
  protected int bitDepth = -1;

  /** The channels */
  protected int channels = -1;

  /** The sampling rate */
  protected int samplingRate = -1;

  /** The bit rate */
  protected float bitRate = -1;

  /** The format */
  protected String format = null;

  /**
   * Creates a new video stream object.
   */
  public AudioStreamImpl() {
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#setBitDepth(int)
   */
  public void setBitDepth(int bitDepth) {
    this.bitDepth = bitDepth;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#getBitDepth()
   */
  public Integer getBitDepth() {
    return bitDepth;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#setChannels(int)
   */
  public void setChannels(int channels) {
    this.channels = channels;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#getChannels()
   */
  public Integer getChannels() {
    return channels;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#setSamplingRate(int)
   */
  public void setSamplingRate(int samplingRate) {
    this.samplingRate = samplingRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#getSamplingRate()
   */
  public Integer getSamplingRate() {
    return samplingRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#setBitRate(float)
   */
  public void setBitRate(float bitRate) {
    this.bitRate = bitRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#getBitRate()
   */
  public Float getBitRate() {
    return bitRate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.AudioStream#setFormat(java.lang.String)
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

    StringBuffer buf = new StringBuffer("<audio>");

    // bit depth
    if (bitDepth != -1) {
      buf.append("<bitdepth>").append(bitDepth).append("</bitdepth>");
    }

    // bit rate
    if (bitRate != -1) {
      buf.append("<bitrate>").append(bitRate).append("</bitrate>");
    }

    // channels
    if (channels != -1) {
      buf.append("<channels>").append(channels).append("</channels>");
    }

    // format
    if (StringUtils.isNotBlank(format)) {
      buf.append("<format>").append(format).append("</format>");
    }

    // sampling rate
    if (samplingRate != -1) {
      buf.append("<samplingrate>").append(samplingRate).append("</samplingrate>");
    }

    buf.append("</audio>");

    return buf.toString();
  }

}
