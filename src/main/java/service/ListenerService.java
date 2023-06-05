package service;

import api.Credentials;
import api.Discovery;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import utility.Constant;

public class ListenerService extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        final Vertx vertx = getVertx();

        final EventBus eventBus = vertx.eventBus();

        Router router = Router.router(vertx);

        Router credentialRouter = Router.router(vertx);

        Router discoveryRouter = Router.router(vertx);

        router.route(Constant.MAIN_CREDENTIAL_ROUTE).subRouter(credentialRouter);

        router.route(Constant.MAIN_DISCOVERY_ROUTE).subRouter(discoveryRouter);

        handleCredentialRoutes(credentialRouter,eventBus);

        handleDiscoveryRoutes(discoveryRouter,eventBus);

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

    private void handleCredentialRoutes(Router credentialRouter, EventBus eventBus)
    {
        credentialRouter.route().method(HttpMethod.POST).method(HttpMethod.PUT).handler(BodyHandler.create());

        credentialRouter.post(Constant.CREATE_ROUTE).handler(routingContext ->
        {
            new Credentials(routingContext, eventBus).create();
        });

        credentialRouter.get(Constant.READ_ROUTE).handler(routingContext ->
        {
            new Credentials(routingContext, eventBus).read();
        });

        credentialRouter.get(Constant.READ_ALL_ROUTE).handler(routingContext ->
        {
            new Credentials(routingContext,eventBus).readAll();
        });

        credentialRouter.put(Constant.UPDATE_ROUTE).handler(routingContext ->
        {
            new Credentials(routingContext,eventBus).update();
        });

        credentialRouter.delete(Constant.DELETE_ROUTE).handler(routingContext ->
        {
            new Credentials(routingContext,eventBus).delete();
        });
    }

    private void handleDiscoveryRoutes(Router discoveryRouter, EventBus eventBus)
    {
        discoveryRouter.route().method(HttpMethod.POST).method(HttpMethod.PUT).handler(BodyHandler.create());

        discoveryRouter.post(Constant.CREATE_ROUTE).handler(routingContext ->
        {
            new Discovery(routingContext).create();
        });

        discoveryRouter.get(Constant.READ_ROUTE).handler(routingContext ->
        {
            new Discovery(routingContext).read();
        });

        discoveryRouter.get(Constant.READ_ALL_ROUTE).handler(routingContext ->
        {
            new Discovery(routingContext).readAll();
        });

        discoveryRouter.put(Constant.UPDATE_ROUTE).handler(routingContext ->
        {
            new Discovery(routingContext).update();
        });

        discoveryRouter.delete(Constant.DELETE_ROUTE).handler(routingContext ->
        {
            new Discovery(routingContext).delete();
        });
    }
}
