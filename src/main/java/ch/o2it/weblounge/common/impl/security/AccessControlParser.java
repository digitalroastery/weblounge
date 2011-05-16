/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.AccessControlList;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Marshals and unmarshals {@link AccessControlListImpl}s to and from XML.
 */
public final class AccessControlParser {
  private static final JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance("ch.o2it.weblounge.security", AccessControlParser.class.getClassLoader());
    } catch (JAXBException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Disallow construction of this utility class.
   */
  private AccessControlParser() {
  }

  public static AccessControlList parseAcl(String serializedForm)
      throws IOException {
    return parseAcl(IOUtils.toInputStream(serializedForm, "utf-8"));
  }

  /**
   * Reads an ACL from an xml input stream.
   * 
   * @param in
   *          the xml input stream
   * @return the access control list
   * @throws IOException
   *           if there is a problem reading the stream
   */
  public static AccessControlList parseAcl(InputStream in) throws IOException {
    Unmarshaller unmarshaller;
    try {
      unmarshaller = jaxbContext.createUnmarshaller();
      return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in), AccessControlListImpl.class).getValue();
    } catch (Throwable t) {
      throw new IOException(t);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Serializes an AccessControlList to its XML form.
   * 
   * @param acl
   *          the access control list
   * @return the xml as a string
   * @throws IOException
   *           if there is a problem marshaling the xml
   */
  public static String toXml(AccessControlList acl) throws IOException {
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      Writer writer = new StringWriter();
      marshaller.marshal(acl, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

}
