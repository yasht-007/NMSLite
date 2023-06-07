package com.nms.lite.service;

import com.nms.lite.database.CredentialDb;
import com.nms.lite.database.DiscoveryDb;
import com.nms.lite.database.ProvisionDb;
import com.nms.lite.utility.Global;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.nms.lite.model.Credentials;
import com.nms.lite.model.Discovery;
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.KeyGen;

import java.util.List;

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

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_CREDENTIALS).handler(message ->
            {

                readAll(Constant.CREDENTIALS, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_DISCOVERY).handler(message ->
            {

                readAll(Constant.DISCOVERY, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_PROVISION).handler(message ->
            {

                readAll(Constant.PROVISION, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.CREATE_DISCOVERY).handler(message ->
            {

                create(Constant.DISCOVERY, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.READ_DISCOVERY).handler(message ->
            {

                read(Constant.DISCOVERY, message);

            });

            eventBus.<JsonObject>localConsumer(Constant.UPDATE_DISCOVERY).handler(message ->
            {
                update(Constant.DISCOVERY, message);
            });

            eventBus.<JsonObject>localConsumer(Constant.UPDATE_CREDENTIALS).handler(message ->
            {
                update(Constant.CREDENTIALS, message);
            });

            eventBus.<JsonObject>localConsumer(Constant.CREATE_PROVISION).handler(this::runProvision);

            eventBus.<JsonObject>localConsumer(Constant.READ_PROVISION).handler(message ->
            {
                read(Constant.PROVISION, message);
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

                case Constant.PROVISION ->
                {
                    ProvisionDb provisionDb = ProvisionDb.getInstance();

                    long provisionId = data.getLong(Constant.PROVISION_ID);

                    List<String> provision = provisionDb.read(String.valueOf(provisionId));

                    if (provision != null)
                    {
                        JsonObject provisionData = new JsonObject();

                        provisionData.put(Constant.STATUS_RESULT, new JsonObject(provision.get(0)));

                        provisionData.put(Constant.CREDENTIALS_ID, provision.get(1));

                        promise.complete(provisionData);
                    }

                    else
                    {
                        promise.fail(Constant.PROVISION + Constant.DATA_DOES_NOT_EXIST);
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

                else if (handler.result() instanceof JsonObject)
                {
                    JsonObject result = (JsonObject) handler.result();

                    JsonObject provisionData = new JsonObject();

                    provisionData.put(Constant.PROVISION, result.getJsonObject(Constant.PROVISION_ID));

                    provisionData.put(Constant.CREDENTIALS_ID, Constant.CREDENTIALS_ID);

                    result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                    result.put(Constant.STATUS_MESSAGE, Constant.READ_SUCCESS);

                    result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                    message.reply(result);

                }

            }
            else
            {
                JsonObject result = new JsonObject();

                result.put(Constant.STATUS, Constant.STATUS_FAIL);

                result.put(Constant.STATUS_MESSAGE, handler.cause().getMessage());

                result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                message.reply(result);
            }
        });

    }

    private void update(String type, Message<JsonObject> message)
    {
        var data = message.body();

        vertx.executeBlocking(promise ->
        {
            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    CredentialDb credentialsDb = CredentialDb.getInstance();

                    long credentialsId = data.getLong(Constant.CREDENTIALS_ID);

                    if (credentialsDb.read(credentialsId) != null)
                    {
                        Credentials credentials = credentialsDb.read(credentialsId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(Constant.USERNAME))
                            {
                                credentials.setUsername(data.getString(Constant.USERNAME));
                            }

                            if (change.equalsIgnoreCase(Constant.PASSWORD))
                            {
                                credentials.setPassword(data.getString(Constant.PASSWORD));
                            }
                        });

                        credentialsDb.update(credentials);

                        promise.complete(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.STATUS_SUCCESS));


                    }
                    else
                    {
                        promise.fail(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.STATUS_FAIL).encode());
                    }
                }

                case Constant.DISCOVERY ->
                {
                    DiscoveryDb discoveryDb = DiscoveryDb.getInstance();

                    long discoveryId;

                    if (data.containsKey(Constant.DISCOVERY_NAME))
                    {
                        discoveryId = KeyGen.getUniqueKeyForName(data.getString(Constant.DISCOVERY_NAME));
                    }

                    else
                    {
                        discoveryId = data.getLong(Constant.DISCOVERY_ID);
                    }

                    if (discoveryDb.read(discoveryId) != null)
                    {
                        Discovery discovery = discoveryDb.read(discoveryId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(Constant.IP_ADDRESS))
                            {
                                discovery.setIp(data.getString(Constant.IP_ADDRESS));
                            }

                            if (change.equalsIgnoreCase(Constant.PORT_NUMBER))
                            {
                                discovery.setPort(data.getInteger(Constant.PORT_NUMBER));
                            }

                            if (change.equalsIgnoreCase(Constant.DISCOVERED))
                            {
                                discovery.setDiscovered(data.getBoolean(Constant.DISCOVERED));
                            }

                            if (change.equalsIgnoreCase(Constant.CREDENTIALS_ID))
                            {
                                discovery.setCredentialProfileId(data.getLong(Constant.CREDENTIALS_ID));
                            }
                        });

                        discoveryDb.update(discovery);

                        promise.complete(new JsonObject().put(Constant.TYPE, Constant.DISCOVERY).put(Constant.STATUS, Constant.STATUS_SUCCESS));


                    }
                    else
                    {
                        promise.fail(new JsonObject().put(Constant.TYPE, Constant.DISCOVERY).put(Constant.STATUS, Constant.STATUS_FAIL).encode());
                    }
                }
            }

        }, handler ->
        {
            if (handler.succeeded())
            {
                JsonObject successResult = (JsonObject) handler.result();

                successResult.put(Constant.STATUS_MESSAGE, Constant.UPDATE_SUCCESS);

                message.reply(successResult);

            }

            else
            {
                JsonObject errorResult = new JsonObject(handler.cause().getMessage());

                errorResult.put(Constant.STATUS_MESSAGE, Constant.DATA_DOES_NOT_EXIST);

                message.reply(errorResult);

            }

        });

    }

    private void runProvision(Message<JsonObject> message)
    {
        var data = message.body();

        vertx.executeBlocking(promise ->
        {
            JsonObject result = new JsonObject();

            long discoveryId = data.getLong(Constant.DISCOVERY_ID);

            ProvisionDb provisionDb = ProvisionDb.getInstance();

            DiscoveryDb discoveryDb = DiscoveryDb.getInstance();

            Discovery discovery = discoveryDb.read(discoveryId);

            if (discovery != null)
            {
                // means Did hai

                if (discovery.getDiscovered())
                {
                    // means discovered device hai

                    if (!provisionDb.containsIp(discovery.getIp()))
                    {
                        CredentialDb credentialDb = CredentialDb.getInstance();

                        Credentials credentials = credentialDb.read(discovery.getCredentialProfileId());

                        if (credentials != null)
                        {
                            long provisionId = Global.provisionCounter.incrementAndGet();

                            JsonObject provisionData = new JsonObject();

                            provisionData.put(Constant.PROVISION_ID, provisionId);

                            provisionData.put(Constant.IP_ADDRESS, discovery.getIp());

                            provisionData.put(Constant.PORT_NUMBER, discovery.getPort());

                            provisionDb.create(discovery.getIp(), String.valueOf(provisionId), String.valueOf(discovery.getCredentialProfileId()), provisionData.encode());

                            result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                            result.put(Constant.PROVISION_ID, provisionId);

                            result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                            result.put(Constant.STATUS_MESSAGE, Constant.PROVISION_RUN_SUCCESS);

                            credentials.incrementCounter();

                            credentialDb.update(credentials);

                            promise.complete(result);
                        }

                        else
                        {
                            // means credential nai hai

                            promise.fail(Constant.CREDENTIALS_NOT_FOUND);

                        }
                    }

                    else
                    {
                        // means already hai provision list me

                        promise.fail(Constant.ALREADY_IN_PROVISION_LIST);

                    }
                }

                else
                {
                    // means discovered device nai hai

                    promise.fail(Constant.DEVICE_NOT_DISCOVERED);

                }

            }

            else
            {
                // means Did nai hai

                promise.fail(Constant.DISCOVERY_NOT_FOUND);

            }

        }, handler ->
        {
            if (handler.succeeded())
            {
                JsonObject successResult = (JsonObject) handler.result();

                message.reply(successResult);
            }

            else
            {
                JsonObject failedresult = new JsonObject();

                switch (handler.cause().getMessage())
                {
                    case Constant.DISCOVERY_NOT_FOUND ->
                    {

                        failedresult.put(Constant.STATUS, Constant.STATUS_FAIL);

                        failedresult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                        failedresult.put(Constant.STATUS_MESSAGE, Constant.DISCOVERY + Constant.DATA_DOES_NOT_EXIST);

                    }

                    case Constant.CREDENTIALS_NOT_FOUND ->
                    {
                        failedresult.put(Constant.STATUS, Constant.STATUS_FAIL);

                        failedresult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                        failedresult.put(Constant.STATUS_MESSAGE, Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST);
                    }

                    case Constant.DEVICE_NOT_DISCOVERED ->
                    {
                        failedresult.put(Constant.STATUS, Constant.STATUS_ERROR);

                        failedresult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                        failedresult.put(Constant.STATUS_MESSAGE, Constant.DEVICE_NOT_DISCOVERED_MESSAGE);

                    }

                    case Constant.ALREADY_IN_PROVISION_LIST ->
                    {
                        failedresult.put(Constant.STATUS, Constant.STATUS_ERROR);

                        failedresult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                        failedresult.put(Constant.STATUS_MESSAGE, Constant.PROVISION + Constant.DATA_ALREADY_EXISTS);

                    }
                }

                message.reply(failedresult);
            }
        });
    }

    private void readAll(String type, Message<JsonObject> message)
    {
        vertx.executeBlocking(promise ->
        {
            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    CredentialDb credentialDb = CredentialDb.getInstance();

                    if (credentialDb.readAll().size() > 0)
                    {
                        JsonArray result = new JsonArray(credentialDb.readAll());

                        promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, result).put(Constant.TYPE, Constant.CREDENTIALS));

                    }

                    else
                    {
                        promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.CREDENTIALS).encode());
                    }
                }

                case Constant.DISCOVERY ->
                {
                    DiscoveryDb discoveryDb = DiscoveryDb.getInstance();

                    if (discoveryDb.readAll().size() > 0)
                    {
                        JsonArray result = new JsonArray(discoveryDb.readAll());

                        promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, result).put(Constant.TYPE, Constant.DISCOVERY));

                    }

                    else
                    {
                        promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.DISCOVERY).encode());
                    }
                }

                case Constant.PROVISION ->
                {
                    ProvisionDb provisionDb = ProvisionDb.getInstance();

                    CredentialDb credentialDb = CredentialDb.getInstance();

                    JsonArray resultData = new JsonArray();

                    if (provisionDb.readAll().size() > 0)
                    {
                        provisionDb.readAll().forEach(key ->
                        {
                            List<String> list = provisionDb.read(key);

                            JsonObject data = new JsonObject(list.get(0));

                            Credentials credentials = credentialDb.read(Long.parseLong(list.get(1)));

                            data.put(Constant.USERNAME, credentials.getUsername());

                            data.put(Constant.PASSWORD, credentials.getPassword());

                            resultData.add(data);
                        });

                        promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, resultData).put(Constant.TYPE, Constant.PROVISION));
                    }

                    else
                    {
                        promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.PROVISION).encode());
                    }

                }
            }


        }, handler ->
        {
            if (handler.succeeded())
            {
                JsonObject successResult = (JsonObject) handler.result();

                successResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                message.reply(successResult.encode());

            }

            else
            {
                JsonObject failResult = new JsonObject(handler.cause().getMessage());

                failResult.put(Constant.STATUS_MESSAGE, Constant.DATA_DOES_NOT_EXIST);

                failResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                message.reply(failResult.encode());

            }

        });
    }
}