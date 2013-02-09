<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns="http://www.entwinemedia.com/weblounge/3.0/module" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <module xmlns="http://www.entwinemedia.com/weblounge/3.0/module" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.entwinemedia.com/weblounge/3.0/module http://www.entwinemedia.com/xsd/weblounge/3.0/module.xsd">
      <xsl:attribute name="id"><xsl:value-of select="module/@id" /></xsl:attribute>

      <enable>
        <xsl:value-of select="module/enable" />
      </enable>

      <name>
        <xsl:value-of select="module/name[1]" />
      </name>

      <pagelets>
        <xsl:apply-templates select="module/renderers/renderer" />
      </pagelets>

      <actions>
        <xsl:apply-templates select="module/actions/action" />
      </actions>

    </module>
  </xsl:template>

  <xsl:template match="renderer">
    <pagelet>
      <xsl:attribute name="id"><xsl:value-of select="./@id" /></xsl:attribute>
      <xsl:attribute name="composeable"><xsl:value-of select="./@composeable" /></xsl:attribute>
      <name>
        <xsl:value-of select="./name[1]" />
      </name>
      <renderer>
        <xsl:text>file://${module.root}/</xsl:text>
        <xsl:value-of select="jsp/file"></xsl:value-of>
      </renderer>
      <xsl:if test="editor">
      <editor>
        <xsl:text>file://${module.root}/</xsl:text>
        <xsl:value-of select="editor/jsp"></xsl:value-of>
      </editor>
      </xsl:if>
      <recheck>
        <xsl:value-of select="./recheck"></xsl:value-of>
      </recheck>
      <valid>
        <xsl:value-of select="./valid"></xsl:value-of>
      </valid>
    </pagelet>
  </xsl:template>

  <xsl:template match="action">
    <action>
      <xsl:attribute name="id"><xsl:value-of select="./@id" /></xsl:attribute>
      <name>
        <xsl:value-of select="./name[1]" />
      </name>
      <class>
        <xsl:value-of select="handler/class"></xsl:value-of>
      </class>
      <mountpoint>
        <xsl:value-of select="mountpoint"></xsl:value-of>
      </mountpoint>
      <page>
        <xsl:value-of select="target"></xsl:value-of>
      </page>
    </action>
  </xsl:template>

</xsl:stylesheet>