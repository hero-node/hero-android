/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hero.depandency;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 图案解锁加密、解密工具类
 *
 * @author way
 */
public class LockPatternUtils {
    public static final int PATTERN_NUM = 9;
    /**
     * The minimum number of dots in a valid pattern.
     */
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    /**
     * The maximum number of incorrect attempts before the user is prevented
     * from trying again for {@link #FAILED_ATTEMPT_TIMEOUT_MS}.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    /**
     * The minimum number of dots the user must include in a wrong pattern
     * attempt for it to be counted against the counts that affect
     * {@link #FAILED_ATTEMPTS_BEFORE_TIMEOUT} and
     */
    public static final int MIN_PATTERN_REGISTER_FAIL = MIN_LOCK_PATTERN_SIZE;
    /**
     * How long the user is prevented from trying again after entering the wrong
     * pattern too many times.
     */
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;
    private static final String TAG = "LockPatternUtils";
    private static final String LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_TOKEN_FILE = "token.key";
    private static final AtomicBoolean sHaveNonZeroPatternFile = new AtomicBoolean(false);
    private static final AtomicBoolean sHaveNonZeroTokenFile = new AtomicBoolean(false);
    private static File sLockPatternFilename;
    private static File sLockTokenFilename;
    private static FileObserver sPasswordObserver;
    private static FileObserver sTokenObserver;

    private static LockPatternUtils instance;
    private static List<Cell> alivePatternCells;

    private LockPatternUtils(Context context) {

        String dataSystemDirectory = context.getFilesDir().getAbsolutePath();
        if (sLockPatternFilename == null) {
            sLockPatternFilename = new File(dataSystemDirectory, LOCK_PATTERN_FILE);
            sHaveNonZeroPatternFile.set(sLockPatternFilename.length() > 0);
            int fileObserverMask = FileObserver.CLOSE_WRITE | FileObserver.DELETE | FileObserver.MOVED_TO | FileObserver.CREATE;
            sPasswordObserver = new LockFileObserver(dataSystemDirectory, fileObserverMask);
            sPasswordObserver.startWatching();
        }

        if (sLockTokenFilename == null) {
            sLockTokenFilename = new File(dataSystemDirectory, LOCK_TOKEN_FILE);
            sHaveNonZeroTokenFile.set(sLockTokenFilename.length() > 0);
            int fileObserverMask = FileObserver.CLOSE_WRITE | FileObserver.DELETE | FileObserver.MOVED_TO | FileObserver.CREATE;
            sTokenObserver = new LockFileObserver(dataSystemDirectory, fileObserverMask);
            sTokenObserver.startWatching();
        }

    }

    public static LockPatternUtils getInstance(Context context) {
        if (instance == null) {
            instance = new LockPatternUtils(context);
        }
        return instance;
    }

    /**
     * Deserialize a pattern. 解密,用于保存状态
     *
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<Cell> stringToPattern(String string) {
        List<Cell> result = new ArrayList<Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern. 加密
     *
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return new String(res);
    }

    /*
     * Generate an SHA-1 hash for the pattern. Not the most secure, but it is at
     * least a second level of protection. First level is that the file is in a
     * location only readable by the system process.
     *
     * @param pattern the gesture pattern.
     *
     * @return the hash of the pattern in a byte array.
     */
    private static byte[] patternToHash(List<Cell> pattern) throws NoSuchAlgorithmException {
        if (pattern == null) {
            return null;
        }

        String patternValue = parsePattern(pattern);
        byte[] hash = convertToHash(patternValue);
        return hash;
    }

