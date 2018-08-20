/**
 * THIS SUBMISSION IS WORK-IN-PROGRESS. IT DOES NOT COMPLY WITH THE REQUIRED
 * STYLE GUIDE AND IT MAY INCLUDE UNUSED CODE, BAD STRUCTURE OR GENERALLY BAD DESIGN.
 */

package uk.ac.cam.eim26.fjava.tick0;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The root class of the project
 */
public class ExternalSort {
    private static long startTime;
    private static ExternalSortBase externalSortStrategy;

    public static void sort(String f1, String f2) throws Exception {
        externalSortStrategy = StrategySelector.selectStrategy(f1);
        System.out.println("Chosen strategy = " + externalSortStrategy.getStrategy());
        externalSortStrategy.sort(f1, f2);
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

    public static void main(String[] args) throws Exception {
        //String f1 = args[0];
        //String f2 = args[1];

        String f1 = "test17a.dat";
        String f2 = "test17b.dat";

        startTime = System.nanoTime();

        System.out.println("Total mem = " + Runtime.getRuntime().totalMemory());

        sort(f1, f2);

        long duration = System.nanoTime() - startTime;

        System.out.println("The checksum is: " + checkSum(f1));
        System.out.println("Total time = " + (double)duration / 1000000.0);
    }
}