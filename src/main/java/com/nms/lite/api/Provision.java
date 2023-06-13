package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Constant;
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
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

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
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK));
                        }

                        else
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST));
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

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }
    }

    public void read(RoutingContext context)
    {
        try
        {
            long provisionId = Long.parseLong(context.pathParam(Constant.PROVISION_ID));

            eventBus.<JsonObject>request(Constant.READ_PROVISION, new JsonObject().put(Constant.PROVISION_ID, provisionId)).onComplete(handler ->
            {
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK));
                        }

                        else
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST));
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

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }
    }

    public void readAll(RoutingContext context)
    {
        eventBus.<JsonObject>request(Constant.READ_ALL_PROVISION, new JsonObject()).onComplete(handler ->
        {
            try
            {
                if (handler.succeeded())
                {
                    context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK));
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
            long provisionId = Long.parseLong(context.pathParam(Constant.PROVISION_ID));

            eventBus.<JsonObject>request(Constant.DELETE_PROVISION, new JsonObject().put(Constant.PROVISION_ID, provisionId)).onComplete(handler ->
            {
                try
                {
                    if (handler.succeeded())
                    {
                        if (handler.result().body().getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK));
                        }

                        else
                        {
                            context.json(handler.result().body().put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST));
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

            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }
    }
}