    private static byte[] convertToHash(String originalString) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(Base64Utils.encode(originalString.getBytes()));
        return hash;
    }

    private static String parsePattern(List<Cell> pattern) {
        String patternValue = "";
        for (Cell cell : pattern) {
            patternValue += "" + parseCell(cell);
        }
        return patternValue;
    }

    private static int parseCell(Cell cell) {

        int cellNum = cell.getRow() * 3 + cell.getColumn() + 1;

        return cellNum;
    }

    private static Cell intToCell(int value) {

        Cell cell = new Cell(value / 3, value % 3);

        return cell;
    }

    public static JSONArray patternToArray(List<Cell> pattern) {
        ArrayList<Integer> array = new ArrayList();
        for (Cell cell : pattern) {
            array.add(parseCell(cell) - 1);
        }
        JSONArray jsonArray = new JSONArray();
        for (Cell cell : pattern) {
            jsonArray.put(parseCell(cell) - 1);
        }
        return jsonArray;
    }

    public static List<Cell> arrayToPattern(JSONArray values) {
        ArrayList array = new ArrayList<LockPatternUtils.Cell>();
        for (int i = 0; i < values.length(); i ++) {
            try {
                array.add(intToCell(values.getInt(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    /**
     * Check to see if the user has stored a lock pattern.
     *
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists() {
        return sHaveNonZeroPatternFile.get();
    }

    public void clearLock() {
        try {
            clearAlivePattern();
            saveLockPattern(null);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Save a lock pattern.
     *
     * @param pattern The new pattern to save.
     */
    public void saveLockPattern(List<Cell> pattern) throws NoSuchAlgorithmException {
        // Compute the hash
        final byte[] hash = LockPatternUtils.patternToHash(pattern);
        saveTOFile(hash, sLockPatternFilename);

    }

    public void saveLockToken(String tokenValue, List<Cell> pattern) throws Exception {

        saveTOFile(encrypToken(tokenValue, parsePattern(pattern)), sLockTokenFilename);
    }

    private void saveTOFile(byte[] hashResult, File storeFile) {
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(storeFile, "rwd");
            // Truncate the file if pattern is null, to clear the lock
            if (hashResult == null) {
                raf.setLength(0);
            } else {
                raf.write(hashResult, 0, hashResult.length);
            }
            raf.close();
        } catch (FileNotFoundException fnfe) {
            // Cant do much, unless we want to fail over to using the settings
            // provider
            Log.e(TAG, "Unable to save lock pattern to " + storeFile.getName());
        } catch (IOException ioe) {
            // Cant do much
            Log.e(TAG, "Unable to save lock pattern to " + storeFile.getName());
        }
    }

    /**
     * Check to see if a pattern matches the saved pattern. If no pattern
     * exists, always returns true.
     *
     * @param pattern The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    public boolean checkPattern(List<Cell> pattern) throws IOException, NoSuchAlgorithmException {
        // Read all the bytes from the file
        final byte[] stored = getBytesFromFile(sLockPatternFilename);

        if (stored.length <= 0) {
            return true;
        }
        // Compare the hash from the file with the entered pattern's hash
        return Arrays.equals(stored, LockPatternUtils.patternToHash(pattern));

    }

    private byte[] getBytesFromFile(File targetFile) throws IOException {

        // RandomAccessFile raf = new RandomAccessFile(targetFile,"r");
        RandomAccessFile raf = new RandomAccessFile(targetFile, "r");

        byte[] stored = new byte[(int) raf.length()];
        int got = raf.read(stored, 0, stored.length);
        raf.close();
        if (got <= 0) {
            return new byte[0];
        }

        return stored;
    }

    public byte[] encrypToken(String tokenValue, String tokenKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(convertToHash(tokenKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted;
        encrypted = cipher.doFinal(Base64Utils.encode(tokenValue.getBytes()));

        return encrypted;
    }

    public byte[] decryptToken(List<Cell> patternCells) throws Exception {
        byte[] encryptedValue = getBytesFromFile(sLockTokenFilename);
        String tokenKey = parsePattern(patternCells);

        SecretKeySpec skeySpec = new SecretKeySpec(convertToHash(tokenKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encryptedValue);

        return decrypted;
    }

    public void setAlivePattern(List<Cell> patternCells) {
        alivePatternCells = patternCells;
    }

    public void clearAlivePattern() {
        alivePatternCells = null;
    }

    public String getAliveToken() {
        if (alivePatternCells == null) {
            return null;
        }
        try {
            return new String(Base64Utils.decode(decryptToken(alivePatternCells)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class LockFileObserver extends FileObserver {
        public LockFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            Log.d(TAG, "file path" + path);
            if (LOCK_PATTERN_FILE.equals(path)) {
                Log.d(TAG, "lock pattern file changed");
                sHaveNonZeroPatternFile.set(sLockPatternFilename.length() > 0);
            }

            if (LOCK_TOKEN_FILE.equals(path)) {
                Log.d(TAG, "lock token file changed");
                sHaveNonZeroTokenFile.set(sLockTokenFilename.length() > 0);
            }
        }
    }

    /**
     * Represents a cell in the 3 X 3 matrix of the unlock pattern view.
     */
    public static class Cell {
        // keep # objects limited to 9
        static Cell[][] sCells = new Cell[3][3];

        static {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        public int row;
        public int column;

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            }
            if (column < 0 || column > 2) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "(row=" + row + ",clmn=" + column + ")";
        }
    }
}
