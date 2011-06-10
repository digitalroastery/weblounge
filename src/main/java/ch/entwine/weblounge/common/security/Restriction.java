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

package ch.entwine.weblounge.common.security;

/**
 * Defines the methods for the evaluation of restrictions containing
 * <code>allow</code> and <code>deny</code> directives.
 */
public interface Restriction {

  /** Evaluation order which first evaluates the allow, then the deny entries */
  int ALLOW_DENY = 0;

  /** Evaluation order which first evaluates the deny, then the allow entries */
  int DENY_ALLOW = 1;

  /**
   * Adds <code>authorization</code> to the list of allowed items.
   * 
   * @param authority
   *          the authorization to allow
   */
  void allow(Authority authority);

  /**
   * Permits every authorization to pass the <code>allow</code> rule evaluation.
   */
  void allowAll();

  /**
   * Adds <code>authorization</code> to the list of denied authorizations.
   * 
   * @param authority
   *          the authorization to deny
   */
  void deny(Authority authority);

  /**
   * Permits every authorization to pass the <code>deny</code> rule evaluation.
   */
  void denyAll();

  /**
   * Returns the order in which the restriction is tested. The order is one of:
   * <ul>
   * <li>{@link #ALLOW_DENY}</li>
   * <li>{@link #DENY_ALLOW}</li>
   * </ul>
   * 
   * @return the evaluation order
   */
  int getEvaluationOrder();

  /**
   * Sets the evaluation order of this restriction. The order must be one of
   * <ul>
   * <li>{@link #ALLOW_DENY}</li>
   * <li>{@link #DENY_ALLOW}</li>
   * </ul>
   * otherwise, an <code>IllegalArgumentException</code> is thrown.
   * 
   * @param order
   *          the new evaluation order
   * @throws IllegalArgumentException
   *           if the order is unknown
   */
  void setEvaluationOrder(int order) throws IllegalArgumentException;

  /**
   * Returns <code>true</code> if the authorization passes the restriction.
   * 
   * @param authority
   *          the authorization to test
   * @return <code>true</code> if the authorization passes the test
   */
  boolean check(Authority authority);

  /**
   * Returns <code>true</code> if the authorization passes the allow rules.
   * 
   * @param authority
   *          the authorization to test
   * @return <code>true</code> if the authorization passes the allow rules
   */
  boolean isAllowed(Authority authority);

  /**
   * Returns <code>true</code> if the authorization passes the deny rules.
   * 
   * @param authority
   *          the authorization to test
   * @return <code>true</code> if the authorization passes the deny rules
   */
  boolean isDenied(Authority authority);

  /**
   * Returns the types that are used in this authorization set.
   * 
   * @return the types
   */
  String[] getTypes();

  /**
   * Returns all <code>allow</code> entries for the given type.
   * 
   * @return the allow entries for <code>type</code>
   */
  Authority[] getAllowed(String type);

  /**
   * Returns all <code>allow</code> entries.
   * 
   * @return the allow entries
   */
  Authority[] getAllowed();

  /**
   * Returns all <code>allow</code> entries for the given type.
   * 
   * @return the allow entries for <code>type</code>
   */
  Authority[] getDenied(String type);

  /**
   * Returns all <code>deny</code> entries.
   * 
   * @return the deny entries
   */
  Authority[] getDenied();

  /**
   * Returns this restriction serialized as an xml string.
   * 
   * @return the xml representation
   */
  String toXml();

}