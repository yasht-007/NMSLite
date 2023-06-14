package com.nms.lite.engine;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.BuildProcess;
import static com.nms.lite.utility.Constant.*;
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
    private final EventBus eventBus = Bootstrap.vertx.eventBus();
    private final Logger logger = LoggerFactory.getLogger(DiscoveryEngine.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        try
        {
            Bootstrap.vertx.eventBus().<JsonObject>localConsumer(RUN_DISCOVERY).handler(message ->
            {

                long discoveryId = message.body().getLong(DISCOVERY_ID);

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

        eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(DISCOVERY_ID, discoveryId).put(OPERATION, READ).put(TYPE, DISCOVERY)).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                var result = handler.result().body();

                if (result.getString(STATUS).equals(STATUS_SUCCESS))
                {
                    long credentialId = result.getJsonObject(STATUS_RESULT).getLong(CREDENTIALS_ID);

                    eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(CREDENTIALS_ID, credentialId).put(OPERATION, READ).put(TYPE, CREDENTIALS)).onComplete(credentialHandler ->
                    {

                        if (credentialHandler.succeeded())
                        {
                            var credResult = credentialHandler.result().body();

                            if (credResult.getString(STATUS).equals(STATUS_SUCCESS))
                            {
                                JsonObject data = new JsonObject();

                                data.put(IP_ADDRESS, result.getJsonObject(STATUS_RESULT).getString(IP_ADDRESS));

                                data.put(PORT_NUMBER, result.getJsonObject(STATUS_RESULT).getString(PORT_NUMBER));

                                data.put(USERNAME, credResult.getJsonObject(STATUS_RESULT).getString(USERNAME));

                                data.put(PASSWORD, credResult.getJsonObject(STATUS_RESULT).getString(PASSWORD));

                                new PingDevice().ping(result.getJsonObject(STATUS_RESULT).getString(IP_ADDRESS), vertx).onComplete(response ->
                                {
                                    if (response.succeeded())
                                    {
                                        if (response.result())
                                        {
                                            discover(data).onSuccess(discoveryresult ->
                                            {
                                                if (discoveryresult.getString(STATUS).equals(STATUS_SUCCESS))
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
                                            promise.fail(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, PING).encode());
                                        }
                                    }
                                });

                            }
                            else
                            {
                                JsonObject response = new JsonObject()

                                        .put(STATUS, STATUS_FAIL)

                                        .put(STATUS_MESSAGE, CREDENTIALS + credResult.getString(STATUS_ERROR));

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
                    promise.fail(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, result.getString(STATUS_ERROR)).encode());
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

        eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(DISCOVERY_ID, discoveryId).put(DISCOVERED, true).put(OPERATION, UPDATE).put(TYPE, DISCOVERY)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                if (handler.result().body().getString(STATUS).equals(STATUS_SUCCESS))
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

        data.put(SERVICE, DISCOVER);

        data.put(METRIC_GROUP, EMPTY_STRING);

        List<String> command = new ArrayList<>();

        command.add(GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(Base64.getEncoder().encodeToString(data.encode().getBytes()));

        BuildProcess.build(command, DISCOVERY_TIMEOUT, true).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                if (handler.result().getString(STATUS).equals(STATUS_SUCCESS) && handler.result().getString(STATUS_RESULT).contains(String.valueOf(STATUS_CODE_OK)))
                {
                    var commandResult = new JsonObject(handler.result().getString(STATUS_RESULT));

                    promise.complete(new JsonObject().put(STATUS, handler.result().getString(STATUS)).put(STATUS_RESULT, commandResult.getJsonObject(STATUS_RESULT)));

                }
                else
                {
                    promise.complete(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DISCOVERY_FAILED));
                }
            }
        });

        return promise.future();

    }
}
