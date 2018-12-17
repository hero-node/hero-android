package com.hero.utils;

import com.hero.signature.Constants;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Aron on 2018/7/26.
 */

public class FileUtils {

    public static File getKeystoreFile(String fileName) throws IOException {
        if (fileName.contains(".json")) {
            return getFile(Constants.KEYSTORE_FILE_PATH + fileName);
        }
        return getFile(Constants.KEYSTORE_FILE_PATH + fileName +".json");
    }

    public static File getHintFile(String fileName) throws IOException {
        if (fileName.contains(".txt")) {
            return getFile(Constants.PASSWORDHINT_FILE_PATH + fileName);
        }
        return getFile(Constants.PASSWORDHINT_FILE_PATH + fileName +".txt");
    }

    public static String getKeystoreFilecontent(String fileName) throws IOException {
        if (fileName.contains(".json")) {
            return getFileContent(Constants.KEYSTORE_FILE_PATH + fileName);
        }
        return getFileContent(Constants.KEYSTORE_FILE_PATH + fileName +".json");
    }

    public static String getHintFilecontent(String fileName) throws IOException {
        if (fileName.contains(".txt")) {
            return getFileContent(Constants.PASSWORDHINT_FILE_PATH + fileName);
        }
        return getFileContent(Constants.PASSWORDHINT_FILE_PATH + fileName +".txt");
    }

    public static boolean hasKeystoreFile() {
        File path = new File(Constants.KEYSTORE_FILE_PATH);// 获得路径
        File[] files = path.listFiles();// 读取文件
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".json")) {
                return true;
            }
        }
        return false;
    }

    public static int getNumbersOfKeystore() {
        File path = new File(Constants.KEYSTORE_FILE_PATH);// 获得路径
        File[] files = path.listFiles();// 读取文件
        int j = 0;
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".json") && !fileName.startsWith("default")) {
                j++;
            }
        }
        return j;
    }

    public static ArrayList getKeystroeFilesWithoutDefault() {
        File path = new File(Constants.APP_FILE_PATH);// 获得路径
        // File path = new File("/mnt/sdcard/");
        File[] files = path.listFiles();// 读取文件
        ArrayList<File> fileArrayList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".json") && !fileName.startsWith("default")) {
                fileArrayList.add(files[i]);
                Collections.sort(fileArrayList);
            }
        }
        return fileArrayList;
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

    public static void renameFile(String fileName, int index) {
        //文件重命名
        File keystoreFile = new File(Constants.APP_FILE_PATH, fileName);
        File keystoreFilenew = new File(Constants.KEYSTORE_FILE_PATH + "Keystore" + index + ".json");
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


    // 文件复制
    public static boolean copyFile(String source, String copy) throws Exception {
        source = source.replace("\\", "/");
        copy = copy.replace("\\", "/");

        File source_file = new File(source);
        File copy_file = new File(copy);

        // BufferedStream缓冲字节流
        if (!source_file.exists()) {
            throw new IOException("文件复制失败：源文件（" + source_file + "） 不存在");
        }
        if (copy_file.isDirectory()) {
            throw new IOException("文件复制失败：复制路径（" + copy_file + "） 错误");
        }
        File parent = copy_file.getParentFile();
        // 创建复制路径
        if (!parent.exists()) {
            parent.mkdirs();
        }
        // 创建复制文件
        if (!copy_file.exists()) {
            copy_file.createNewFile();
        }

        FileInputStream fis = new FileInputStream(source_file);
        FileOutputStream fos = new FileOutputStream(copy_file);

        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] KB = new byte[1024];
        int index;
        while ((index = bis.read(KB)) != -1) {
            bos.write(KB, 0, index);
        }

        bos.close();
        bis.close();
        fos.close();
        fis.close();

        if (!copy_file.exists()) {
            return false;
        } else if (source_file.length() != copy_file.length()) {
            return false;
        } else {
            return true;
        }

    }

}
