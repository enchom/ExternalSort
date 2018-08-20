package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split

public class ExternalBucketSpecificHybridSort extends ExternalBucketSortBase {
    public void sort(String f1, String f2) throws IOException {
        int len;

        sortByFirstByte(f1, f2);

        BufferedOutputStream firstFileStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        InputStream d = new FileInputStream(secondFile); //TODO - More descriptive name

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            len = Resources.count[realInd];

            if (len == 0) {
                continue;
            }

            d.read(arr, 0, len * 3);

            int realLen = len * 4;

            for (int j = 3 * len - 3; j >= 0; j -= 3) {
                arr[realLen-1] = arr[j+2];
                arr[realLen-2] = arr[j+1];
                arr[realLen-3] = arr[j];
                arr[realLen-4] = (byte) realInd;
                realLen -= 4;
            }

            RadixByteSort.sortByteArray(arr, len, 1);

            firstFileStream.write(arr, 0, len * 4);
        }

        firstFileStream.close();
        d.close();
    }

    @Override
    public String getStrategy() {
        return "External bucket specific hybrid sort";
    }
}
