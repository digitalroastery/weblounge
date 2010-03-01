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

package ch.o2it.weblounge.test;

import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Test job that will print a friendly greeting to <code>System.out</code>.
 */
@SuppressWarnings("unchecked")
public class GreeterJob implements Job {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(GreeterJob.class);

  /** Hello world in many languages */
  protected static Map.Entry<String, String>[] greetings = null;

  static {
    Map<String, String> hellos = new HashMap<String, String>();
    hellos.put("arabic", "مرحبا أنا أشعر بالسعادة العالم اليوم");
    hellos.put("bulgarian", "Здравейте аз съм щастлив света днес");
    hellos.put("croation", "Hello world ja sam danas sretan");
    hellos.put("czech", "hello world Jsem rád, dnes");
    hellos.put("danish", "hello world Jeg er glad for i dag");
    hellos.put("dutch", "hello world Ik ben blij vandaag");
    hellos.put("finish", "Hello world Olen onnellinen tänään");
    hellos.put("french", "Bonjour, je suis heureux aujourd'hui");
    hellos.put("german", "Hallo Welt, ich bin heute glücklich");
    hellos.put("greek", "Γεια σας κόσμο χαρά σήμερα");
    hellos.put("hebrew", "שלום אני שמח בעולם היום");
    hellos.put("italian", "Ciao, sono felice di oggi");
    hellos.put("japanese", "を今日は満足している");
    hellos.put("korean", "안녕하세요 오늘은 행복 해요");
    hellos.put("norwegian", "hello world i am glad i dag");
    hellos.put("polish", "Hello I am happy dzisiejszym świecie");
    hellos.put("portugese", "Olá mundo eu estou feliz hoje");
    hellos.put("romanian", "Eu salut lume sunt fericit astăzi");
    hellos.put("russian", "Здравствуйте, я рада Мир сегодня");
    hellos.put("spanish", "Me complace saludar el mundo de hoy");
    hellos.put("swedish", "Hallå världen Jag är glad idag");
    hellos.put("swiss german", "Hoi zäme, ich bi guet z'wäg");
    hellos.put("english", "hello world i am happy today");
    greetings = hellos.entrySet().toArray(new Map.Entry[hellos.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.Job#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {
    int index = (int) ((greetings.length - 1)* Math.random());
    Map.Entry<String, String> entry = greetings[index];
    try {
      log_.info(new String(entry.getValue().getBytes("UTF-8")) + " (" + entry.getKey() + ")");
    } catch (UnsupportedEncodingException e) {
      log_.error("Cant' believe that utf-8 is not supported on this platform!", e);
    }
  }

}
