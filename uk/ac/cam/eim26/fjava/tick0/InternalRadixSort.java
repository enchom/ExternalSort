package uk.ac.cam.eim26.fjava.tick0;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class InternalRadixSort implements ExternalSortBase {
    int[] countingSortArr;

    private void countingSort(int minValue, int maxValue) {
        int value;
        int ptr = 0;

        countingSortArr = new int[maxValue - minValue + 1];

        for (int i = 0; i < Resources.totalSize; i += 4) {
            value = PartialByteHeapSort.bytesToInteger(Resources.arr, i / 4);
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
    public void sort(String f1, String f2) throws Exception {
        Resources.arr = new byte[(int) Resources.totalSize * 4];

        InputStream inputStream = new FileInputStream(f1);
        int len = inputStream.read(Resources.arr);
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int value;

        if (len == -1) {
            return;
        }

        for (int i = 0; i < len; i += 4) {
            value = PartialByteHeapSort.bytesToInteger(Resources.arr, i / 4);
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

        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f1));

        outputStream.write(Resources.arr, 0, len);

        outputStream.close();
    }

    @Override
    public String getStrategy() {
        return "Internal radix sort";
    }
}
