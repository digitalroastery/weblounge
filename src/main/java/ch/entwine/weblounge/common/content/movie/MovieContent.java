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

package ch.entwine.weblounge.common.content.movie;

import ch.entwine.weblounge.common.content.file.FileContent;

/**
 * Describes the contents of an audio visual resource, including the general
 * attributes such as file size and mime type. In addition, it contains
 * technical information like resolution, bit rate, frame rate etc.
 */
public interface MovieContent extends FileContent {

  /**
   * Returns the audio visual streams contained in this movie.
   * 
   * @return the streams
   */
  Stream[] getStreams();

  /**
   * Returns <code>true</code> if the track features an audio stream.
   * 
   * @return <code>true</code> if the track has an audio stream
   */
  boolean hasAudio();

  /**
   * Returns <code>true</code> if the track features a video stream.
   * 
   * @return <code>true</code> if the track has a video stream
   */
  boolean hasVideo();

  /**
   * Returns the track duration in milliseconds.
   * 
   * @return the track duration
   */
  long getDuration();

}
