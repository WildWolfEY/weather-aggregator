<#macro input_website>
<form action = "/admin/website/add">
    <input type="text" name="title" placeholder="Название сайта"/>
    <input type="text" name="url" placeholder="Адрес сайта"/>
    <button type="submit">Добавить</button>
</form>
</#macro>

<#macro filter_weather>
<form action = "/admin/weather/filter">
    <select name="website">
        <#list webs as website>
        <option>${website.title}</option>
        </#list>
    </select>
    <button type="submit">Найти</button>
</form>
</#macro>