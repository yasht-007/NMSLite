package com.nms.lite.engine;

import com.nms.lite.api.Credentials;
import com.nms.lite.api.Discovery;
import com.nms.lite.api.Provision;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import static com.nms.lite.utility.Constant.*;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiEngine extends AbstractVerticle
{
    private final Logger logger = LoggerFactory.getLogger(ApiEngine.class);

    @Override
    public void start(Promise<Void> promise)
    {
        try
        {

            Router router = Router.router(vertx);

            router.route().method(HttpMethod.POST).method(HttpMethod.PUT).handler(BodyHandler.create().setBodyLimit(BODY_LIMIT));

            Router credentialRouter = Router.router(vertx);

            Router discoveryRouter = Router.router(vertx);

            Router provisionRouter = Router.router(vertx);

            router.route(MAIN_CREDENTIAL_ROUTE).subRouter(credentialRouter);

            router.route(MAIN_DISCOVERY_ROUTE).subRouter(discoveryRouter);

            router.route(MAIN_PROVISION_ROUTE).subRouter(provisionRouter);

            new Credentials(credentialRouter).handleCredentialRoutes();

            new Discovery(discoveryRouter).handleDiscoveryRoutes();

            new Provision(provisionRouter).handleProvisionRoutes();

            vertx.createHttpServer().requestHandler(router).listen(PORT).onComplete(handler ->
            {
                if (handler.succeeded())
                {
                    promise.complete();
                }

                else
                {
                    logger.error(handler.cause().getMessage());

                    promise.fail(handler.cause().getMessage());
                }
            });

        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());

            promise.fail(exception.getMessage());

        }
    }
}
