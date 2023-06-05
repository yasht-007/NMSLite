package api;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import utility.BodyValidator;
import utility.Constant;

public class Credentials
{
    private final RoutingContext context;
    private JsonObject requestBody;
    private final EventBus eventBus;

    public Credentials(RoutingContext routingContext, EventBus eventBus)
    {
        context = routingContext;

        this.eventBus = eventBus;
    }

    public void create()
    {
        requestBody = context.body().asJsonObject();

        var response = context.response().setChunked(true).putHeader(Constant.HTTP_HEADER_CONTENT_TYPE, Constant.HTTP_MIME_TYPE_APPLICATION_JSON);

        JsonObject bodyValidationResult = BodyValidator.validateRequestBody(requestBody);

        if (bodyValidationResult.getJsonArray(Constant.ERROR).size() > 0)
        {
            String jsonResponse = new JsonObject()

                    .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL)

                    .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_BAD_REQUEST)

                    .put(Constant.HTTP_STATUS_MESSAGE, Constant.HTTP_STATUS_MESSAGE_INVALID_INPUT)

                    .put(Constant.HTTP_STATUS_RESULT, Constant.EMPTY_STRING)

                    .put(Constant.HTTP_STATUS_ERRORS, bodyValidationResult).encodePrettily();

            response.end(jsonResponse);

        }
        else
        {
            eventBus.<JsonObject>request(Constant.CREATE_CREDENTIALS, requestBody).onComplete(handler ->
            {

                if (handler.succeeded())
                {
                    var successResult = handler.result().body();

                    if (successResult.getString(Constant.HTTP_STATUS).equals(Constant.HTTP_STATUS_SUCCESS))
                    {
                        JsonObject jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_SUCCESS)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_OK)

                                .put(Constant.HTTP_STATUS_MESSAGE, Constant.HTTP_STATUS_MESSAGE)

                                .put(Constant.HTTP_STATUS_RESULT, successResult.getLong(Constant.HTTP_STATUS_RESULT))

                                .put(Constant.HTTP_STATUS_ERRORS, Constant.EMPTY_STRING);

                        context.json(jsonResponse);
                    }

                    else
                    {
                        var failedResult = handler.result().body();

                        String jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_CONFLICT)

                                .put(Constant.HTTP_STATUS_MESSAGE, failedResult.getString(Constant.HTTP_STATUS_MESSAGE))

                                .put(Constant.HTTP_STATUS_RESULT, Constant.EMPTY_STRING)

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
        long credId = Long.parseLong(context.pathParam(Constant.CREDENTIALS_ID));

        var response = context.response().setChunked(true).putHeader(Constant.HTTP_HEADER_CONTENT_TYPE, Constant.HTTP_MIME_TYPE_APPLICATION_JSON);

            eventBus.<JsonObject>request(Constant.READ_CREDENTIALS, new JsonObject().put(Constant.CREDENTIALS_ID,credId)).onComplete(handler ->{

                if (handler.succeeded())
                {
                    var successResult = handler.result().body();

                    if (successResult.getString(Constant.HTTP_STATUS).equals(Constant.HTTP_STATUS_SUCCESS))
                    {
                        JsonObject jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_SUCCESS)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_OK)

                                .put(Constant.HTTP_STATUS_MESSAGE, Constant.HTTP_STATUS_MESSAGE)

                                .put(Constant.HTTP_STATUS_RESULT, successResult.getJsonObject(Constant.HTTP_STATUS_RESULT))

                                .put(Constant.HTTP_STATUS_ERRORS, Constant.EMPTY_STRING);

                        context.json(jsonResponse);
                    }

                    else
                    {
                        var failedResult = handler.result().body();

                        String jsonResponse = new JsonObject()

                                .put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL)

                                .put(Constant.HTTP_STATUS_CODE, Constant.HTTP_STATUS_CODE_CONFLICT)

                                .put(Constant.HTTP_STATUS_MESSAGE, failedResult.getString(Constant.HTTP_STATUS_MESSAGE))

                                .put(Constant.HTTP_STATUS_RESULT, Constant.EMPTY_STRING)

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
