package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoKeyCodingException extends CryptoException {
    public CryptoKeyCodingException(String message, Exception e) {
        super(message, e);
    }

    public CryptoKeyCodingException(Exception e) {
        super("Ошибка при шифровании/дешифровании данных", e);
    }
}
