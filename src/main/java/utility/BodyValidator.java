package utility;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BodyValidator
{
    public static JsonObject validateRequestBody(JsonObject requestBody)
    {
        List<String> errors = new ArrayList<>();
        
        requestBody.fieldNames().forEach(key ->
        {
            var isNull = Validator.checkNull(requestBody.getString(key).trim());

            if (isNull)
            {
                errors.add("Invalid " + key);
            }
        });


        return new JsonObject().put("error",errors);
    }
}
