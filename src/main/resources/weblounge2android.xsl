<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" omit-xml-declaration="no" indent="true" encoding="utf-8" />

  <xsl:template match="/">
    <resources>
      <xsl:apply-templates select="i18n/message" />
    </resources>
  </xsl:template>

  <xsl:template match="message">
    <string>
      <xsl:attribute name="name"><xsl:value-of select="./@name" /></xsl:attribute>
      <xsl:value-of select="value" />
    </string>
  </xsl:template>

</xsl:stylesheet>