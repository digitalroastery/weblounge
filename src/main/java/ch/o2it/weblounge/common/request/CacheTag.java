/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License;
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not;
 *  Inc.;
 */

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.content.Tag;

/**
 * This interface contains the cache tags that are used by default throughout
 * the system.
 */
public interface CacheTag extends Tag {

  /** Special object representing the "any" value */
  Object ANY = new Object();

  String Url = "webl:url";
  String Language = "webl:language";
  String User = "webl:user";
  String Module = "webl:module";
  String Action = "webl:action";
  String Site = "webl:site";
  String Parameters = "webl:parameters";

}
