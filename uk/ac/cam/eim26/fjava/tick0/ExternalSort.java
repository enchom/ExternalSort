/**
 * THIS SUBMISSION IS WORK-IN-PROGRESS. IT DOES NOT COMPLY WITH THE REQUIRED
 * STYLE GUIDE AND IT MAY INCLUDE UNUSED CODE, BAD STRUCTURE OR GENERALLY BAD DESIGN.
 */

package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * The root class of the project
 */
public class ExternalSort {
    private static long startTime;
    private static ExternalSortBase externalSortStrategy;

    public static void sort(String f1, String f2) throws Exception {
        long myTime = System.nanoTime();

        externalSortStrategy = StrategySelector.selectStrategy(f1);

        System.out.println("STRATEGY CHOSEN IN " + (System.nanoTime() - myTime)/1000000 + "ms");

        System.out.println("Chosen strategy = " + externalSortStrategy.getStrategy());
        externalSortStrategy.setFiles(f1, f2);
        externalSortStrategy.sort();

        long myTotalTime = (System.nanoTime() - myTime) / 1000000;

        System.out.println("MY MEASURE OF TIME IS " + myTotalTime + "ms");
    }

    private static String byteToHex(byte b) {
        String r = Integer.toHexString(b);
        if (r.length() == 8) {
            return r.substring(6);
        }
        return r;
    }

    public static String checkSum(String f) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream ds = new DigestInputStream(new FileInputStream(f), md);
            byte[] b = new byte[512];
            while (ds.read(b) != -1) ;

            String computed = "";
            for(byte v : md.digest())
                computed += byteToHex(v);

            return computed;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<error computing checksum>";
    }

    public static void printFile(String f) throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

        System.out.println("FILE " + f);
        while(true) {
            try {
                int num = inputStream.readInt();
                System.out.println(num);
            } catch(EOFException e) {
                break;
            }
        }
        System.out.println("-------------------");

        inputStream.close();
    }

    public static void generateFile() throws IOException {
        String fileName_A = "test_specialA.dat";
        String fileName_B = "test_specialB.dat";
        Random random = new Random();
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName_A)));
        int printed = 0;

        for (int i = 0; i < 7500; i++) {
            printed++;

            int min = -2147483648;
            int max = -2147483549;
            int randomInteger = random.nextInt(max - min + 1) + min;

            outputStream.writeInt(randomInteger);
        }

        for (int i = 0; i < 2500; i++) {
            printed++;

            int bit3 = random.nextInt(256);
            int bit4 = random.nextInt(256);
            int num = (127 << 24) | (255 << 16) | (bit3 << 8) | (bit4);

            outputStream.writeInt(num);
        }

        for (int i = 10000; i < 10000 + 4990000; i++) {
            printed++;

            outputStream.writeInt(i);
        }

        for (int i = 2137483648 + 5000000 - 1; i >= 2137483648; i--) {
            printed++;

            outputStream.writeInt(i);
        }

        System.out.println("PRINTED " + printed);

        outputStream.close();
        outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName_B)));
        for (int i = 0; i < printed; i++) {
            outputStream.writeInt(0);
        }

        outputStream.close();

        return;
    }

    public static void generateRandomFile() throws IOException {
        String fileName_A = "test_specialA.dat";
        String fileName_B = "test_specialB.dat";
        Random random = new Random();
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName_A)));

        for (int i = 0; i < 10000000; i++) {
            outputStream.writeInt(random.nextInt());
        }
        outputStream.close();

        outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName_B)));

        for (int i = 0; i < 10000000; i++) {
            outputStream.writeInt(random.nextInt());
        }
        outputStream.close();

        return;
    }

    public static void normalMain() throws Exception {
        String f1 = "test_specialA.dat";
        String f2 = "test_specialB.dat";

        startTime = System.nanoTime();

        System.out.println("Total mem = " + Runtime.getRuntime().totalMemory());

        sort(f1, f2);

        long duration = System.nanoTime() - startTime;

        System.out.println("The checksum is: " + checkSum(f1));
        System.out.println("Total time = " + (double)duration / 1000000.0);
    }

    public static void main(String[] args) throws Exception {
        //String f1 = args[0];
        //String f2 = args[1];

        //generateFile();
        //generateRandomFile();
        normalMain();
    }
}