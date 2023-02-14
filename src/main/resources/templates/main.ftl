<#import "main_page/common.ftl" as c>
<@c.page>
<form action="/rating">
    Выберите город:
    <select name="cityId">
        <option value="0">Не важно</option>
        <#list cities as city>
        <option value="${city.id}">${city.names[0]},${city.country}</option>
    </#list>
    </select>
    <a href="/city">Другой</a>
    </br>
    Выберите давность:
    <select name="antiquity">
        <option value="0">Не важно</option>
        <option value="1">1</option>
        <option value="2">2</option>
        <option value="3">3</option>
        <option value="4">4</option>
        <option value="5">5</option>
        <option value="6">6</option>
        <option value="7">7</option>
        <option value="8">8</option>
        <option value="9">9</option>
        <option value="10">10</option>
    </select>
    </br>
    Что важнее:
    <select name="temperatureOrPrecipitation">
        <option value="0">Не важно</option>
        <option value="1">Температура</option>
        <option value="2">Осадки</option>
    </select>

    <button type="submit">Посчитать</button>
</form>
</br></br>
<#if antiquity??>
    <#if antiquity == 0>
            Давность не учитывается
    <#elseif antiquity == 1>
            Для давности ${antiquity} день
    <#elseif antiquity < 5>
            Для давности ${antiquity} дня
    <#else>
            Для давности ${antiquity} дней
    </#if>
</#if>
<#if city?? && (city)?? >
      Город ${city.names[0]}
</#if>
<#if temperatureOrPrecipitation??>
      <#if temperatureOrPrecipitation == 1>
        с учетом приоритета температуры
      <#elseif temperatureOrPrecipitation == 2>
        с учетом приоритета осадков
      </#if>
</#if>
</br>
<#if websites??>
    <#if websites?has_content>
    Рейтинг сайтов:
        <#list websites as website>
            </br>
            ${website.title}
        </#list>
    <#else>
        Нет статистики для подсчёта рейтинга
    </#if>
</#if>
<#if exc??>
<div>
    <@e.exception/>
</div>
</#if>
</@c.page>