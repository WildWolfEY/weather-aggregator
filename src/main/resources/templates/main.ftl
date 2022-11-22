<#import "main_page/common.ftl" as c>
<@c.page>
Выберите город:
<select>
    <#list cities as city>
        <option>${city.names[0]},${city.country}</option>
    </#list>
</select>
<a href="/city">Другой</a>
</@c.page>