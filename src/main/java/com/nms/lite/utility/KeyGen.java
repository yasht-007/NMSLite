package com.nms.lite.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.nms.lite.utility.Constant.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyGen
{
    private static final Logger logger = LoggerFactory.getLogger(KeyGen.class);
    public static long getUniqueKeyForName(String name)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);

            byte[] hashBytes = digest.digest(name.getBytes(StandardCharsets.UTF_8));

            long hashCode = byteArrayToLong(hashBytes);

            return Math.abs(hashCode);
        }

        catch (NoSuchAlgorithmException exception)
        {
            logger.error(exception.getMessage());
        }

        return -1;
    }

    private static long byteArrayToLong(byte[] bytes)
    {
        long value = 0;

        for (int i = 0; i < 3 && i < bytes.length; i++)
        {
            value |= (long) (bytes[i] & 0xFF) << (8 * i);
        }

        return value;
    }
}
