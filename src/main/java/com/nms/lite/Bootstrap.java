package com.nms.lite;

import com.nms.lite.engine.DiscoveryEngine;
import com.nms.lite.engine.PollingEngine;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import com.nms.lite.engine.DatabaseEngine;
import com.nms.lite.engine.ApiEngine;
import com.nms.lite.utility.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap extends AbstractVerticle
{
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args)
    {
        Logger logger = LoggerFactory.getLogger(Bootstrap.class);

        try
        {
            deployAllVerticles();
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

    public static void deployAllVerticles()
    {
        vertx.deployVerticle(ApiEngine.class.getName())

                .compose(result -> vertx.deployVerticle(DatabaseEngine.class.getName()))

                .compose(result -> vertx.deployVerticle(DiscoveryEngine.class.getName()))

                .compose(result -> vertx.deployVerticle(PollingEngine.class.getName()))

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

    public static Vertx getVertxInstance()
    {
        return vertx;
    }
}
