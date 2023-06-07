package com.nms.lite;

import com.nms.lite.service.DiscoveryService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import com.nms.lite.service.DatabaseService;
import com.nms.lite.service.ApiService;
import com.nms.lite.utility.Constant;

public class Bootstrap extends AbstractVerticle
{
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args)
    {
        try
        {
            deployAllVerticles();
        }

        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static void deployAllVerticles()
    {
        vertx.deployVerticle(ApiService.class.getName())

                .compose(result -> vertx.deployVerticle(DatabaseService.class.getName()))

                .compose(result -> vertx.deployVerticle(DiscoveryService.class.getName(), new DeploymentOptions().setWorker(true)))

                .onComplete(handler ->
                {
                    if (handler.succeeded())
                    {
                        if (handler.succeeded())
                        {
                            System.out.println(Constant.VERTICAL_DEPLOYMENT_SUCCESS);
                        }

                    }

                    else
                    {
                        System.out.println(handler.cause().getMessage());
                    }
                });
    }

    public static EventBus getEventBus()
    {
        return vertx.eventBus();
    }
}
