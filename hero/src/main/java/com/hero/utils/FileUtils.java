package com.hero.utils;

import com.hero.signature.Constants;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Aron on 2018/7/26.
 */

public class FileUtils {

    public static File getKeystoreFile() throws IOException {
        return getFile(Constants.KEYSTORE_FILE_PATH);
    }

    public static File getHintFile() throws IOException {
        return getFile(Constants.PASSWORDHINT_FILE_PATH);
    }

    public static String getKeystoreFilecontent() throws IOException {
        return getFileContent(Constants.KEYSTORE_FILE_PATH);
    }

    public static String getHintFilecontent() throws IOException {
        return getFileContent(Constants.PASSWORDHINT_FILE_PATH);
    }

    public static String getFileContent(String filePath) throws IOException {
        File file = new File(filePath);
        String fileString;

        FileInputStream fis = new FileInputStream(file);

        int length = fis.available();

        byte [] buffer = new byte[length];

        fis.read(buffer);

        fileString = EncodingUtils.getString(buffer, "UTF-8");

        fis.close();

        return fileString;
    }

    public static File getAppFileDir() {
        String filePath = Constants.APP_FILE_PATH;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static File getFile(String filePath) throws IOException {
        File file = new File(filePath);

        return file;
    }

    public static void renameFile(String fileName) {
        //文件重命名
        File keystoreFile = new File(Constants.APP_FILE_PATH, fileName);
        File keystoreFilenew = new File(Constants.KEYSTORE_FILE_PATH);
        keystoreFile.renameTo(keystoreFilenew);
    }

    public static void writeFile(String path, String content) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        //给定一个字符串，将其转换成字节数组
        byte[] bytes = content.getBytes();
        //通过输出流对象写入字节数组
        fileOutputStream.write(bytes);
        //关流
        fileOutputStream.close();
    }

}
