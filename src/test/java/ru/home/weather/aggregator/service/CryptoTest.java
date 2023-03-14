package ru.home.weather.aggregator.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import ru.home.weather.aggregator.exceptions.crypto.CryptoException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CryptoTest {
    @Autowired
    Charset charset;
    @InjectMocks
    @Spy
    @Autowired
    Crypto crypto;

    @BeforeEach
    void setMockGetSecretKey() throws CryptoException{
        byte[] keyBytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        String algorithm = "AES";
        SecretKey key = new SecretKeySpec(keyBytes, algorithm);
        Mockito.when(crypto.getSecretKey())
                .thenReturn(key);
    }

    @Test
    void getToken() throws CryptoException {
        byte[] bytes = new byte[]{-80, -117, 31, -128, -102, 3, 80, 100, 66, 13, 29, 117, 64, 34, -85, 85};
        InputStream stream = new ByteArrayInputStream(bytes);
        byte[] resultBytes = crypto.getToken(stream);
        Assertions.assertEquals(new String(resultBytes, charset),"abc","Ошибка при расшифровке данных");
    }

    @Test
    void encryptToken() throws CryptoException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        crypto.encryptToken("abc".getBytes(charset), stream);
        byte[] resultBytes = stream.toByteArray();
        Assertions.assertArrayEquals(resultBytes,
                new byte[]{-80, -117, 31, -128, -102, 3, 80, 100, 66, 13, 29, 117, 64, 34, -85, 85},
                "Ошибка при шифровании данных");
    }
}