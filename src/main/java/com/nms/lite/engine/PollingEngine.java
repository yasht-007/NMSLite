package com.nms.lite.engine;

import com.nms.lite.utility.BuildProcess;

import static com.nms.lite.utility.Constant.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PollingEngine extends AbstractVerticle
{
    private final Logger logger = LoggerFactory.getLogger(PollingEngine.class);

    @Override
    public void start(Promise<Void> promise)
    {
        vertx.setPeriodic(Polling_INTERVAL, id -> vertx.eventBus().<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(OPERATION, READ_ALL).put(TYPE, PROVISION)).onComplete(response ->
        {

            if (response.succeeded())
            {
                if (response.result().body().getString(STATUS).equals(STATUS_SUCCESS))
                {
                    JsonArray provisionData = response.result().body().getJsonArray(STATUS_RESULT);

                    poll(provisionData);
                }

            }
            else
            {
                logger.error(response.cause().getMessage());
            }

        }));

        promise.complete();
    }

    public void poll(JsonArray provisionData)
    {

        if (provisionData.size() > 0)
        {
            for (int index = 0; index < provisionData.size(); index++)
            {
                JsonObject device = provisionData.getJsonObject(index);

                device.remove(PROVISION_ID);

                device.put(SERVICE, COLLECT);

                Collect(device, CPU_METRIC).onComplete(handler ->
                {
                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), TABULAR_METRICS, CPU_METRIC);
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });

                Collect(device, PROCESS_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), TABULAR_METRICS, PROCESS_METRIC);
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });

                Collect(device, DISK_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), TABULAR_METRICS, DISK_METRIC);
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });

                Collect(device, SYSTEM_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), SCALAR_METRICS, SYSTEM_METRIC);
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });

                Collect(device, MEMORY_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), SCALAR_METRICS, MEMORY_METRIC);
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });

            }

        }

    }

    private Future<JsonObject> Collect(JsonObject device, String metricGroup)
    {
        Promise<JsonObject> promise = Promise.promise();

        device.put(METRIC_GROUP, metricGroup);

        device.put(PORT_NUMBER, device.getString(PORT_NUMBER));

        List<String> command = new ArrayList<>();

        command.add(GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(Base64.getEncoder().encodeToString(device.encode().getBytes()));

        BuildProcess.build(command, POLLING_TIMEOUT, true).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                promise.complete(handler.result());
            }
            else
            {
                logger.error(handler.cause().getMessage());
            }
        });

        return promise.future();
    }

    public void writePollDataToFile(JsonObject pollingData, String type, String metricType)
    {
        FileSystem filesSystem = vertx.fileSystem();

        if (pollingData.getString(STATUS).equals(STATUS_SUCCESS))
        {
            JsonObject data = new JsonObject(pollingData.getString(STATUS_RESULT));

            String path = OUTPUT_PATH + FORWARD_SLASH + data.getString(IP_ADDRESS) + FORWARD_SLASH + metricType + JSON_EXTENSION;

            if (data.getString(STATUS).equals(STATUS_SUCCESS))
            {
                JsonObject result = new JsonObject();

                switch (type)
                {
                    case SCALAR_METRICS -> result.put(SCALAR_METRICS, data.getJsonObject(STATUS_RESULT));

                    case TABULAR_METRICS -> result.put(TABULAR_METRICS, data.getJsonArray(STATUS_RESULT));
                }

                filesSystem.readFile(path).onComplete(readHandler ->
                {
                    if (readHandler.succeeded())
                    {
                        JsonArray fileData;

                        if (readHandler.result().length() > 0)
                        {
                            fileData = readHandler.result().toJsonArray();

                            if (fileData != null && fileData.size() > 0)
                            {
                                if (result.containsKey(SCALAR_METRICS))
                                {
                                    fileData.add(result.getJsonObject(SCALAR_METRICS).put(TIMESTAMP, data.getString(TIME)));

                                }
                                else
                                {
                                    fileData.add(new JsonObject().put(TIMESTAMP, data.getString(TIME)).put(DATA, result.getJsonArray(TABULAR_METRICS)));

                                }
                            }
                        }
                        else
                        {
                            if (result.containsKey(SCALAR_METRICS))
                            {
                                fileData = new JsonArray().add(new JsonObject().put(TIMESTAMP, data.getString(TIME)).put(DATA, result.getJsonObject(SCALAR_METRICS)));
                            }
                            else
                            {
                                fileData = new JsonArray().add(new JsonObject().put(TIMESTAMP, data.getString(TIME)).put(DATA, result.getJsonArray(TABULAR_METRICS)));

                            }
                        }

                        assert fileData != null;

                        filesSystem.writeFile(path, Buffer.buffer(fileData.encodePrettily())).onComplete(writeHandler ->
                        {

                            if (writeHandler.succeeded())
                            {
                                logger.info(data.getString(IP_ADDRESS) + EMPTY_SPACE + DATA_DUMP_SUCCESS);
                            }
                            else
                            {
                                logger.error(writeHandler.cause().getMessage());
                            }

                        });
                    }
                    else
                    {
                        logger.error(readHandler.cause().getMessage());
                    }

                });
            }
            else
            {
                logger.info(POLL_FAILURE + EMPTY_SPACE + data.getString(IP_ADDRESS) + EMPTY_SPACE + data.getString(STATUS_MESSAGE));
            }
        }
        else

        {
            logger.error(POLL_FAILURE + PROCESS_ABNORMALLY_TERMINATED);
        }
    }
}
