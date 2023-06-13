package com.nms.lite.utility;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;

public class PingDevice
{
    public Future<Boolean> ping(String ipAddress, Vertx vertx)
    {
        Promise<Boolean> promise = Promise.promise();

        List<String> command = new ArrayList<>();

        command.add(Constant.FPING);

        command.add(Constant.COMMAND_COUNT);

        command.add(Constant.PACKET_COUNT);

        command.add(Constant.COMMAND_SUPPRESSED);

        command.add(ipAddress);

        BuildProcess.build(command, Constant.PING_TIMEOUT, false, vertx).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                if (handler.result().getString(Constant.PROCESS_STATUS).equals(Constant.PROCESS_NORMAL))
                {
                    var splitWithLine = handler.result().getString(Constant.STATUS_RESULT).split(Constant.NEW_LINE);

                    for (String s : splitWithLine)
                    {
                        String[] filteredResult = s.split(Constant.COLON)[1].split(Constant.EQUAL_TO)[1].split(Constant.FORWARD_SLASH);

                        if (filteredResult[0].trim().equals(filteredResult[1].trim()) && filteredResult[2].substring(0, filteredResult[2].indexOf(Constant.PERCENTAGE)).equals(Constant.NUMERIC_ZERO_IN_STRING))
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
