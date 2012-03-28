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
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.Stream;
import ch.entwine.weblounge.common.content.movie.VideoStream;
import ch.entwine.weblounge.common.impl.content.file.FileContentImpl;
import ch.entwine.weblounge.common.language.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of audio visual content.
 */
public class MovieContentImpl extends FileContentImpl implements MovieContent {

  /** The audio and video streams */
  protected List<Stream> streams = new ArrayList<Stream>();

  /** The duration in milliseconds */
  protected long duration = -1L;

  /**
   * Creates a new audio visual content representation.
   */
  public MovieContentImpl() {
    super();
  }

  /**
   * Creates a new audio visual content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the audio visual's mime type
   */
  public MovieContentImpl(String filename, Language language, String mimetype) {
    this(filename, language, mimetype, -1, -1);
  }

  /**
   * Creates a new audio visual content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the audio visual's mime type
   * @param duration
   *          the audio visual duration in milliseconds
   */
  public MovieContentImpl(String filename, Language language, String mimetype,
      long duration) {
    this(filename, language, mimetype, duration, -1);
  }

  /**
   * Creates a new audio visual content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param mimetype
   *          the audio visual's mime type
   * @param duration
   *          the audio visual duration in milliseconds
   * @param filesize
   *          the file size in bytes
   */
  public MovieContentImpl(String filename, Language language, String mimetype,
      long filesize, long duration) {
    super(filename, language, mimetype, filesize);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#addStream(ch.entwine.weblounge.common.content.movie.Stream)
   */
  public void addStream(Stream stream) {
    streams.add(stream);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#getStreams()
   */
  public Stream[] getStreams() {
    return streams.toArray(new Stream[streams.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#hasAudio()
   */
  public boolean hasAudio() {
    for (Stream stream : streams) {
      if (stream instanceof AudioStream)
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#hasVideo()
   */
  public boolean hasVideo() {
    for (Stream stream : streams) {
      if (stream instanceof VideoStream)
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#setDuration(long)
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.movie.MovieContent#getDuration()
   */
  public long getDuration() {
    return duration;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentImpl#extendXml(java.lang.StringBuffer)
   */
  @Override
  protected StringBuffer extendXml(StringBuffer xml) {
    xml = super.extendXml(xml);

    // Duration
    if (duration != -1) {
      xml.append("<duration>").append(duration).append("</duration>");
    }

    // Add streams
    for (Stream stream : streams) {
      xml.append(stream.toXml());
    }

    return xml;
  }

}
