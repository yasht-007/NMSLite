package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Constant;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Provision
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();

    public Provision(Router router)
    {
        this.router = router;
    }

    public void handleProvisionRoutes()
    {
        try
        {
            router.get(Constant.RUN_PROVISION_ROUTE).handler(this::run);

            router.get(Constant.READ_PROVISION_ROUTE).handler(this::read);

            router.get(Constant.READ_ALL_ROUTE).handler(this::readAll);

            router.delete(Constant.DELETE_PROVISION_ROUTE).handler(this::delete);
        }

        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void run(RoutingContext context)
    {
        long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

        eventBus.<JsonObject>request(Constant.CREATE_PROVISION, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                context.json(handler.result().body());
            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }

        });

    }

    public void read(RoutingContext context)
    {
        long provisionId = Long.parseLong(context.pathParam(Constant.PROVISION_ID));

        eventBus.<JsonObject>request(Constant.READ_PROVISION, new JsonObject().put(Constant.PROVISION_ID, provisionId)).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                JsonObject result = handler.result().body();

                if (result.getString(Constant.STATUS).equals(Constant.STATUS_FAIL))
                {
                    result.put(Constant.STATUS_MESSAGE, Constant.DATA_DOES_NOT_EXIST);
                }

                context.json(handler.result().body());
            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }
        });


    }

    public void readAll(RoutingContext context)
    {
        eventBus.<String>request(Constant.READ_ALL_PROVISION, new JsonObject()).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                JsonObject result = new JsonObject(handler.result().body());

                context.json(result);
            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }
        });
    }

    public void delete(RoutingContext context)
    {
        long provisionId = Long.parseLong(context.pathParam(Constant.PROVISION_ID));

        eventBus.<JsonObject>request(Constant.DELETE_PROVISION, new JsonObject().put(Constant.PROVISION_ID, provisionId)).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                context.json(handler.result().body());

            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }
        });
    }
}
