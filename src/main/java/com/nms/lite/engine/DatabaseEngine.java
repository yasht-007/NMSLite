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
import static com.nms.lite.utility.Constant.*;
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

            eventBus.<JsonObject>localConsumer(DATABASE_OPERATIONS).handler(message ->
            {

                switch (message.body().getString(OPERATION))
                {
                    case CREATE -> create(message.body().getString(TYPE), message);

                    case READ -> read(message.body().getString(TYPE), message);

                    case READ_ALL -> readAll(message.body().getString(TYPE), message);

                    case UPDATE -> update(message.body().getString(TYPE), message);

                    case DELETE -> delete(message.body().getString(TYPE), message);

                    case RUN -> runProvision(message);
                }

            });

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
                case CREDENTIALS ->
                {
                    long credId = KeyGen.getUniqueKeyForName(data.getString(CREDENTIALS_NAME));

                    if (credentialStore.read(credId) != null)
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_ALREADY_EXISTS));
                    }
                    else
                    {
                        credentialStore.create(new JsonObject().put(ID, credId).put(NAME, data.getString(CREDENTIALS_NAME)).put(USERNAME, data.getString(USERNAME)).put(PASSWORD, data.getString(PASSWORD)).put(COUNTER, 0));

                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, credId));
                    }
                }

                case DISCOVERY ->
                {

                    if (credentialStore.read(data.getLong(CREDENTIALS_ID)) != null)
                    {
                        long discoveryId = KeyGen.getUniqueKeyForName(data.getString(DISCOVERY_NAME));

                        if (discoveryStore.read(discoveryId) != null)
                        {
                            message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_ALREADY_EXISTS));
                        }
                        else
                        {
                            discoveryStore.create(new JsonObject().put(ID, discoveryId).put(NAME, data.getString(DISCOVERY_NAME)).put(IP_ADDRESS, data.getString(IP_ADDRESS)).put(PORT_NUMBER, data.getInteger(PORT_NUMBER)).put(CREDENTIALS_ID, data.getLong(CREDENTIALS_ID)).put(DISCOVERED, false));

                            message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, discoveryId));
                        }
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void read(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case CREDENTIALS ->
                {
                    long credId = data.getLong(CREDENTIALS_ID);

                    if (credentialStore.read(credId) != null)
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, credentialStore.read(credId)));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }

                case DISCOVERY ->
                {
                    long discoveryId = data.getLong(DISCOVERY_ID);

                    if (discoveryStore.read(discoveryId) != null)
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, discoveryStore.read(discoveryId)));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }

                case PROVISION ->
                {
                    long provisionId = data.getLong(PROVISION_ID);

                    List<String> provision = provisionStore.read(String.valueOf(provisionId));

                    if (provision.size() > 0)
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, new JsonObject(provision.get(0)).put(CREDENTIALS_ID, Long.parseLong(provision.get(1)))));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }

                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void update(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case CREDENTIALS ->
                {
                    long credentialsId = data.getLong(CREDENTIALS_ID);

                    if (credentialStore.read(credentialsId) != null)
                    {
                        JsonObject credentials = credentialStore.read(credentialsId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(USERNAME))
                            {
                                credentials.put(USERNAME, data.getString(USERNAME));
                            }

                            if (change.equalsIgnoreCase(PASSWORD))
                            {
                                credentials.put(PASSWORD, data.getString(PASSWORD));
                            }
                        });

                        credentialStore.update(credentials);

                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS));

                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }

                case DISCOVERY ->
                {
                    long discoveryId;

                    if (data.containsKey(DISCOVERY_NAME))
                    {
                        discoveryId = KeyGen.getUniqueKeyForName(data.getString(DISCOVERY_NAME));
                    }
                    else
                    {
                        discoveryId = data.getLong(DISCOVERY_ID);
                    }

                    if (discoveryStore.read(discoveryId) != null)
                    {
                        JsonObject discovery = discoveryStore.read(discoveryId);

                        data.fieldNames().forEach(change ->
                        {

                            if (change.equalsIgnoreCase(IP_ADDRESS))
                            {
                                discovery.put(IP_ADDRESS, data.getString(IP_ADDRESS));
                            }

                            if (change.equalsIgnoreCase(PORT_NUMBER))
                            {
                                discovery.put(PORT_NUMBER, data.getInteger(PORT_NUMBER));
                            }

                            if (change.equalsIgnoreCase(DISCOVERED))
                            {
                                discovery.put(DISCOVERED, data.getBoolean(DISCOVERED));
                            }

                            if (change.equalsIgnoreCase(CREDENTIALS_ID))
                            {
                                discovery.put(CREDENTIALS_ID, data.getLong(CREDENTIALS_ID));
                            }
                        });

                        discoveryStore.update(discovery);

                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS));

                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }
            }

        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void runProvision(Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            JsonObject discovery = discoveryStore.read(data.getLong(DISCOVERY_ID));

            if (discovery != null)
            {
                if (discovery.getBoolean(DISCOVERED))
                {
                    if (!provisionStore.containsIp(discovery.getString(IP_ADDRESS)))
                    {
                        JsonObject credentials = credentialStore.read(discovery.getLong(CREDENTIALS_ID));

                        if (credentials != null)
                        {
                            long provisionId = Global.provisionCounter.incrementAndGet();

                            provisionStore.create(discovery.getString(IP_ADDRESS), String.valueOf(provisionId), String.valueOf(discovery.getLong(CREDENTIALS_ID)), new JsonObject().put(PROVISION_ID, provisionId).put(IP_ADDRESS, discovery.getString(IP_ADDRESS)).put(PORT_NUMBER, discovery.getInteger(PORT_NUMBER)).encode());

                            String path = OUTPUT_PATH + FORWARD_SLASH + discovery.getString(IP_ADDRESS);

                            vertx.fileSystem().mkdirsBlocking(path).createFileBlocking(path + FORWARD_SLASH + CPU_METRIC + JSON_EXTENSION).createFileBlocking(path + FORWARD_SLASH + DISK_METRIC + JSON_EXTENSION).createFileBlocking(path + FORWARD_SLASH + PROCESS_METRIC + JSON_EXTENSION).createFileBlocking(path + FORWARD_SLASH + SYSTEM_METRIC + JSON_EXTENSION).createFileBlocking(path + FORWARD_SLASH + MEMORY_METRIC + JSON_EXTENSION);

                            logger.info(DIRECTORY_CREATION_SUCCESS);

                            logger.info(FILES_CREATION_SUCCESS);

                            credentials.put(COUNTER, credentials.getInteger(COUNTER) + 1);

                            credentialStore.update(credentials);

                            message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, provisionId));
                        }
                        else
                        {
                            message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, CREDENTIALS + DATA_DOES_NOT_EXIST));
                        }
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, ALREADY_IN_PROVISION_LIST_MESSAGE));
                    }
                }
                else
                {
                    message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DEVICE_NOT_DISCOVERED_MESSAGE));
                }

            }
            else
            {
                message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DISCOVERY + DATA_DOES_NOT_EXIST));
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }

    }

    private void readAll(String type, Message<JsonObject> message)
    {
        try
        {
            switch (type)
            {
                case CREDENTIALS ->
                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, credentialStore.readAll()));

                case DISCOVERY ->
                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, discoveryStore.readAll()));

                case PROVISION ->
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
                                resultData.add(new JsonObject(list.get(0)).put(USERNAME, credentials.getString(USERNAME)).put(PASSWORD, credentials.getString(PASSWORD)));
                            }
                        }
                    });

                    message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS).put(STATUS_RESULT, resultData));
                }
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private void delete(String type, Message<JsonObject> message)
    {
        try
        {
            var data = message.body();

            switch (type)
            {
                case CREDENTIALS ->
                {
                    long credentialsId = data.getLong(CREDENTIALS_ID);

                    JsonObject credentials = credentialStore.read(credentialsId);

                    if (credentials != null)
                    {
                        if (credentials.getInteger(COUNTER) == 0)
                        {
                            credentialStore.delete(credentialsId);

                            message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS));
                        }
                        else
                        {
                            message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, PROFILE_ALREADY_IN_USE));
                        }
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }

                case DISCOVERY ->
                {
                    long discoveryId = data.getLong(DISCOVERY_ID);

                    JsonObject discovery = discoveryStore.read(discoveryId);

                    if (discovery != null)
                    {
                        discoveryStore.delete(discoveryId);

                        message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS));
                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }

                case PROVISION ->
                {
                    long provisionId = data.getLong(PROVISION_ID);

                    List<String> provision = provisionStore.read(String.valueOf(provisionId));

                    if (provision.size() > 0)
                    {
                        provisionStore.delete(String.valueOf(provisionId));

                        JsonObject credentials = credentialStore.read(Long.parseLong(provision.get(1)));

                        if (credentials != null)
                        {
                            credentials.put(COUNTER, credentials.getInteger(COUNTER) - 1);

                            credentialStore.update(credentials);

                            message.reply(new JsonObject().put(STATUS, STATUS_SUCCESS));

                        }
                        else
                        {
                            message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, CREDENTIALS + DATA_DOES_NOT_EXIST));
                        }

                    }
                    else
                    {
                        message.reply(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_ERROR, DATA_DOES_NOT_EXIST));
                    }
                }
            }

        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            message.fail(STATUS_CODE_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}