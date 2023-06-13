package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.nms.lite.utility.RequestValidator;
import com.nms.lite.utility.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery
{
    Logger logger = LoggerFactory.getLogger(Discovery.class);
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();

    public Discovery(Router router)
    {
        this.router = router;
    }

    public void handleDiscoveryRoutes()
    {
        try
        {
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

            router.post(Constant.CREATE_ROUTE).handler(this::create);

            router.get(Constant.READ_DISCOVERY_ROUTE).handler(this::read);

            router.get(Constant.RUN_DISCOVERY_ROUTE).handler(this::run);

            router.get(Constant.READ_ALL_ROUTE).handler(this::readAll);

            router.put(Constant.UPDATE_ROUTE).handler(this::update);

            router.delete(Constant.DELETE_DISCOVERY_ROUTE).handler(this::delete);
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    public void create(RoutingContext context)
    {
        try
        {
            JsonObject requestBody = context.body().asJsonObject();

            JsonObject bodyValidationResult = RequestValidator.validateRequestBody(requestBody);

            if (bodyValidationResult.getJsonArray(Constant.STATUS_ERROR).size() > 0)
            {
                JsonObject response = new JsonObject()

                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                        .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                        .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT)

                        .put(Constant.STATUS_ERRORS, bodyValidationResult);

                context.json(response);

            }
            else
            {
                eventBus.<JsonObject>request(Constant.CREATE_DISCOVERY, requestBody).onComplete(handler ->
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
        }

        catch (DecodeException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }
    }

    public void read(RoutingContext context)
    {
        try
        {
            long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

            eventBus.<JsonObject>request(Constant.READ_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
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
        eventBus.<JsonObject>request(Constant.READ_ALL_DISCOVERY, new JsonObject()).onComplete(handler ->
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

    public void update(RoutingContext context)
    {
        try
        {
            JsonObject requestBody = context.body().asJsonObject();

            JsonObject bodyValidationResult = RequestValidator.validateRequestBody(requestBody);

            if (bodyValidationResult.getJsonArray(Constant.STATUS_ERROR).size() > 0)
            {
                JsonObject response = new JsonObject()

                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                        .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                        .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT)

                        .put(Constant.STATUS_ERRORS, bodyValidationResult);

                context.json(response);

            }

            else
            {
                eventBus.<JsonObject>request(Constant.UPDATE_DISCOVERY, requestBody).onComplete(handler ->
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
        }

        catch (DecodeException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }
    }

    public void delete(RoutingContext context) throws NumberFormatException
    {
        try
        {
            long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

            eventBus.<JsonObject>request(Constant.DELETE_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
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

    public void run(RoutingContext context) throws NumberFormatException
    {
        try
        {
            long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

            eventBus.<JsonObject>request(Constant.RUN_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId), new DeliveryOptions().setSendTimeout(Constant.MESSAGE_SEND_TIMEOUT)).onComplete(handler ->
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
