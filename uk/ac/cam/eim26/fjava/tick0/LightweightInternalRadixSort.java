package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class LightweightInternalRadixSort implements ExternalSortBase {
    private File firstFile;
    private int[] arr;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
    }

    @Override
    public void sort() throws Exception {
        arr = new int[(int)Resources.totalSize];

        DataInputStream inputStream = new DataInputStream(new FileInputStream(firstFile));
        int len = (int)Resources.totalSize;
        int swp;

        for (int i = 0; i < len; i++) {
            arr[i] = inputStream.readInt();

            for (int j = i - 1; j >= 0; j--) {
                if (arr[j] > arr[j + 1]) {
                    swp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = swp;
                }
                else {
                    break;
                }
            }
        }

        inputStream.close();

        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(firstFile));

        for (int i = 0; i < len; i++) {
            outputStream.writeInt(arr[i]);
        }

        outputStream.close();
    }

    @Override
    public String getStrategy() {
        return "Lightweight internal radix sort";
    }
}
