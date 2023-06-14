package com.nms.lite.utility;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import static com.nms.lite.utility.Constant.*;
import java.util.ArrayList;
import java.util.List;

public class PingDevice
{
    public Future<Boolean> ping(String ipAddress, Vertx vertx)
    {
        Promise<Boolean> promise = Promise.promise();

        List<String> command = new ArrayList<>();

        command.add(FPING);

        command.add(COMMAND_COUNT);

        command.add(PACKET_COUNT);

        command.add(COMMAND_SUPPRESSED);

        command.add(ipAddress);

        BuildProcess.build(command, PING_TIMEOUT, false).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                if (handler.result().getString(PROCESS_STATUS).equals(PROCESS_NORMAL))
                {
                    var splitWithLine = handler.result().getString(STATUS_RESULT).split(NEW_LINE);

                    for (String s : splitWithLine)
                    {
                        String[] filteredResult = s.split(COLON)[1].split(EQUAL_TO)[1].split(FORWARD_SLASH);

                        if (filteredResult[0].trim().equals(filteredResult[1].trim()) && filteredResult[2].substring(0, filteredResult[2].indexOf(PERCENTAGE)).equals(NUMERIC_ZERO_IN_STRING))
                        {
                            promise.complete(true);
                        }

                        else
                        {
                            promise.complete(false);
                        }
                    }
                }
                else
                {
                    promise.complete(false);
                }
            }
        });


        return promise.future();

    }

}
