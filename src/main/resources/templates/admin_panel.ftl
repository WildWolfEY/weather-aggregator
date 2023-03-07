<#import "admin_page/common.ftl" as common>
<#import "admin_page/input.ftl" as b>
<#import "admin_page/data.ftl" as d>
<#import "common_elements/exception.ftl" as e>
<#import "admin_page/keys.ftl" as k>
<@common.page>
<#if "${page}"=="websites">
    <@b.input_website/>
    <@d.data_websites/>
<#elseif "${page}"=="weathers" >
    <@b.filter_weather/>
    <@d.data_weather/>
<#else>
    <@k.keys/>
</#if>
<@e.exception/>
</@common.page>