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

package ch.o2it.weblounge.common.impl.util.mailer;

import ch.o2it.weblounge.common.impl.util.encoding.Base64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * An SMTP mail transport backend with support for authentication extension<br>
 * Conforming to RFC2821: "Simple Mail Transfer Protocol" and RFC2554:SMTP
 * Service Extension for Authentication".
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class ESMTPRelay extends SMTPRelay {

  /** The identifier for unknown or no authentication mechanism */
  public final int MECH_UNKNOWN = -1;

  /** The PLAIN authentication mechanism */
  public final int MECH_PLAIN = 0;

  /** The PLAIN authentication mechanism */
  public final int MECH_LOGIN = 1;

  /** The PLAIN authentication mechanism */
  public final int MECH_CRAM_MD5 = 2;

  // TODO: Add MECH_DIGEST_MD5

  /** The authentication mechanism commands */
  public final String[] commands = { "PLAIN", "LOGIN", "CRAM-MD5" };

  /** The username */
  protected String login;

  /** The password */
  protected String password;

  /**
   * @param login
   *          the login name
   * @param pass
   *          the password
   * @throws UnknownHostException
   */
  public ESMTPRelay(String login, String pass) throws UnknownHostException {
    super();
    setCredentials(login, pass);
  }

  /**
   * @param host
   * @param login
   *          the login name
   * @param pass
   *          the password
   * @throws UnknownHostException
   */
  public ESMTPRelay(String host, String login, String pass)
      throws UnknownHostException {
    this(host, DEFAULT_PORT, login, pass);
  }

  /**
   * @param host
   * @param port
   * @param login
   *          the login name
   * @param pass
   *          the password
   * @throws UnknownHostException
   */
  public ESMTPRelay(String host, int port, String login, String pass)
      throws UnknownHostException {
    super(host, port);
    setCredentials(login, pass);
  }

  /**
   * @param addr
   * @param login
   *          the login name
   * @param pass
   *          the password
   */
  public ESMTPRelay(InetAddress addr, String login, String pass) {
    this(addr, DEFAULT_PORT, login, pass);
  }

  /**
   * @param addr
   * @param port
   * @param login
   *          the login name
   * @param pass
   *          the password
   */
  public ESMTPRelay(InetAddress addr, int port, String login, String pass) {
    super(addr, port);
    setCredentials(login, pass);
  }

  /**
   * Sets the login credentials for authenticated smtp.
   */
  public void setCredentials(String login, String password) {
    this.login = login;
    this.password = password;
  }

  public void send(StringBuffer email, Recipient from, Iterator recipients)
      throws IOException, EmailException {
    if (login == null) {
      super.send(email, from, recipients);
    }
    // connect to the relay server
    Socket s = new Socket(addr, port);

    // get the input and output streams
    BufferedReader r = null;
    PrintWriter w = null;

    try {
      r = new BufferedReader(new InputStreamReader(s.getInputStream()));
      w = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
    } catch (IOException e) {
      try {
        s.close();
      } catch (IOException e2) {
      }
      throw e;
    }

    // the smtp protocol
    SMTPReply[] reply = null;
    try {
      reply = nextStep(null, 220, w, r);
      reply = nextStep("EHLO " + s.getLocalAddress().getHostName(), 250, w, r);

      // Check if the server supports authentication
      int authMech = getAuthMech(reply);
      authenticate(authMech, w, r);

      // Proceed with standard SMTP
      nextStep("MAIL FROM: <" + from.getEmail() + ">", 250, w, r);
      while (recipients.hasNext())
        nextStep("RCPT TO: <" + ((Recipient) recipients.next()).getEmail() + ">", 250, w, r);
      nextStep("DATA", 354, w, r);
      // TODO: encode lines with single '.'
      nextStep(email + CRLF + ".", 250, w, r);
      nextStep("QUIT", 221, w, r);
    } finally {
      // close the streams and the connection
      w.close();
      try {
        r.close();
      } catch (IOException e) {
      }
      try {
        s.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Performs a single step in the SMTP protocol.
   * 
   * @param command
   *          the SMTP command to send to the server
   * @param ok
   *          the expected SMTP reply
   * @param w
   *          the writer connected to the server
   * @param r
   *          the reader connected to the server
   * @return the SMTP reply containing status and optional text
   * @throws IOException
   *           if an i/o exception occurs
   * @throws EmailException
   *           if a protocol exception occurs
   */
  protected SMTPReply[] nextStep(String command, int[] ok, PrintWriter w,
      BufferedReader r) throws IOException, EmailException {
    if (command != null) {
      w.write(command + CRLF);
      w.flush();
    }
    SMTPReply[] reply = readResponse(r);
    for (int i = 0; i < reply.length; i++) {
      boolean match = false;
      for (int j = 0; j < ok.length; j++) {
        if (reply[i].getStatus() == ok[j]) {
          match = true;
          break;
        }
      }
      if (!match) {
        throw new EmailException("Unexpected reply from server. Expected: " + ok + ", got: " + reply[i].getStatus() + "-" + reply[i].getText());
      }
    }
    return reply;
  }

  /**
   * Does the ESMTP authentication. Throws an IOException if authentication
   * fails.
   * 
   * @param authMech
   *          the mechanism
   */
  protected void authenticate(int authMech, PrintWriter w, BufferedReader r)
      throws EmailException, IOException {
    if (authMech != -1) {

      // TODO: React to Error "Mechanism too weak"

      SMTPReply[] reply = null;
      switch (authMech) {

      case MECH_LOGIN:
        reply = nextStep("AUTH " + commands[authMech], 334, w, r);

        // Username
        if (reply.length > 0) {
          byte[] prompt64 = Base64Encoder.decode(reply[0].getText());
          String prompt = new String(prompt64, 0, prompt64.length - 1);
          if (prompt.trim().toUpperCase().equals("USERNAME")) {
            reply = nextStep(Base64Encoder.encodeBytes(login.getBytes()), 334, w, r);
          } else {
            throw new EmailException("Expected prompt for username but received '" + prompt + "'");
          }
        }
        // Username
        if (reply.length > 0) {
          byte[] prompt64 = Base64Encoder.decode(reply[0].getText());
          String prompt = new String(prompt64, 0, prompt64.length - 1);
          if (prompt.trim().toUpperCase().equals("PASSWORD")) {
            reply = nextStep(Base64Encoder.encodeBytes(password.getBytes()), 235, w, r);
          } else {
            throw new EmailException("Expected prompt for password but received '" + prompt + "'");
          }
        }
        break;

      case MECH_PLAIN:
        String auth = login + '\0' + login + '\0' + password;
        String auth64 = Base64Encoder.encodeBytes(auth.getBytes());
        reply = nextStep("AUTH PLAIN " + auth64, 235, w, r);
        break;

      case MECH_CRAM_MD5:
        break;
      default:
        throw new IOException("No known authentication method supported by SMTP server");
      }
    }
  }

  /**
   * Selects a suitable auth mech from the server reply that was sent in
   * response to the HELO command. <br>
   * This method either returns one of the constants for
   * <ul>
   * <li>PLAIN</li>
   * <li>LOGIN</li>
   * <li>CRAM-MD5</li>
   * </ul>
   * or returns <code>{@link #MECH_UNKNOWN}</code> if the server does not
   * support authenticated SMTP.
   * 
   * @param reply
   *          the server response
   * @return an auth mech method
   */
  protected int getAuthMech(SMTPReply[] reply) {
    for (int i = 0; i < reply.length; i++) {
      String replyText = reply[i].getText();
      if (replyText != null && replyText.toUpperCase().startsWith("AUTH")) {
        StringTokenizer tok = new StringTokenizer(replyText, " ");
        tok.nextToken();
        while (tok.hasMoreTokens()) {
          String authMech = tok.nextToken().trim();
          if (authMech.toUpperCase().equals(commands[MECH_LOGIN])) {
            return MECH_LOGIN;
          } else if (authMech.toUpperCase().equals(commands[MECH_PLAIN])) {
            return MECH_PLAIN;
            /*
             * } else if
             * (authMech.toUpperCase().equals(commands[MECH_CRAM_MD5])) { return
             * MECH_CRAM_MD5;
             */
          }
        }
      }
    }
    return MECH_UNKNOWN;
  }

}