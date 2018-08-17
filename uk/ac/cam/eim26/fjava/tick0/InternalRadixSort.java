package uk.ac.cam.eim26.fjava.tick0;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class InternalRadixSort implements ExternalSortBase {
    @Override
    public void sort(String f1, String f2) throws Exception {
        Resources.arr = new byte[(int) Resources.totalSize * 4];

        InputStream inputStream = new FileInputStream(f1);
        int len = inputStream.read(Resources.arr);

        if (len == -1) {
            return;
        }

        RadixByteSort.sortByteArray(Resources.arr, len / 4);

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
