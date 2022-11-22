<#macro input_website>
<form action = "/admin/website/add">
    <input type="text" name="title" placeholder="Название сайта"/>
    <input type="text" name="http" placeholder="Адрес сайта"/>
    <button type="submit">Добавить</button>
</form>
</#macro>

<#macro filter_weather>
<form action = "/admin/weather/filter">
    <input type="text" name="website" placeholder="Название сайта"/>
    <button type="submit">Найти</button>
</form>
</#macro>