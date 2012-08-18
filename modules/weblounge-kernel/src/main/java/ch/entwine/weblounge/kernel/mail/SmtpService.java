/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.kernel.mail;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * OSGi service that allows to send e-mails using <code>javax.mail</code>.
 */
public class SmtpService implements ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SmtpService.class);

  /** Parameter prefix common to all "mail" properties */
  private static final String OPT_SMTP_PREFIX = "smtp.";

  /** Parameter suffix for the mail host */
  private static final String OPT_SMTP_HOST = "host";

  /** Parameter suffix for the mail port */
  private static final String OPT_SMTP_PORT = "port";

  /** Parameter suffix for the start tls status */
  private static final String OPT_SMTP_TLS = "starttls";

  /** Parameter suffix for the authentication setting */
  private static final String OPT_SMTP_AUTH = "auth";

  /** Parameter name for the username */
  private static final String OPT_SMTP_USER = "user";

  /** Parameter name for the password */
  private static final String OPT_SMTP_PASSWORD = "password";

  /** Parameter name for the recipient */
  private static final String OPT_SMTP_FROM = "from";

  /** Parameter name for the debugging setting */
  private static final String OPT_SMTP_DEBUG = "debug";

  /** Parameter name for the test setting */
  private static final String OPT_SMTP_TEST = "test";

  /** Default value for the mail server */
  private static final String DEFAULT_SMTP_HOST = "localhost";

  /** Default value for the mail port */
  private static final String DEFAULT_SMTP_PORT = "25";

  /** The mail properties */
  private final Properties mailProperties = new Properties();

  /** The mail host */
  private String mailHost = null;

  /** The mail user */
  private String mailUser = null;

  /** The mail password */
  private String mailPassword = null;

  /** The default mail session */
  private Session defaultMailSession = null;

  /** The mail transport protocol */
  private static final String MAIL_TRANSPORT = "smtp";

  /**
   * Callback from the OSGi <code>ConfigurationAdmin</code> on configuration
   * changes.
   * 
   * @param properties
   *          the configuration properties
   * @throws ConfigurationException
   *           if configuration fails
   */
  @Override
  public void updated(Dictionary properties) throws ConfigurationException {

    // Read the mail server properties
    mailProperties.clear();

    // The mail host is mandatory
    String propName = getConfigurationKey(OPT_SMTP_HOST);
    mailHost = StringUtils.trimToNull((String) properties.get(propName));
    if (mailHost == null) {
      mailHost = DEFAULT_SMTP_HOST;
      logger.debug("Mail server defaults to '{}'", mailHost);
    } else {
      logger.debug("Mail host is {}", mailHost);
    }
    mailProperties.put(getJavaMailSmtpKey(OPT_SMTP_HOST), mailHost);

    // Mail port
    propName = getConfigurationKey(OPT_SMTP_PORT);
    String mailPort = StringUtils.trimToNull((String) properties.get(propName));
    if (mailPort == null) {
      mailPort = DEFAULT_SMTP_PORT;
      logger.debug("Mail server port defaults to '{}'", mailPort);
    } else {
      logger.debug("Mail server port is '{}'", mailPort);
    }
    mailProperties.put(getJavaMailSmtpKey(OPT_SMTP_PORT), mailPort);

    // TSL over SMTP support
    propName = getConfigurationKey(OPT_SMTP_TLS);
    String smtpStartTLSStr = StringUtils.trimToNull((String) properties.get(propName));
    boolean smtpStartTLS = Boolean.parseBoolean(smtpStartTLSStr);
    if (smtpStartTLS) {
      mailProperties.put(getJavaMailSmtpKey(OPT_SMTP_TLS) + ".enable", "true");
      logger.debug("TLS over SMTP is enabled");
    } else {
      logger.debug("TLS over SMTP is disabled");
    }

    // Mail user
    propName = getConfigurationKey(OPT_SMTP_USER);
    mailUser = StringUtils.trimToNull((String) properties.get(propName));
    if (mailUser != null) {
      mailProperties.put(getJavaMailKey(OPT_SMTP_USER), mailUser);
      logger.debug("Mail user is '{}'", mailUser);
    } else {
      logger.debug("Sending mails to {} without authentication", mailHost);
    }

    // Mail password
    propName = getConfigurationKey(OPT_SMTP_PASSWORD);
    mailPassword = StringUtils.trimToNull((String) properties.get(propName));
    if (mailPassword != null) {
      mailProperties.put(getJavaMailKey(OPT_SMTP_PASSWORD), mailPassword);
      logger.debug("Mail password set");
    }

    // Mail sender
    propName = getConfigurationKey(OPT_SMTP_FROM);
    String mailFrom = StringUtils.trimToNull((String) properties.get(propName));
    if (mailFrom == null) {
      try {
        mailFrom = "weblounge@" + InetAddress.getLocalHost().getCanonicalHostName();
        logger.info("Mail sender defaults to '{}'", mailFrom);
      } catch (UnknownHostException e) {
        logger.error("Error retreiving localhost hostname used to create default sender address: {}", e.getMessage());
        throw new ConfigurationException(OPT_SMTP_FROM, "Error retreiving localhost hostname used to create default sender address");
      }
    } else {
      logger.debug("Mail sender is '{}'", mailFrom);
    }
    mailProperties.put(getJavaMailKey(OPT_SMTP_FROM), mailFrom);

    // Authentication
    propName = getConfigurationKey(OPT_SMTP_AUTH);
    mailProperties.put(getJavaMailSmtpKey(OPT_SMTP_AUTH), Boolean.toString(mailUser != null));

    // Mail debugging
    propName = getConfigurationKey(OPT_SMTP_DEBUG);
    String mailDebug = StringUtils.trimToNull((String) properties.get(propName));
    if (mailDebug != null) {
      boolean mailDebugEnabled = Boolean.parseBoolean(mailDebug);
      mailProperties.put(getJavaMailKey(OPT_SMTP_DEBUG), Boolean.toString(mailDebugEnabled));
      logger.info("Mail debugging is {}", mailDebugEnabled ? "enabled" : "disabled");
    }

    defaultMailSession = null;
    logger.info("Mail service configured with {}", mailHost);

    // Test
    propName = getConfigurationKey(OPT_SMTP_TEST);
    String mailTest = StringUtils.trimToNull((String) properties.get(propName));
    if (mailTest != null) {
      try {
        sendTestMessage(mailTest);
      } catch (MessagingException e) {
        logger.error("Error sending test message to " + mailTest + ": " + e.getMessage());
        throw new ConfigurationException(OPT_SMTP_PREFIX + MAIL_TRANSPORT + OPT_SMTP_HOST, "Failed to send test message to " + mailTest);
      }
    }
  }

  /**
   * Returns the default mail session that can be used to create a new message.
   * 
   * @return the default mail session
   */
  public Session getSession() {
    if (defaultMailSession == null) {
      defaultMailSession = Session.getInstance(mailProperties);
    }
    return defaultMailSession;
  }

  /**
   * Creates a new message.
   * 
   * @return the new message
   */
  public MimeMessage createMessage() {
    return new MimeMessage(getSession());
  }

  /**
   * Sends <code>message</code> using the configured transport.
   * 
   * @param message
   *          the message
   * @throws MessagingException
   *           if sending the message failed
   */
  public void send(MimeMessage message) throws MessagingException {
    Transport t = getSession().getTransport(MAIL_TRANSPORT);
    try {
      if (mailUser != null)
        t.connect(mailUser, mailPassword);
      else
        t.connect();
      t.sendMessage(message, message.getAllRecipients());
    } finally {
      t.close();
    }
  }

  /**
   * Method to send a test message.
   * 
   * @throws MessagingException
   *           if sending the message failed
   */
  private void sendTestMessage(String recipient) throws MessagingException {
    MimeMessage message = createMessage();
    message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
    message.setSubject("Test from Weblounge");
    message.setText("Hello world");
    message.saveChanges();
    send(message);
  }

  /**
   * Returns the key as expected in the service configuration.
   * 
   * @param option
   *          the option name
   * @return the full configuration key
   */
  private String getConfigurationKey(String option) {
    return OPT_SMTP_PREFIX + option;
  }

  /**
   * Returns the key as expected by the JavaMail library.
   * 
   * @param option
   *          the option name
   * @return the full configuration key
   */
  private String getJavaMailKey(String option) {
    return "mail." + option;
  }

  /**
   * Returns the key as expected by the JavaMail library configured for the smtp
   * transport.
   * 
   * @param option
   *          the option name
   * @return the full configuration key
   */
  private String getJavaMailSmtpKey(String option) {
    return "mail." + MAIL_TRANSPORT + "." + option;
  }

}