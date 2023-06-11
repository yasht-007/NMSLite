package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Constant;
import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();
    private final Logger logger = LoggerFactory.getLogger(Provision.class);

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
            logger.error(exception.getMessage());
        }
    }

    public void run(RoutingContext context)
    {
        try
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
                    logger.error(handler.cause().getMessage());

                    Global.sendExceptionMessage(context);
                }

            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            Global.sendExceptionMessage(context);
        }
    }

    public void read(RoutingContext context)
    {
        try
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
                    logger.error(handler.cause().getMessage());

                    Global.sendExceptionMessage(context);
                }
            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            Global.sendExceptionMessage(context);
        }
    }

    public void readAll(RoutingContext context)
    {
        try
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
                    logger.error(handler.cause().getMessage());

                    Global.sendExceptionMessage(context);
                }
            });
        }

        catch (DecodeException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            Global.sendExceptionMessage(context);
        }
    }

    public void delete(RoutingContext context)
    {
        try
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
                    logger.error(handler.cause().getMessage());

                    Global.sendExceptionMessage(context);
                }
            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            Global.sendExceptionMessage(context);
        }
    }
}
