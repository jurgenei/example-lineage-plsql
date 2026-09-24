<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:a="http://www.opengroup.org/xsd/archimate/3.0/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="urn:functions"
                expand-text="yes"
                exclude-result-prefixes="xs f a xsi"
>
    <xsl:output method="xml" indent="yes" />
    <!-- cdata-section-elements="doc" -->
    <xsl:strip-space elements="*"/>
    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:variable name="prop-defs" select="/a:model/a:propertyDefinitions/a:propertyDefinition"/>
    <xsl:variable name="relationships" select="/a:model/a:relationships/a:relationship"/>
    <xsl:variable name="elements" select="/a:model/a:elements/a:element"/>
    <xsl:variable name="nodes" select="/a:model/a:diagrams//a:node"/>



    <xsl:template match="/">
        <xsl:variable name="extract" as="node()*">
            <xsl:for-each-group select="$elements"
                                group-by="@xsi:type">
                <xsl:element name="{lower-case(current-grouping-key())}-group">
                    <xsl:apply-templates select="current-group()">
                        <xsl:sort select="a:name"/>
                    </xsl:apply-templates>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:variable>
        <model>
            <xsl:apply-templates select="$extract" mode="dedup"/>
        </model>
    </xsl:template>



    <xsl:template match="a:element">
        <xsl:variable name="id" select="@identifier"/>
        <xsl:variable name="name" select="a:name"/>
        <xsl:element name="{lower-case(@xsi:type)}">
            <xsl:attribute name="id">{@identifier}</xsl:attribute>
            <xsl:attribute name="name">{$name}</xsl:attribute>

<!--            properties-->
            <xsl:apply-templates select="a:properties"/>

<!--            relations-->
            <xsl:variable name="outgoing-relations" as="node()*">
                <xsl:apply-templates select="$relationships[@source = $id]">
                    <xsl:sort select="@xsi:type"/>
                    <xsl:with-param name="direction">source</xsl:with-param>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($outgoing-relations))">
                    <outgoing>
                        <xsl:sequence select="$outgoing-relations"/>
                    </outgoing>
                </xsl:when>
            </xsl:choose>
            <xsl:variable name="incoming-relations" as="node()*">
                <xsl:apply-templates select="$relationships[@target = $id]">
                    <xsl:sort select="@xsi:type"/>
                    <xsl:with-param name="direction">target</xsl:with-param>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($incoming-relations))">
                    <incoming>
                        <xsl:sequence select="$incoming-relations"/>
                    </incoming>
                </xsl:when>
            </xsl:choose>
            <xsl:variable name="labels" select="$nodes[@elementRef = $id]/a:label" as="node()*"/>
            <xsl:sequence select="$labels"/>


            <xsl:apply-templates select="a:documentation">
                <xsl:with-param name="proc" select="$name" tunnel="true"/>
            </xsl:apply-templates>


        </xsl:element>
    </xsl:template>

    <xsl:template match="a:relationship">
        <xsl:param name="direction"/>
        <xsl:element name="rel">
            <xsl:attribute name="id">{@identifier}</xsl:attribute>
            <xsl:attribute name="type">{lower-case(@xsi:type)}</xsl:attribute>
            <xsl:choose>
                <xsl:when test="$direction eq 'target'">
                    <xsl:copy-of select="@* except (@identifier,@target,@xsi:type)"/>
                    <xsl:variable name="source" select="@source"/>
                    <xsl:attribute name="name">{$elements[@identifier = $source]/a:name}</xsl:attribute>
                </xsl:when>
                <xsl:when test="$direction = 'source'">
                    <xsl:copy-of select="@* except (@identifier,@source,@xsi:type)"/>
                    <xsl:variable name="target" select="@target"/>
                    <xsl:attribute name="name">{$elements[@identifier = $target]/a:name}</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="@* except (@identifier,@xsi:type)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!--
Property Handling
   -->


    <xsl:function name="f:slice">
        <xsl:param name="input" as="xs:string"/>

        <xsl:variable name="els">
               <xsl:analyze-string select="($input,'')[1]" regex="(.*?)```xml(.*)```(.*)" flags="s">
                <xsl:matching-substring>
                    <xsl:sequence>
                        <item>{regex-group(1)}</item>
                        <item>{regex-group(2)}</item>
                        <item>{regex-group(3)}</item>
                    </xsl:sequence>
                </xsl:matching-substring>
                <xsl:non-matching-substring>
                    <item>
                        <xsl:value-of select="$input"/>
                    </item>
                </xsl:non-matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <xsl:sequence select="$els"/>
    </xsl:function>

    <xsl:function name="f:unescape" as="xs:string">
        <xsl:param name="input" as="xs:string"/>
        <xsl:sequence select="
        $input
          => replace('&amp;lt;', '&lt;')
          => replace('&amp;gt;', '&gt;')
          => replace('&amp;amp;', '&amp;')
       "/>
    </xsl:function>

    <xsl:template match="a:documentation">
        <xsl:param name="proc" tunnel="true"/>
        <xsl:variable name="data" select="." as="xs:string"/>
        <xsl:variable name="slice" select="f:slice($data)/item/text()"/>

        <doc>
            <xsl:sequence select="$slice[1],$slice[3]"/>
        </doc>

        <xsl:choose>
            <xsl:when test="not(empty($slice[2]))">
                <xsl:try>
                    <xsl:sequence select="parse-xml(f:unescape($slice[2]))" />
                    <xsl:catch>
                        <xsl:message>ERROR parsing: {$proc}
                            parsetext: {$data}</xsl:message>
                    </xsl:catch>
                </xsl:try>
            </xsl:when>
        </xsl:choose>

    </xsl:template>

    <!--
Property Handling
   -->

    <xsl:template match="a:property">
        <xsl:variable name="keyRef" select="@propertyDefinitionRef"/>
        <xsl:variable name="key" select="$prop-defs[@identifier = $keyRef]/a:name"/>
        <xsl:variable name="value" select="a:value"/>
        <property key="{$key}">{$value}</property>
    </xsl:template>

    <xsl:template match="a:properties">
        <properties>
            <xsl:apply-templates select="a:property"/>
        </properties>
    </xsl:template>

    <!--
    [preceding-sibling::*/@id = @id or following-sibling::*/@id = @id]
    -->

        <xsl:template match="node() | @*" mode="dedup">
            <xsl:copy>
                <xsl:apply-templates select="node() | @*" mode="dedup"/>
            </xsl:copy>
        </xsl:template>

    <xsl:template match="incoming" mode="dedup">
        <xsl:copy>
            <xsl:for-each-group select="rel"
                                group-by="@source">
                <relation>
                    <xsl:attribute name="type" select="string-join(current-group()/@type,',')"/>
                    <xsl:copy-of select="current-group()[1]/(@* except @type)"/>
                </relation>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="outgoing" mode="dedup">
        <xsl:copy>
            <xsl:for-each-group select="rel"
                                group-by="@target">
                <relation>
                    <xsl:attribute name="xtype" select="string-join(current-group()/@type,',')"/>
                    <xsl:copy-of select="current-group()[1]/(@* except @type)"/>
                </relation>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>