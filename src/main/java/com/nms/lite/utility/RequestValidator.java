package com.nms.lite.utility;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.nms.lite.utility.Constant.*;

public class RequestValidator
{
    private static final Logger logger = LoggerFactory.getLogger(RequestValidator.class);
    public static Pattern pattern;
    public static Matcher matcher;

    public static JsonObject validateRequestBody(JsonObject requestBody)
    {
        try
        {
            JsonArray errors = new JsonArray();

            requestBody.fieldNames().forEach(key ->
            {
                var isNull = checkNull(requestBody.getString(key).trim());

                if (isNull)
                {
                    addErrors(errors, key);
                }

                if (key.equals(PASSWORD))
                {
                    if (!validatePattern(PASS_REGEX, requestBody.getString(key).trim()))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(CREDENTIALS_ID) || key.equals(DISCOVERY_ID) || key.equals(PROVISION_ID))
                {
                    if (!validatePattern(DIGITS_REGEX, String.valueOf(requestBody.getLong(key))))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(IP_ADDRESS))
                {
                    if (!validatePattern(IP_REGEX, requestBody.getString(key).trim()))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(PORT_NUMBER))
                {
                    if (!validatePattern(DIGITS_REGEX, String.valueOf(requestBody.getInteger(key))))
                    {
                        addErrors(errors, key);
                    }

                    if (!validatePattern(PORT_REGEX, String.valueOf(requestBody.getInteger(key))))
                    {
                        addErrors(errors, key);
                    }
                }

            });

            return new JsonObject().put(STATUS_ERROR, errors);
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            return new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_MESSAGE, SOME_EXCEPTION_OCCURRED);
        }
    }

    public static boolean validatePattern(String regexPattern, String input)
    {

        pattern = Pattern.compile(regexPattern);

        matcher = pattern.matcher(input);

        return matcher.matches();
    }

    public static boolean checkNull(String input)
    {
        return input == null || input.equalsIgnoreCase(EMPTY_STRING);
    }

    private static void addErrors(JsonArray errors, String key)
    {
        errors.add(INVALID + EMPTY_SPACE + key);
    }

}
