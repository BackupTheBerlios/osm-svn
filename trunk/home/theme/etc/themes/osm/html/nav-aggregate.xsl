<?xml version="1.0"?>
<xsl:stylesheet 
    version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xml:lang="en" 
>
  <xsl:param name="directory" />
  <xsl:param name="file" />
  <xsl:param name="fullpath" />
  <xsl:template match="project" >
    <project>
      <xsl:apply-templates />  
    </project>
  </xsl:template>
  
  <xsl:template match="title" >
    <title>
      <xsl:value-of select="." />
    </title>
  </xsl:template>
  
  <xsl:template match="body" >
    <body>
      <xsl:apply-templates />  
    </body>
  </xsl:template>
  
  <xsl:template match="menu" >
    <xsl:param name="level" select="''" />
    <menu>
      <level><xsl:value-of select="$level" /></level>
      <xsl:choose >
        <xsl:when test="count( ../links ) = 0" >
          <xsl:apply-templates select="document('../navigation.xml', / )/project/body/menu" >
            <xsl:with-param name="level" select="concat( $level, '../' )" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <category>
            <xsl:apply-templates select="../links/item" />
          </category>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates />  
    </menu>
  </xsl:template>

  <xsl:template match="item" >
    <item>
      <xsl:choose>
        <!-- Contains index.html and a directory -->
        <xsl:when test="contains( @href, 'index.html') and contains( @href, '/')" >
          <xsl:attribute name="href"><xsl:value-of select="substring-before( @href, 'index.html' ) " /></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
          <xsl:if test="contains( $fullpath, substring-before( @href, '/' ) )" >
            <xsl:attribute name="selected">true</xsl:attribute>
          </xsl:if> 
        </xsl:when>
        <!-- Contains a file without a directory -->
        <xsl:when test="contains( @href, '.') and not( contains( @href, '/') )" >
          <xsl:attribute name="href"><xsl:value-of select="@href" /></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
        </xsl:when>
        <!-- Contains a directory without file -->
        <xsl:when test="not( contains( @href, '.') ) and contains( @href, '/')" >
          <xsl:attribute name="href"><xsl:value-of select="substring-before( @href, 'index.html' ) " /></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
          <xsl:if test="contains( $fullpath, substring-before( @href, '/' ) )" >
            <xsl:attribute name="selected">true</xsl:attribute>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="href"><xsl:value-of select="substring-before( @href, 'index.html' ) " /></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
          <xsl:if test="contains( $fullpath, @href)" >
            <xsl:attribute name="selected">true</xsl:attribute>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </item>
  </xsl:template>

  <!--
  <xsl:template match="sponsors" >
    <sponsors>
      <xsl:apply-templates />
    </sponsors>
  </xsl:template >

  <xsl:template match="sponsor" >
    <xsl:copy-of select="." />
  </xsl:template >
  -->

  <xsl:template match="links" >
  </xsl:template >
    
</xsl:stylesheet> 
