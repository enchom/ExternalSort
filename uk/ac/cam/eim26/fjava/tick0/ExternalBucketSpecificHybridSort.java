package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split

public class ExternalBucketSpecificHybridSort extends ExternalBucketSortBase {
    private int[] countingSortArr;
    private int COUNTING_SIZE;
    private byte[] arr;

    public void sort() throws IOException {
        arr = Resources.arr;

        int skipped = 0;

        COUNTING_SIZE = Resources.blockSize / 4;
        countingSortArr = new int[COUNTING_SIZE];

        sortByFirstByte();

        BufferedOutputStream firstFileOutputStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        InputStream secondFileInputStream = new BufferedInputStream(new FileInputStream(secondFile));

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            if (Resources.count[realInd] == 0) {
                continue;
            }

            int len = 0, leftEnd = 0, rightEnd;

            while(leftEnd <  0) {

            }

            skipped += Resources.count[realInd] * 3;
        }

        firstFileOutputStream.close();
        secondFileInputStream.close();
    }

    @Override
    public String getStrategy() {
        return "External bucket specific hybrid sort";
    }
}
