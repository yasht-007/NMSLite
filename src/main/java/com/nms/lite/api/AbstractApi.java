package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import com.nms.lite.utility.RequestValidator;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nms.lite.utility.Constant.*;
import static com.nms.lite.utility.Constant.STATUS_MESSAGE_INVALID_INPUT;

public abstract class AbstractApi
{
    private final String type;
    private final EventBus eventBus = Bootstrap.vertx.eventBus();
    private final Logger logger = LoggerFactory.getLogger(Credentials.class);
    private final String idType;

    public AbstractApi(String type, String idType)
    {
        this.type = type;

        this.idType = idType;
    }

    public void create(RoutingContext context)
    {
        try
        {
            JsonObject requestBody = context.body().asJsonObject();

            JsonObject bodyValidationResult = RequestValidator.validateRequestBody(requestBody);

            if (bodyValidationResult.getJsonArray(STATUS_ERROR).size() > 0)
            {
                context.json(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_CODE, STATUS_CODE_BAD_REQUEST).put(STATUS_MESSAGE, STATUS_MESSAGE_INVALID_INPUT).put(STATUS_ERRORS, bodyValidationResult));

            }
            else
            {
                eventBus.<JsonObject>request(DATABASE_OPERATIONS, requestBody.put(OPERATION, CREATE).put(TYPE, type)).onComplete(handler ->
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
        }

        catch (DecodeException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(STATUS_MESSAGE_INVALID_INPUT));
        }
    }

    public void read(RoutingContext context)
    {
        try
        {
            long id = Long.parseLong(context.pathParam(idType));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(idType, id).put(OPERATION, READ).put(TYPE, type)).onComplete(handler ->
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
        eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(OPERATION, READ_ALL).put(TYPE, type)).onComplete(handler ->
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

    public void update(RoutingContext context)
    {
        try
        {
            JsonObject requestBody = context.body().asJsonObject();

            JsonObject bodyValidationResult = RequestValidator.validateRequestBody(requestBody);

            if (bodyValidationResult.getJsonArray(STATUS_ERROR).size() > 0)
            {
                context.json(new JsonObject().put(STATUS, STATUS_FAIL).put(STATUS_CODE, STATUS_CODE_BAD_REQUEST).put(STATUS_MESSAGE, STATUS_MESSAGE_INVALID_INPUT).put(STATUS_ERRORS, bodyValidationResult));
            }
            else
            {
                eventBus.<JsonObject>request(DATABASE_OPERATIONS, requestBody.put(OPERATION, UPDATE).put(TYPE, type)).onComplete(handler ->
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
        }

        catch (DecodeException exception)
        {
            logger.error(exception.getMessage());

            context.json(Global.FormatErrorResponse(STATUS_MESSAGE_INVALID_INPUT));
        }
    }

    public void delete(RoutingContext context)
    {
        try
        {
            long id = Long.parseLong(context.pathParam(idType));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(idType, id).put(OPERATION, DELETE).put(TYPE, type)).onComplete(handler ->
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

            context.json(Global.FormatErrorResponse(INVALID));
        }
    }

}
