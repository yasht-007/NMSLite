package com.nms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import com.nms.service.DatabaseService;
import com.nms.service.ApiService;
import com.nms.utility.Constant;

public class Bootstrap extends AbstractVerticle
{
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args)
    {

        vertx.deployVerticle(new Bootstrap()).onComplete(result ->
        {

            if (result.succeeded())
            {
                System.out.println(Constant.VERTICAL_DEPLOYMENT_SUCCESS);
            }

            else
            {
                System.out.println(result.cause().getMessage());
            }
        });
    }

    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        vertx.deployVerticle(ApiService.class.getName())

                .compose(result -> vertx.deployVerticle(DatabaseService.class.getName()))

                .onComplete(handler ->
                {
                    if (handler.succeeded())
                    {
                        promise.complete();
                    }

                    else
                    {
                        promise.fail(handler.cause().getMessage());
                    }
                });
    }

    public static EventBus getEventBus()
    {
        return vertx.eventBus();
    }
}
