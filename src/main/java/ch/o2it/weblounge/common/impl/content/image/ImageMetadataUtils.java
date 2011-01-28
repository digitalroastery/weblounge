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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

/**
 * Utility class for dealing with image meta data
 */
public final class ImageMetadataUtils {

  private static Logger logger = LoggerFactory.getLogger(ImageMetadataUtils.class);

  /**
   * This class is not meant to be instantiated.
   */
  private ImageMetadataUtils() {
    // Utility classes should not have a public or default constructor
  }

  /**
   * This utility method extracts image metadata stored in EXIF and IPTC tags
   * and returns the extracted information in a {@link ImageMetadata}.
   * 
   * @param img
   *          image file
   * @return extracted meta information
   */
  public static ImageMetadata extractMetadata(File img) {
    try {
      return extractMetadata(new BufferedInputStream(new FileInputStream(img)));
    } catch (FileNotFoundException e) {
      logger.warn("Tried to extract image metadata from none existing file '{}'", img.getName());
      return null;
    }
  }

  /**
   * This utility method extracts image metadata stored in EXIF and IPTC tags
   * and returns the extracted information in a {@link ImageMetadata}.
   * 
   * @param img
   *          image input stream
   * @return extracted meta information
   */
  public static ImageMetadata extractMetadata(BufferedInputStream img) {
    Metadata meta;
    try {
      meta = ImageMetadataReader.readMetadata(img);
    } catch (ImageProcessingException e) {
      logger.warn("Failed to extract image metadata from image");
      return null;
    }

    if (meta == null) {
      logger.debug("Extracted metadata is null.");
      return null;
    } else {

      ImageMetadata imgmeta = new ImageMetadata();
      // extract IPTC information
      Directory iptc = meta.getDirectory(IptcDirectory.class);
      if (iptc.containsTag(IptcDirectory.TAG_HEADLINE))
        imgmeta.setCaption(iptc.getString(IptcDirectory.TAG_HEADLINE));
      if (iptc.containsTag(IptcDirectory.TAG_CAPTION))
        imgmeta.setLegend(iptc.getString(IptcDirectory.TAG_CAPTION));
      if (iptc.containsTag(IptcDirectory.TAG_BY_LINE))
        imgmeta.setPhotographer(iptc.getString(IptcDirectory.TAG_BY_LINE));
      if (iptc.containsTag(IptcDirectory.TAG_COPYRIGHT_NOTICE))
        imgmeta.setCopyright(iptc.getString(IptcDirectory.TAG_COPYRIGHT_NOTICE));
      if (iptc.containsTag(IptcDirectory.TAG_CITY))
        imgmeta.setLocation(iptc.getString(IptcDirectory.TAG_CITY));
      if (iptc.containsTag(IptcDirectory.TAG_KEYWORDS)) {
        StringTokenizer st = new StringTokenizer(iptc.getString(IptcDirectory.TAG_KEYWORDS), ",;");
        while (st.hasMoreTokens()) {
          imgmeta.addKeyword(st.nextToken());
        }
      }

      // extract EXIF information
      Directory exif = meta.getDirectory(ExifDirectory.class);
      if (exif.containsTag(ExifDirectory.TAG_DATETIME)) {
        try {
          imgmeta.setDateTaken(exif.getDate(ExifDirectory.TAG_DATETIME));
        } catch (MetadataException e) {
        }
      }
      if (exif.containsTag(ExifDirectory.TAG_ISO_EQUIVALENT)) {
        try {
          imgmeta.setFilmspeed(exif.getInt(ExifDirectory.TAG_ISO_EQUIVALENT));
        } catch (MetadataException e) {
        }
      }
      if (exif.containsTag(ExifDirectory.TAG_FNUMBER)) {
        try {
          imgmeta.setFNumber(exif.getFloat(ExifDirectory.TAG_FNUMBER));
        } catch (MetadataException e) {
        }
      }
      if (exif.containsTag(ExifDirectory.TAG_FOCAL_LENGTH)) {
        try {
          imgmeta.setFocalWidth(exif.getInt(ExifDirectory.TAG_FOCAL_LENGTH));
        } catch (MetadataException e) {
        }
      }
      if (exif.containsTag(ExifDirectory.TAG_EXPOSURE_TIME)) {
        try {
          imgmeta.setExposureTime(exif.getFloat(ExifDirectory.TAG_EXPOSURE_TIME));
        } catch (MetadataException e) {
        }
      }
      if (StringUtils.isBlank(imgmeta.getCopyright()) && exif.containsTag(ExifDirectory.TAG_COPYRIGHT))
        imgmeta.setCopyright(exif.getString(ExifDirectory.TAG_COPYRIGHT));

      // extract GPS information
      Directory gps = meta.getDirectory(GpsDirectory.class);
      if (exif.containsTag(GpsDirectory.TAG_GPS_DEST_LATITUDE))
        imgmeta.setGpsLat(gps.getString(GpsDirectory.TAG_GPS_DEST_LATITUDE));
      if (exif.containsTag(GpsDirectory.TAG_GPS_DEST_LONGITUDE))
        imgmeta.setGpsLong(gps.getString(GpsDirectory.TAG_GPS_DEST_LONGITUDE));

      return imgmeta;
    }
  }
}
