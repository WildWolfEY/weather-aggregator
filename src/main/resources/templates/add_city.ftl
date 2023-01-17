<#import "main_page/common.ftl" as c>
<#import "common_elements/exception.ftl" as e>
<@c.page>
<form action="/city/search">
    <input type="text" name="name" placeholder="Введите город"/>
    <input type="text" name="area" placeholder="Введите область"/>
    <select name="country">
        <#list countries as key, value>
        <option value="${key}">${key}</option>
    </#list>
    </select>
    <button type="submit">Найти</button>
</form>
<#if cities??>
<!--/ya/add-data-->
<form action="/city/add">
    <#if cities?has_content>
    <select name="cityJson">
        <#list cities as city>
        <option value='${city.json}'>
            ${city.placeNameRu}
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