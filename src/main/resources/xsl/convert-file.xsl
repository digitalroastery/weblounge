<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" omit-xml-declaration="no" indent="true" encoding="utf-8" standalone="yes" cdata-section-elements="title description subject type coverage rights text property filename" />

  <xsl:include href="./utils.xsl" />

  <xsl:param name="fileid" />
  <xsl:param name="uuid" />
  <xsl:param name="path" />

  <xsl:variable name="adminuserid">
    <xsl:text>admin</xsl:text>
  </xsl:variable>
  <xsl:variable name="adminusername">
    <xsl:text>Administrator</xsl:text>
  </xsl:variable>

  <xsl:template match="/">
    <file xmlns="http://www.o2it.ch/weblounge/3.0/file" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.o2it.ch/weblounge/3.0/file http://www.o2it.ch/xsd/weblounge/3.0/file.xsd">
      <xsl:attribute name="id">
        <xsl:value-of select="$uuid" />
      </xsl:attribute>
      <xsl:attribute name="path">
        <xsl:value-of select="$path" />
      </xsl:attribute>
      <xsl:attribute name="version">live</xsl:attribute>
      <xsl:apply-templates select="collection/header" />
      <body>
        <content language="de" original="true">
          <created>
            <date>
              <xsl:call-template name="formatdate">
                <xsl:with-param name="DateTime" select="/collection/entry[@id=$fileid]/created" />
              </xsl:call-template>
            </date>
            <xsl:call-template name="user">
              <xsl:with-param name="userid" select="/collection/entry[@id=$fileid]/modified/user" />
            </xsl:call-template>
          </created>
          <xsl:copy-of select="/collection/entry[@id=$fileid]/filename" />
          <xsl:copy-of select="/collection/entry[@id=$fileid]/mimetype" />
          <xsl:copy-of select="/collection/entry[@id=$fileid]/size" />
        </content>
      </body>
    </file>
  </xsl:template>

  <xsl:template match="header">
    <head>
      <promote>true</promote>
      <index>true</index>
      <metadata>
        <title language="de">
          <xsl:value-of select="/collection/entry[@id=$fileid]/name"></xsl:value-of>
        </title>
        <xsl:apply-templates select="/collection/entry[@id=$fileid]/keywords" />
      </metadata>
      <security>
        <owner>
          <xsl:call-template name="user">
            <xsl:with-param name="userid" select="/collection/entry[@id=$fileid]/security/owner" />
          </xsl:call-template>
        </owner>
      </security>
      <created>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="/collection/entry[@id=$fileid]/created" />
          </xsl:call-template>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="/collection/entry[@id=$fileid]/modified/user" />
        </xsl:call-template>
      </created>
      <modified>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="/collection/entry[@id=$fileid]/modified/date" />
          </xsl:call-template>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="/collection/entry[@id=$fileid]/modified/user" />
        </xsl:call-template>
      </modified>
      <published>
        <from>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="/collection/entry[@id=$fileid]/publish/from" />
          </xsl:call-template>
        </from>
        <to>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="/collection/entry[@id=$fileid]/publish/to" />
          </xsl:call-template>
        </to>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="/collection/entry[@id=$fileid]/modified/user" />
        </xsl:call-template>
      </published>
    </head>
  </xsl:template>

  <xsl:template match="header/security/permission">
    <restrict>
      <permission>
        <xsl:attribute name="realm">weblounge</xsl:attribute>
        <xsl:value-of select="substring-after(@id,':')" />
      </permission>
      <group>
        <xsl:attribute name="realm">weblounge</xsl:attribute>
        <xsl:value-of select="substring-after(.,':')" />
        <xsl:text>s</xsl:text>
      </group>
    </restrict>
  </xsl:template>

  <xsl:template match="keywords">
    <xsl:apply-templates select="keyword" />
  </xsl:template>

  <xsl:template match="keyword">
    <subject>
      <xsl:value-of select="." />
    </subject>
  </xsl:template>

  <xsl:template match="header/title">
    <xsl:copy-of select="." />
  </xsl:template>

  <xsl:template match="body">
    <body>
      <xsl:apply-templates select="./composer" />
    </body>
  </xsl:template>

  <xsl:template match="composer">
    <composer>
      <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
      <xsl:apply-templates select="./pagelet" />
    </composer>
  </xsl:template>

  <xsl:template match="pagelet">
    <pagelet>
      <xsl:attribute name="module"><xsl:value-of select="@module" /></xsl:attribute>
      <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
      <security>
        <owner>
          <xsl:call-template name="user">
            <xsl:with-param name="userid" select="security/owner" />
          </xsl:call-template>
        </owner>
      </security>
      <created>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="content/modified/user" />
        </xsl:call-template>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="content/modified/date" />
          </xsl:call-template>
        </date>
      </created>
      <published>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="security/owner" />
        </xsl:call-template>
        <from>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="publish/from" />
          </xsl:call-template>
        </from>
        <to>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="publish/to" />
          </xsl:call-template>
        </to>
      </published>
      <xsl:apply-templates select="./content" />
      <xsl:apply-templates select="./properties" />
    </pagelet>
  </xsl:template>

  <xsl:template match="content">
    <locale>
      <xsl:attribute name="language"><xsl:value-of select="@language" /></xsl:attribute>
      <xsl:if test="@original = 'true'">
        <xsl:attribute name="original"><xsl:value-of select="@original" /></xsl:attribute>
      </xsl:if>
      <modified>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="modified/user" />
        </xsl:call-template>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="modified/date" />
          </xsl:call-template>
        </date>
      </modified>
      <xsl:copy-of select="./text" />
    </locale>
  </xsl:template>

  <xsl:template match="property">
    <property>
      <xsl:attribute name="id"><xsl:value-of select="./@id" /></xsl:attribute>
      <xsl:value-of select="." />
    </property>
  </xsl:template>

  <xsl:template name="user">
    <xsl:param name="userid"></xsl:param>
    <xsl:choose>
      <xsl:when test="$userid = 'www'">
        <user>
          <xsl:attribute name="id"><xsl:value-of select="$adminuserid" /></xsl:attribute>
          <xsl:attribute name="realm">weblounge</xsl:attribute>
          <xsl:value-of select="$adminusername" />
        </user>
      </xsl:when>
      <xsl:otherwise>
        <user>
          <xsl:attribute name="id"><xsl:value-of select="$userid" /></xsl:attribute>
          <xsl:attribute name="realm">weblounge</xsl:attribute>
          <xsl:call-template name="capitalize">
            <xsl:with-param name="data" select="substring-before($userid,'.')" />
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <xsl:call-template name="capitalize">
            <xsl:with-param name="data" select="substring-after($userid,'.')" />
          </xsl:call-template>
        </user>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>