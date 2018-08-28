package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

//1576483782260
public class Benchmark {
    private static byte[] byteArray = new byte[(1<<19)];
    private static int[] intArray = new int[(1<<18)];

    public static void readByteStream(String f) throws IOException {
        InputStream inputStream = new FileInputStream(f);
        int len = 0;
        long sum = 0;

        while(len >= 0) {
            len = inputStream.read(byteArray);

            for (int i = 0; i < len; i++) {
                sum = sum + (long) PartialByteHeapSort.bytesToInteger(byteArray, i / 4);
            }
        }

        System.out.println("Sum is " + sum + " (regular input stream)");
    }

    public static void readBufferedByteStream(String f) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(f));
        int len = 0;
        long sum = 0;

        while(len >= 0) {
            len = inputStream.read(byteArray);

            for (int i = 0; i < len; i++) {
                sum = sum + (long) PartialByteHeapSort.bytesToInteger(byteArray, i / 4);
            }
        }

        System.out.println("Sum is " + sum + " (buffered input stream)");
    }

    public static void readDataStream(String f) throws IOException {
        DataInputStream inputStream = new DataInputStream(new FileInputStream(f));
        int num = 0;
        long sum = 0;

        while(sum != 0 || true) {
            try {
                num = inputStream.readInt();
                sum += num;
            }
            catch (EOFException e) {
                break;
            }
        }

        System.out.println("Sum is " + sum + " (regular data stream)");
    }

    public static void readBufferedDataStream(String f) throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f), (1<<18)));
        int num = 0;
        long sum = 0;

        while(sum != 0 || true) {
            try {
                num = inputStream.readInt();
                sum += num;
            }
            catch (EOFException e) {
                break;
            }
        }

        System.out.println("Sum is " + sum + " (buffered data stream)");
    }

    public static void readCustomIntegerStream(String f) throws IOException {
        IntegerInputStream inputStream = new IntegerInputStream(new FileInputStream(f));
        long sum = 0;

        while(sum != 0 || true) {
            try {
                inputStream.read(intArray);
                System.out.println("Read " + intArray.length);
                for (int num : intArray) {
                    sum += num;
                }
            }
            catch (EOFException e) {
                break;
            }
        }

        System.out.println("SUM = " + sum);
    }

    public static void writeStream(String f) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
        for (int i = 1; i <= 1000; i++) {
            for (int j = 0; j < (1<<19); j++) {
                byteArray[j] = (byte)j;
            }
            outputStream.write(byteArray, 0, (1<<19));
        }
    }

    public static void writeCompressedStream(String f) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.HUFFMAN_ONLY);

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(f));
        DeflaterOutputStream outputStream = new DeflaterOutputStream(bufferedOutputStream, deflater);

        for (int i = 1; i <= 1000; i++) {
            for (int j = 0; j < (1<<19); j++) {
                byteArray[j] = (byte)j;
            }

            outputStream.write(byteArray, 0, (1<<19));
        }
    }
}
