package com.servxglobal.tms.traineeservice.utils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
public class EncryptorUtil {

    public static String encryptPropertyValue(String textToEncrypt) throws Exception {

        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("tms#1");
        final String  encryptedPassword = encryptor.encrypt(textToEncrypt);
        System.out.println(encryptedPassword);
        return encryptedPassword;

    }
    public static String decryptPropertyValue(String textTodecrypt) throws Exception {
        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("tms#1");
        final String decryptedPropertyValue = encryptor.decrypt(textTodecrypt);
        System.out.println(decryptedPropertyValue);
        return decryptedPropertyValue;
    }
}
