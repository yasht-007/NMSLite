package com.nms.lite.service;

import com.nms.lite.api.Credentials;
import com.nms.lite.api.Discovery;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import com.nms.lite.utility.Constant;

public class ApiService extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        final Vertx vertx = getVertx();

        Router router = Router.router(vertx);

        Router credentialRouter = Router.router(vertx);

        Router discoveryRouter = Router.router(vertx);

        router.route(Constant.MAIN_CREDENTIAL_ROUTE).subRouter(credentialRouter);

        router.route(Constant.MAIN_DISCOVERY_ROUTE).subRouter(discoveryRouter);

        Credentials credentials = new Credentials(credentialRouter);

        credentials.handleCredentialRoutes();

        Discovery discovery = new Discovery(discoveryRouter);

        discovery.handleDiscoveryRoutes();

        vertx.createHttpServer().requestHandler(router).listen(Constant.PORT).onComplete(handler ->
        {
            if (handler.succeeded())
            {
                System.out.println(Constant.SERVER_LISTEN_SUCCESS + Constant.PORT);
            }
            else
            {
                System.out.println(Constant.SERVER_LISTEN_FAILURE + Constant.PORT);
            }
        });

        promise.complete();

    }
}
