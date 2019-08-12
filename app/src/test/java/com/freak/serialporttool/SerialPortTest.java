package com.freak.serialporttool;


import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Freak
 * @date 2019/8/10.
 */
public class SerialPortTest {
    public static void main(String[] args) {
        String a = "3938373635343332313938373635343332310D";
        String b = "31353631353631313435313534310D";
        serialPortTest(a);
//        serialPortTest(b);
    }

    @Test
    public static void serialPortTest(String data) {
//        System.out.println(ByteUtil.hexStr2decimal(data));
//        System.out.println(ByteUtil.str2HexString(data));
//            System.out.println(Arrays.toString(Hex.encodeHex(data.getBytes())));
//            System.out.println(Arrays.toString(fromHex(data)));
        System.out.println(hexToString(data));
        System.out.println(hexToString1(data));
//            System.out.println(hexToString2(data));
        System.out.println(hexToString3(data));
//        try {
//            System.out.println(str2HexStr2(data,"UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }


    }

    /**
     * 将编码字符串解码为字节数组
     *
     * @param data
     * @return
     */
    public static byte[] fromHex(String data) {

        byte[] ret = null;
        //todo 将字符串转换为字节数组
        if (data != null) {
            int len = data.length();
            char[] chs = data.toCharArray();
            ret = new byte[len / 2];
            int ih = 0, il = 0, v = 0, j = 0;
            if (len > 0 && len % 2 == 0) {
                for (int i = 0; i < len - 1; i += 2, j++) {
                    char ch = chs[i];
                    char cl = chs[i + 1];

                    if (ch >= 'A' && ch <= 'F') {
                        ih = 10 + (ch - 'A');
                    } else if (ch >= 'a' && ch <= 'f') {
                        ih = 10 + (ch - 'a');
                    } else if (ch > '0' && ch <= '9') {
                        ih = ch - '0';
                    }

                    if (cl >= 'A') {
                        il = 10 + (cl - 'A');
                    } else if (cl >= 'a') {
                        il = 10 + (cl - 'a');
                    } else if (cl > '0') {
                        il = cl - '0';
                    }
                    v = ((ih & 0x0f) << 4) | (il & 0x0f);
                    ret[j] = (byte) v;

                }
            }
        }
        return ret;
    }

    /**
     * 将原始字符串转换成16进制字符串【方法二】
     */
    public static String str2HexStr2(String str, String charsetName) throws UnsupportedEncodingException {
        byte[] bs = str.getBytes(charsetName);
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, bit; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    /**
     * 十六进制字符串转换成十进制数字  耗性能，计算慢
     *
     * @param hex
     * @return
     */
    public static String hexToString(String hex) {
        StringBuilder sb = new StringBuilder();

        for (int count = 0; count < hex.length() - 1; count += 2) {
            String output = hex.substring(count, (count + 2));    //分离字符串，两位一组

            int decimal = Integer.parseInt(output, 16);    //十六进制到十进制

            sb.append((char) decimal);    //将小数点转换为字符
        }
        return sb.toString();
    }

    /**
     * 相对上面的快些
     *
     * @param hex
     * @return
     */
    public static String hexToString1(String hex) {
        StringBuilder sb = new StringBuilder();
        char[] hexData = hex.toCharArray();
        for (int count = 0; count < hexData.length - 1; count += 2) {
            int firstDigit = Character.digit(hexData[count], 16);
            int lastDigit = Character.digit(hexData[count + 1], 16);
            int decimal = firstDigit * 16 + lastDigit;
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    /******************************************************/

    private static final Map<String, Character> lookupHex = new HashMap<String, Character>();

    static {
        for (int i = 0; i < 256; i++) {
            String key = Integer.toHexString(i);
            Character value = (char) (Integer.parseInt(key, 16));
            lookupHex.put(key, value);
        }
    }

    public static String hexToString2(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int count = 0; count < hex.length() - 1; count += 2) {
            String output = hex.substring(count, (count + 2));
            sb.append((char) lookupHex.get(output));
        }
        return sb.toString();
    }

    /**********************************************************/

    public static String hexToString3(final String str) {
        return new String(new BigInteger(str, 16).toByteArray());
    }
}
