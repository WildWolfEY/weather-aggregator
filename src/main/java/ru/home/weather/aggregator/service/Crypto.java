package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.exceptions.crypto.CryptoException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoKeyCodingException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoKeyGeneratedException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoKeyReadingException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoTokenReadingException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoTokenSavingException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

@Log4j2
@Service
public class Crypto {

    @Value("${crypto.keystore.secret.key}")
    String secretKeyPath;

    private byte[] encryptBytes(byte[] bytes, SecretKey key) throws CryptoKeyCodingException {
        log.debug("encryptBytes(byte[] bytes, SecretKey key)");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyCodingException("Ошибка шифрования данных", e);
        }
    }

    private byte[] decryptBytes(byte[] bytes) throws CryptoKeyCodingException {
        log.debug("decryptBytes(byte[] bytes)");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyCodingException("Ошибка при расшифровке данных", e);
        }
    }

    private SecretKey generateSecretKey() throws CryptoKeyGeneratedException {
        log.debug("generateSecretKey()");
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            int keyBitSize = 256;
            keyGenerator.init(keyBitSize, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyGeneratedException("Невозможно сгенерировать ключ", e);
        }
    }

    private SecretKey readSecretKey(InputStream stream) throws CryptoKeyReadingException {
        log.debug("readSecretKey(InputStream stream)");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(stream);
            return (SecretKey) objectInputStream.readObject();
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyReadingException("Невозможно получить ключ", e);
        }
    }

    public byte[] getToken(InputStream stream) throws CryptoException{
        try {
            return decryptBytes(stream.readAllBytes());
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new CryptoTokenReadingException("Невозможно получить токен", e);
        }
    }

    protected SecretKey getSecretKey() throws CryptoException{
        try {
            boolean keyExists = new File(secretKeyPath).createNewFile();
            if (keyExists) {
                return (SecretKey) new ObjectInputStream(new FileInputStream(secretKeyPath)).readObject();
            } else {
                SecretKey key = generateSecretKey();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(secretKeyPath));
                objectOutputStream.writeObject(key);
                return key;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new CryptoKeyReadingException("Невозможно получить секретный ключ", e);
        }
    }

    public void encryptToken(byte[] token, OutputStream stream) throws CryptoException {
        try {
            stream.write(encryptBytes(token, getSecretKey()));
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new CryptoTokenSavingException("Невозможно сохранить токен", e);
        }
    }
}
