<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns="http://www.o2it.ch/weblounge/3.0/page" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:importer="ch.o2it.weblounge.tools.importer.Importer" exclude-result-prefixes="importer">
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="utf-8" standalone="yes" cdata-section-elements="title description subject type coverage rights text property" />

  <xsl:param name="uuid" />
  <xsl:param name="path" />
  
  <xsl:variable name="adminuserid">
    <xsl:text>admin</xsl:text>
  </xsl:variable>
  <xsl:variable name="adminusername">
    <xsl:text>Administrator</xsl:text>
  </xsl:variable>

  <xsl:template match="/">
    <page xmlns="http://www.o2it.ch/weblounge/3.0/page" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.o2it.ch/weblounge/3.0/page http://www.o2it.ch/xsd/weblounge/3.0/page.xsd">
      <xsl:attribute name="id">
        <xsl:value-of select="$uuid" />
      </xsl:attribute>
      <xsl:attribute name="path">
        <xsl:value-of select="$path" />
      </xsl:attribute>
      <xsl:attribute name="version"><xsl:value-of select="page/@version" /></xsl:attribute>
      <xsl:apply-templates select="page/header" />
      <xsl:apply-templates select="page/body" />
    </page>
  </xsl:template>

  <xsl:template match="header">
    <head>
      <template>
        <xsl:choose>
          <xsl:when test="string-length(renderer) = 0">default</xsl:when>
          <xsl:otherwise><xsl:value-of select="renderer" /></xsl:otherwise>
        </xsl:choose>
      </template>
      <xsl:copy-of select="layout"></xsl:copy-of>
      <promote>true</promote>
      <index>true</index>
      <metadata>
        <xsl:apply-templates select="./title" />
        <xsl:apply-templates select="keywords" />
      </metadata>
      <security>
        <owner>
          <xsl:call-template name="user">
            <xsl:with-param name="userid" select="security/owner" />
          </xsl:call-template>
        </owner>
      </security>
      <created>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="modified/date" />
          </xsl:call-template>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="modified/user" />
        </xsl:call-template>
      </created>
      <modified>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="modified/date" />
          </xsl:call-template>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="modified/user" />
        </xsl:call-template>
      </modified>
      <published>
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
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="modified/user" />
        </xsl:call-template>
      </published>
    </head>
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
          <xsl:with-param name="userid" select="content[@original='true']/modified/user" />
        </xsl:call-template>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="content[@original='true']/modified/date" />
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

  <xsl:template match="properties">
    <properties>
      <xsl:if test="property[@id='partition'] and property[@id='path']">
        <property id="pageid">
          <xsl:value-of select="importer:getUUID(property[@id='partition'], property[@id='path'])" />
        </property>
      </xsl:if>
      <xsl:if test="not(property[@id='partition']) and property[@id='path']">
        <property id="resourceid">
          <xsl:value-of select="importer:getUUID(property[@id='path'])" />
        </property>
      </xsl:if>
      <xsl:apply-templates select="./property" />
    </properties>
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
      <xsl:when test="$userid = 'www' or $userid = 'guest'">
        <user>
          <xsl:attribute name="id"><xsl:value-of select="$adminuserid" /></xsl:attribute>
          <xsl:attribute name="realm">weblounge</xsl:attribute>
          <xsl:value-of select="$adminusername" />
        </user>
      </xsl:when>
      <xsl:when test="$userid = 'balsiger'">
        <user>
          <xsl:attribute name="id">evelyne.balsiger</xsl:attribute>
          <xsl:attribute name="realm">weblounge</xsl:attribute>
          <xsl:text>Evelyne Balsiger</xsl:text>
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

  <xsl:template name="capitalize">
    <xsl:param name="data" />
    <!-- generate first character -->
    <xsl:value-of select="translate(substring($data,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
    <!-- generate remaining string -->
    <xsl:value-of select="substring($data,2,string-length($data)-1)" />
  </xsl:template>

  <xsl:template name="formatdate">
    <xsl:param name="DateTime" />
    <xsl:if test="string-length($DateTime) != 0">
      <!-- expected date format 2006/01/17 19:05:41 GMT -->
      <!-- new date format 2006-01-17T19:05:41+01:00 -->
      <xsl:variable name="year">
        <xsl:value-of select="substring-before($DateTime,'/')" />
      </xsl:variable>
      <xsl:variable name="month-temp">
        <xsl:value-of select="substring-after($DateTime,'/')" />
      </xsl:variable>
      <xsl:variable name="month">
        <xsl:value-of select="substring-before($month-temp,'/')" />
      </xsl:variable>
      <xsl:variable name="day-temp">
        <xsl:value-of select="substring-after($month-temp,'/')" />
      </xsl:variable>
      <xsl:variable name="day">
        <xsl:value-of select="substring-before($day-temp,' ')" />
      </xsl:variable>

      <xsl:variable name="time">
        <xsl:value-of select="substring-after($DateTime,' ')" />
      </xsl:variable>
      <xsl:variable name="hh">
        <xsl:value-of select="substring($time,1,2)" />
      </xsl:variable>
      <xsl:variable name="mm">
        <xsl:value-of select="substring($time,4,2)" />
      </xsl:variable>
      <xsl:variable name="ss">
        <xsl:value-of select="substring($time,7,2)" />
      </xsl:variable>

      <xsl:variable name="tz">
        <xsl:value-of select="substring-after($time,' ')" />
      </xsl:variable>

      <xsl:value-of select="$year" />
      <xsl:value-of select="'-'" />
      <xsl:value-of select="$month" />
      <xsl:value-of select="'-'" />
      <xsl:value-of select="$day" />
      <xsl:value-of select="'T'" />
      <xsl:value-of select="$hh" />
      <xsl:value-of select="':'" />
      <xsl:value-of select="$mm" />
      <xsl:value-of select="':'" />
      <xsl:value-of select="$ss" />
      <xsl:choose>
        <xsl:when test="$tz = 'GMT'">
          <xsl:text>+01:00</xsl:text>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="string-length($DateTime) = 0">
      <xsl:text>292278994-08-17T07:12:55+01:00</xsl:text>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>