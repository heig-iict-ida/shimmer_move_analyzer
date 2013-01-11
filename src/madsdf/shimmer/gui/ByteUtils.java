/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madsdf.shimmer.gui;

/**
 * Utilities functions to deal with bytes and other low-level magic
 * @author julien
 */
public class ByteUtils {
     /**
     * Convert an unsigned 2 bytes little-eidna integer to Integer
     */
    public static int uint16ToInt(byte lb, byte hb) {
        return ((int)hb << 8) | ((int)lb & 0xFF);
    }

    /**
     * Convert an array of bytes in their hexadecimal string representation
     *
     * @param b is the bytes array
     * @param a is the number of byte to convert
     * @return the hexadecimal string representation
     */
    public static String getHexString(byte[] b, int a) {

        String result = "";
        try {
            for (int i = 0; i < b.length && i < a; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            }
        } catch (Exception ex) {
            System.err.println("AccelGyroSample.getHexString : " + ex);
        }
        return result.toUpperCase();
    }  
}
