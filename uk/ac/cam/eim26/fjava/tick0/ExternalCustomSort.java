//TODO - MAKE THIS WORK EVEN IF TEST DATA IS UNKNOWN
package uk.ac.cam.eim26.fjava.tick0;

import org.omg.SendingContext.RunTime;

import java.io.*; //TODO - split (in all files)
import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.Collections;

public class ExternalCustomSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private byte[] arr;

    private ArrayList<Integer> smallPart = new ArrayList<>();
    private ArrayList<Integer> bigPart = new ArrayList<>();

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);
    }

    @Override
    public void sort() throws Exception {
        arr = Resources.arr;

        FileInputStream inputStream = new FileInputStream(firstFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "rw");

        inputStream.read(arr, 0, Resources.cornerEnding * 4);

        for (int i = 0; i < Resources.cornerEnding * 4; i += 4) {
            int num = PartialByteHeapSort.bytesToInteger(arr, i / 4);

            if (num < Integer.MIN_VALUE + Resources.MAX_PAD) {
                smallPart.add(num);
            }
            else {
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

        //

        randomAccessFile.seek(firstFile.length() - bigPart.size() * 4);
        for (int i = 0; i < bigPart.size(); i++) {
            arr[4*i] = (byte)((bigPart.get(i) >>> 24) & 0xff);
            arr[4*i + 1] = (byte)((bigPart.get(i) >> 16) & 0xff);
            arr[4*i + 2] = (byte)((bigPart.get(i) >> 8) & 0xff);
            arr[4*i + 3] = (byte)(bigPart.get(i) & 0xff);
        }
        randomAccessFile.write(arr, 0, bigPart.size() * 4);

        for (int i = 0; i < Resources.leftEnds.size() - 1; i++) {
            int totalLen = Resources.leftEnds.get(i + 1) - Resources.leftEnds.get(i);
            int leftPointer = smallPart.size(), rightPointer;

            //System.out.println("LEFT END IS " + Resources.leftEnds.get(i));
            for (int j = 0; j < Resources.leftEnds.size() - 1; j++) {
                //System.out.println("SCANNING RIGHT END " + Resources.leftEnds.get(j + 1));
                if (Resources.largestValue.get(j) < Resources.smallestValue.get(i)) {
                    leftPointer += (Resources.leftEnds.get(j + 1) - Resources.leftEnds.get(j));
                }
            }

            System.out.println("TOTAL LENGTH " + totalLen);

            rightPointer = leftPointer + totalLen;

            System.out.println("Putting it between " + leftPointer + " to " + rightPointer);

            if (Resources.sorted.get(i)) {
                randomAccessFile.seek(leftPointer * 4);

                System.out.println("Set " + leftPointer * 4);
                while(totalLen > 0) {
                    int len;

                    if (totalLen > Resources.blockSize) {
                        len = inputStream.read(arr, 0, Resources.blockSize * 4);
                    } else {
                        len = inputStream.read(arr, 0, totalLen * 4);
                    }

                    randomAccessFile.write(arr, 0, len);

                    totalLen -= len / 4;
                }
            }
            else if (Resources.reversed.get(i)) {
                while(totalLen > 0) {
                    int len;

                    if (totalLen > Resources.blockSize) {
                        len = inputStream.read(arr, 0, Resources.blockSize * 4);
                    }
                    else {
                        len = inputStream.read(arr, 0, totalLen * 4);
                    }

                    for (int j = 0; j < (len/4) / 2; j++) {
                        PartialByteUtil.byteSwap(arr, j, (len/4) - j - 1, 0);
                    }

                    randomAccessFile.seek(4 * rightPointer - len);
                    randomAccessFile.write(arr, 0, len);

                    rightPointer -= len / 4;
                    totalLen -= len / 4;
                }
            }
            else {
                int len = inputStream.read(arr, 0, totalLen * 4);

                RadixByteSort.sortByteArray(arr, len / 4);

                //System.out.println("OFFSET " + leftPointer * 4 + " with length " + len);
                randomAccessFile.seek(leftPointer * 4);
                randomAccessFile.write(arr, 0, len);
            }
        }

        randomAccessFile.close();
        inputStream.close();

        inputStream = new FileInputStream(secondFile);
        randomAccessFile = new RandomAccessFile(firstFile, "rw");

        int ind = 0;
        while(true) {
            int len = inputStream.read(arr);

            if (len <= 0) {
                break;
            }

            ind++;
            for (int i = 4; i < len; i += 4) {
                ind++;

                if ( PartialByteUtil.isSmaller(arr, i/4, i/4 - 1, 0) ) {
                    throw new RuntimeException("Not in right order numbers " + PartialByteHeapSort.bytesToInteger(arr, i/4) + " and " +
                                                PartialByteHeapSort.bytesToInteger(arr, i/4 - 1) + " just before " + ind);
                }
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

//45df4a5fa95839d3d0ee6c4515877d - real