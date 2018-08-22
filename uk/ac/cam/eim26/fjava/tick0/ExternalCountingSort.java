package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;

public class ExternalCountingSort implements ExternalSortBase {
    private File firstFile;
    private int[] counting;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
    }

    @Override
    public void sort() throws Exception {
        InputStream inputStream = new FileInputStream(firstFile);

        counting = new int[Resources.maxValue - Resources.minValue + 1];

        while (true) {
            int len = inputStream.read(Resources.arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                counting[ PartialByteHeapSort.bytesToInteger(Resources.arr, i / 4) - Resources.minValue ]++;
            }
        }

        inputStream.close();

        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        int ptr = 0;

        for (int i = Resources.minValue; i <= Resources.maxValue; i++) {
            for (int j = 1; j <= counting[i - Resources.minValue]; j++) {
                Resources.arr[ptr] = (byte)(i >> 24);
                Resources.arr[ptr+1] = (byte)((i >> 16) & (0xff));
                Resources.arr[ptr+2] = (byte)((i >> 8) & (0xff));
                Resources.arr[ptr+3] = (byte)(i & 0xff);
                ptr += 4;

                if (ptr == Resources.arr.length) {
                    outputStream.write(Resources.arr);
                    ptr = 0;
                }
            }
        }

        if (ptr > 0) {
            outputStream.write(Resources.arr, 0, ptr);
        }

        outputStream.close();
    }

    @Override
    public String getStrategy() {
        return "External counting sort";
    }
}
