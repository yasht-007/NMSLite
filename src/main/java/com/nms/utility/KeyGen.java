package com.nms.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyGen
{
    public static long getUniqueKeyForName(String name)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(name.getBytes(StandardCharsets.UTF_8));

            long hashCode = byteArrayToLong(hashBytes);

            return Math.abs(hashCode);
        }

        catch (NoSuchAlgorithmException exception)
        {
            exception.printStackTrace();
        }

        return -1;
    }

    public static long byteArrayToLong(byte[] bytes)
    {
        long value = 0;

        for (int i = 0; i < 3 && i < bytes.length; i++)
        {
            value |= (long) (bytes[i] & 0xFF) << (8 * i);
        }

        return value;
    }
}
