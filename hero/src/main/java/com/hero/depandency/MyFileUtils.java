/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero.depandency;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyFileUtils {

    private static int COPY_BLOCK_SIZE = 2048;
    private static Object mLock = new Object();
    private static boolean mCopyInProgress = false;

    public static long getSystemFreeSpace() {
        File root = Environment.getDataDirectory();
        StatFs sysFs = new StatFs(root.getPath());
        long blockSize = sysFs.getBlockSize();
        return blockSize * sysFs.getAvailableBlocks();
    }

    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        String extension = null;

        if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }


    public static Bitmap decodeFile(String filePath, boolean computeSampleSize, int width, int height) {
        if (computeSampleSize) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            final int minSideLength = Math.min(width, height);
            opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
            opts.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(filePath, opts);
        } else {
            return BitmapFactory.decodeFile(filePath);
        }
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap decodeStream(InputStream is, long size) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int count = -1, totalCount = 0;

        while ((count = is.read(data, 0, 2048)) != -1) {
            totalCount += count;
            byteStream.write(data, 0, count);
        }
        data = null;
        if (totalCount == 0) return null;
        return BitmapFactory.decodeByteArray(byteStream.toByteArray(), 0, totalCount);
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    public static boolean isCopyInProgress() {
        return mCopyInProgress;
    }

    public static boolean copyFile(Context context, String sourcePath, String targetFileName) {
        File path = new File(context.getFilesDir().getAbsolutePath() + "/Images");
        File targetFile = new File(path.getAbsolutePath() + "/" + targetFileName);
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return false;
        }
        if (!path.exists()) {
            path.mkdirs();
        }
        if (targetFile.exists()) {
            targetFile.delete();
        }
        InputStream is = null;
        FileOutputStream out = null;
        try {
            is = new FileInputStream(sourceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is != null) {
            try {
                // Copy data from in stream to out stream
                out = new FileOutputStream(targetFile);
                byte[] buf = new byte[COPY_BLOCK_SIZE];
                int len, totalLen = 0;

                while ((len = is.read(buf)) > 0) {
                    out.write(buf, 0, len);
                    totalLen += len;
                }
                if (out != null) {
                    Log.i("FileUtils", "copy file " + sourcePath);
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (out != null) out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static String encodeFileToBase64(String sourcePath) {
        byte[] buffer = null;
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return null;
        }

        try {
            InputStream is = new FileInputStream(sourceFile);
            buffer = new byte[(int)sourceFile.length()];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buffer == null) {
            return null;
        }
        String content =  Base64Utils.encodeToString(buffer);
        return content;
    }
}