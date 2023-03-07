package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.exception.CryptoKeyCodingException;
import ru.home.weather.aggregator.exception.CryptoKeyGeneratedException;
import ru.home.weather.aggregator.exception.CryptoKeyReadingException;
import ru.home.weather.aggregator.exception.CryptoTokenReadingException;
import ru.home.weather.aggregator.exception.CryptoTokenSavingException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

@Log4j2
@Service
public class Crypto {

    private byte[] encryptBytes(byte[] bytes) throws CryptoKeyCodingException, CryptoKeyGeneratedException {
        log.debug("encryptBytes(byte[] bytes)");
        SecretKey key;
        try {
            key = readSecretKey();
        } catch (CryptoKeyReadingException e) {
            key = generateSecretKey();
            saveSecretKey(key);
        }
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
            SecretKey key = readSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
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

    @Value("${crypto.keystore.secret.key}")
    String secretKeyPath;

    public SecretKey readSecretKey() throws CryptoKeyReadingException {
        log.debug("readSecretKey()");
        try {
            FileInputStream fileInputStream = new FileInputStream(secretKeyPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (SecretKey) objectInputStream.readObject();
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyReadingException("Невозможно получить ключ", e);
        }
    }

    private void saveSecretKey(SecretKey key) throws CryptoKeyGeneratedException {
        log.debug("saveSecretKey(SecretKey key)");
        try {
            File fileSecretKey = new File(secretKeyPath);
            if (!fileSecretKey.exists() && !fileSecretKey.isFile()) {
                fileSecretKey.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileSecretKey);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(key);
            objectOutputStream.close();
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new CryptoKeyGeneratedException("Невозможно сохранить ключ", e);
        }
    }

    private void saveEncryptToken(byte[] token, String filePath) throws CryptoTokenSavingException {
        log.debug("saveEncryptToken(byte[] token, String filePath) параметр filePath:{}", filePath);
        try {
            File file = new File(filePath);
            if (!file.exists() && !file.isFile()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(token);
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new CryptoTokenSavingException("Невозможно сохранить токен", e);
        }
    }

    private byte[] readEncryptedToken(String path) throws CryptoTokenReadingException {
        log.debug("readEncryptedToken(String path) параметр:{}", path);
        try {
            File tokenFile = new File(path);
            if (tokenFile.exists() && tokenFile.isFile())
                return new FileInputStream(tokenFile).readAllBytes();
            else {
                tokenFile.createNewFile();
                return new byte[0];
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new CryptoTokenReadingException("Невозможно получить токен", e);
        }
    }

    public byte[] getToken(String path) throws CryptoTokenReadingException, CryptoKeyCodingException {
        return decryptBytes(readEncryptedToken(path));
    }

    public void encryptToken(byte[] token, String path) throws CryptoKeyCodingException,
            CryptoTokenSavingException, CryptoKeyGeneratedException {
        saveEncryptToken(encryptBytes(token), path);
    }
}
