package com.nms.lite;

import com.nms.lite.engine.DiscoveryEngine;
import com.nms.lite.engine.PollingEngine;
import io.vertx.core.Vertx;
import com.nms.lite.engine.DatabaseEngine;
import com.nms.lite.engine.ApiEngine;

import static com.nms.lite.utility.Constant.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap
{
    public static final Vertx vertx = Vertx.vertx();
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args)
    {
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
                        logger.info(VERTICAL_DEPLOYMENT_SUCCESS);
                    }


                    else
                    {
                        logger.error(handler.cause().getMessage());
                    }
                });
    }
}
