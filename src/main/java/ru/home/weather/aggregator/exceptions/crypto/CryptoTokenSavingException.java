package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoTokenSavingException extends CryptoException {
    public CryptoTokenSavingException(String message, Exception e) {
        super(message, e);
    }

    public CryptoTokenSavingException(Exception e) {
        super("Ошибка сохранения зашифрованных данных",e);
    }
}
