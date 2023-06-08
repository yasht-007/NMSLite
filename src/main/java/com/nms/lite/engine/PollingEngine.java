package com.nms.lite.engine;

import com.nms.lite.utility.BuildProcess;
import com.nms.lite.utility.Constant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PollingEngine extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        JsonArray provisionData = new JsonArray();

        getProvisioningData(provisionData);

        vertx.setPeriodic(10000, id ->
        {
            poll(provisionData);
        });

        promise.complete();
    }

    public void getProvisioningData(JsonArray result)
    {
        vertx.setPeriodic(5000, handler ->
        {
            vertx.eventBus().<String>request(Constant.READ_ALL_PROVISION, new JsonObject()).onComplete(response ->
            {

                if (response.succeeded())
                {
                    JsonObject resultData = new JsonObject(response.result().body());

                    if (resultData.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                    {
                        result.clear();

                        result.addAll(resultData.getJsonArray(Constant.STATUS_RESULT));
                    }

                }
                else
                {
                    System.out.println(response.cause().getMessage());
                }

            });
        });
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
                        writePollDataToFile(handler.result(), Constant.TABULAR_METRICS);
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
                        writePollDataToFile(handler.result(), Constant.TABULAR_METRICS);
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
                        writePollDataToFile(handler.result(), Constant.SCALAR_METRICS);
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

        device.put(Constant.PORT_NUMBER, "5985");

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

    public void writePollDataToFile(JsonObject pollingData, String type)
    {

        System.out.println("-------" + pollingData);

        if (pollingData.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
        {
            JsonObject data = new JsonObject(pollingData.getString(Constant.STATUS_RESULT));

            if (data.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
            {
                FileSystem fileSystem = vertx.fileSystem();

                switch (type)
                {
                    case Constant.SCALAR_METRICS ->
                    {
                        JsonObject result = data.getJsonObject(Constant.STATUS_RESULT);

                        System.out.println("Scalar :" + result);
                    }

                    case Constant.TABULAR_METRICS ->
                    {
                        JsonArray result = data.getJsonArray(Constant.STATUS_RESULT);

                        System.out.println("Tabular :" + result);
                    }
                }
            }

            else
            {
                System.out.println("FAILURE " + data.getString(Constant.STATUS_MESSAGE));

            }
        }
        else
        {
            System.out.println("Process abnormal termination");
        }
    }
}
