<#import "pager.ftl" as p>
<#macro data_websites>
<#list websites as website>
<div>
    ${website.title}
</div>
</#list>
</#macro>

<#macro data_weather>
<#if weathers??>
<@p.pager url weathers />
<table>
    <tr>
        <th>id</th>
        <th>WebSite</th>
        <th>City</th>
        <th>dateIndicate</th>
        <th>dateRequest</th>
        <th>t</th>
        <th>p</th>
    </tr>
<#list weathers.content as weather>
<div>

    <tr>
        <td>
            ${weather.id}
        </td>
        <td>
            <#if weather.website??>
                ${weather.website.title}
            </#if>
        </td>
        <td>
            <#if weather.city??>
                ${weather.city.names[0]}
            </#if>
        </td>
        <td>
            <#if weather.dateIndicate??>
                ${weather.dateIndicate}
            </#if>
        </td>
        <td>
            <#if weather.dateRequest??>
                ${weather.dateRequest}
            </#if>
        </td>
        <td>
            ${weather.temperature}
        </td>
        <td>
            <#if weather.precipitation??>
                ${weather.precipitation}
            </#if>
        </td>
    </tr>
</div>
</#list>
</table>
<#else>
<div>Нет измерений</div>
<@p.pager url weathers />
</#if>
</#macro>

