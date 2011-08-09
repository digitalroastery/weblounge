<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns="http://www.entwinemedia.com/weblounge/3.0/image" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:date="http://exslt.org/dates-and-times" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xsl xs date">
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="utf-8" standalone="yes" cdata-section-elements="title description subject type coverage rights text property filename" />

  <xsl:param name="fileid" />
  <xsl:param name="uuid" />
  <xsl:param name="path" />
  <xsl:param name="imgwidth" />
  <xsl:param name="imgheight" />

  <xsl:variable name="adminuserid">
    <xsl:text>admin</xsl:text>
  </xsl:variable>
  <xsl:variable name="adminusername">
    <xsl:text>Administrator</xsl:text>
  </xsl:variable>
  <xsl:variable name="entry" select="/collection/entry[@id=$fileid][1]" />

  <xsl:template match="/">
    <image xmlns="http://www.entwinemedia.com/weblounge/3.0/image" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.entwinemedia.com/weblounge/3.0/image http://www.entwinemedia.com/xsd/weblounge/3.0/image.xsd">
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
              <xsl:if test="$entry/created != ''">
                <xsl:call-template name="formatdate">
                  <xsl:with-param name="DateTime" select="$entry/created" />
                </xsl:call-template>
              </xsl:if>
              <xsl:if test="$entry/creationdate != ''">
                <xsl:call-template name="formatdate">
                  <xsl:with-param name="DateTime" select="$entry/creationdate" />
                </xsl:call-template>
              </xsl:if>
            </date>
            <xsl:call-template name="user">
              <xsl:with-param name="userid" select="$entry/modified/user" />
            </xsl:call-template>
          </created>
          <filename>
            <xsl:value-of select="$entry/filename" />
          </filename>
          <xsl:if test="$entry/mimetype = 'image/pjpeg'">
            <mimetype>image/jpeg</mimetype>
          </xsl:if>
          <xsl:if test="$entry/mimetype != 'image/pjpeg'">
            <mimetype>
              <xsl:value-of select="$entry/mimetype" />
            </mimetype>
          </xsl:if>
          <size>
            <xsl:value-of select="$entry/size" />
          </size>
          <width>
            <xsl:value-of select="$imgwidth" />
          </width>
          <height>
            <xsl:value-of select="$imgheight" />
          </height>
        </content>
      </body>
    </image>
  </xsl:template>

  <xsl:template match="header">
    <head>
      <promote>true</promote>
      <index>true</index>
      <metadata>
        <title language="de">
          <xsl:value-of select="$entry/name"></xsl:value-of>
        </title>
        <xsl:apply-templates select="$entry/keywords" />
        <xsl:if test="string-length(/collection/@id) > 0">
          <xsl:analyze-string select="/collection/@id" regex="^/portrait/spieler/(damen|herren)/[A-Za-z]/(\d+)/(club|nati)/$" flags="i">
            <xsl:matching-substring>
              <subject>
                player:
                <xsl:value-of select="regex-group(2)" />
              </subject>
              <subject>
                portrait:
                <xsl:value-of select="regex-group(3)" />
              </subject>
            </xsl:matching-substring>
          </xsl:analyze-string>
        </xsl:if>
        <xsl:if test="string-length($entry/@id) > 0">
          <xsl:analyze-string select="$entry/@id" regex="^/portrait/teams/(damen|herren)/[A-Za-z]/(\d+)/\d+\.\w+$" flags="i">
            <xsl:matching-substring>
              <subject>
                clublogo:
                <xsl:value-of select="regex-group(2)" />
              </subject>
            </xsl:matching-substring>
          </xsl:analyze-string>
          <xsl:analyze-string select="$entry/@id" regex="^/portrait/teams/(damen|herren)/[A-Za-z]/(\d+)/\d+_(\d+)\.\w+$" flags="i">
            <xsl:matching-substring>
              <subject>
                club:
                <xsl:value-of select="regex-group(2)" />
              </subject>
              <subject>
                leaguecode:
                <xsl:value-of select="regex-group(3)" />
              </subject>
              <subject>portrait:team</subject>
            </xsl:matching-substring>
          </xsl:analyze-string>
        </xsl:if>
      </metadata>
      <security>
        <owner>
          <xsl:call-template name="user">
            <xsl:with-param name="userid" select="$entry/security/owner" />
          </xsl:call-template>
        </owner>
      </security>
      <created>
        <date>
          <xsl:if test="$entry/created != ''">
            <xsl:call-template name="formatdate">
              <xsl:with-param name="DateTime" select="$entry/created" />
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="$entry/creationdate != ''">
            <xsl:call-template name="formatdate">
              <xsl:with-param name="DateTime" select="$entry/creationdate" />
            </xsl:call-template>
          </xsl:if>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="$entry/modified/user" />
        </xsl:call-template>
      </created>
      <modified>
        <date>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="$entry/modified/date" />
          </xsl:call-template>
        </date>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="$entry/modified/user" />
        </xsl:call-template>
      </modified>
      <published>
        <from>
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="$entry/publish/from" />
          </xsl:call-template>
        </from>
        <xsl:variable name="publish-to">
          <xsl:call-template name="formatdate">
            <xsl:with-param name="DateTime" select="$entry/publish/to" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="xs:dateTime($publish-to) &lt; xs:dateTime(date:date-time())">
          <to>
            <xsl:value-of select="$publish-to" />
          </to>
        </xsl:if>
        <xsl:call-template name="user">
          <xsl:with-param name="userid" select="$entry/modified/user" />
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
    <!-- expected date format 2006/01/17 19:05:41 GMT -->
    <!-- new date format 2006-01-17T19:05:41+01:00 -->
    <xsl:param name="DateTime" />
    <xsl:variable name="now" select="date:date-time()" />
    <xsl:if test="string-length($DateTime) &gt; 0">
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

      <xsl:if test="string-length($year) &gt; 4">
        <xsl:value-of select="$now" />
      </xsl:if>
      <xsl:if test="string-length($year) &lt; 5">
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
          <xsl:otherwise>
            <xsl:text>+00:00</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:if>
    <xsl:if test="string-length($DateTime) = 0">
      <xsl:value-of select="$now" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>