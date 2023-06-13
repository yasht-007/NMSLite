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
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseEngine extends AbstractVerticle
{
    private final Logger logger = LoggerFactory.getLogger(DatabaseEngine.class);
    private final CredentialStore credentialStore = CredentialStore.getInstance();
    private final DiscoveryStore discoveryStore = DiscoveryStore.getInstance();
    private final ProvisionStore provisionStore = ProvisionStore.getInstance();


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

            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    long credId = KeyGen.getUniqueKeyForName(data.getString(Constant.CREDENTIALS_NAME));

                    if (credentialStore.read(credId) != null)
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_ALREADY_EXISTS));
                    }

                    else
                    {
                        credentialStore.create(new JsonObject().put(Constant.ID, credId).put(Constant.NAME, data.getString(Constant.CREDENTIALS_NAME)).put(Constant.USERNAME, data.getString(Constant.USERNAME)).put(Constant.PASSWORD, data.getString(Constant.PASSWORD)).put(Constant.COUNTER, 0));

                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, credId));
                    }
                }

                case Constant.DISCOVERY ->
                {

                    if (credentialStore.read(data.getLong(Constant.CREDENTIALS_ID)) != null)
                    {
                        long discoveryId = KeyGen.getUniqueKeyForName(data.getString(Constant.DISCOVERY_NAME));

                        if (discoveryStore.read(discoveryId) != null)
                        {
                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_ALREADY_EXISTS));
                        }

                        else
                        {
                            discoveryStore.create(new JsonObject().put(Constant.ID, discoveryId).put(Constant.NAME, data.getString(Constant.DISCOVERY_NAME)).put(Constant.IP_ADDRESS, data.getString(Constant.IP_ADDRESS)).put(Constant.PORT_NUMBER, data.getInteger(Constant.PORT_NUMBER)).put(Constant.CREDENTIALS_ID, data.getLong(Constant.CREDENTIALS_ID)).put(Constant.DISCOVERED, false));

                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, discoveryId));
                        }
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void read(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    long credId = data.getLong(Constant.CREDENTIALS_ID);

                    if (credentialStore.read(credId) != null)
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, credentialStore.read(credId)));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }

                case Constant.DISCOVERY ->
                {
                    long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                    if (discoveryStore.read(discoveryId) != null)
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, discoveryStore.read(discoveryId)));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }

                case Constant.PROVISION ->
                {
                    long provisionId = data.getLong(Constant.PROVISION_ID);

                    List<String> provision = provisionStore.read(String.valueOf(provisionId));

                    if (provision.size() > 0)
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, new JsonObject(provision.get(0)).put(Constant.CREDENTIALS_ID, Long.parseLong(provision.get(1)))));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }

                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void update(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    long credentialsId = data.getLong(Constant.CREDENTIALS_ID);

                    if (credentialStore.read(credentialsId) != null)
                    {
                        JsonObject credentials = credentialStore.read(credentialsId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(Constant.USERNAME))
                            {
                                credentials.put(Constant.USERNAME, data.getString(Constant.USERNAME));
                            }

                            if (change.equalsIgnoreCase(Constant.PASSWORD))
                            {
                                credentials.put(Constant.PASSWORD, data.getString(Constant.PASSWORD));
                            }
                        });

                        credentialStore.update(credentials);

                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS));

                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }

                case Constant.DISCOVERY ->
                {
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
                        JsonObject discovery = discoveryStore.read(discoveryId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(Constant.IP_ADDRESS))
                            {
                                discovery.put(Constant.IP_ADDRESS, data.getString(Constant.IP_ADDRESS));
                            }

                            if (change.equalsIgnoreCase(Constant.PORT_NUMBER))
                            {
                                discovery.put(Constant.PORT_NUMBER, data.getInteger(Constant.PORT_NUMBER));
                            }

                            if (change.equalsIgnoreCase(Constant.DISCOVERED))
                            {
                                discovery.put(Constant.DISCOVERED, data.getBoolean(Constant.DISCOVERED));
                            }

                            if (change.equalsIgnoreCase(Constant.CREDENTIALS_ID))
                            {
                                discovery.put(Constant.CREDENTIALS_ID, data.getLong(Constant.CREDENTIALS_ID));
                            }
                        });

                        discoveryStore.update(discovery);

                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS));

                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }
            }

        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void runProvision(Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            JsonObject discovery = discoveryStore.read(data.getLong(Constant.DISCOVERY_ID));

            if (discovery != null)
            {
                if (discovery.getBoolean(Constant.DISCOVERED))
                {
                    if (!provisionStore.containsIp(discovery.getString(Constant.IP_ADDRESS)))
                    {
                        JsonObject credentials = credentialStore.read(discovery.getLong(Constant.CREDENTIALS_ID));

                        if (credentials != null)
                        {
                            long provisionId = Global.provisionCounter.incrementAndGet();

                            provisionStore.create(discovery.getString(Constant.IP_ADDRESS), String.valueOf(provisionId), String.valueOf(discovery.getLong(Constant.CREDENTIALS_ID)), new JsonObject().put(Constant.PROVISION_ID, provisionId).put(Constant.IP_ADDRESS, discovery.getString(Constant.IP_ADDRESS)).put(Constant.PORT_NUMBER, discovery.getInteger(Constant.PORT_NUMBER)).encode());

                            String path = Constant.OUTPUT_PATH + Constant.FORWARD_SLASH + discovery.getString(Constant.IP_ADDRESS);

                            vertx.fileSystem().mkdirsBlocking(path).createFileBlocking(path + Constant.FORWARD_SLASH + Constant.CPU_METRIC + Constant.JSON_EXTENSION).createFileBlocking(path + Constant.FORWARD_SLASH + Constant.DISK_METRIC + Constant.JSON_EXTENSION).createFileBlocking(path + Constant.FORWARD_SLASH + Constant.PROCESS_METRIC + Constant.JSON_EXTENSION).createFileBlocking(path + Constant.FORWARD_SLASH + Constant.SYSTEM_METRIC + Constant.JSON_EXTENSION).createFileBlocking(path + Constant.FORWARD_SLASH + Constant.MEMORY_METRIC + Constant.JSON_EXTENSION);

                            credentials.put(Constant.COUNTER, credentials.getInteger(Constant.COUNTER) + 1);

                            credentialStore.update(credentials);

                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, provisionId));
                        }

                        else
                        {
                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST));
                        }
                    }

                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.ALREADY_IN_PROVISION_LIST_MESSAGE));
                    }
                }

                else
                {
                    message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DEVICE_NOT_DISCOVERED_MESSAGE));
                }

            }
            else
            {
                message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DISCOVERY + Constant.DATA_DOES_NOT_EXIST));
            }
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }

    }

    private void readAll(String type, Message<JsonObject> message)
    {
        try
        {
            switch (type)
            {
                case Constant.CREDENTIALS ->
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, credentialStore.readAll()));

                case Constant.DISCOVERY ->
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, discoveryStore.readAll()));

                case Constant.PROVISION ->
                {
                    JsonArray resultData = new JsonArray();

                    provisionStore.readAll().forEach(key ->
                    {
                        List<String> list = provisionStore.read(key);

                        if (list.size() > 0)
                        {

                            JsonObject credentials = credentialStore.read(Long.parseLong(list.get(1)));

                            if (credentials != null)
                            {
                                resultData.add(new JsonObject(list.get(0)).put(Constant.USERNAME, credentials.getString(Constant.USERNAME)).put(Constant.PASSWORD, credentials.getString(Constant.PASSWORD)));
                            }
                        }
                    });

                    message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.STATUS_RESULT, resultData));
                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void delete(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case Constant.CREDENTIALS ->
                {
                    long credentialsId = data.getLong(Constant.CREDENTIALS_ID);

                    JsonObject credentials = credentialStore.read(credentialsId);

                    if (credentials != null)
                    {
                        if (credentials.getInteger(Constant.COUNTER) == 0)
                        {
                            credentialStore.delete(credentialsId);

                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS));
                        }
                        else
                        {
                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.PROFILE_ALREADY_IN_USE));
                        }
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }

                case Constant.DISCOVERY ->
                {
                    long discoveryId = data.getLong(Constant.DISCOVERY_ID);

                    JsonObject discovery = discoveryStore.read(discoveryId);

                    if (discovery != null)
                    {
                        discoveryStore.delete(discoveryId);

                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }

                case Constant.PROVISION ->
                {
                    long provisionId = data.getLong(Constant.PROVISION_ID);

                    List<String> provision = provisionStore.read(String.valueOf(provisionId));

                    if (provision.size() > 0)
                    {
                        provisionStore.delete(String.valueOf(provisionId));

                        JsonObject credentials = credentialStore.read(Long.parseLong(provision.get(1)));

                        if (credentials != null)
                        {
                            credentials.put(Constant.COUNTER, credentials.getInteger(Constant.COUNTER) - 1);

                            credentialStore.update(credentials);

                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS));

                        }
                        else
                        {
                            message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.CREDENTIALS + Constant.DATA_DOES_NOT_EXIST));
                        }

                    }
                    else
                    {
                        message.reply(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_ERROR, Constant.DATA_DOES_NOT_EXIST));
                    }
                }
            }

        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(Constant.STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}