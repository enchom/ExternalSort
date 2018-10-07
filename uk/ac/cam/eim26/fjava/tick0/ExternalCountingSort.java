package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * External counting sort strategy. Used only when all numbers fall inside a relatively small interval.
 */
public class ExternalCountingSort implements ExternalSortBase {
    private File firstFile;
    private byte[] arr;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
    }

    @Override
    public void sort() throws Exception {
        arr = Resources.arr;

        InputStream inputStream = new FileInputStream(firstFile);
        int[] counting = new int[Resources.maxValue - Resources.minValue + 1];

        while (true) {
            int len = inputStream.read(arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                counting[ByteUtil.bytesToInteger(arr, i / 4) - Resources.minValue]++;
            }
        }

        inputStream.close();

        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "rw");
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));
        int ptr = 0;

        for (int i = Resources.minValue; i <= Resources.maxValue; i++) {
            for (int j = 1; j <= counting[i - Resources.minValue]; j++) {
                arr[ptr] = (byte) (i >> 24);
                arr[ptr + 1] = (byte) ((i >> 16) & (0xff));
                arr[ptr + 2] = (byte) ((i >> 8) & (0xff));
                arr[ptr + 3] = (byte) (i & 0xff);
                ptr += 4;

                if (ptr == arr.length) {
                    outputStream.write(arr);
                    ptr = 0;
                }
            }
        }

        if (ptr > 0) {
            outputStream.write(arr, 0, ptr);
        }

        outputStream.close();
        randomAccessFile.close();
    }

    @Override
    public String getStrategy() {
        return "External counting sort";
    }
}
