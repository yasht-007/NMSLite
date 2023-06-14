package com.nms.lite.api;

import com.nms.lite.Bootstrap;

import static com.nms.lite.utility.Constant.*;

import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.vertx.eventBus();
    private final Logger logger = LoggerFactory.getLogger(Provision.class);

    public Provision(Router router)
    {
        this.router = router;
    }

    public void handleProvisionRoutes()
    {
        try
        {
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

            router.get(RUN_PROVISION_ROUTE).handler(this::run);

            router.get(READ_PROVISION_ROUTE).handler(this::read);

            router.get(READ_ALL_ROUTE).handler(this::readAll);

            router.delete(DELETE_PROVISION_ROUTE).handler(this::delete);
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
            long discoveryId = Long.parseLong(context.pathParam(DISCOVERY_ID));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(DISCOVERY_ID, discoveryId).put(OPERATION, RUN).put(TYPE, PROVISION)).onComplete(handler ->
            {
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_OK));
                        }
                        else
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_BAD_REQUEST));
                        }
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());

                        Global.sendExceptionMessage(context);
                    }
                }

                catch (Exception exception)
                {
                    logger.error(exception.getMessage());

                    Global.sendExceptionMessage(context);
                }

            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(INVALID_ID));
        }
    }

    public void read(RoutingContext context)
    {
        try
        {
            long provisionId = Long.parseLong(context.pathParam(PROVISION_ID));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(PROVISION_ID, provisionId).put(OPERATION, READ).put(TYPE, PROVISION)).onComplete(handler ->
            {
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_OK));
                        }
                        else
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_BAD_REQUEST));
                        }
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());

                        Global.sendExceptionMessage(context);
                    }
                }

                catch (Exception exception)
                {
                    logger.error(exception.getMessage());

                    Global.sendExceptionMessage(context);
                }
            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(INVALID_ID));
        }
    }

    public void readAll(RoutingContext context)
    {
        eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(OPERATION, READ_ALL).put(TYPE, PROVISION)).onComplete(handler ->
        {
            try
            {
                if (handler.succeeded())
                {
                    context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_OK));
                }
                else
                {
                    logger.error(handler.cause().getMessage());

                    Global.sendExceptionMessage(context);
                }
            }

            catch (Exception exception)
            {
                logger.error(exception.getMessage());

                Global.sendExceptionMessage(context);
            }
        });
    }

    public void delete(RoutingContext context)
    {
        try
        {
            long provisionId = Long.parseLong(context.pathParam(PROVISION_ID));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(PROVISION_ID, provisionId).put(OPERATION, DELETE).put(TYPE, PROVISION)).onComplete(handler ->
            {
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_OK));
                        }
                        else
                        {
                            context.json(handler.result().body().put(STATUS_CODE, STATUS_CODE_BAD_REQUEST));
                        }
                    }
                    else
                    {
                        logger.error(handler.cause().getMessage());

                        Global.sendExceptionMessage(context);
                    }
                }

                catch (Exception exception)
                {
                    logger.error(exception.getMessage());

                    Global.sendExceptionMessage(context);

                }
            });
        }

        catch (NumberFormatException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(INVALID_ID));
        }
    }
}
