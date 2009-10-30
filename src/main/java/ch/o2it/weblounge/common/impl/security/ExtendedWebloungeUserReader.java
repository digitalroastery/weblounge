/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.text.ParseException;

/**
 * Utility class used to parse user data.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class ExtendedWebloungeUserReader extends WebloungeUserReader {

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = ExtendedWebloungeUserReader.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new user data reader that will parse the sax data and store it in
   * the user object.
   * 
   * @param user
   *          the user object
   * @param site
   *          the associated site
   */
  ExtendedWebloungeUserReader(ExtendedWebloungeUserImpl user, Site site) {
    super(user, site);
  }

  /**
   * The parser found the end of an element.
   * 
   * @param uri
   *          information about the namespace
   * @param local
   *          the local name of the element
   * @param raw
   *          the raw name of the element
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {
    super.endElement(uri, local, raw);

    // challenge
    if (local.equals("challenge")) {
      ((ExtendedWebloungeUserImpl) user).setChallenge(nodeContent.toString());
    }

    // response
    if (local.equals("response")) {
      ((ExtendedWebloungeUserImpl) user).setResponse(nodeContent.toString());
    }

    // salutation
    if (local.equals("salutation")) {
      ((ExtendedWebloungeUserImpl) user).setSalutation(nodeContent.toString());
    }

    // street
    if (local.equals("street")) {
      ((ExtendedWebloungeUserImpl) user).setStreet(nodeContent.toString());
    }

    // zip
    else if (local.equals("zip")) {
      ((ExtendedWebloungeUserImpl) user).setZip(nodeContent.toString());
    }

    // city
    else if (local.equals("city")) {
      ((ExtendedWebloungeUserImpl) user).setCity(nodeContent.toString());
    }

    // city
    else if (local.equals("country")) {
      ((ExtendedWebloungeUserImpl) user).setCountry(nodeContent.toString());
    }

    // phone
    else if (local.equals("phone")) {
      ((ExtendedWebloungeUserImpl) user).setPhone(nodeContent.toString());
    }

    // mobile phone
    else if (local.equals("mobilephone")) {
      ((ExtendedWebloungeUserImpl) user).setMobilePhone(nodeContent.toString());
    }

    // birthday

    else if (local.equals("birthday")) {
      try {
        ((ExtendedWebloungeUserImpl) user).setBirthday(WebloungeDateFormat.parseStatic(nodeContent.toString()));
      } catch (ParseException e) {
        log_.debug("Unable to parse birthday for user " + this);
      }
    }

  }

}