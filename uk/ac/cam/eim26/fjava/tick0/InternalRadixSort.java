package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;

public class InternalRadixSort implements ExternalSortBase {
    private int[] countingSortArr;
    private File firstFile;

    private void countingSort(int minValue, int maxValue) {
        int value;
        int ptr = 0;

        countingSortArr = new int[maxValue - minValue + 1];

        for (int i = 0; i < Resources.totalSize * 4; i += 4) {
            value = ByteUtil.bytesToInteger(Resources.arr, i / 4);
            countingSortArr[value - minValue]++;
        }

        for (int i = minValue; i <= maxValue; i++) {
            for (int j = 1; j <= countingSortArr[i - minValue]; j++) {
                Resources.arr[ptr] = (byte)(i >> 24);
                Resources.arr[ptr+1] = (byte)((i >> 16) & (0xff));
                Resources.arr[ptr+2] = (byte)((i >> 8) & (0xff));
                Resources.arr[ptr+3] = (byte)(i & 0xff);
                ptr += 4;
            }
        }

        return;
    }

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
    }

    @Override
    public void sort() throws Exception {
        Resources.arr = new byte[(int) Resources.totalSize * 4];

        InputStream inputStream = new FileInputStream(firstFile);
        int len = inputStream.read(Resources.arr);
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int value;

        if (len == -1) {
            return;
        }

        for (int i = 0; i < len; i += 4) {
            value = ByteUtil.bytesToInteger(Resources.arr, i / 4);
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }

        System.out.println("Difference is " + ((long)maxValue-(long)minValue));

        if ( (long)maxValue - (long)minValue < Resources.blockSize / 4 ) {
            countingSort(minValue, maxValue);
        }
        else {
            RadixByteSort.sortByteArray(Resources.arr, len / 4);
        }

        inputStream.close();

        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "rw");
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));

        outputStream.write(Resources.arr, 0, len);

        outputStream.close();
        randomAccessFile.close();
    }

    @Override
    public String getStrategy() {
        return "Internal radix sort";
    }
}
