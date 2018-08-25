package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.util.ArrayList;

public abstract class ExternalBucketSortBase implements ExternalSortBase {
    protected File firstFile;
    protected File secondFile;

    protected ArrayList<Integer> blockOffsets = new ArrayList<>();
    protected ArrayList<Integer> blockEndings = new ArrayList<>();
    protected ArrayList<Integer> currentPointers = new ArrayList<>();

    protected byte[] arr;
    protected BufferedOutputStream[] outputStreams = new BufferedOutputStream[256];

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);
    }

    public void sortByFirstByte() throws IOException {
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
    }
}
