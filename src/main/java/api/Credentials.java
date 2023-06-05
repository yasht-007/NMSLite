package api;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import utility.BodyValidator;
import utility.Constant;

public class Credentials
{
    private final RoutingContext context;
    private final JsonObject requestBody;
    private final EventBus eventBus;

    public Credentials(RoutingContext routingContext, EventBus eventBus)
    {
        context = routingContext;

        requestBody = routingContext.body().asJsonObject();

        this.eventBus = eventBus;
    }

    public void create()
    {
        var response = context.response().setChunked(true).putHeader(Constant.HTTP_HEADER_CONTENT_TYPE, Constant.HTTP_MIME_TYPE_APPLICATION_JSON);

        JsonObject bodyValidationResult = BodyValidator.validateRequestBody(requestBody);

        if (bodyValidationResult.getJsonArray("error").size() > 0)
        {
            String jsonResponse = new JsonObject()

                    .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL)

                    .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_BAD_REQUEST)

                    .put(Constant.HTTP_STATUS_MESSAGE, Constant.HTTP_STATUS_MESSAGE_INVALID_INPUT)

                    .put(Constant.HTTP_STATUS_RESULT, "")

                    .put(Constant.HTTP_STATUS_ERRORS, bodyValidationResult).encodePrettily();

            response.end(jsonResponse);

        }
        else
        {
            eventBus.<JsonObject>request(Constant.CREATE_CREDENTIALS, requestBody).onComplete(handler ->
            {

                if (handler.succeeded())
                {
                    var successresult = handler.result().body();

                    if (successresult.getString(Constant.HTTP_STATUS).equals(Constant.HTTP_STATUS_SUCCESS))
                    {
                        String jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_SUCCESS)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_OK)

                                .put(Constant.HTTP_STATUS_MESSAGE, Constant.HTTP_STATUS_MESSAGE)

                                .put(Constant.HTTP_STATUS_RESULT, successresult.getString(Constant.HTTP_STATUS_MESSAGE))

                                .put(Constant.HTTP_STATUS_ERRORS, "").encodePrettily();

                        response.end(jsonResponse);
                    }

                    else
                    {
                        var failedResult = handler.result().body();

                        String jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_CONFLICT)

                                .put(Constant.HTTP_STATUS_MESSAGE, failedResult.getString(Constant.HTTP_STATUS_MESSAGE))

                                .put(Constant.HTTP_STATUS_RESULT, "")

                                .put(Constant.HTTP_STATUS_ERRORS, failedResult.getString(Constant.HTTP_STATUS_MESSAGE)).encodePrettily();

                        response.end(jsonResponse);
                    }
                }

                else
                {
                    System.out.println(handler.cause().getMessage());
                }
            });

        }
    }

    public void read()
    {

    }

    public void readAll()
    {

    }

    public void update()
    {

    }

    public void delete()
    {

    }
}
