package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.util.Random;

public class LightweightInternalRadixSort implements ExternalSortBase {
    private File firstFile;
    private int[] arr;

    private void countingSort(int minValue, int maxValue) {
        int value;
        int ptr = 0;
        int[] countingSortArr;
        int len = (int)Resources.totalSize;

        countingSortArr = new int[maxValue - minValue + 1];

        for (int i = 0; i < len; i++) {
            countingSortArr[ arr[i] - minValue ]++;
        }

        for (int i = minValue; i <= maxValue; i++) {
            for (int j = 1; j <= countingSortArr[i - minValue]; j++) {
                arr[ptr] = i;
                ptr++;
            }
        }
    }

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
    }

    @Override
    public void sort() throws Exception {
        arr = new int[(int)Resources.totalSize];

        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "r");
        DataInputStream inputStream = new DataInputStream(
                new BufferedInputStream(new FileInputStream(randomAccessFile.getFD())));
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int len = (int)Resources.totalSize;

        for (int i = 0; i < len; i++) {
            arr[i] = inputStream.readInt();

            minValue = Math.min(minValue, arr[i]);
            maxValue = Math.max(maxValue, arr[i]);
        }

        System.out.println("Difference is " + ((long)maxValue-(long)minValue));

        if ( (long)maxValue - (long)minValue < Resources.blockSize / 4 ) {
            countingSort(minValue, maxValue);
        }
        else {
            RadixIntegerSort.sortIntArray(arr, len);
        }

        inputStream.close();
        randomAccessFile.close();

        randomAccessFile = new RandomAccessFile(firstFile, "rw");
        DataOutputStream outputStream = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD())));

        for (int i = 0; i < len; i++) {
            outputStream.writeInt(arr[i]);
        }

        outputStream.close();
        randomAccessFile.close();
    }

    @Override
    public String getStrategy() {
        return "Internal radix sort";
    }
}
