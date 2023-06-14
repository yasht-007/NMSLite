package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.nms.lite.utility.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nms.lite.utility.Constant.*;


public class Credentials
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.vertx.eventBus();
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

            router.post(CREATE_ROUTE).handler(this::create);

            router.get(READ_CREDENTIAL_ROUTE).handler(this::read);

            router.get(READ_ALL_ROUTE).handler(this::readAll);

            router.put(UPDATE_ROUTE).handler(this::update);

            router.delete(DELETE_CREDENTIAL_ROUTE).handler(this::delete);
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

            if (bodyValidationResult.getJsonArray(STATUS_ERROR).size() > 0)
            {
                context.json(new JsonObject()

                        .put(STATUS, STATUS_FAIL)

                        .put(STATUS_CODE, STATUS_CODE_BAD_REQUEST)

                        .put(STATUS_MESSAGE, STATUS_MESSAGE_INVALID_INPUT)

                        .put(STATUS_ERRORS, bodyValidationResult));

            }
            else
            {
                eventBus.<JsonObject>request(DATABASE_OPERATIONS, requestBody.put(OPERATION, CREATE).put(TYPE, CREDENTIALS)).onComplete(handler ->
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
            long credId = Long.parseLong(context.pathParam(CREDENTIALS_ID));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(CREDENTIALS_ID, credId).put(OPERATION, READ).put(TYPE, CREDENTIALS)).onComplete(handler ->
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
        eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(OPERATION, READ_ALL).put(TYPE, CREDENTIALS)).onComplete(handler ->
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
                eventBus.<JsonObject>request(DATABASE_OPERATIONS, requestBody.put(OPERATION, UPDATE).put(TYPE, CREDENTIALS)).onComplete(handler ->
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
            long credId = Long.parseLong(context.pathParam(CREDENTIALS_ID));

            eventBus.<JsonObject>request(DATABASE_OPERATIONS, new JsonObject().put(CREDENTIALS_ID, credId).put(OPERATION, DELETE).put(TYPE, CREDENTIALS)).onComplete(handler ->
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
