package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static com.nms.lite.utility.Constant.*;

class Device
{
    float utilization;
    String monitorIp;

    public Device(String s, float i)
    {
        utilization = i;

        monitorIp = s;
    }

    public JsonObject toJSONObject()
    {
        return new JsonObject().put("Monitor Ip", monitorIp).put("Cpu utilization(percentage)", utilization);
    }
}

public class Query
{
    private final Router router;

    private final Vertx vertx = Bootstrap.vertx;
    private final Logger logger = LoggerFactory.getLogger(Credentials.class);

    public Query(Router router)
    {
        this.router = router;
    }

    public void handleQueryRoutes()
    {
        try
        {
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

            router.get("/getcpupercent/:devicesCount").handler(this::getCpuPercent);
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    private void getCpuPercent(RoutingContext context)
    {
        int n = Integer.parseInt(context.pathParam("devicesCount"));

        PriorityQueue<Device> topNMonitors = new PriorityQueue<>(Comparator.comparingDouble(compare -> compare.utilization));

        List<String> devices = vertx.fileSystem().readDirBlocking(OUTPUT_PATH);

        devices.forEach(device ->
        {
            JsonArray data = vertx.fileSystem().readFileBlocking(device + FORWARD_SLASH + CPU_METRIC + JSON_EXTENSION).toJsonArray();

            float cpuUtilization = 0;

            for (int index = 0; index < data.size(); index++)
            {
                JsonArray metricData = data.getJsonObject(index).getJsonArray(DATA);

                for (int metricIndex = 0; metricIndex < metricData.size(); metricIndex++)
                {
                    JsonObject singleMetricData = metricData.getJsonObject(metricIndex);

                    if (singleMetricData.getString("system.cpu.core").equals("_Total"))
                    {
                        cpuUtilization = cpuUtilization + singleMetricData.getInteger("system.cpu.percentage");
                    }

                }

            }

            cpuUtilization /= data.size();

            if (topNMonitors.size() >= n)
            {
                topNMonitors.poll();
            }

            topNMonitors.add(new Device(device.split("/")[4], cpuUtilization));
        });

        JsonArray response = new JsonArray();

        while (!topNMonitors.isEmpty())
        {
            response.add(topNMonitors.poll().toJSONObject());
        }

        context.json(response);
    }
}
