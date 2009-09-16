/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This pro gram is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An SMTP mail transport backend.<br>
 * Conforming to RFC2821: "Simple Mail Transfer Protocol"
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 * @see http://ftp.rfc-editor.org/in-notes/rfc2821.txt
 */

public class SMTPRelay implements Transport {

  /** the default SMTP port */
  protected static final int DEFAULT_PORT = 25;

  /** the line ending */
  protected static final String CRLF = "\r\n";

  /** the address of the relay server */
  protected InetAddress addr = null;

  /** the port used to relay */
  protected int port = 0;

  /**
   * @throws UnknownHostException
   */
  public SMTPRelay() throws UnknownHostException {
    this(InetAddress.getLocalHost());
  }

  /**
   * @param host
   * @throws UnknownHostException
   */
  public SMTPRelay(String host) throws UnknownHostException {
    this(host, DEFAULT_PORT);
  }

  /**
   * @param host
   * @param port
   * @throws UnknownHostException
   */
  public SMTPRelay(String host, int port) throws UnknownHostException {
    this(InetAddress.getByName(host), port);
  }

  /**
   * @param addr
   */
  public SMTPRelay(InetAddress addr) {
    this(addr, DEFAULT_PORT);
  }

  /**
   * @param addr
   * @param port
   */
  public SMTPRelay(InetAddress addr, int port) {
    this.addr = addr;
    this.port = port;
  }

  /**
   * Sends the mail through an SMTP server.
   * 
   * @see Transport#send(java.lang.StringBuffer, Recipient, java.util.Iterator)
   */
  public void send(StringBuffer email, Recipient from, Iterator recipients)
      throws IOException, EmailException {
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
    try {
      nextStep(null, 220, w, r);
      nextStep("HELO " + s.getLocalAddress().getHostName(), 250, w, r);
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
  protected SMTPReply[] nextStep(String command, int ok, PrintWriter w,
      BufferedReader r) throws IOException, EmailException {
    if (command != null) {
      w.write(command + CRLF);
      w.flush();
    }
    SMTPReply[] reply = readResponse(r);
    for (int i = 0; i < reply.length; i++) {
      if (reply[i].getStatus() != ok) {
        throw new EmailException("Unexpected reply from server. Expected: " + ok + ", got: " + reply[i].getStatus() + "-" + reply[i].getText());
      }
    }
    return reply;
  }

  /**
   * Reads the server response for the last command and generate appropriate
   * response objects.
   * 
   * @param reader
   *          the response reader
   * @return the response objects
   * @throws IOException
   */
  protected SMTPReply[] readResponse(BufferedReader reader) throws IOException {
    String line = null;
    List lines = new ArrayList();
    List replyList = new ArrayList();
    boolean firstLine = true;

    // Read status
    do {
      line = null;
      if (firstLine || reader.ready()) {
        line = reader.readLine();
        firstLine = false;
      }
      if (line == null || (line.length() >= 4 && (line.charAt(3) == '-' || line.charAt(3) == ' '))) {
        if (lines.size() > 0) {
          replyList.add(new SMTPReply((String[]) lines.toArray(new String[lines.size()])));
        }
        lines = new ArrayList();
      }
      lines.add(line);
    } while (line != null);
    return (SMTPReply[]) replyList.toArray(new SMTPReply[replyList.size()]);
  }

  /**
   * Returns the relay's nost name.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return addr.getCanonicalHostName();
  }

  /**
   * This class encapsulates a server reply to an SMTP command.
   * 
   * @author Tobias Wunden
   * @version 1.0
   */
  protected class SMTPReply {

    /** The return code */
    private int status_ = 200;

    /** Additional reply */
    private String reply_;

    public SMTPReply(String[] response) {
      String line = null;
      StringBuffer reply = new StringBuffer();

      // Read status
      for (int i = 0; i < response.length; i++) {
        line = response[i];
        if (status_ == 200 && line != null && line.length() >= 4 && (line.charAt(3) == '-' || line.charAt(3) == ' ')) {
          status_ = Integer.parseInt(line.substring(0, 3));
          reply.append(line.substring(4).trim());
        } else {
          reply.append(line);
        }
        reply.append("\r\n");
      }
      reply_ = reply.toString();
    }

    /**
     * Returns the return code.
     * 
     * @return the status
     */
    public int getStatus() {
      return status_;
    }

    /**
     * Returns the text that followed the status. If no text followed, then this
     * method returns <code>null</code>.
     * 
     * @return the text
     */
    public String getText() {
      return reply_;
    }

    public String toString() {
      return "[" + status_ + "] " + reply_;
    }

  }

}