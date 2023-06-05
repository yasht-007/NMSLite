package api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Discovery
{
    private RoutingContext context;
    private final JsonObject requestBody;
    public Discovery(RoutingContext routingContext)
    {
        context = routingContext;

        requestBody = routingContext.body().asJsonObject();
    }
    public void create()
    {
        System.out.println(requestBody);
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
