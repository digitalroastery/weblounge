package ch.entwine.weblounge.bridge.oaipmh;

import org.w3c.dom.Node;

/**
 * Pluggable component to handle OAI-PMH records harvested by the
 * {@link MatterhornHarvester}.
 */
public interface RecordHandler {

  /**
   * Return the OAI-PMH metadata prefix this handler deals with. See <a href=
   * "http://www.openarchives.org/OAI/openarchivesprotocol.html#MetadataNamespaces"
   * >this section</a> of the OAI-PMH specification for more details.
   */
  String getMetadataPrefix();

  /**
   * Handle an OAI-PMH record. See section <a
   * href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Record">2.5
   * Record</a> of the OAI-PMH specification.
   */
  void handle(Node record);
}