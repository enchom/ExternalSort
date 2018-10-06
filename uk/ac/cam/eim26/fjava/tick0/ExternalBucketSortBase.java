package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public abstract class ExternalBucketSortBase implements ExternalSortBase {
    protected File firstFile;
    protected File secondFile;

    protected ArrayList<Integer> blockOffsets = new ArrayList<>();
    protected ArrayList<Integer> blockEndings = new ArrayList<>();
    protected ArrayList<Integer> currentPointers = new ArrayList<>();

    protected byte[] arr;
    protected BufferedOutputStream[] outputStreams = new BufferedOutputStream[256];
    protected RandomAccessFile[] randomAccessFiles = new RandomAccessFile[256];

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);
    }

    public void sortByFirstByte() throws IOException {
        long localTime, T1 = 0, T2 = 0, T3 = 0;

        arr = Resources.arr;

        int len = 0;
        int lastLen = 0;

        InputStream d = new FileInputStream(firstFile);

        for (int i = 0; i < 256; i++) {
            blockOffsets.add(0);
            blockEndings.add(0);
            currentPointers.add(0);
        }

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            localTime = System.nanoTime();
            randomAccessFiles[realInd] = new RandomAccessFile(secondFile, "rw");

            blockOffsets.set(realInd, lastLen * 3);
            blockEndings.set(realInd, lastLen * 3 + Resources.count[realInd] * 3);
            currentPointers.set(realInd, lastLen * 3);

            randomAccessFiles[realInd].skipBytes(lastLen * 3);
            T1 += System.nanoTime() - localTime;

            lastLen += Resources.count[realInd];

            outputStreams[realInd] = new BufferedOutputStream(new FileOutputStream(randomAccessFiles[realInd].getFD()));
        }

        //First pass - sort blocks
        while (true) {
            localTime = System.nanoTime();
            len = d.read(arr);
            T2 += System.nanoTime() - localTime;

            if (len == -1) {
                break;
            }

            localTime = System.nanoTime();
            for (int i = 0; i < len; i += 4) {
                outputStreams[ arr[i]&0xff ].write(arr, i + 1, 3);
            }
            T3 += System.nanoTime() - localTime;
        }

        localTime = System.nanoTime();
        for (int i = 0; i < 256; i++) {
            outputStreams[i].close();
            randomAccessFiles[i].close();
        }
        d.close();
        System.out.println("Closing buffers in " + (System.nanoTime() - localTime)/1000000 + "ms");
        System.out.println("Buffer creating and skipping in " + T1/1000000 + "ms");
        System.out.println("Reading in " + T2/1000000 + "ms");
        System.out.println("Writing in " + T3/1000000 + "ms");
    }
}
