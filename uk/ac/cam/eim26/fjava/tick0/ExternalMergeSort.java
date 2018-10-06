package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.util.ArrayList;

//TODO - clean up to make it look like a proper separated class
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

    private int readNextInteger(int block) throws NoNumbersLeftException, IOException {
        if (currentPointers.get(block) > blockEndings.get(block)) {
            throw new NoNumbersLeftException();
        }

        int value = streamsToMerge.get(block).readInt();

        byteCache[block][0] = (byte)((value >> 24) & 0xff);
        byteCache[block][1] = (byte)((value >> 16) & 0xff);
        byteCache[block][2] = (byte)((value >> 8) & 0xff);
        byteCache[block][3] = (byte)(value & 0xff);

        currentPointers.set(block, currentPointers.get(block) + 1);

        return value;

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
        byteCache = new byte[blocks][4];

        for (int i = 0; i < blocks; i++) {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(secondFile)));
            //BufferedInputStream stream = new BufferedInputStream( new FileInputStream(secondFile) );

            stream.skipBytes(blockOffsets.get(i) * 4);

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

        //for (BufferedInputStream stream : streamsToMerge) {
        //    stream.close();
        //}
    }

    @Override
    public String getStrategy() {
        return "External merge sort";
    }
}
