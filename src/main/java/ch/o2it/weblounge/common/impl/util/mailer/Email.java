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

import ch.o2it.weblounge.common.impl.util.encoding.QuotedPrintableEnconder;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class Email implements Serializable {

  /** message priority: HIGHEST */
  public static final int HIGHEST = 5;

  /** message priority: HIGH */
  public static final int HIGH = 4;

  /** message priority: NORMAL */
  public static final int NORMAL = 3;

  /** message priority: LOW */
  public static final int LOW = 2;

  /** message priority: LOWEST */
  public static final int LOWEST = 1;

  /** address type: TO */
  public static final int TO = 0;

  /** address type: CC */
  public static final int CC = 1;

  /** address type: BCC */
  public static final int BCC = 2;

  /** the mailer identifier */
  private static final String MAILER = "YAM Mailer! ($Revision: 1059 $, $Date: 2009-09-10 17:55:59 +0200 (Thu, 10 Sep 2009) $)";

  /** line terminator */
  private static final String CRLF = "\r\n";

  /** the strings for the message priorities */
  private static final String PRIORITY[] = { " (lowest)", " (low)", " (normal)", " (high)", " (highest)" };

  /** the recipients */
  private List to = new ArrayList();

  /** carbon copy */
  private List cc = new ArrayList();

  /** blind carbon copy */
  private List bcc = new ArrayList();

  /** additional header */
  private List headers = new ArrayList();

  /** attachments */
  private List attachments = new ArrayList();

  /** mail parts */
  private List alternatives = new ArrayList();

  /** the sender */
  private Recipient from;

  /** reply to */
  private Recipient replyTo;

  /** the subject */
  private String subject;

  /** the importance */
  private int priority = NORMAL;

  /** return receipt */
  private boolean returnReceipt = false;

  /** the external mailer */
  private Transport mailer = null;

  /**
   * Creates a new <code>Email</code>.
   */
  public Email() {
  }

  /**
   * Creates a new <code>Email</code> with the given mailer backend.
   * 
   * @param mailer
   *          the backend used to send the mail
   */
  public Email(Transport mailer) {
    this.mailer = mailer;
  }

  /**
   * reset all data.
   */
  public void reset() {
    from = null;
    subject = null;
    replyTo = null;
    headers.clear();
    to.clear();
    cc.clear();
    bcc.clear();
    attachments.clear();
    alternatives.clear();
    priority = NORMAL;
    returnReceipt = false;
  }

  /**
   * Returns the TO: addresses.
   * 
   * @return the TO: addresses
   */
  public Iterator getTo() {
    return to.iterator();
  }

  /**
   * Returns the CC: addresses.
   * 
   * @return the CC: addresses
   */
  public Iterator getCC() {
    return cc.iterator();
  }

  /**
   * Returns the BCC: addresses.
   * 
   * @return the BCC: addresses
   */
  public Iterator getBCC() {
    return bcc.iterator();
  }

  /** Clears all TO: addresses. */
  public void clearTo() {
    to.clear();
  }

  /** Clears all CC: addresses. */
  public void clearCC() {
    cc.clear();
  }

  /** Clears all BCC: addresses. */
  public void clearBCC() {
    bcc.clear();
  }

  /**
   * Adds a recipient to the TO: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipient(String email) throws InvalidAddressException {
    addRecipient(email, TO);
  }

  /**
   * Adds a recipient to the TO: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @param name
   *          the name of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipient(String email, String name)
      throws InvalidAddressException {
    addRecipient(email, name, TO);
  }

  /**
   * Adds a recipient to the CC: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipientCc(String email) throws InvalidAddressException {
    addRecipient(email, CC);
  }

  /**
   * Adds a recipient to the CC: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @param name
   *          the name of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipientCc(String email, String name)
      throws InvalidAddressException {
    addRecipient(email, name, CC);
  }

  /**
   * Adds a recipient to the BCC: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipientBcc(String email) throws InvalidAddressException {
    addRecipient(email, BCC);
  }

  /**
   * Adds a recipient to the BCC: addresses.
   * 
   * @param email
   *          the address of the recipient
   * @param name
   *          the name of the recipient
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipientBcc(String email, String name)
      throws InvalidAddressException {
    addRecipient(email, name, BCC);
  }

  /**
   * Adds a recipient with the given address type.
   * 
   * @param email
   *          the address of the recipient
   * @param type
   *          the address type (TO, CC or BCC)
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipient(String email, int type)
      throws InvalidAddressException {
    int lt = email.indexOf("<");
    int gt = email.indexOf(">");
    if (lt > -1 && gt > -1 && lt < gt) {
      addRecipient(email.substring(lt + 1, gt).trim(), email.substring(0, lt - 1).trim(), type);
    } else {
      addRecipient(email, null, type);
    }
  }

  /**
   * Adds a recipient with the given address type.
   * 
   * @param email
   *          the address of the recipient
   * @param name
   *          the name of the recipient
   * @param type
   *          the address type (TO, CC or BCC)
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void addRecipient(String email, String name, int type)
      throws InvalidAddressException {
    Recipient r = new Recipient(email, name);
    switch (type) {
    case TO:
      to.add(r);
      break;
    case CC:
      cc.add(r);
      break;
    case BCC:
      bcc.add(r);
      break;
    default:
      break;
    }
  }

  /**
   * Returns the headres of the email.
   * 
   * @return the headers of htis email
   */
  public Iterator getHeaders() {
    return headers.iterator();
  }

  /**
   * Adds a custom header to the message.
   * 
   * @param header
   *          the custom header to add to the message
   */
  public void addHeader(String header) {
    headers.add(header);
  }

  /** Clears all custom headers. */
  public void clearHeaders() {
    headers.clear();
  }

  /**
   * Returns all message alternatives.
   * 
   * @return the message alternatives
   */
  public Iterator getAlternatives() {
    return alternatives.iterator();
  }

  /** Clears the message alternatives */
  public void clearAlternatives() {
    alternatives.clear();
  }

  /**
   * Adds an alternative to the message.
   * 
   * @param contentType
   *          the content type of the alternative
   * @param body
   *          the content body of the alternative
   */
  public void addAlternative(String contentType, String body) {
    if (body != null) {
      alternatives.add(new Alternative(contentType, body));
    }
  }

  /**
   * Sets the primary message body of the message.
   * 
   * @param contentType
   *          the content type of the main body
   * @param body
   *          the main body
   */
  public void setBody(String contentType, String body) {
    clearAlternatives();
    addAlternative(contentType, body);
  }

  /**
   * Returns the formatted message body.
   * 
   * @return the formatted message body
   */
  public String getBody() {
    StringBuffer body = new StringBuffer();
    formatBody(body);
    return body.toString();
  }

  /**
   * Returns the attachaments of the message.
   * 
   * @return all attachments of this message
   */
  public Iterator getAttachments() {
    return attachments.iterator();
  }

  /** clears all attachements of this message. */
  public void clearAttachments() {
    attachments.clear();
  }

  /**
   * Adds an attachment to the message.
   * 
   * @param file
   *          the name of the file containsing the attachament
   */
  public void addAttachment(String file) {
    attachments.add(file);
  }

  /**
   * Returns the sender of the message.
   * 
   * @return the sender of this message
   */
  public Recipient getFrom() {
    return from;
  }

  /**
   * Returns the priority of the message.
   * 
   * @return the priority of this message
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Returns the ReplyTo: address of the message.
   * 
   * @return the ReplayTo: address of this message
   */
  public Recipient getReplyTo() {
    return replyTo;
  }

  /**
   * Returns the subject if the message.
   * 
   * @return the subject of this message
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the sender of the message.
   * 
   * @param email
   *          the sender address
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void setFrom(String email) throws InvalidAddressException {
    int delimiter = email.indexOf("<");
    if (delimiter > 0) {
      int delimiterEnd = email.indexOf(">");
      if (delimiterEnd < 0) {
        throw new InvalidAddressException("Unable to extract address!", email);
      }
      setFrom(email.substring(delimiter + 1, delimiterEnd), email.substring(0, delimiter - 1).trim());
      return;
    }
    setFrom(email, null);
  }

  /**
   * Sets the sender of the message.
   * 
   * @param email
   *          the sender address
   * @param name
   *          the sender name
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void setFrom(String email, String name) throws InvalidAddressException {
    this.from = new Recipient(email, name);
  }

  /**
   * Sets the priority of the message.
   * 
   * @param priority
   *          the priority of this message
   */
  public void setPriority(int priority) {
    if (priority < LOWEST || priority > HIGHEST)
      throw new IllegalArgumentException("Invalid priority");
    this.priority = priority;
  }

  /**
   * Sets the ReplyTo: address of the message.
   * 
   * @param email
   *          the ReplyTo: address of this message
   * @throws InvalidAddressException
   *           if the address is inavlid
   */
  public void setReplyTo(String email) throws InvalidAddressException {
    setReplyTo(email, null);
  }

  /**
   * Sets the ReplyTo: address of the message.
   * 
   * @param email
   *          the ReplyTo: address of this message
   * @param name
   *          the name of the ReplyTo: address
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  public void setReplyTo(String email, String name)
      throws InvalidAddressException {
    replyTo = new Recipient(email, name);
  }

  /**
   * Sets the subject of the message.
   * 
   * @param subject
   *          the subject of this message
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Checks whether a retur receipt should be generated for the message.
   * 
   * @return <code>true</code> if a return receipt should be generated
   */
  public boolean hasReturnReceipt() {
    return returnReceipt;
  }

  /**
   * Sets the return receipt generation flag on the message.
   * 
   * @param returnReceipt
   *          <code>true</code> if a return receipt should be generated
   */
  public void setReturnReceipt(boolean returnReceipt) {
    this.returnReceipt = returnReceipt;
  }

  /**
   * Sets the mailer backend of the message.
   * 
   * @param mailer
   *          the mailer backend of this message
   */
  public void setTransport(Transport mailer) {
    this.mailer = mailer;
  }

  /**
   * Formats the complete message body.
   * 
   * @param body
   *          the buffer to append the formatted message body to
   */
  private void formatBody(StringBuffer body) {
    String altBoundary = "----------------ALT" + Long.toHexString(System.currentTimeMillis());
    /*
     * String mixBoundary = "----------------MIX" +
     * Long.toHexString(System.currentTimeMillis()); if (alternatives.size() > 0
     * && attachments.size() > 0) { body.append(
     * "Content-Type: multipart/mixed;" + CRLF + "    boundary=\"" + mixBoundary
     * + "\"" + CRLF); body.append(CRLF);
     * body.append("This is a multi-part message in MIME format." + CRLF); }
     */
    if (alternatives.size() == 0) {
      // do nothing!
    } else if (alternatives.size() == 1) {
      ((Alternative) alternatives.get(0)).format(body);
    } else {
      body.append("Content-Type: multipart/alternative;" + CRLF + "    boundary=\"" + altBoundary + "\"" + CRLF);
      body.append(CRLF);
      body.append("This is a multi-part message in MIME format." + CRLF);
      for (Iterator iter = alternatives.iterator(); iter.hasNext();) {
        Alternative alt = (Alternative) iter.next();
        body.append("--" + altBoundary + CRLF);
        alt.format(body);
        body.append(CRLF);
      }
      body.append("--" + altBoundary + "--" + CRLF);
    }

    // TODO: Body: handle attachments
  }

  /**
   * Formats the message headers.
   * 
   * @param headers
   *          the buffer to append the formatted message headers to
   * @throws EmailException
   *           if the headers are invalid
   */
  private void formatHeader(StringBuffer headers) throws EmailException {
    // check whether the mail has a sender
    if (from == null)
      throw new EmailException("Must specify sender");

    // check whether the mail has a recipient
    if (to.size() == 0)
      throw new EmailException("Must specify recipients");

    // build all the headers
    headers.append("Message-ID: <" + Long.toHexString(System.currentTimeMillis()) + from.email.substring(from.email.indexOf('@')) + ">" + CRLF);
    headers.append("Date: " + new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date()) + new DecimalFormat("' +'0000;' -'0000").format(
    // ~TODO: Fix timezone offset: +0950 -> +0930
    TimeZone.getDefault().getRawOffset() / 36000)
    // TimeZone.getTimeZone("Australia/Darwin").getRawOffset() / 36000)
    + CRLF);
    headers.append("From: " + from.toString() + CRLF);
    if (replyTo != null)
      headers.append("Reply-To: " + replyTo.toString() + CRLF);
    if (returnReceipt)
      headers.append("Disposition-Notification-To: " + ((replyTo != null) ? replyTo.toString() : from.toString()) + CRLF);
    if (subject != null)
      headers.append("Subject: " + QuotedPrintableEnconder.encodeHeader(subject) + CRLF);
    if (priority != NORMAL)
      headers.append("X-Priority: " + priority + PRIORITY[priority - 1] + CRLF);
    headers.append("X-Mailer: " + MAILER + CRLF);
    headers.append("Mime-Version: 1.0" + CRLF);
    headers.append(formatAddresses(to, "To: "));
    headers.append(formatAddresses(cc, "Cc: "));
    for (Iterator iter = this.headers.iterator(); iter.hasNext();)
      headers.append(QuotedPrintableEnconder.encodeHeader((String) iter.next()) + CRLF);
  }

  /**
   * Formats a list of addresses for inclusion in message header.
   * 
   * @param list
   *          the list of addresses to format
   * @param header
   *          the name of the message header
   * @return the formatted message header
   */
  private StringBuffer formatAddresses(List list, String header) {
    StringBuffer addr = new StringBuffer();
    if (list.size() == 0)
      return addr;
    addr.append(header);
    String recipient = null;
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      recipient = ((Recipient) iter.next()).toString();
      if ((addr.length() % 72) + recipient.length() > 72)
        addr.append(CRLF + "    ");
      addr.append(recipient);
      if (iter.hasNext())
        addr.append(", ");
    }
    return addr.append(CRLF);
  }

  /**
   * Formats the whole message according to RFC822.
   * 
   * @param mail
   *          the buffer to append the formatted mail to
   * @throws EmailException
   *           if the message is inavlid
   */
  private void formatMail(StringBuffer mail) throws EmailException {
    formatHeader(mail);
    formatBody(mail);
  }

  /**
   * Sends the message using the specified transport backend.
   * 
   * @throws IOException
   *           if an I/O exception occurs
   * @throws EmailException
   *           if the message is invalid
   */
  public void send() throws IOException, EmailException {
    // calculate the email
    StringBuffer mail = new StringBuffer();
    formatMail(mail);

    // check whether we should use a different transport
    if (mailer == null)
      throw new EmailException("No mail transport backend spcified.");
    List recipients = new ArrayList();
    recipients.addAll(to);
    recipients.addAll(cc);
    recipients.addAll(bcc);
    mailer.send(mail, from, recipients.iterator());

  }

  /**
   * Encapsulates a MIME alternative mail body.
   * 
   * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep
   *          2009) $
   * @author Daniel Steiner
   */
  private class Alternative implements Serializable {
    /** the content type of the alternative */
    protected String contentType;

    /** the content of the alternative */
    protected String body;

    /**
     * Creates a new MIME alternative.
     * 
     * @param contentType
     *          the content type
     * @param body
     *          the message body
     */
    protected Alternative(String contentType, String body) {
      this.contentType = contentType;
      this.body = body;
    }

    /**
     * Formats a message alternative.
     * 
     * @param alt
     *          the buffer to append the formatted alternative to
     */
    protected void format(StringBuffer alt) {
      alt.append("Content-Type: " + contentType + "; charset=ISO-8859-1; format=flowed" + CRLF);
      if (QuotedPrintableEnconder.needsEncoding(body)) {
        alt.append("Content-Transfer-Encoding: quoted-printable" + CRLF);
        alt.append(CRLF);
        alt.append(QuotedPrintableEnconder.encodeBody(body));
      } else {
        // TODO: linebreaks?
        alt.append("Content-Transfer-Encoding: 7bit" + CRLF);
        alt.append(CRLF);
        alt.append(body);
      }
    }
  }
}
