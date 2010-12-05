<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="capitalize">
		<xsl:param name="data" />
		<!-- generate first character -->
		<xsl:value-of
			select="translate(substring($data,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
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