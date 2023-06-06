package com.nms.lite.api;

import com.nms.lite.Bootstrap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.nms.lite.utility.RequestValidator;
import com.nms.lite.utility.Constant;
import io.vertx.ext.web.handler.BodyHandler;

public class Discovery
{
    private final Router router;
    private final EventBus eventBus = Bootstrap.getEventBus();

    public Discovery(Router router)
    {
        this.router = router;
    }

    public void handleDiscoveryRoutes()
    {
        router.route().method(HttpMethod.POST).method(HttpMethod.PUT).handler(BodyHandler.create());

        router.post(Constant.CREATE_ROUTE).handler(this::create);

        router.get(Constant.READ_DISCOVERY_ROUTE).handler(this::read);

        router.get(Constant.RUN_DISCOVERY_ROUTE).handler(this::run);

        router.get(Constant.READ_ALL_ROUTE).handler(this::readAll);

        router.put(Constant.UPDATE_ROUTE).handler(this::update);

        router.delete(Constant.DELETE_ROUTE).handler(this::delete);
    }

    public void create(RoutingContext context)
    {
        JsonObject requestBody = context.body().asJsonObject();

        JsonObject bodyValidationResult = RequestValidator.validateRequestBody(requestBody);

        if (bodyValidationResult.getJsonArray(Constant.STATUS_ERROR).size() > 0)
        {
            JsonObject response = new JsonObject()

                    .put(Constant.STATUS, Constant.STATUS_FAIL)

                    .put(Constant.STATUS_CODE, Constant.STATUS_CODE_BAD_REQUEST)

                    .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE_INVALID_INPUT)

                    .put(Constant.STATUS_RESULT, "")

                    .put(Constant.STATUS_ERRORS, bodyValidationResult);

            context.json(response);

        }
        else
        {
            eventBus.<JsonObject>request(Constant.CREATE_DISCOVERY, requestBody).onComplete(handler ->
            {

                if (handler.succeeded())
                {
                    var successResult = handler.result().body();

                    if (successResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                    {
                        JsonObject response = new JsonObject()

                                .put(Constant.STATUS, Constant.STATUS_SUCCESS)

                                .put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK)

                                .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE)

                                .put(Constant.STATUS_RESULT, successResult.getLong(Constant.STATUS_RESULT))

                                .put(Constant.STATUS_ERRORS, "");

                        context.json(response);
                    }

                    else
                    {
                        var failedResult = handler.result().body();

                        JsonObject response = new JsonObject()

                                .put(Constant.STATUS, Constant.STATUS_FAIL)

                                .put(Constant.STATUS_CODE, Constant.STATUS_CODE_CONFLICT)

                                .put(Constant.STATUS_MESSAGE, failedResult.getString(Constant.STATUS_MESSAGE))

                                .put(Constant.STATUS_RESULT, "")

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

    public void read(RoutingContext context)
    {
        long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

        eventBus.<JsonObject>request(Constant.READ_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                var successResult = handler.result().body();

                if (successResult.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS))
                {
                    JsonObject response = new JsonObject()

                            .put(Constant.STATUS, Constant.STATUS_SUCCESS)

                            .put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK)

                            .put(Constant.STATUS_MESSAGE, Constant.STATUS_MESSAGE)

                            .put(Constant.STATUS_RESULT, successResult.getJsonObject(Constant.STATUS_RESULT))

                            .put(Constant.STATUS_ERRORS, Constant.EMPTY_STRING);

                    context.json(response);
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

    public void readAll(RoutingContext context)
    {

    }

    public void update(RoutingContext context)
    {

    }

    public void delete(RoutingContext context)
    {

    }

    public void run(RoutingContext context)
    {
        long discoveryId = Long.parseLong(context.pathParam(Constant.DISCOVERY_ID));

        eventBus.<JsonObject>request(Constant.RUN_DISCOVERY, new JsonObject().put(Constant.DISCOVERY_ID, discoveryId)).onComplete(handler ->
        {

            if (handler.succeeded())
            {
                var result = handler.result().body();

                if (result.getString(Constant.STATUS).equals(Constant.STATUS_SUCCESS) && result.getString(Constant.PROCESS_STATUS).equals(Constant.PROCESS_NORMAL))
                {
                    result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK)

                            .put(Constant.STATUS_RESULT, result.getJsonObject(Constant.STATUS_RESULT).getString(Constant.HOSTNAME));
                }

                else
                {
                    result.remove(Constant.STATUS_RESULT);
                }

                context.json(result);

            }

            else
            {
                System.out.println(handler.cause().getMessage());
            }

        });

    }
}
