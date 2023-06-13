package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.nms.lite.utility.RequestValidator;
import com.nms.lite.utility.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Credentials
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();
    private final Logger logger = LoggerFactory.getLogger(Credentials.class);

    public Credentials(Router router)
    {
        this.router = router;
    }

    public void handleCredentialRoutes()
    {
        try
        {
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

            router.post(Constant.CREATE_ROUTE).handler(this::create);

            router.get(Constant.READ_CREDENTIAL_ROUTE).handler(this::read);

            router.get(Constant.READ_ALL_ROUTE).handler(this::readAll);

            router.put(Constant.UPDATE_ROUTE).handler(this::update);

            router.delete(Constant.DELETE_CREDENTIAL_ROUTE).handler(this::delete);
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
                context.json(new JsonObject()

                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                        .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                        .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT)

                        .put(Constant.STATUS_ERRORS, bodyValidationResult));

            }
            else
            {
                eventBus.<JsonObject>request(Constant.CREATE_CREDENTIALS, requestBody).onComplete(handler ->
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
            long credId = Long.parseLong(context.pathParam(Constant.CREDENTIALS_ID));

            eventBus.<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credId)).onComplete(handler ->
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
        eventBus.<JsonObject>request(Constant.READ_ALL_CREDENTIALS, new JsonObject()).onComplete(handler ->
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
                context.json(new JsonObject().put(Constant.STATUS, Constant.STATUS_FAIL).put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST).put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT).put(Constant.STATUS_ERRORS, bodyValidationResult));
            }

            else
            {
                eventBus.<JsonObject>request(Constant.UPDATE_CREDENTIALS, requestBody).onComplete(handler ->
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

    public void delete(RoutingContext context)
    {
        try
        {
            long credId = Long.parseLong(context.pathParam(Constant.CREDENTIALS_ID));

            eventBus.<JsonObject>request(Constant.DELETE_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credId)).onComplete(handler ->
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

            context.json(Global.FormatErrorResponse(Constant.INVALID));
        }
    }
}
