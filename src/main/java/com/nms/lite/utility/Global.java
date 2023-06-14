package com.nms.lite.utility;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import static com.nms.lite.utility.Constant.*;
import java.util.concurrent.atomic.AtomicLong;

public class Global
{
    public static AtomicLong provisionCounter = new AtomicLong(1000);

    public static JsonObject FormatErrorResponse(String message)
    {

        return new JsonObject()

                .put(STATUS, STATUS_FAIL)

                .put(STATUS_CODE, STATUS_CODE_BAD_REQUEST)

                .put(STATUS_MESSAGE, message)

                .put(STATUS_RESULT, EMPTY_STRING);
    }

    public static void sendExceptionMessage(RoutingContext context)
    {
        context.json(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_MESSAGE, SOME_EXCEPTION_OCCURRED));
    }
}
