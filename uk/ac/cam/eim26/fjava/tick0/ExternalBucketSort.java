package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * External bucket sort that groups numbers in buckets based on their first byte. Similar to radix sort.
 */
@Deprecated
public class ExternalBucketSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private byte[] arr;
    private BufferedOutputStream[] outputStreams;
    private RandomAccessFile[] randomAccessFiles;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);

        outputStreams = new BufferedOutputStream[256];
        randomAccessFiles = new RandomAccessFile[256];
    }

    /**
     * Separates all numbers in groups making one group per first byte for a total of 256 groups.
     */
    private void sortByFirstByte() throws IOException {
        arr = Resources.arr;

        int len = 0;
        int lastLen = 0;

        InputStream d = new FileInputStream(firstFile);

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            randomAccessFiles[realInd] = new RandomAccessFile(secondFile, "rw");

            randomAccessFiles[realInd].skipBytes(lastLen * 3);

            lastLen += Resources.count[realInd];

            outputStreams[realInd] = new BufferedOutputStream(new FileOutputStream(randomAccessFiles[realInd].getFD()));
        }

        while (true) {
            len = d.read(arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                outputStreams[arr[i] & 0xff].write(arr, i + 1, 3);
            }
        }

        for (int i = 0; i < 256; i++) {
            outputStreams[i].close();
            randomAccessFiles[i].close();
        }
        d.close();
    }

    @Override
    public void sort() throws IOException {
        int len;

        sortByFirstByte();

        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "rw");
        BufferedOutputStream firstFileStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));

        FileInputStream inputStream = new FileInputStream(secondFile);

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            len = Resources.count[realInd];

            if (len == 0) {
                continue;
            }

            inputStream.read(arr, 0, len * 3);

            int realLen = len * 4;
            int minVal = (realInd << 24);
            int maxVal = (realInd << 24) | (255 << 16) | (255 << 8) | 255;

            for (int j = 3 * len - 3; j >= 0; j -= 3) {
                arr[realLen - 1] = arr[j + 2];
                arr[realLen - 2] = arr[j + 1];
                arr[realLen - 3] = arr[j];
                arr[realLen - 4] = (byte) realInd;
                realLen -= 4;
            }

            Resources.convertToIntegers(len);
            Resources.integerArr =
                    BucketIntegerSort.attemptBucketSort(Resources.integerArr, len, minVal, maxVal,
                            Resources.secondIntegerArr);
            Resources.convertToBytes(len);

            firstFileStream.write(arr, 0, len * 4);
        }

        firstFileStream.close();
        randomAccessFile.close();
        inputStream.close();
    }

    @Override
    public String getStrategy() {
        return "External bucket sort (one thread)";
    }
}
