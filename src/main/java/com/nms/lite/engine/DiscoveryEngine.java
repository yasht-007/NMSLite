package com.nms.lite.engine;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.BuildProcess;
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.PingDevice;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DiscoveryEngine extends AbstractVerticle
{
    private final EventBus eventBus = Bootstrap.getEventBus();
    private final Logger logger = LoggerFactory.getLogger(DiscoveryEngine.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        try
        {
            Bootstrap.vertx.eventBus().<JsonObject>localConsumer(Constant.RUN_DISCOVERY).handler(message ->
            {

                long discoveryId = message.body().getLong(Constant.DISCOVERY_ID);

                handleDiscovery(discoveryId).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        message.reply(handler.result());

                    }

                    else
                    {
                        message.reply(new JsonObject(handler.cause().getMessage()));

                    }

                });

            });

            startPromise.complete();
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private Future<JsonObject> handleDiscovery(long discoveryId)
    {
        Promise<JsonObject> promise = Promise.promise();

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

                                new PingDevice().ping(result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.IP_ADDRESS), vertx).onComplete(response ->
                                {
                                    if (response.succeeded())
                                    {
                                        if (response.result())
                                        {
                                            discover(data).onSuccess(discoveryresult ->
                                            {
                                                if (discoveryresult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                                {
                                                    updateDiscoveryStatus(discoveryId).onSuccess(updationResult ->
                                                    {

                                                        if (updationResult)
                                                        {
                                                            promise.complete(discoveryresult);
                                                        }

                                                        else
                                                        {
                                                            promise.fail(discoveryresult.encode());
                                                        }

                                                    });
                                                }

                                                else
                                                {
                                                    promise.fail(discoveryresult.encode());
                                                }
                                            });

                                        }
                                        else
                                        {
                                            promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.PING).encode());
                                        }
                                    }
                                });

                            }

                            else
                            {
                                JsonObject response = new JsonObject()

                                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                                        .put(Constant.STATUS_MESSAGE, Constant.CREDENTIALS + credResult.getString(Constant.STATUS_ERROR));

                                promise.fail(response.encode());
                            }
                        }

                        else
                        {
                            logger.error(handler.cause().getMessage());
                        }

                    });
                }

                else
                {
                    promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, result.getString(Constant.STATUS_ERROR)).encode());
                }

            }

            else
            {
                logger.error(handler.cause().getMessage());
            }

        });

        return promise.future();
    }

    private Future<Boolean> updateDiscoveryStatus(long discoveryId)
    {
        Promise<Boolean> promise = Promise.promise();

        eventBus.<JsonObject>request(Constant.UPDATE_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId).put(Constant.DISCOVERED, true)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                if (handler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                {
                    promise.complete(true);
                }

                else
                {
                    promise.complete(false);
                }
            }

            else
            {
                logger.error(handler.cause().getMessage());
            }

        });

        return promise.future();

    }

    public Future<JsonObject> discover(JsonObject data)
    {
        Promise<JsonObject> promise = Promise.promise();

        data.put(Constant.SERVICE, Constant.DISCOVER);

        data.put(Constant.METRIC_GROUP, Constant.EMPTY_STRING);

        List<String> command = new ArrayList<>();

        command.add(Constant.GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(Base64.getEncoder().encodeToString(data.encode().getBytes()));

        BuildProcess.build(command, Constant.DISCOVERY_TIMEOUT, true, vertx).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                if (handler.result().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS) && handler.result().getString(Constant.STATUS_RESULT).contains(String.valueOf(Constant.STATUS_CODE_OK)))
                {
                    var commandResult = new JsonObject(handler.result().getString(Constant.STATUS_RESULT));

                    promise.complete(new JsonObject().put(Constant.STATUS, handler.result().getString(Constant.STATUS)).put(Constant.STATUS_RESULT, commandResult.getJsonObject(Constant.STATUS_RESULT)));

                }

                else
                {
                    promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DISCOVERY_FAILED));
                }
            }
        });

        return promise.future();

    }
}
