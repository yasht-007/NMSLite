package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import com.nms.lite.utility.Global;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.nms.lite.utility.RequestValidator;
import com.nms.lite.utility.Constant;
import io.vertx.ext.web.handler.BodyHandler;


public class Credentials
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();

    public Credentials(Router router)
    {
        this.router = router;
    }

    public void handleCredentialRoutes()
    {
        try
        {

            router.route().method(HttpMethod.POST).method(HttpMethod.PUT).handler(BodyHandler.create());

            router.post(Constant.CREATE_ROUTE).handler(this::create);

            router.get(Constant.READ_CREDENTIAL_ROUTE).handler(this::read);

            router.get(Constant.READ_ALL_ROUTE).handler(this::readAll);

            router.put(Constant.UPDATE_ROUTE).handler(this::update);

            router.delete(Constant.DELETE_CREDENTIAL_ROUTE).handler(this::delete);
        }

        catch (Exception exception)
        {
            exception.printStackTrace();
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
                String jsonResponse = new JsonObject()

                        .put(Constant.STATUS, Constant.STATUS_FAIL)

                        .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                        .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT)

                        .put(Constant.STATUS_RESULT, Constant.EMPTY_STRING)

                        .put(Constant.STATUS_ERRORS, bodyValidationResult).encodePrettily();

                context.end(jsonResponse);

            }
            else
            {
                eventBus.<JsonObject>request(Constant.CREATE_CREDENTIALS, requestBody).onComplete(handler ->
                {

                    if (handler.succeeded())
                    {
                        var successResult = handler.result().body();

                        if (successResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            JsonObject jsonResponse = new JsonObject()

                                    .put(Constant.STATUS, Constant.STATUS_SUCCESS)

                                    .put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK)

                                    .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE)

                                    .put(Constant.STATUS_RESULT, successResult.getLong(Constant.STATUS_RESULT))

                                    .put(Constant.STATUS_ERRORS, Constant.EMPTY_STRING);

                            context.json(jsonResponse);
                        }

                        else
                        {
                            var failedResult = handler.result().body();

                            JsonObject response = new JsonObject()

                                    .put(Constant.STATUS, Constant.STATUS_FAIL)

                                    .put(Constant.STATUS_CODE, Constant.STATUS_CODE_CONFLICT)

                                    .put(Constant.STATUS_MESSAGE, failedResult.getString(Constant.STATUS_MESSAGE))

                                    .put(Constant.STATUS_RESULT, Constant.EMPTY_STRING)

                                    .put(Constant.STATUS_ERRORS, failedResult.getString(Constant.STATUS_MESSAGE));

                            context.json(response);
                        }
                    }

                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

            }
        }

        catch (DecodeException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }

        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }

    public void read(RoutingContext context)
    {
        try
        {
            long credId = Long.parseLong(context.pathParam(Constant.CREDENTIALS_ID));

            eventBus.<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credId)).onComplete(handler ->
            {

                if (handler.succeeded())
                {
                    var successResult = handler.result().body();

                    if (successResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                    {
                        JsonObject jsonResponse = new JsonObject()

                                .put(Constant.STATUS, Constant.STATUS_SUCCESS)

                                .put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK)

                                .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE)

                                .put(Constant.STATUS_RESULT, successResult.getJsonObject(Constant.STATUS_RESULT))

                                .put(Constant.STATUS_ERRORS, Constant.EMPTY_STRING);

                        context.json(jsonResponse);
                    }

                    else
                    {
                        var failedResult = handler.result().body();

                        JsonObject response = new JsonObject()

                                .put(Constant.STATUS, Constant.STATUS_FAIL)

                                .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                                .put(Constant.STATUS_MESSAGE, failedResult.getString(Constant.STATUS_MESSAGE))

                                .put(Constant.STATUS_RESULT, Constant.EMPTY_STRING)

                                .put(Constant.STATUS_ERRORS, failedResult.getString(Constant.STATUS_MESSAGE));

                        context.json(response);

                    }
                }

                else
                {
                    System.out.println(handler.cause().getMessage());
                }
            });
        }

        catch (NumberFormatException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.INVALID_ID));
        }

        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }

    public void readAll(RoutingContext context)
    {
        try
        {

            eventBus.<String>request(Constant.READ_ALL_CREDENTIALS, new JsonObject()).onComplete(handler ->
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

        catch (DecodeException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }

        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
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

                        .put(Constant.STATUS_RESULT, Constant.EMPTY_STRING)

                        .put(Constant.STATUS_ERRORS, bodyValidationResult);

                context.json(response);

            }

            else
            {
                eventBus.<JsonObject>request(Constant.UPDATE_CREDENTIALS, requestBody).onComplete(handler ->
                {
                    if (handler.succeeded())
                    {
                        var result = handler.result().body();

                        if (result.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                        {
                            result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);
                        }

                        else
                        {
                            result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST);
                        }

                        result.remove(Constant.TYPE);

                        context.json(result);

                    }

                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });

            }
        }

        catch (DecodeException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }

        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }

    public void delete(RoutingContext context)
    {
        try
        {
            long credId = Long.parseLong(context.pathParam(Constant.CREDENTIALS_ID));

            eventBus.<JsonObject>request(Constant.DELETE_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID, credId)).onComplete(handler ->
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

        catch (DecodeException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.STATUS_MESSAGE_INVALID_INPUT));
        }

        catch (NumberFormatException exception)
        {
            context.json(Global.FormatErrorResponse(Constant.INVALID));
        }

        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }
}
