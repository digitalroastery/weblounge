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

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.security.ExtendedWebloungeUser;
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.site.Site;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

/**
 * This class extends the general weblounge user to a registered user with more
 * than only the standard properties.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public class ExtendedWebloungeUserImpl extends WebloungeUserImpl implements ExtendedWebloungeUser {

  /** The challenge */
  protected String challenge;

  /** The response */
  protected String response;

  /** The person's title */
  protected String salutation;

  /** The person's street */
  protected String street;

  /** The person's zip */
  protected String zip;

  /** The person's city */
  protected String city;

  /** The person's country */
  protected String country;

  /** The person's birthday */
  protected Date birthday;

  /** The private phone number */
  protected String phone;

  /** The mobile phone number */
  protected String mobile;

  /**
   * Creates a new registered user for the given site, using the provided login
   * name.
   * 
   * @param xml
   *          the xml resource containing the users's data
   * @param login
   *          the login name
   * @param context
   *          the login context
   * @param site
   *          the associated site
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XMLDBException
   *           if the user could not be loaded
   */
  public ExtendedWebloungeUserImpl(InputStream xml, String login,
      LoginContext context, Site site) throws IOException, SAXException,
      ParserConfigurationException {
    super(xml, login, context, site);
  }

  /**
   * Creates a new registered user for the given site, using the provided login
   * name.
   * 
   * @param xml
   *          the xml resource containing the users's data
   * @param login
   *          the login name
   * @param site
   *          the associated site
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XMLDBException
   *           if the user could not be loaded
   */
  public ExtendedWebloungeUserImpl(InputStream xml, String login, Site site)
      throws IOException, SAXException, ParserConfigurationException {
    super(xml, login, site);
  }

  /**
   * Creates a new registered user for the given site, using the provided login
   * name.
   * 
   * @param login
   *          the login name
   * @param site
   *          the associated site
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XMLDBException
   *           if the user could not be loaded
   */
  public ExtendedWebloungeUserImpl(String login, Site site) throws IOException,
      SAXException, ParserConfigurationException {
    super(null, login, site);
  }

  /**
   * Sets the user's challenge.
   * 
   * @param challenge
   *          the challenge
   */
  public void setChallenge(String challenge) {
    this.challenge = challenge;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getChallenge()
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * Sets the response to the challenge.
   * 
   * @param response
   *          the response
   */
  public void setResponse(String response) {
    this.response = response;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getResponse()
   */
  public String getResponse() {
    return response;
  }

  /**
   * Sets the users's salutation.
   * 
   * @param title
   */
  public void setSalutation(String title) {
    this.salutation = title;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getSalutation()
   */
  public String getSalutation() {
    return salutation;
  }

  /**
   * Sets the street name.
   * 
   * @param street
   *          the street
   */
  public void setStreet(String street) {
    this.street = street;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getStreet()
   */
  public String getStreet() {
    return street;
  }

  /**
   * Sets the zip number.
   * 
   * @param zip
   *          the zip
   */
  public void setZip(String zip) {
    this.zip = zip;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getZip()
   */
  public String getZip() {
    return zip;
  }

  /**
   * Sets the city.
   * 
   * @param city
   *          the city
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getCity()
   */
  public String getCity() {
    return city;
  }

  /**
   * Sets the country.
   * 
   * @param country
   *          the country
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getCountry()
   */
  public String getCountry() {
    return country;
  }

  /**
   * Sets the phone number.
   * 
   * @param home
   *          the phone
   */
  public void setPhone(String home) {
    this.phone = home;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getPhone()
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the mobile phone number.
   * 
   * @param mobile
   *          the mobile phone
   */
  public void setMobilePhone(String mobile) {
    this.mobile = mobile;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getMobilePhone()
   */
  public String getMobilePhone() {
    return mobile;
  }

  /**
   * Sets the date of birth.
   * 
   * @param birthday
   *          the birthday
   */
  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  /**
   * @see ch.o2it.weblounge.common.security.ExtendedWebloungeUser#getBirthday()
   */
  public Date getBirthday() {
    return birthday;
  }

  /**
   * Initializes this user object by reading all needed information from the xml
   * configuration node.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  protected void init(InputStream resource) throws IOException, SAXException,
      ParserConfigurationException {
    if (resource != null) {
      ExtendedWebloungeUserReader reader = new ExtendedWebloungeUserReader(this, site);
      reader.read(resource);
    }
  }

  /**
   * Returns the data that should be stored in the user database.
   * 
   * @see ch.o2it.weblounge.core.security.WebloungeUserImpl#getXmlExtension()
   */
  protected String getXmlExtension() {
    StringBuffer b = new StringBuffer();

    if (challenge != null && response != null) {
      b.append("<challenge>");
      b.append(challenge);
      b.append("</challenge>");
      b.append("<response>");
      b.append(response);
      b.append("</response>");
    }

    // Salutation
    if (salutation != null && !salutation.equals("")) {
      b.append("<salutation>");
      b.append(salutation);
      b.append("</salutation>");
    }

    // Address
    b.append("<address type=\"home\">");
    if (street != null) {
      b.append("<street>");
      b.append(street);
      b.append("</street>");
    }
    if (zip != null && !zip.equals("")) {
      b.append("<zip>");
      b.append(zip);
      b.append("</zip>");
    }
    if (city != null && !city.equals("")) {
      b.append("<city>");
      b.append(city);
      b.append("</city>");
    }
    if (country != null && !country.equals("")) {
      b.append("<country>");
      b.append(country);
      b.append("</country>");
    }
    b.append("</address>");

    // Phone
    if (this.phone != null && !phone.equals("")) {
      b.append("<phone>");
      b.append(phone);
      b.append("</phone>");
    }

    // Mobile
    if (this.mobile != null && !mobile.equals("")) {
      b.append("<mobilephone>");
      b.append(mobile);
      b.append("</mobilephone>");
    }

    // Birthday
    if (this.birthday != null) {
      b.append("<birtday>");
      b.append(WebloungeDateFormat.formatStatic(birthday));
      b.append("</birtday>");
    }

    return b.toString();

  }

}