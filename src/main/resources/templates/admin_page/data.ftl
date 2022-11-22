<#macro data_websites>
<#list websites as website>
<div>
    <b>${website.id}</b>
    ${website.title} ${website.http}
</div>
</#list>
</#macro>

<#macro data_weather>
<#if weathers??>
<#list weathers as weather>
<div>
    ${weather.webSite.title}
    ${weather.city.names[0]}
    ${weather.dateIndicate}
    ${weather.dateForecast}
    ${weather.millimeters}
    ${weather.temperature}
</div>
</#list>
<#else>
<div>Нет измерений</div>
</#if>
</#macro>

