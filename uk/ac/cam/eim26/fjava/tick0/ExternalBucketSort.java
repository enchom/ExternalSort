package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static java.lang.Thread.yield;

//TODO - Test if working with 3 bytes after sorting first one is better
//TODO - Make buffer memory dynamic
public class ExternalBucketSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<Integer> blockOffsets = new ArrayList<>();
    private ArrayList<Integer> blockEndings = new ArrayList<>();
    private ArrayList<Integer> currentPointers = new ArrayList<>();

    private byte[] arr;

    AsynchronousFileChannel[] asyncOutputs = new AsynchronousFileChannel[256];

    private static final int BUFFER_LENGTH = 4096;
    private byte[][] asyncBuffers = new byte[256][BUFFER_LENGTH];
    private byte[][] asyncBuffersCopy = new byte[256][BUFFER_LENGTH];
    private int[] asyncBuffersLen = new int[256];
    private int writeCounter = 0;
    private final WriteHandler handler = new WriteHandler();

    private void flushBuffer(int ind) {
        while (!handler.getFree(ind)) {
            yield();
        }

        handler.setFree(ind);

        byte[] copyHelper = asyncBuffers[ind];
        asyncBuffers[ind] = asyncBuffersCopy[ind];
        asyncBuffersCopy[ind] = copyHelper;

        asyncOutputs[ind]
                .write((ByteBuffer) ByteBuffer.wrap(asyncBuffersCopy[ind], 0, asyncBuffersLen[ind]).rewind(),
                        currentPointers.get(ind), ind, handler);

        writeCounter++;

        currentPointers.set(ind, currentPointers.get(ind) + asyncBuffersLen[ind]);
        asyncBuffersLen[ind] = 0;
    }

    private void asyncWrite(int ind, int bitsInd) {
        int bufLen = asyncBuffersLen[ind];

        for (int i = 0; i < 3; i++) {
            asyncBuffers[ind][bufLen + i] = arr[bitsInd + i + 1];
        }
        asyncBuffersLen[ind] += 3;

        if (asyncBuffersLen[ind] + 3 >= BUFFER_LENGTH) {
            flushBuffer(ind);
        }
    }

    public void sort(String f1, String f2) throws IOException {
        arr = Resources.arr;

        firstFile = new File(f1);
        secondFile = new File(f2);
        int len = 0;
        int lastLen = 0;

        InputStream d = new FileInputStream(firstFile);
        BufferedOutputStream firstFileStream;

        for (int i = 0; i < 256; i++) {
            blockOffsets.add(0);
            blockEndings.add(0);
            currentPointers.add(0);
            asyncBuffersLen[i] = 0;
        }

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            asyncOutputs[realInd] = AsynchronousFileChannel.open(secondFile.toPath(), StandardOpenOption.WRITE);

            blockOffsets.set(realInd, lastLen * 3);
            blockEndings.set(realInd, lastLen * 3 + Resources.count[realInd] * 3);
            currentPointers.set(realInd, lastLen * 3);

            lastLen += Resources.count[realInd];
        }

        //First pass - sort blocks
        while (true) {
            len = d.read(arr);

            if (len == -1) {
                break;
            }

            //System.out.println("Block of size " + len);

            for (int i = 0; i < len; i += 4) {
                asyncWrite(arr[i] & 0xff, i);
            }
        }

        for (int i = 0; i < 256; i++) {
            if (asyncBuffersLen[i] > 0) {
                flushBuffer(i);
            }
        }

        System.out.println("Wrote " + writeCounter + " times. Waiting.");

        while (handler.getSuccessCounter() != writeCounter) {
            yield();
            //System.out.println(handler.getSuccessCounter() + " +vs- " + handler.getFailCounter());
        }

        System.out.println("Wrote all");

        for (int i = 0; i < 256; i++) {
            asyncOutputs[i].close();
        }
        d.close();

        firstFileStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        d = new FileInputStream(secondFile);

        /*for (int i = 0; i < 256; i++) {
            //System.out.println("For " + i + " " + blockOffsets.get(i) + " / " + currentPointers.get(i) + " / " + blockEndings.get(i));

            if (!currentPointers.get(i).equals(blockEndings.get(i))) {
                System.out.println("NO POR FAVOR");
                throw new RuntimeException();
            }
        }*/

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

            //System.out.println("READING A BLOCK OF SIZE " + realLen);

            /*if (realLen != len * 4) {
                System.out.println("mismatch");

                throw new RuntimeException();
            }

            System.out.println("VALUE = " + PartialByteHeapSort.bytesToInteger(arr, 0));

            for (int j = 4; j < len * 4; j += 4) {
                if (arr[j] != arr[j - 4] || (arr[j] & 0xff) != realInd) {
                    System.out.println("Difference");
                    System.out.println("At " + realInd + " we have " + (arr[j] & 0xff) + " and " + (arr[j - 4] & 0xff) +
                            " at index " + j / 4 + " at byte " + j);

                    System.out.println(arr[j] + " vs " + arr[j - 4]);
                    System.out.println("Next is " + arr[j + 4] + " and " + arr[j + 8]);
                    throw new RuntimeException();
                }
            }*/

            //PartialByteHeapSort.byteHeapSort(arr, 0, len - 1);
            RadixByteSort.sortByteArray(arr, len, 1);

            /*for (int j = 1; j < len; j++) {
                if (ByteUtil.isSmaller(arr, j, j - 1)) {
                    System.out.println("NOT SORTED");
                    throw new RuntimeException();
                }
            }*/

            firstFileStream.write(arr, 0, len * 4);
        }

        firstFileStream.close();
        d.close();
    }

    @Override
    public String getStrategy() {
        return "External bucket sort";
    }
}
