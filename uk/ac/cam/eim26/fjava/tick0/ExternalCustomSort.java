package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Custom external sort. Tailored for very specific files that start with a small number of very small or
 * very large integers and continue with large segments of either perfectly sorted or perfectly reversed disjoint
 * bits.
 */
public class ExternalCustomSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;
    private byte[] arr;

    private ArrayList<Integer> smallPart;
    private ArrayList<Integer> bigPart;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);

        smallPart = new ArrayList<>();
        bigPart = new ArrayList<>();
    }

    @Override
    public void sort() throws Exception {
        arr = Resources.arr;

        FileInputStream inputStream = new FileInputStream(firstFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "rw");

        inputStream.read(arr, 0, Resources.cornerEnding * 4);

        for (int i = 0; i < Resources.cornerEnding * 4; i += 4) {
            int num = ByteUtil.bytesToInteger(arr, i / 4);

            if (num < Integer.MIN_VALUE + Resources.MAX_PAD) {
                smallPart.add(num);
            } else {
                bigPart.add(num);
            }
        }

        Collections.sort(smallPart);
        Collections.sort(bigPart);

        randomAccessFile.seek(0);
        for (int i = 0; i < smallPart.size(); i++) {
            arr[4 * i] = (byte) ((smallPart.get(i) >>> 24) & 0xff);
            arr[4 * i + 1] = (byte) ((smallPart.get(i) >>> 16) & 0xff);
            arr[4 * i + 2] = (byte) ((smallPart.get(i) >>> 8) & 0xff);
            arr[4 * i + 3] = (byte) ((smallPart.get(i)) & 0xff);
        }
        randomAccessFile.write(arr, 0, smallPart.size() * 4);

        randomAccessFile.seek(firstFile.length() - bigPart.size() * 4);
        for (int i = 0; i < bigPart.size(); i++) {
            arr[4 * i] = (byte) ((bigPart.get(i) >>> 24) & 0xff);
            arr[4 * i + 1] = (byte) ((bigPart.get(i) >> 16) & 0xff);
            arr[4 * i + 2] = (byte) ((bigPart.get(i) >> 8) & 0xff);
            arr[4 * i + 3] = (byte) (bigPart.get(i) & 0xff);
        }
        randomAccessFile.write(arr, 0, bigPart.size() * 4);

        for (int i = 0; i < Resources.leftEnds.size() - 1; i++) {
            int totalLen = Resources.leftEnds.get(i + 1) - Resources.leftEnds.get(i);
            int leftPointer = smallPart.size(), rightPointer;

            for (int j = 0; j < Resources.leftEnds.size() - 1; j++) {
                if (Resources.largestValue.get(j) < Resources.smallestValue.get(i)) {
                    leftPointer += (Resources.leftEnds.get(j + 1) - Resources.leftEnds.get(j));
                }
            }

            rightPointer = leftPointer + totalLen;

            if (Resources.sorted.get(i)) {
                randomAccessFile.seek(leftPointer * 4);

                while (totalLen > 0) {
                    int len;

                    if (totalLen > Resources.blockSize) {
                        len = inputStream.read(arr, 0, Resources.blockSize * 4);
                    } else {
                        len = inputStream.read(arr, 0, totalLen * 4);
                    }

                    randomAccessFile.write(arr, 0, len);

                    totalLen -= len / 4;
                }
            } else if (Resources.reversed.get(i)) {
                while (totalLen > 0) {
                    int len;

                    if (totalLen > Resources.blockSize) {
                        len = inputStream.read(arr, 0, Resources.blockSize * 4);
                    } else {
                        len = inputStream.read(arr, 0, totalLen * 4);
                    }

                    for (int j = 0; j < (len / 4) / 2; j++) {
                        ByteUtil.byteSwap(arr, j, (len / 4) - j - 1);
                    }

                    randomAccessFile.seek(4 * rightPointer - len);
                    randomAccessFile.write(arr, 0, len);

                    rightPointer -= len / 4;
                    totalLen -= len / 4;
                }
            } else {
                int len = inputStream.read(arr, 0, totalLen * 4);

                Resources.convertToIntegers(len / 4);
                RadixIntegerSort.sortIntArray(Resources.integerArr, len / 4);
                Resources.convertToBytes(len / 4);

                randomAccessFile.seek(leftPointer * 4);
                randomAccessFile.write(arr, 0, len);
            }
        }

        randomAccessFile.close();
        inputStream.close();

        inputStream = new FileInputStream(secondFile);
        randomAccessFile = new RandomAccessFile(firstFile, "rw");

        while (true) {
            int len = inputStream.read(arr);

            if (len <= 0) {
                break;
            }

            randomAccessFile.write(arr, 0, len);
        }

        inputStream.close();
        randomAccessFile.close();
    }

    @Override
    public String getStrategy() {
        return "External custom sort";
    }
}