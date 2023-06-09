package com.nms.lite.engine;

import com.nms.lite.utility.BuildProcess;
import com.nms.lite.utility.Constant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PollingEngine extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise)
    {
        JsonArray provisionData = new JsonArray();

        getProvisioningData(provisionData);

        vertx.setPeriodic(Constant.PROVISION_DATA_FETCH_INTERVAL, id -> poll(provisionData));

        promise.complete();
    }

    public void getProvisioningData(JsonArray result)
    {
        vertx.setPeriodic(5000, handler -> vertx.eventBus().<String>request(Constant.READ_ALL_PROVISION, new JsonObject()).onComplete(response ->
        {

            if (response.succeeded())
            {
                JsonObject resultData = new JsonObject(response.result().body());

                if (resultData.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                {
                    result.clear();

                    result.addAll(resultData.getJsonArray(Constant.STATUS_RESULT));

                }

                else if (resultData.getString(Constant.STATUS).equals(Constant.STATUS_FAIL))
                {
                    result.clear();
                }

            }
            else
            {
                System.out.println(response.cause().getMessage());
            }

        }));
    }

    public void poll(JsonArray provisionData)
    {

        if (provisionData != null)
        {
            for (int index = 0; index < provisionData.size(); index++)
            {
                JsonObject device = provisionData.getJsonObject(index);

                device.remove(Constant.PROVISION_ID);

                device.put(Constant.SERVICE, Constant.COLLECT);

                Collect(device, Constant.CPU_METRIC).onComplete(handler ->
                {
                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), Constant.TABULAR_METRICS, Constant.CPU_METRIC);
                    }
                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

                Collect(device, Constant.PROCESS_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), Constant.TABULAR_METRICS, Constant.PROCESS_METRIC);
                    }
                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

                Collect(device, Constant.DISK_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), Constant.TABULAR_METRICS, Constant.DISK_METRIC);
                    }
                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

                Collect(device, Constant.SYSTEM_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), Constant.SCALAR_METRICS, Constant.SYSTEM_METRIC);
                    }
                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

                Collect(device, Constant.MEMORY_METRIC).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        writePollDataToFile(handler.result(), Constant.SCALAR_METRICS, Constant.MEMORY_METRIC);
                    }
                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

            }

        }

    }

    private Future<JsonObject> Collect(JsonObject device, String metricGroup)
    {
        Promise<JsonObject> promise = Promise.promise();

        device.put(Constant.METRIC_GROUP, metricGroup);

        device.put(Constant.PORT_NUMBER, device.getString(Constant.PORT_NUMBER));

        BuildProcess process = new BuildProcess();

        List<String> command = new ArrayList<>();

        command.add(Constant.GO_PLUGIN_EXE_ABSOLUTE_PATH);

        command.add(device.encode());

        process.build(command, Constant.POLLING_TIMEOUT).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                promise.complete(handler.result());
            }
            else
            {
                System.out.println(handler.cause().getMessage());
            }
        });

        return promise.future();
    }

    public void writePollDataToFile(JsonObject pollingData, String type, String metricType)
    {
        FileSystem filesSystem = vertx.fileSystem();

        if (pollingData.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
        {
            JsonObject data = new JsonObject(pollingData.getString(Constant.STATUS_RESULT));

            String path = Constant.OUTPUT_PATH + Constant.FORWARD_SLASH + data.getString(Constant.IP_ADDRESS) + Constant.FORWARD_SLASH + metricType + Constant.JSON_EXTENSION;

            if (data.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
            {
                JsonObject result = new JsonObject();

                switch (type)
                {
                    case Constant.SCALAR_METRICS ->
                            result.put(Constant.SCALAR_METRICS, data.getJsonObject(Constant.STATUS_RESULT));

                    case Constant.TABULAR_METRICS ->
                            result.put(Constant.TABULAR_METRICS, data.getJsonArray(Constant.STATUS_RESULT));
                }

                filesSystem.open(path, new OpenOptions().setWrite(true)).onComplete(openHandler ->
                {

                    if (openHandler.succeeded())
                    {
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
                                        if (result.containsKey(Constant.SCALAR_METRICS))
                                        {
                                            fileData.add(new JsonObject().put(Constant.TIMESTAMP, data.getString(Constant.TIME)).put(Constant.DATA, result.getJsonObject(Constant.SCALAR_METRICS)));

                                        }
                                        else
                                        {
                                            fileData.add(new JsonObject().put(Constant.TIMESTAMP, data.getString(Constant.TIME)).put(Constant.DATA, result.getJsonArray(Constant.TABULAR_METRICS)));

                                        }
                                    }
                                }
                                else
                                {
                                    if (result.containsKey(Constant.SCALAR_METRICS))
                                    {
                                        fileData = new JsonArray().add(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.TIMESTAMP, data.getString(Constant.TIME)).put(Constant.DATA, result.getJsonObject(Constant.SCALAR_METRICS)));
                                    }
                                    else
                                    {
                                        fileData = new JsonArray().add(new JsonObject().put(Constant.STATUS, Constant.STATUS_SUCCESS).put(Constant.TIMESTAMP, data.getString(Constant.TIME)).put(Constant.DATA, result.getJsonArray(Constant.TABULAR_METRICS)));

                                    }
                                }

                                assert fileData != null;

                                filesSystem.writeFile(path, Buffer.buffer(fileData.encodePrettily())).onComplete(writeHandler ->
                                {

                                    if (writeHandler.succeeded())
                                    {
                                        System.out.println(Constant.DATA_DUMP_SUCCESS);
                                    }
                                    else
                                    {
                                        System.out.println(writeHandler.cause().getMessage());
                                    }

                                });
                            }
                            else
                            {
                                System.out.println(readHandler.cause().getMessage());
                            }

                        });
                    }
                    else
                    {
                        System.out.println(openHandler.cause().getMessage());
                    }

                });
            }
            else
            {
                System.out.println(Constant.POLL_FAILURE + Constant.EMPTY_SPACE + data.getString(Constant.IP_ADDRESS) + data.getString(Constant.STATUS_MESSAGE));

            }
        }
        else
        {
            System.out.println(Constant.POLL_FAILURE + Constant.PROCESS_ABNORMALLY_TERMINATED);
        }
    }
}
