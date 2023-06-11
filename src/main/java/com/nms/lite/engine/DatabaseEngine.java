package com.nms.lite.engine;

import com.nms.lite.database.CredentialStore;
import com.nms.lite.database.DiscoveryStore;
import com.nms.lite.database.ProvisionStore;
import com.nms.lite.utility.Global;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.nms.lite.model.Credentials;
import com.nms.lite.model.Discovery;
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseEngine extends AbstractVerticle
{
    private final Logger logger = LoggerFactory.getLogger(DatabaseEngine.class);

    @Override
    public void start(Promise<Void> promise)
    {
        try
        {
            EventBus eventBus = vertx.eventBus();

            eventBus.<JsonObject>localConsumer(Constant.CREATE_CREDENTIALS).handler(message -> create(Constant.CREDENTIALS, message));

            eventBus.<JsonObject>localConsumer(Constant.READ_CREDENTIALS).handler(message -> read(Constant.CREDENTIALS, message));

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_CREDENTIALS).handler(message -> readAll(Constant.CREDENTIALS, message));

            eventBus.<JsonObject>localConsumer(Constant.DELETE_CREDENTIALS).handler(message -> delete(Constant.CREDENTIALS, message));

            eventBus.<JsonObject>localConsumer(Constant.CREATE_DISCOVERY).handler(message -> create(Constant.DISCOVERY, message));

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_DISCOVERY).handler(message -> readAll(Constant.DISCOVERY, message));

            eventBus.<JsonObject>localConsumer(Constant.READ_DISCOVERY).handler(message -> read(Constant.DISCOVERY, message));

            eventBus.<JsonObject>localConsumer(Constant.UPDATE_DISCOVERY).handler(message -> update(Constant.DISCOVERY, message));

            eventBus.<JsonObject>localConsumer(Constant.DELETE_DISCOVERY).handler(message -> delete(Constant.DISCOVERY, message));

            eventBus.<JsonObject>localConsumer(Constant.READ_ALL_PROVISION).handler(message -> readAll(Constant.PROVISION, message));

            eventBus.<JsonObject>localConsumer(Constant.CREATE_PROVISION).handler(this::runProvision);

            eventBus.<JsonObject>localConsumer(Constant.READ_PROVISION).handler(message -> read(Constant.PROVISION, message));

            eventBus.<JsonObject>localConsumer(Constant.UPDATE_CREDENTIALS).handler(message -> update(Constant.CREDENTIALS, message));

            eventBus.<JsonObject>localConsumer(Constant.DELETE_PROVISION).handler(message -> delete(Constant.PROVISION, message));

            promise.complete();

        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void create(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            vertx.executeBlocking(promise ->
            {

                switch (type)
                {

                    case Constant.CREDENTIALS ->
                    {
                        CredentialStore credentialStore = CredentialStore.getInstance();

                        long credId = KeyGen.getUniqueKeyForName(data.getString(Constant.CREDENTIALS_NAME));

                        if (credentialStore.read(credId) != null)
                        {
                            promise.fail(Constant.CREDENTIALS + Constant.DATA_ALREADY_EXISTS);
                        }
                        else
                        {
                            Credentials credentials = new Credentials(credId, data.getString(Constant.CREDENTIALS_NAME), data.getString(Constant.USERNAME), data.getString(Constant.PASSWORD));

                            credentialStore.create(credentials);

                            promise.complete(Constant.CREDENTIALS + Constant.CREATE_SUCCESS + Constant.COLON + credId);
                        }
                    }

                    case Constant.DISCOVERY ->
                    {

                        DiscoveryStore discoveryStore = DiscoveryStore.getInstance();

                        long discoveryId = KeyGen.getUniqueKeyForName(data.getString(Constant.DISCOVERY_NAME));

                        if (discoveryStore.read(discoveryId) != null)
                        {
                            promise.fail(Constant.DISCOVERY + Constant.DATA_ALREADY_EXISTS);
                        }
                        else
                        {

                            Discovery discovery = new Discovery(discoveryId, data.getString(Constant.DISCOVERY_NAME), data.getString(Constant.IP_ADDRESS), data.getInteger(Constant.PORT_NUMBER), data.getLong(Constant.CREDENTIALS_ID));

                            discoveryStore.create(discovery);

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

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void read(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            vertx.executeBlocking(promise ->
            {
                switch (type)
                {
                    case Constant.CREDENTIALS ->
                    {
                        CredentialStore credentialStore = CredentialStore.getInstance();

                        long credId = data.getLong(Constant.CREDENTIALS_ID);

                        if (credentialStore.read(credId) != null)
                        {
                            Credentials credentials = credentialStore.read(credId);

                            promise.complete(credentials);
                        }
                        else
                        {
                            promise.fail(Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST);
                        }
                    }

                    case Constant.DISCOVERY ->
                    {
                        DiscoveryStore discoveryStore = DiscoveryStore.getInstance();

                        long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                        if (discoveryStore.read(discoveryId) != null)
                        {
                            Discovery discovery = discoveryStore.read(discoveryId);

                            promise.complete(discovery);
                        }
                        else
                        {
                            promise.fail(Constant.DISCOVERY + Constant.DATA_DOES_NOT_EXIST);
                        }
                    }

                    case Constant.PROVISION ->
                    {
                        ProvisionStore provisionStore = ProvisionStore.getInstance();

                        long provisionId = data.getLong(Constant.PROVISION_ID);

                        List<String> provision = provisionStore.read(String.valueOf(provisionId));

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

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void update(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            vertx.executeBlocking(promise ->
            {
                switch (type)
                {
                    case Constant.CREDENTIALS ->
                    {
                        CredentialStore credentialsDb = CredentialStore.getInstance();

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

                                if (change.equalsIgnoreCase(Constant.CREDENTIAL_COUNTER))
                                {
                                    if (data.getBoolean(Constant.CREDENTIAL_COUNTER))
                                    {
                                        credentials.incrementCounter();
                                    }

                                    else
                                    {
                                        credentials.decrementCounter();
                                    }
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
                        DiscoveryStore discoveryStore = DiscoveryStore.getInstance();

                        long discoveryId;

                        if (data.containsKey(Constant.DISCOVERY_NAME))
                        {
                            discoveryId = KeyGen.getUniqueKeyForName(data.getString(Constant.DISCOVERY_NAME));
                        }

                        else
                        {
                            discoveryId = data.getLong(Constant.DISCOVERY_ID);
                        }

                        if (discoveryStore.read(discoveryId) != null)
                        {
                            Discovery discovery = discoveryStore.read(discoveryId);

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
                                    discovery.setCredentialsId(data.getLong(Constant.CREDENTIALS_ID));
                                }
                            });

                            discoveryStore.update(discovery);

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

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void runProvision(Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            vertx.executeBlocking(promise ->
            {
                JsonObject result = new JsonObject();

                long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                ProvisionStore provisionStore = ProvisionStore.getInstance();

                vertx.eventBus().<JsonObject>request(Constant.READ_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(readHandler ->
                {

                    if (readHandler.succeeded())
                    {
                        if (readHandler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            Discovery discovery = readHandler.result().body().getJsonObject(Constant.STATUS_RESULT).mapTo(Discovery.class);

                            if (discovery.getDiscovered())
                            {
                                if (!provisionStore.containsIp(discovery.getIp()))
                                {
                                    vertx.eventBus().<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, discovery.getCredentialsId())).onComplete(credentialHandler ->
                                    {
                                        if (credentialHandler.succeeded())
                                        {
                                            if (credentialHandler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                            {
                                                Credentials credentials = credentialHandler.result().body().getJsonObject(Constant.STATUS_RESULT).mapTo(Credentials.class);

                                                long provisionId = Global.provisionCounter.incrementAndGet();

                                                JsonObject provisionData = new JsonObject();

                                                provisionData.put(Constant.PROVISION_ID, provisionId);

                                                provisionData.put(Constant.IP_ADDRESS, discovery.getIp());

                                                provisionData.put(Constant.PORT_NUMBER, discovery.getPort());

                                                provisionStore.create(discovery.getIp(), String.valueOf(provisionId), String.valueOf(discovery.getCredentialsId()), provisionData.encode());

                                                result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                                                result.put(Constant.PROVISION_ID, provisionId);

                                                result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                                                result.put(Constant.STATUS_MESSAGE, Constant.PROVISION_RUN_SUCCESS);

                                                String path = Constant.OUTPUT_PATH + Constant.FORWARD_SLASH + discovery.getIp();

                                                vertx.fileSystem().exists(path).onComplete(handler ->
                                                {
                                                    if (handler.succeeded())
                                                    {
                                                        if (handler.result().equals(false))
                                                        {
                                                            vertx.fileSystem().mkdir(path).onComplete(directoryHandler ->
                                                            {

                                                                if (directoryHandler.succeeded())
                                                                {
                                                                    logger.info(Constant.DIRECTORY_CREATION_SUCCESS);
                                                                }

                                                                else
                                                                {
                                                                    logger.error(directoryHandler.cause().getMessage());
                                                                }

                                                            });
                                                        }
                                                    }

                                                    else
                                                    {
                                                        promise.fail(handler.cause().getMessage());
                                                    }
                                                });

                                                vertx.eventBus().<JsonObject>request(Constant.UPDATE_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credentials.getId()).put(Constant.CREDENTIAL_COUNTER, true)).onComplete(updationHandler ->
                                                {

                                                    if (updationHandler.succeeded())
                                                    {
                                                        if (updationHandler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                                        {
                                                            promise.complete(result);

                                                        }

                                                        else
                                                        {
                                                            promise.fail(Constant.COUNTER_UPDATION_FAILED);
                                                        }
                                                    }

                                                    else
                                                    {
                                                        logger.error(updationHandler.cause().getMessage());
                                                    }
                                                });

                                            }

                                            else
                                            {
                                                promise.fail(Constant.CREDENTIALS_NOT_FOUND);
                                            }
                                        }

                                        else
                                        {
                                            logger.error(credentialHandler.cause().getMessage());
                                        }
                                    });

                                }

                                else
                                {
                                    promise.fail(Constant.ALREADY_IN_PROVISION_LIST);
                                }
                            }

                            else
                            {
                                promise.fail(Constant.DEVICE_NOT_DISCOVERED);
                            }
                        }

                        else
                        {
                            promise.fail(Constant.DISCOVERY_NOT_FOUND);
                        }
                    }

                    else
                    {
                        logger.error(readHandler.cause().getMessage());
                    }

                });

            }, handler ->
            {
                if (handler.succeeded())
                {
                    JsonObject successResult = (JsonObject) handler.result();

                    message.reply(successResult);
                }

                else
                {
                    JsonObject failedResult = new JsonObject();

                    switch (handler.cause().getMessage())
                    {
                        case Constant.DISCOVERY_NOT_FOUND ->
                        {

                            failedResult.put(Constant.STATUS, Constant.STATUS_FAIL);

                            failedResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                            failedResult.put(Constant.STATUS_MESSAGE, Constant.DISCOVERY + Constant.DATA_DOES_NOT_EXIST);

                        }

                        case Constant.CREDENTIALS_NOT_FOUND ->
                        {
                            failedResult.put(Constant.STATUS, Constant.STATUS_FAIL);

                            failedResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                            failedResult.put(Constant.STATUS_MESSAGE, Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST);
                        }

                        case Constant.DEVICE_NOT_DISCOVERED ->
                        {
                            failedResult.put(Constant.STATUS, Constant.STATUS_ERROR);

                            failedResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                            failedResult.put(Constant.STATUS_MESSAGE, Constant.DEVICE_NOT_DISCOVERED_MESSAGE);

                        }

                        case Constant.ALREADY_IN_PROVISION_LIST ->
                        {
                            failedResult.put(Constant.STATUS, Constant.STATUS_ERROR);

                            failedResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                            failedResult.put(Constant.STATUS_MESSAGE, Constant.PROVISION + Constant.DATA_ALREADY_EXISTS);

                        }

                        case Constant.COUNTER_UPDATION_FAILED ->
                        {
                            failedResult.put(Constant.STATUS, Constant.STATUS_ERROR);

                            failedResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                            failedResult.put(Constant.STATUS_MESSAGE, Constant.CREDENTIALS + Constant.COUNTER_UPDATION_FAILED);
                        }
                    }

                    message.reply(failedResult);
                }
            });
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void readAll(String type, Message<JsonObject> message)
    {
        try
        {
            vertx.executeBlocking(promise ->
            {
                switch (type)
                {
                    case Constant.CREDENTIALS ->
                    {
                        CredentialStore credentialStore = CredentialStore.getInstance();

                        List<Credentials> result = credentialStore.readAll();

                        if (result.size() > 0)
                        {
                            promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, result).put(Constant.TYPE, Constant.CREDENTIALS));
                        }

                        else
                        {
                            promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.CREDENTIALS).encode());
                        }
                    }

                    case Constant.DISCOVERY ->
                    {
                        DiscoveryStore discoveryStore = DiscoveryStore.getInstance();

                        if (discoveryStore.readAll().size() > 0)
                        {
                            JsonArray result = new JsonArray(discoveryStore.readAll());

                            promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, result).put(Constant.TYPE, Constant.DISCOVERY));

                        }

                        else
                        {
                            promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.DISCOVERY).encode());
                        }
                    }

                    case Constant.PROVISION ->
                    {
                        ProvisionStore provisionStore = ProvisionStore.getInstance();

                        JsonArray resultData = new JsonArray();

                        if (provisionStore.readAll().size() > 0)
                        {
                            provisionStore.readAll().forEach(key ->
                            {
                                List<String> list = provisionStore.read(key);

                                JsonObject data = new JsonObject(list.get(0));

                                vertx.eventBus().<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, Long.parseLong(list.get(1)))).onComplete(readHandler ->
                                {

                                    if (readHandler.succeeded())
                                    {
                                        if (readHandler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                        {
                                            Credentials credentials = readHandler.result().body().getJsonObject(Constant.STATUS_RESULT).mapTo(Credentials.class);

                                            data.put(Constant.USERNAME, credentials.getUsername());

                                            data.put(Constant.PASSWORD, credentials.getPassword());

                                            resultData.add(data);

                                            promise.complete(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, resultData).put(Constant.TYPE, Constant.PROVISION));

                                        }

                                        else
                                        {
                                            promise.fail(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.FAIL_TYPE, Constant.CREDENTIALS).encode());
                                        }
                                    }

                                    else
                                    {
                                        logger.error(readHandler.cause().getMessage());
                                    }

                                });

                            });
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

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void delete(String type, Message<JsonObject> message)
    {
        try
        {
            vertx.executeBlocking(promise ->
            {

                var data = message.body();

                switch (type)
                {
                    case Constant.CREDENTIALS ->
                    {
                        CredentialStore credentialStore = CredentialStore.getInstance();

                        long credentialsId = data.getLong(Constant.CREDENTIALS_ID);

                        Credentials credentials = credentialStore.read(credentialsId);

                        if (credentials != null)
                        {
                            if (credentials.getCounter() == 0)
                            {
                                credentialStore.delete(credentialsId);

                                promise.complete(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.STATUS_SUCCESS));
                            }

                            else
                            {
                                promise.fail(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.STATUS_ERROR).encode());
                            }
                        }

                        else
                        {
                            promise.fail(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.FAIL_TYPE).encode());
                        }
                    }

                    case Constant.DISCOVERY ->
                    {
                        DiscoveryStore discoveryStore = DiscoveryStore.getInstance();

                        long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                        Discovery discovery = discoveryStore.read(discoveryId);

                        if (discovery != null)
                        {
                            discoveryStore.delete(discoveryId);

                            promise.complete(new JsonObject().put(Constant.TYPE, Constant.DISCOVERY).put(Constant.STATUS, Constant.STATUS_SUCCESS));
                        }

                        else
                        {
                            promise.fail(new JsonObject().put(Constant.TYPE, Constant.DISCOVERY).put(Constant.STATUS, Constant.STATUS_FAIL).encode());
                        }
                    }

                    case Constant.PROVISION ->
                    {
                        ProvisionStore provisionStore = ProvisionStore.getInstance();

                        long provisionId = data.getLong(Constant.PROVISION_ID);

                        List<String> provision = provisionStore.read(String.valueOf(provisionId));

                        if (provision.size() > 0)
                        {
                            provisionStore.delete(String.valueOf(provisionId));

                            vertx.eventBus().<JsonObject>request(Constant.UPDATE_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, Long.parseLong(provision.get(1))).put(Constant.CREDENTIAL_COUNTER, false)).onComplete(updateHandler ->
                            {
                                if (updateHandler.succeeded())
                                {
                                    if (updateHandler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                                    {
                                        promise.complete(new JsonObject().put(Constant.TYPE, Constant.PROVISION).put(Constant.STATUS, Constant.STATUS_SUCCESS));
                                    }

                                    else
                                    {
                                        promise.fail(new JsonObject().put(Constant.TYPE, Constant.CREDENTIALS).put(Constant.STATUS, Constant.STATUS_FAIL).encode());
                                    }
                                }

                                else
                                {
                                    logger.error(updateHandler.cause().getMessage());
                                }
                            });

                        }

                        else
                        {
                            promise.fail(new JsonObject().put(Constant.TYPE, Constant.PROVISION).put(Constant.STATUS, Constant.STATUS_FAIL).encode());
                        }
                    }
                }

            }, handler ->
            {

                if (handler.succeeded())
                {
                    JsonObject successResult = (JsonObject) handler.result();

                    successResult.put(Constant.STATUS_MESSAGE, Constant.DELETE_SUCCESS);

                    successResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                    message.reply(successResult);

                }

                else
                {
                    JsonObject errorResult = new JsonObject(handler.cause().getMessage());

                    if (errorResult.getString(Constant.STATUS).equals(Constant.STATUS_FAIL))
                    {
                        errorResult.put(Constant.STATUS_MESSAGE, Constant.DATA_DOES_NOT_EXIST);

                    }

                    else
                    {
                        errorResult.put(Constant.STATUS_MESSAGE, Constant.PROFILE_ALREADY_IN_USE);

                        errorResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);
                    }

                    errorResult.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);

                    message.reply(errorResult);

                }

            });
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }
}