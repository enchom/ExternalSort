package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.util.ArrayList;

//TODO - clean up to make it look like a proper separated class
public class ExternalMergeSort implements ExternalSortBase {
    private File firstFile;
    private File secondFile;

    private ArrayList<BufferedInputStream> streamsToMerge = new ArrayList<>();
    private ArrayList<Integer> blockOffsets = new ArrayList<>();
    private ArrayList<Integer> blockEndings = new ArrayList<>();
    private ArrayList<Integer> currentPointers = new ArrayList<>();
    private ArrayList<Integer> currentIntegers = new ArrayList<>();

    private byte[] arr;

    private int blocks = 0;

    private byte[][] byteCache;

    private int bufferSize;
    private int[] bufferMarks;

    private CustomPriorityQueue pq;

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

        return;
    }

    private void writeBytes(BufferedOutputStream outStream, int block) throws IOException {
        outStream.write(byteCache[block]);
    }

    @Override
    public void setFiles(String f1, String f2) {
        firstFile = new File(f1);
        secondFile = new File(f2);
    }

    @Override
    public void sort() throws IOException, NoNumbersLeftException {
        arr = Resources.arr;

        int len = 0;

        BufferedInputStream d = new BufferedInputStream(new FileInputStream(firstFile));
        BufferedOutputStream dOut = new BufferedOutputStream(new FileOutputStream(secondFile));

        //First pass - sort blocks
        while(true) {
            len = d.read(arr);

            if (len == -1) {
                break;
            }

            blocks++;

            arr = RadixByteSort.sortByteArray(arr, len / 4);

            blockOffsets.add((blocks - 1) * Resources.blockSize);
            blockEndings.add((blocks - 1) * Resources.blockSize + (len >> 2));

            dOut.write(arr, 0, len);
        }

        dOut.close();
        d.close();

        System.out.println("Total " + blocks + " blocks");

        System.out.println("Finished first pass");
        //startTime = System.nanoTime();

        //Second pass - merge results

        bufferSize = 4 * (Resources.blockSize / blocks);
        bufferMarks = new int[blocks];
        byteCache = new byte[blocks][4];

        for (int i = 0; i < blocks; i++) {
            bufferMarks[i] = bufferSize + 1;
        }

        System.out.println("Size is " + bufferSize);

        for (int i = 0; i < blocks; i++) {
            BufferedInputStream stream = new BufferedInputStream( new FileInputStream(secondFile) );

            stream.skip(blockOffsets.get(i) * Integer.BYTES);

            streamsToMerge.add(stream);

            currentPointers.add(blockOffsets.get(i));
            currentIntegers.add(readNextInteger(i));
        }

        dOut = new BufferedOutputStream( new FileOutputStream(firstFile) );
        pq = new CustomPriorityQueue(blocks, currentIntegers);

        //TODO - Do without an exception
        while(true) {
            try {
                writeNextValue(dOut);
            }catch(NoNumbersLeftException e) {
                break;
            }
        }

        dOut.close();

        for (BufferedInputStream stream : streamsToMerge) {
            stream.close();
        }
    }

    @Override
    public String getStrategy() {
        return "External merge sort";
    }
}
