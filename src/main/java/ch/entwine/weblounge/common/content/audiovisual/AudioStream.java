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

package ch.entwine.weblounge.common.content.audiovisual;

/**
 * An audio stream identifies an audio track inside a movie container.
 */
public interface AudioStream extends Stream {

  /**
   * Returns the number of bits per sample.
   * 
   * @return the number of bits per sample.
   */
  Integer getBitDepth();

  /**
   * Returns the number of channels.
   * 
   * @return the number of channels
   */
  Integer getChannels();

  /**
   * Returns the number of samples per second.
   * 
   * @return the number of samples per second
   */
  Integer getSamplingRate();

  /**
   * Returns the number of bits per second.
   * 
   * @return the number of bits per second
   */
  Float getBitRate();

  /**
   * Returns the name of the encoding format.
   * 
   * @return the encoding format
   */
  String getFormat();

}
