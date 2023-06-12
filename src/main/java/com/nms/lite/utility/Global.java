package com.nms.lite.utility;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.atomic.AtomicLong;

public class Global
{
    public static AtomicLong provisionCounter = new AtomicLong(1000);

    public static JsonObject FormatErrorResponse(String message)
    {

        return new JsonObject()

                .put(Constant.STATUS, Constant.STATUS_FAIL)

                .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                .put(Constant.STATUS_MESSAGE, message)

                .put(Constant.STATUS_RESULT, Constant.EMPTY_STRING);
    }

    public static void sendExceptionMessage(RoutingContext context)
    {
        context.json(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_MESSAGE, Constant.SOME_EXCEPTION_OCCURRED));
    }
}
