//TODO - MAKE THIS WORK EVEN IF TEST DATA IS UNKNOWN
package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split (in all files)
import java.util.ArrayList;

public class ExternalBucketDoubleSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<Integer> blockOffsets = new ArrayList<>();
    private ArrayList<Integer> blockEndings = new ArrayList<>();
    private ArrayList<Integer> currentPointers = new ArrayList<>();

    private byte[] arr;
    private BufferedOutputStream[] outputStreams;
    private RandomAccessFile[] randomAccessFiles;
    private int[] totalCount;

    int firstBytes = 0;
    private int[] bigToSmall = new int[256];
    private int[] smallToBig = new int[3];

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);
    }

    private void sortLastTwoBytes() throws IOException {
        long T1 = 0, T2 = 0, T3 = 0, T4 = 0, localTime;

        RandomAccessFile randomAccessFile = new RandomAccessFile(firstFile, "rw");
        BufferedOutputStream firstFileStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));

        FileInputStream inputStream = new FileInputStream(secondFile);

        for (int bt = 0; bt < firstBytes; bt++) {
            for (int i = 0; i < 256; i++) {
                int index = bt * 256 + i;
                int len = totalCount[index];

                if (len == 0) {
                    continue;
                }

                localTime = System.nanoTime();
                inputStream.read(arr, 0, len * 2);
                T1 += System.nanoTime() - localTime;

                int realLen = len * 4;

                localTime = System.nanoTime();
                for (int j = 2 * len - 2; j >= 0; j -= 2) {
                    arr[realLen-1] = arr[j+1];
                    arr[realLen-2] = arr[j];
                    arr[realLen-3] = (byte) i;
                    arr[realLen-4] = (byte) smallToBig[bt];
                    realLen -= 4;
                }
                T2 += System.nanoTime() - localTime;

                localTime = System.nanoTime();
                RadixByteSort.sortByteArray(arr, len, 2);
                T3 += System.nanoTime() - localTime;

                localTime = System.nanoTime();
                firstFileStream.write(arr, 0, len * 4);
                T4 += System.nanoTime() - localTime;
            }
        }

        System.out.println("Reading " + T1/1000000 + "ms");
        System.out.println("Converting " + T2/1000000 + "ms");
        System.out.println("Sorting " + T3/1000000 + "ms");
        System.out.println("Writing " + T4/1000000 + "ms");

        localTime = System.nanoTime();
        firstFileStream.flush();
        System.out.println("Flushing took " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        firstFileStream.close();
        randomAccessFile.close();
        System.out.println("Closing first file stream is " + (System.nanoTime() - localTime)/1000000 + "ms");

        localTime = System.nanoTime();
        inputStream.close();
        System.out.println("Closing second file stream is " + (System.nanoTime() - localTime)/1000000 + "ms");
    }

    public void sort() throws IOException {
        long localTime, T1 = 0, T2 = 0, T3 = 0;

        arr = Resources.arr;

        int len = 0;
        int lastLen = 0;

        InputStream inputStream = new FileInputStream(firstFile);

        for (int i = 0; i < 256; i++) {
            if (Resources.count[i] > 0) {
                bigToSmall[i] = firstBytes;
                smallToBig[firstBytes] = i;
                firstBytes++;
            }
        }

        outputStreams = new BufferedOutputStream[firstBytes * 256];
        randomAccessFiles = new RandomAccessFile[firstBytes * 256];
        totalCount = new int[firstBytes * 256];

        localTime = System.nanoTime();
        while (true) {
            len = inputStream.read(arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                totalCount[ bigToSmall[ arr[i]&0xff ] * 256 + (arr[i+1]&0xff) ]++;
            }
        }
        inputStream.close();;
        inputStream = new FileInputStream(firstFile);

        System.out.println("Initial count takes " + (System.nanoTime() - localTime)/1000000 + "ms");

        for (int i = 0; i < firstBytes * 256; i++) {
            blockOffsets.add(0);
            blockEndings.add(0);
            currentPointers.add(0);
        }

        for (int bt = 0; bt < firstBytes; bt++) {
            for (int i = 0; i < 256; i++) {
                int index = bt * 256 + i;

                localTime = System.nanoTime();
                randomAccessFiles[index] = new RandomAccessFile(secondFile, "rw");

                blockOffsets.set(index, lastLen * 2);
                blockEndings.set(index, lastLen * 2 + totalCount[index] * 2);
                currentPointers.set(index, lastLen * 2);

                randomAccessFiles[index].skipBytes(lastLen * 2);
                T1 += System.nanoTime() - localTime;

                lastLen += totalCount[index];

                outputStreams[index] = new BufferedOutputStream(new FileOutputStream(randomAccessFiles[index].getFD()), 4096);
                System.out.println("Allocated buffer, memory = " + Runtime.getRuntime().freeMemory());
            }
        }

        //First pass - sort blocks
        while (true) {
            localTime = System.nanoTime();
            len = inputStream.read(arr);
            T2 += System.nanoTime() - localTime;

            if (len == -1) {
                break;
            }

            localTime = System.nanoTime();
            for (int i = 0; i < len; i += 4) {
                outputStreams[ bigToSmall[arr[i] & 0xff] * 256 + (arr[i+1] & 0xff) ].write(arr, i + 2, 2);
            }
            T3 += System.nanoTime() - localTime;
        }

        localTime = System.nanoTime();
        for (int i = 0; i < 256; i++) {
            outputStreams[i].close();
            randomAccessFiles[i].close();
        }
        inputStream.close();

        System.out.println("Closing buffers in " + (System.nanoTime() - localTime) / 1000000 + "ms");
        System.out.println("Buffer creating and skipping in " + T1 / 1000000 + "ms");
        System.out.println("Reading in " + T2 / 1000000 + "ms");
        System.out.println("Writing in " + T3 / 1000000 + "ms");

        sortLastTwoBytes();
    }

    @Override
    public String getStrategy() {
        return "External bucket double sort";
    }
}
