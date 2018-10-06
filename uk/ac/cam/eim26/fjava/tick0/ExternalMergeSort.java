package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.util.ArrayList;

//TODO - clean up to make it look like a proper separated class
public class ExternalMergeSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<BufferedInputStream> streamsToMerge;
    private ArrayList<Integer> blockOffsets;
    private ArrayList<Integer> blockEndings;
    private ArrayList<Integer> currentPointers;
    private ArrayList<Integer> currentIntegers;

    private byte[] arr;

    private int blocks = 0;

    private byte[][] byteCache;

    private int bufferSize;
    private int[] bufferMarks;

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

    private void fillBuffer(int buf, int offset) throws IOException {
        streamsToMerge.get(buf).read(arr, offset, bufferSize);
        bufferMarks[buf] = 0;
    }

    private int readNextInteger(int block) throws NoNumbersLeftException, IOException {
        if (currentPointers.get(block) > blockEndings.get(block)) {
            throw new NoNumbersLeftException();
        }

        int offset = bufferSize * block;

        if (bufferMarks[block] >= bufferSize) {
            fillBuffer(block, offset);
        }

        byteCache[block][0] = arr[offset + bufferMarks[block]];
        byteCache[block][1] = arr[offset + bufferMarks[block] + 1];
        byteCache[block][2] = arr[offset + bufferMarks[block] + 2];
        byteCache[block][3] = arr[offset + bufferMarks[block] + 3];

        currentPointers.set(block, currentPointers.get(block) + 1);
        bufferMarks[block] += 4;

        return  ((byteCache[block][0] & 0xff) << 24) |
                ((byteCache[block][1] & 0xff) << 16) |
                ((byteCache[block][2] & 0xff) << 8) |
                (byteCache[block][3] & 0xff);

    }

    private void writeNextValue(BufferedOutputStream outputStream) throws NoNumbersLeftException, IOException {
        if (pq.empty()) {
            throw new NoNumbersLeftException();
        }

        int minIndex = pq.top();

        writeBytes(outputStream, minIndex);

        if (!currentPointers.get(minIndex).equals(blockEndings.get(minIndex)) ) {
            int nextNumber;

            nextNumber = readNextInteger(minIndex);

            pq.replaceTop(nextNumber);
        }
        else {
            currentPointers.set(minIndex, currentPointers.get(minIndex) + 1);
            pq.popTop();
        }
    }

    private void writeBytes(BufferedOutputStream outStream, int block) throws IOException {
        outStream.write(byteCache[block]);
    }

    @Override
    public void sort() throws IOException, NoNumbersLeftException {
        arr = Resources.arr;

        int len = 0;

        FileInputStream inputStream = new FileInputStream(firstFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "rw");
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()));

        //First pass - sort blocks
        while(true) {
            len = inputStream.read(arr);

            if (len == -1) {
                break;
            }

            blocks++;

            Resources.convertToIntegers(len / 4);
            RadixIntegerSort.sortIntArray(Resources.integerArr, len / 4);
            Resources.convertToBytes(len / 4);
            RadixByteSort.sortByteArray(arr, len / 4);

            blockOffsets.add((blocks - 1) * Resources.blockSize);
            blockEndings.add((blocks - 1) * Resources.blockSize + (len / 4));

            outputStream.write(arr, 0, len);
        }

        outputStream.close();
        randomAccessFile.close();
        inputStream.close();

        //Second pass - merge results
        bufferSize = 4 * (Resources.blockSize / blocks);
        bufferMarks = new int[blocks];
        byteCache = new byte[blocks][4];

        for (int i = 0; i < blocks; i++) {
            bufferMarks[i] = bufferSize + 1;
        }

        for (int i = 0; i < blocks; i++) {
            BufferedInputStream stream = new BufferedInputStream( new FileInputStream(secondFile) );

            stream.skip(blockOffsets.get(i) * Integer.BYTES);

            streamsToMerge.add(stream);

            currentPointers.add(blockOffsets.get(i));
            currentIntegers.add(readNextInteger(i));
        }

        randomAccessFile = new RandomAccessFile(firstFile, "rw");
        outputStream = new BufferedOutputStream( new FileOutputStream(randomAccessFile.getFD()) );
        pq = new CustomPriorityQueue(blocks, currentIntegers);

        //TODO - Do without an exception
        while(true) {
            try {
                writeNextValue(outputStream);
            }catch(NoNumbersLeftException e) {
                break;
            }
        }

        outputStream.close();
        randomAccessFile.close();

        for (BufferedInputStream stream : streamsToMerge) {
            stream.close();
        }
    }

    @Override
    public String getStrategy() {
        return "External merge sort";
    }
}
