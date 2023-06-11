package com.nms.lite.utility;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PingDevice
{
    public Future<JsonObject> ping(String ipAddress, Vertx vertx)
    {
        Promise<JsonObject> promise = Promise.promise();

        BuildProcess process = new BuildProcess();

        List<String> command = new ArrayList<>();

        command.add(Constant.FPING);

        command.add(Constant.COMMAND_COUNT);

        command.add(Constant.PACKET_COUNT);

        command.add(Constant.COMMAND_SUPPRESSED);

        command.add(ipAddress);

        process.build(command, Constant.PING_TIMEOUT,vertx).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                JsonObject result = handler.result();

                if (result.getString(Constant.PROCESS_STATUS).equals(Constant.PROCESS_NORMAL))
                {
                    var splitWithLine = result.getString(Constant.STATUS_ERROR).split(Constant.NEW_LINE);

                    for (String s : splitWithLine)
                    {
                        String[] filteredResult = s.split(Constant.COLON)[1].split(Constant.EQUAL_TO)[1].split(Constant.FORWARD_SLASH);

                        if (filteredResult[0].trim().equals(filteredResult[1].trim()) && filteredResult[2].substring(0, filteredResult[2].indexOf(Constant.PERCENTAGE)).equals(Constant.NUMERIC_ZERO_IN_STRING))
                        {
                            result = new JsonObject()

                                    .put(Constant.STATUS, Constant.STATUS_SUCCESS)

                                    .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS));

                            promise.complete(result);
                        }

                        else
                        {
                            result = new JsonObject()

                                    .put(Constant.STATUS, Constant.STATUS_ERROR)

                                    .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS));

                            promise.complete(result);

                        }
                    }
                }
                else
                {
                    result = new JsonObject()

                            .put(Constant.STATUS, Constant.STATUS_FAIL)

                            .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS));

                    promise.complete(result);
                }
            }
        });


        return promise.future();

    }

}
