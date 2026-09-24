<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt3">
    <!-- This schematron checks that each application component has documentation and app logic -->
    <sch:pattern>
        <sch:rule context="applicationcomponent">
            <sch:assert test="doc" id="has-doc">{@name}: has documentation</sch:assert>
            <sch:assert test="app" id="has-app">{@name}: has app logic</sch:assert>
            <sch:report test="doc" id="has-doc">{@name}: has documentation</sch:report>
            <sch:report test="app" id="has-app">{@name}: has app logic</sch:report>
        </sch:rule>
    </sch:pattern>
</sch:schema>