<#import "main_page/common.ftl" as c>
<#import "common_elements/exception.ftl" as e>
<@c.page>
<form action = "/city/search">
<input type="text" name="name" placeholder="Введите город"/>
<select name="country">
    <#list countries as key, value>
        <option value="${value}">${key}</option>
    </#list>
</select>
    <button type="submit">Найти</button>
</form>
<#if cities??>
<form action="/city/add">
<#if cities?has_content>
    <select name="cityJson">
    <#list cities as city>
       <option value='${city.json}'>
           ${city.names[0]}
           ${city.area}
       </option>
    </#list>
    </select>
    <button type="submit">Выбрать</button>
<#else>
    Город не найден
</#if>
</form>
</#if>
<#if exc??>
<div>
    <@e.exception/>
</div>
</#if>
</@c.page>