package com.nms.lite.service;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.BuildProcess;
import com.nms.lite.utility.Constant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryService extends AbstractVerticle
{
    private final EventBus eventBus = Bootstrap.getEventBus();

    @Override
    public void start(Promise<Void> startPromise)
    {
        try
        {
            eventBus.<JsonObject>localConsumer(Constant.RUN_DISCOVERY).handler(message ->
            {
                long discoveryId = message.body().getLong(Constant.DISCOVERY_ID);

                vertx.executeBlocking(promise ->
                {
                    handleDicovery(discoveryId, promise);

                }, handler ->
                {
                    if (handler.succeeded())
                    {
                        JsonObject successResult = (JsonObject) handler.result();

                        message.reply(successResult);

                    }

                    else
                    {
                        JsonObject failResult = new JsonObject(handler.cause().getMessage());

                        message.reply(failResult);

                    }
                });

            });

            startPromise.complete();
        }

        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void handleDicovery(long discoveryId, Promise<Object> promise)
    {

        eventBus.<JsonObject>request(Constant.READ_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                var result = handler.result().body();

                if (result.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                {
                    long credentialId = result.getJsonObject(Constant.STATUS_RESULT).getLong(Constant.CREDENTIALS_ID);

                    eventBus.<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credentialId)).onComplete(credentialHandler ->
                    {

                        if (credentialHandler.succeeded())
                        {
                            var credResult = credentialHandler.result().body();

                            if (credResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                            {
                                JsonObject data = new JsonObject();

                                data.put(Constant.IP_ADDRESS, result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.IP_ADDRESS));

                                data.put(Constant.PORT_NUMBER, result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.PORT_NUMBER));

                                data.put(Constant.USERNAME, credResult.getJsonObject(Constant.STATUS_RESULT).getString(Constant.USERNAME));

                                data.put(Constant.PASSWORD, credResult.getJsonObject(Constant.STATUS_RESULT).getString(Constant.PASSWORD));

                                String pingStatus = ping(promise, result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.IP_ADDRESS)).getString(Constant.STATUS);

                                switch (pingStatus)
                                {
                                    case Constant.STATUS_SUCCESS ->
                                    {
                                        JsonObject discoveryResult = discover(data);
                                        if (discoveryResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                        {
                                            if (!discoveryResult.getJsonObject(Constant.STATUS_RESULT).isEmpty())
                                            {
                                                discoveryResult.remove(Constant.STATUS_ERROR);

                                                promise.complete(discoveryResult);
                                            }

                                            else
                                            {
                                                discoveryResult.put(Constant.STATUS, Constant.STATUS_FAIL);

                                                discoveryResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_UNAUTHORIZED);

                                                promise.fail(discoveryResult.encode());
                                            }
                                        }

                                        else
                                        {
                                            promise.fail(discoveryResult.encode());
                                        }
                                    }
                                    case Constant.STATUS_ERROR ->
                                    {
                                        JsonObject response = new JsonObject()

                                                .put(Constant.STATUS, Constant.STATUS_FAIL)

                                                .put(Constant.PROCESS_STATUS, Constant.PROCESS_NORMAL)

                                                .put(Constant.STATUS_MESSAGE, Constant.PING);

                                        promise.fail(response.encode());

                                    }
                                    case Constant.STATUS_FAIL ->
                                    {
                                        JsonObject response = new JsonObject()

                                                .put(Constant.STATUS, Constant.STATUS_FAIL)

                                                .put(Constant.PROCESS_STATUS, Constant.PROCESS_ABNORMAL)

                                                .put(Constant.STATUS_MESSAGE, Constant.PING_CHECK_TIMED_OUT);

                                        promise.fail(response.encode());

                                    }
                                }

                            }

                            else
                            {
                                JsonObject response = new JsonObject()

                                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                                        .put(Constant.FAIL_TYPE, Constant.CREDENTIALS)

                                        .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                                        .put(Constant.STATUS_MESSAGE, credResult.getString(Constant.STATUS_MESSAGE));

                                promise.fail(response.encode());
                            }
                        }

                        else
                        {
                            System.out.println(handler.cause().getMessage());

                        }

                    });
                }

                else
                {
                    JsonObject response = new JsonObject()

                            .put(Constant.STATUS, Constant.STATUS_FAIL)

                            .put(Constant.FAIL_TYPE, Constant.DISCOVERY)

                            .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                            .put(Constant.STATUS_MESSAGE, result.getString(Constant.STATUS_MESSAGE));

                    promise.fail(response.encode());

                }

            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }

        });
    }

    public JsonObject ping(Promise<Object> promise, String ipAddress)
    {

        JsonObject result;

        BuildProcess process = new BuildProcess();

        List<String> command = new ArrayList<>();

        command.add(Constant.FPING);

        command.add(Constant.COMMAND_COUNT);

        command.add(Constant.PACKET_COUNT);

        command.add(Constant.COMMAND_SUPPRESSED);

        command.add(ipAddress);

        result = process.build(command, Constant.PING_TIMEOUT);

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
                }

                else
                {
                    result = new JsonObject()

                            .put(Constant.STATUS, Constant.STATUS_ERROR)

                            .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS));

                }
            }
        }
        else
        {
            result = new JsonObject()

                    .put(Constant.STATUS, Constant.STATUS_FAIL)

                    .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS));
        }


        return result;

    }

    public JsonObject discover(JsonObject data)
    {
        data.put(Constant.SERVICE, Constant.DISCOVER);

        data.put(Constant.METRIC_GROUP, Constant.EMPTY_STRING);

        JsonObject result;

        BuildProcess process = new BuildProcess();

        List<String> command = new ArrayList<>();

        command.add(Constant.GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(data.encode());

        result = process.build(command, Constant.DISCOVERY_TIMEOUT);

        JsonObject discoverResult;

        if (result.getString(Constant.PROCESS_STATUS).equals(Constant.PROCESS_NORMAL))
        {
            var commandResult = new JsonObject(result.getString(Constant.STATUS_RESULT));

            discoverResult = new JsonObject()

                    .put(Constant.STATUS, result.getString(Constant.STATUS))

                    .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS))

                    .put(Constant.STATUS_CODE, result.getInteger(Constant.STATUS_CODE))

                    .put(Constant.STATUS_RESULT, commandResult.getJsonObject(Constant.STATUS_RESULT))

                    .put(Constant.STATUS_ERROR, commandResult.getString(Constant.STATUS_MESSAGE));

            return discoverResult;

        }

        else
        {
            discoverResult = new JsonObject()

                    .put(Constant.STATUS, result.getString(Constant.STATUS))

                    .put(Constant.PROCESS_STATUS, result.getString(Constant.PROCESS_STATUS))

                    .put(Constant.STATUS_MESSAGE, Constant.DISCOVERY_TIMED_OUT);

            return discoverResult;
        }

    }
}
