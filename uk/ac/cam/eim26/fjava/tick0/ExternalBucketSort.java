package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split

public class ExternalBucketSort extends ExternalBucketSortBase {
    public void sort() throws IOException {
        long endToSend = System.nanoTime();
        long T1 = 0, T2 = 0, T3 = 0, T4 = 0, localTime;

        int len;

        localTime = System.nanoTime();
        sortByFirstByte();
        System.out.println("First byte sort is " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "rw");
        BufferedOutputStream firstFileStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));
        System.out.println("BufferedOutputStream for first file takes " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        FileInputStream d = new FileInputStream(secondFile); //TODO - More descriptive name
        System.out.println("FileInputStream for second file takes " + (System.nanoTime() - localTime)/1000000 + "ms");

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            len = Resources.count[realInd];

            if (len == 0) {
                continue;
            }

            localTime = System.nanoTime();
            d.read(arr, 0, len * 3);
            T1 += System.nanoTime() - localTime;

            int realLen = len * 4;

            localTime = System.nanoTime();
            for (int j = 3 * len - 3; j >= 0; j -= 3) {
                arr[realLen-1] = arr[j+2];
                arr[realLen-2] = arr[j+1];
                arr[realLen-3] = arr[j];
                arr[realLen-4] = (byte) realInd;
                realLen -= 4;
            }
            T2 += System.nanoTime() - localTime;

            localTime = System.nanoTime();
            Resources.convertToIntegers(len);
            int minVal = (realInd << 24);
            int maxVal = (realInd << 24) | (255 << 16) | (255 << 8) | 255;
            BucketIntegerSort.attemptBucketSort(Resources.integerArr, len, minVal, maxVal, Resources.secondIntegerArr);
            Resources.convertToBytes(len);

            T3 += System.nanoTime() - localTime;

            localTime = System.nanoTime();
            firstFileStream.write(arr, 0, len * 4);
            T4 += System.nanoTime() - localTime;
        }

        System.out.println("Reading " + T1/1000000 + "ms");
        System.out.println("Converting " + T2/1000000 + "ms");
        System.out.println("Sorting " + T3/1000000 + "ms");
        System.out.println("Sorting.Preparing " + BucketIntegerSort.prepareTime/1000000 + "ms");
        System.out.println("Sorting.Sorting " + BucketIntegerSort.sortTime/1000000 + "ms");
        System.out.println("Writing " + T4/1000000 + "ms");

        localTime = System.nanoTime();
        firstFileStream.flush();
        System.out.println("Flushing took " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        firstFileStream.close();
        randomAccessFile.close();
        System.out.println("Closing first file stream is " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        d.close();
        System.out.println("Closing second file stream is " + (System.nanoTime() - localTime)/1000000 + "ms");

        System.out.println("Total end to end is " + (System.nanoTime() - endToSend)/1000000 + "ms");
    }

    @Override
    public String getStrategy() {
        return "External bucket sort (one thread)";
    }
}
