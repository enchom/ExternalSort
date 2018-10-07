package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;

/**
 * External merge sort. For efficiency reasons the initial blocks are of large size and are
 * sorted using radix sort internally. K-way merge is used to merge all blocks in a single step so as to reduce
 * passes over files and hence I/O.
 */
@Deprecated
public class ExternalMergeSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<DataInputStream> streamsToMerge;
    private ArrayList<Integer> blockOffsets;
    private ArrayList<Integer> blockEndings;
    private ArrayList<Integer> currentPointers;
    private ArrayList<Integer> currentIntegers;
    private byte[] arr;
    private int blocks = 0;
    private byte[][] byteCache;

    private CustomPriorityQueue pq;

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);

        streamsToMerge = new ArrayList<>();
        blockOffsets = new ArrayList<>();
        blockEndings = new ArrayList<>();
        currentPointers = new ArrayList<>();
        currentIntegers = new ArrayList<>();
    }

    /**
     * Reads and returns the next integer from the corresponding block's stream
     */
    private int readNextInteger(int block) throws IOException {
        int value = streamsToMerge.get(block).readInt();

        byteCache[block][0] = (byte) ((value >> 24) & 0xff);
        byteCache[block][1] = (byte) ((value >> 16) & 0xff);
        byteCache[block][2] = (byte) ((value >> 8) & 0xff);
        byteCache[block][3] = (byte) (value & 0xff);

        currentPointers.set(block, currentPointers.get(block) + 1);

        return value;

    }

    /**
     * Writes the smallest value to the given output stream and replaces it with the next smallest one in the
     * priority queue.
     */
    private boolean writeNextValue(BufferedOutputStream outputStream) throws IOException {
        if (pq.empty()) {
            return false;
        }

        int minIndex = pq.top();

        writeBytes(outputStream, minIndex);

        if (!currentPointers.get(minIndex).equals(blockEndings.get(minIndex))) {
            int nextNumber;

            nextNumber = readNextInteger(minIndex);

            pq.replaceTop(nextNumber);
        } else {
            currentPointers.set(minIndex, currentPointers.get(minIndex) + 1);
            pq.popTop();
        }

        return true;
    }

    private void writeBytes(BufferedOutputStream outStream, int block) throws IOException {
        outStream.write(byteCache[block]);
    }

    @Override
    public void sort() throws IOException {
        arr = Resources.arr;

        int len = 0;

        FileInputStream inputStream = new FileInputStream(firstFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "rw");
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));

        //First pass - sort blocks
        while (true) {
            len = inputStream.read(arr);

            if (len == -1) {
                break;
            }

            blocks++;

            Resources.convertToIntegers(len / 4);
            RadixIntegerSort.sortIntArray(Resources.integerArr, len / 4);
            Resources.convertToBytes(len / 4);

            blockOffsets.add((blocks - 1) * Resources.blockSize);
            blockEndings.add((blocks - 1) * Resources.blockSize + (len / 4));

            outputStream.write(arr, 0, len);
        }

        outputStream.close();
        randomAccessFile.close();
        inputStream.close();

        //Second pass - merge results
        byteCache = new byte[blocks][4];

        for (int i = 0; i < blocks; i++) {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(secondFile)));
            stream.skipBytes(blockOffsets.get(i) * 4);

            streamsToMerge.add(stream);

            currentPointers.add(blockOffsets.get(i));
            currentIntegers.add(readNextInteger(i));
        }

        randomAccessFile = new RandomAccessFile(firstFile, "rw");
        outputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));
        pq = new CustomPriorityQueue(blocks, currentIntegers);

        while (writeNextValue(outputStream)) ;

        outputStream.close();
        randomAccessFile.close();

        for (DataInputStream stream : streamsToMerge) {
            stream.close();
        }
    }

    @Override
    public String getStrategy() {
        return "External merge sort";
    }
}
