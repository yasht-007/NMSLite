package com.nms.utility;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class RequestValidator
{
    public static JsonObject validateRequestBody(JsonObject requestBody)
    {
        List<String> errors = new ArrayList<>();
        
        requestBody.fieldNames().forEach(key ->
        {
            var isNull = checkNull(requestBody.getString(key).trim());

            if (isNull)
            {
                errors.add(Constant.INVALID + key);
            }
        });


        return new JsonObject().put(Constant.ERROR,errors);
    }

    public static boolean checkNull(String input)
    {
        return input == null || input.equalsIgnoreCase(Constant.EMPTY_STRING);
    }
}
