package com.nms.lite.utility;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                if (key.equals(Constant.PASSWORD))
                {
                    if (!validatePattern(Constant.PASS_REGEX, requestBody.getString(key).trim()))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(Constant.CREDENTIALS_ID) || key.equals(Constant.DISCOVERY_ID) || key.equals(Constant.PROVISION_ID))
                {
                    if (!validatePattern(Constant.DIGITS_REGEX, String.valueOf(requestBody.getLong(key))))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(Constant.IP_ADDRESS))
                {
                    if (!validatePattern(Constant.IP_REGEX, requestBody.getString(key).trim()))
                    {
                        addErrors(errors, key);
                    }
                }

                if (key.equals(Constant.PORT_NUMBER))
                {
                    if (!validatePattern(Constant.DIGITS_REGEX, String.valueOf(requestBody.getInteger(key))))
                    {
                        addErrors(errors, key);
                    }

                    if (!validatePattern(Constant.PORT_REGEX, String.valueOf(requestBody.getInteger(key))))
                    {
                        addErrors(errors, key);
                    }
                }

            });

            return new JsonObject().put(Constant.STATUS_ERROR, errors);
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            return new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_MESSAGE, Constant.SOME_EXCEPTION_OCCURRED);
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
        return input == null || input.equalsIgnoreCase(Constant.EMPTY_STRING);
    }

    private static void addErrors(JsonArray errors, String key)
    {
        errors.add(Constant.INVALID + Constant.EMPTY_SPACE + key);
    }

}
