package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static java.lang.Thread.yield;

public class ExternalBucketSortOneThread implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<Integer> blockOffsets = new ArrayList<>();
    private ArrayList<Integer> blockEndings = new ArrayList<>();
    private ArrayList<Integer> currentPointers = new ArrayList<>();

    private byte[] arr;
    BufferedOutputStream[] outputStreams = new BufferedOutputStream[256];

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
        }

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "rw");

            blockOffsets.set(realInd, lastLen * 3);
            blockEndings.set(realInd, lastLen * 3 + Resources.count[realInd] * 3);
            currentPointers.set(realInd, lastLen * 3);

            randomAccessFile.skipBytes(lastLen * 3);

            lastLen += Resources.count[realInd];

            outputStreams[realInd] = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));
        }

        //First pass - sort blocks
        while (true) {
            len = d.read(arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                outputStreams[ arr[i]&0xff ].write(arr, i + 1, 3);
            }
        }

        for (int i = 0; i < 256; i++) {
            outputStreams[i].close();
        }
        d.close();

        firstFileStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        d = new FileInputStream(secondFile);

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
        return "External bucket sort (one thread)";
    }
}
