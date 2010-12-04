<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml" version="1.0" encoding="utf-8" indent="yes"/>

    <!-- Start by applying templates from the root node -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="page">
        <page>
            <xsl:attribute name="id">/<xsl:value-of select="@partition"/><xsl:if test="@path != '/'"><xsl:value-of select="@path"/></xsl:if></xsl:attribute>
            <xsl:attribute name="version"><xsl:value-of select="@version"/></xsl:attribute>
            <xsl:apply-templates select="header"/>
            <xsl:apply-templates select="body"/>
        </page>
    </xsl:template>

    <xsl:template match="header">
        <header>
            <xsl:apply-templates select="security"/>
            <workflow>
                <xsl:apply-templates select="modified"/>
            </workflow>
            <content>
                <xsl:apply-templates select="title"/>
            </content>
            <renderer><xsl:value-of select="renderer/text()"/></renderer>
            <layout><xsl:value-of select="layout/text()"/></layout>
            <type><xsl:value-of select="type/text()"/></type>
            <xsl:apply-templates select="keywords"/>
        </header>
    </xsl:template>

    <!-- header -->
    <xsl:template match="title">
        <locale>
            <xsl:attribute name="language"><xsl:value-of select="@language"/></xsl:attribute>
            <text id="title"><xsl:value-of select="text()"/></text>
        </locale>
    </xsl:template>

    <xsl:template match="modified">
        <created language="de">
            <user realm="weblounge"><xsl:value-of select="user/text()"/></user>
            <date><xsl:value-of select="date/text()"/></date>
        </created>
        <modified language="de">
            <user realm="weblounge"><xsl:value-of select="user/text()"/></user>
            <date><xsl:value-of select="date/text()"/></date>
        </modified>
    </xsl:template>

    <xsl:template match="keywords">
        <keywords>
            <xsl:apply-templates select="keyword"/>
        </keywords>
    </xsl:template>

    <xsl:template match="keyword">
        <keyword><xsl:value-of select="text()"/></keyword>
    </xsl:template>
    
    <xsl:template match="security">
        <security>
            <xsl:if test="owner/text() = 'www'">
                <owner>
                    <user realm="weblounge">${siteadmin}</user>
                </owner>
            </xsl:if>
            <xsl:if test="owner/text() != 'www'">
                <owner>
                    <user realm="weblounge"><xsl:value-of select="owner/text()"/></user>
                </owner>
            </xsl:if>
            <xsl:apply-templates select="permission"/>
        </security>
    </xsl:template>

    <xsl:template match="permission">
        <restrict>
            <permission>
            <xsl:attribute name="realm">weblounge</xsl:attribute>
            <xsl:value-of select="substring(@id, 8)"/>
            </permission>
            <role realm="weblounge"><xsl:value-of select="substring(text(), 8)"/></role>
        </restrict>
    </xsl:template>

    <!--                                                  -->
    <!--                       Body                   -->
    <!--                                                  -->
    
    <xsl:template match="body">
        <body>
            <xsl:apply-templates select="composer"/>
        </body>
    </xsl:template>

    <xsl:template match="composer">
        <composer>
            <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:apply-templates select="pagelet"/>
        </composer>
    </xsl:template>
    
    <xsl:template match="pagelet">
        <pagelet>
            <xsl:attribute name="module"><xsl:value-of select="@module"/></xsl:attribute>
            <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:apply-templates select="security"/>
            <workflow>
                <xsl:apply-templates select="content/modified"/>
            </workflow>
            <content>
                <xsl:apply-templates select="content"/>
            </content>
            <xsl:apply-templates select="properties"/>
        </pagelet>
    </xsl:template>

    <xsl:template match="content">
        <locale>
            <xsl:attribute name="langage"><xsl:value-of select="@language"/></xsl:attribute>
            <xsl:apply-templates select="text"/>
        </locale>
    </xsl:template>
    
    <xsl:template match="text">
        <xsl:if test="text() != ''">
            <text>
                <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:value-of select="text()"/>
            </text>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="properties">
        <properties>
            <xsl:call-template name="pageproperties"/>
            <xsl:apply-templates select="property"/>
        </properties>
    </xsl:template>
    
    <xsl:template match="property">
        <xsl:if test="@id != 'partition' and @id != 'path'">
            <property>
                <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:value-of select="text()"/>
            </property>
        </xsl:if>
    </xsl:template>

    <xsl:template name="pageproperties">
        <xsl:if test="property[@id = 'partition'] and property[@id = 'path']">
            <property>
                <xsl:attribute name="id">page</xsl:attribute>/<xsl:value-of select="property[@id = 'partition']"/><xsl:value-of select="property[@id = 'path']"/>            </property>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>