package com.nms.lite.service;

import com.nms.lite.database.CredentialDb;
import com.nms.lite.database.DiscoveryDb;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import com.nms.lite.model.Credentials;
import com.nms.lite.model.Discovery;
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.KeyGen;

public class DatabaseService extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise)
    {
        try
        {

            EventBus eventBus = vertx.eventBus();

            eventBus.<JsonObject>localConsumer(Constant.CREATE_CREDENTIALS).handler(message ->
            {

                create(Constant.CREDENTIALS, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.READ_CREDENTIALS).handler(message ->
            {

                read(Constant.CREDENTIALS, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.CREATE_DISCOVERY).handler(message ->
            {

                create(Constant.DISCOVERY, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.READ_DISCOVERY).handler(message ->
            {

                read(Constant.DISCOVERY, message);

            });

            promise.complete();

        }

        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void create(String type, Message<JsonObject> message)
    {

        var data = message.body();

        vertx.executeBlocking(promise ->
        {

            switch (type)
            {

                case Constant.CREDENTIALS ->
                {

                    CredentialDb credentialDb = CredentialDb.getInstance();

                    long credId = KeyGen.getUniqueKeyForName(data.getString(Constant.CREDENTIALS_NAME));

                    if (credentialDb.read(credId) != null)
                    {
                        promise.fail(Constant.CREDENTIALS + Constant.DATA_ALREADY_EXISTS);
                    }
                    else
                    {
                        Credentials credentials = new Credentials(credId, data.getString(Constant.CREDENTIALS_NAME), data.getString(Constant.USERNAME), data.getString(Constant.PASSWORD));

                        credentialDb.create(credentials);

                        promise.complete(Constant.CREDENTIALS + Constant.CREATE_SUCCESS + Constant.COLON + credId);
                    }
                }

                case Constant.DISCOVERY ->
                {

                    DiscoveryDb discoveryDb = DiscoveryDb.getInstance();

                    long discoveryId = KeyGen.getUniqueKeyForName(data.getString(Constant.DISCOVERY_NAME));

                    if (discoveryDb.read(discoveryId) != null)
                    {
                        promise.fail(Constant.DISCOVERY + Constant.DATA_ALREADY_EXISTS);
                    }
                    else
                    {

                        Discovery discovery = new Discovery(discoveryId, data.getString(Constant.DISCOVERY_NAME), data.getString(Constant.IP_ADDRESS), data.getInteger(Constant.PORT_NUMBER), data.getLong(Constant.CREDENTIALS_ID));

                        discoveryDb.create(discovery);

                        promise.complete(Constant.DISCOVERY + Constant.CREATE_SUCCESS + Constant.COLON + discoveryId);
                    }
                }

            }

        }, handler ->
        {
            if (handler.succeeded())
            {

                String[] successResult = handler.result().toString().split(Constant.COLON);

                JsonObject result = new JsonObject();

                result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                result.put(Constant.STATUS_MESSAGE, Constant.CREATE_SUCCESS);

                result.put(Constant.STATUS_RESULT, Long.parseLong(successResult[1]));

                message.reply(result);

            }
            else
            {
                JsonObject result = new JsonObject();

                result.put(Constant.STATUS, Constant.STATUS_FAIL);

                result.put(Constant.STATUS_MESSAGE, handler.cause().getMessage());

                message.reply(result);

            }
        });
    }

    private void read(String type, Message<JsonObject> message)
    {
        var data = message.body();

        vertx.executeBlocking(promise ->
        {
            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    CredentialDb credentialDb = CredentialDb.getInstance();

                    long credId = data.getLong(Constant.CREDENTIALS_ID);

                    if (credentialDb.read(credId) != null)
                    {
                        Credentials credentials = credentialDb.read(credId);

                        promise.complete(credentials);
                    }

                    else
                    {
                        promise.fail(Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST);
                    }
                }

                case Constant.DISCOVERY ->
                {
                    DiscoveryDb discoveryDb = DiscoveryDb.getInstance();

                    long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                    if (discoveryDb.read(discoveryId) != null)
                    {
                        Discovery discovery = discoveryDb.read(discoveryId);

                        promise.complete(discovery);
                    }

                    else
                    {
                        promise.fail(Constant.DISCOVERY + Constant.DATA_DOES_NOT_EXIST);
                    }
                }
            }
        }, handler ->
        {
            if (handler.succeeded())
            {
                if (handler.result() instanceof Credentials credentials)
                {

                    JsonObject result = new JsonObject();

                    result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                    result.put(Constant.STATUS_MESSAGE, Constant.READ_SUCCESS);

                    result.put(Constant.STATUS_RESULT, credentials.toJsonObject());

                    message.reply(result);
                }

                else if (handler.result() instanceof Discovery discovery)
                {

                    JsonObject result = new JsonObject();

                    result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                    result.put(Constant.STATUS_MESSAGE, Constant.READ_SUCCESS);

                    result.put(Constant.STATUS_RESULT, discovery.toJsonObject());

                    message.reply(result);
                }

            }

            else
            {
                JsonObject result = new JsonObject();

                result.put(Constant.STATUS, Constant.STATUS_FAIL);

                result.put(Constant.STATUS_MESSAGE, handler.cause().getMessage());

                message.reply(result);
            }
        });

    }
}