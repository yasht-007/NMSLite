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

                                boolean pingStatus = ping(promise, result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.IP_ADDRESS));

                                if (pingStatus)
                                {
                                   JsonObject jb =  discover(data);
                                }

                                else
                                {
                                    JsonObject response = new JsonObject()

                                            .put(Constant.STATUS, Constant.STATUS_FAIL)

                                            .put(Constant.FAIL_TYPE, Constant.PING)

                                            .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                                            .put(Constant.STATUS_MESSAGE, Constant.EMPTY_STRING);

                                    promise.fail(response.encode());

                                }

                            }

                            else
                            {
                                JsonObject response = new JsonObject()

                                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                                        .put(Constant.FAIL_TYPE, Constant.CREDENTIALS)

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

    public boolean ping(Promise<Object> promise, String ipAddress)
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

        if (result.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
        {
            var splitWithLine = result.getString(Constant.STATUS_ERROR).split(Constant.NEW_LINE);

            for (String s : splitWithLine)
            {
                String[] filteredResult = s.split(Constant.COLON)[1].split(Constant.EQUAL_TO)[1].split(Constant.FORWARD_SLASH);

                return filteredResult[0].trim().equals(filteredResult[1].trim()) && filteredResult[2].substring(0, filteredResult[2].indexOf(Constant.PERCENTAGE)).equals(Constant.NUMERIC_ZERO_IN_STRING);
            }
        }

        else
        {
            return false;
        }

        return false;
    }

    public JsonObject discover(JsonObject data)
    {
        data.put(Constant.SERVICE,Constant.DISCOVER);

        data.put(Constant.METRIC_GROUP,Constant.EMPTY_STRING);

        JsonObject result;

        BuildProcess process = new BuildProcess();

        List<String> command = new ArrayList<>();

        command.add(Constant.GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(data.encode());

        result = process.build(command, Constant.DISCOVERY_TIMEOUT);

        System.out.println(result);

        return null;

    }
}
