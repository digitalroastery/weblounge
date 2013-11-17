/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.security.Securable;

import java.util.List;

/**
 * A ResoourceRepresentation is, as its name states, a kind of representation of
 * the resource. Each representation consists of a particular type (e.g.
 * PageData) and a set of {@link ResourceRepresentationCharacteristic}.
 * <p>
 * Each {@link Resource} contains an undefined number of representations. There
 * may be multiple representations of the same type having a unique set of
 * characteristics.<br>
 * E.g.: A page resource may contains multiple content resource representations,
 * each with a unique language characteristic.
 * 
 * @see Resource
 * @see ResourceRepresentationCharacteristic
 */
public interface ResourceRepresentation extends Modifiable, Creatable, Securable {

  /**
   * Sets the type of this {@link ResourceRepresentation}.
   * 
   * @param type
   *          the type
   */
  void setType(String type);

  /**
   * Returns the type of this {@link ResourceRepresentation}.
   * 
   * @return the type
   */
  String getType();

  /**
   * Adds a {@link ResourceRepresentationCharacteristic} of this
   * {@link ResourceRepresentation}
   * 
   * @param characteristic
   *          the characteristic
   */
  void addCharacteristic(ResourceRepresentationCharacteristic characteristic);

  /**
   * Removes a {@link ResourceRepresentationCharacteristic} from this
   * {@link ResourceRepresentation}
   * 
   * @param characteristic
   *          the characteristic to remove
   */
  void removeCharacteristic(ResourceRepresentationCharacteristic characteristic);

  /**
   * Lists all {@link ResourceRepresentationCharacteristic} hold by this
   * {@link ResourceRepresentation}
   * 
   * @return list of all {@link ResourceRepresentationCharacteristic}
   */
  List<ResourceRepresentationCharacteristic> getCharacteristics();

}
