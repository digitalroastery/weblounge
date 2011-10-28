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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;

import org.apache.commons.io.IOUtils;
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

  /** The logging facility */
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
    BufferedInputStream is = null;
    try {
      is = new BufferedInputStream(new FileInputStream(img));
      return extractMetadata(is);
    } catch (FileNotFoundException e) {
      logger.warn("Tried to extract image metadata from none existing file '{}'", img.getName());
      return null;
    } finally {
      IOUtils.closeQuietly(is);
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
      logger.warn("Failed to extract image metadata from image", e);
      return null;
    }

    if (meta == null) {
      logger.debug("Extracted metadata is null");
      return null;
    } else {

      ImageMetadata imgmeta = new ImageMetadata();

      // Extract IPTC information
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

      // Extract EXIF information
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

      // Extract GPS information
      try {
        Directory gps = meta.getDirectory(GpsDirectory.class);
        if (gps.containsTag(GpsDirectory.TAG_GPS_LATITUDE)) {
          Rational[] lat = gps.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
          String latRef = gps.getString(GpsDirectory.TAG_GPS_LATITUDE_REF);
          double latitude = parseHMS(lat);
          if (latitude != 0) {
            if (StringUtils.isNotBlank(latRef) && "S".equalsIgnoreCase(latRef) && latitude > 0)
              latitude *= -1;
          }
          imgmeta.setGpsLat(latitude);
        }
        if (gps.containsTag(GpsDirectory.TAG_GPS_LONGITUDE)) {
          Rational[] lng = gps.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
          String lngRef = gps.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF);
          double longitude = parseHMS(lng);
          if (longitude != 0) {
            if (StringUtils.isNotBlank(lngRef) && "W".equalsIgnoreCase(lngRef) && longitude > 0)
              longitude *= -1;
          }
          imgmeta.setGpsLong(longitude);
        }
      } catch (MetadataException e) {
        logger.info("Error while extracting GPS information out of an image.");
        imgmeta.setGpsLat(0);
        imgmeta.setGpsLong(0);
      }

      return imgmeta;
    }
  }

  private static double parseHMS(Rational[] hms) {
    return hms[0].doubleValue() + (hms[1].doubleValue() + (hms[2].doubleValue() / 60)) / 60;
  }

}
