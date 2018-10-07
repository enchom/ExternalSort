/**
 * THIS SUBMISSION IS WORK-IN-PROGRESS. IT DOES NOT COMPLY WITH THE REQUIRED
 * STYLE GUIDE AND IT MAY INCLUDE UNUSED CODE, BAD STRUCTURE OR GENERALLY BAD DESIGN.
 */

//TODO: Add JavaDoc

package uk.ac.cam.eim26.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * The root of the project
 */
public class ExternalSort {


    public static void sort(String f1, String f2) throws Exception {
        ExternalSortBase externalSortStrategy;

        long myTime = System.nanoTime();

        if ((new File(f1)).length() <= 4) {
            return;
        }

        externalSortStrategy = StrategySelector.selectStrategy(f1);
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
            for (byte v : md.digest())
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
        sort(args[0], args[1]);
        System.out.println(checkSum(args[0]));
    }
}