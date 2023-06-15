package com.nms.lite.api;

import com.nms.lite.utility.Global;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nms.lite.utility.Constant.*;


public class Credentials extends AbstractApi
{
    private final Router router;
    private final Logger logger = LoggerFactory.getLogger(Credentials.class);

    public Credentials(Router router)
    {
        super(CREDENTIALS, CREDENTIALS_ID);

        this.router = router;
    }

    public void handleCredentialRoutes()
    {
        try
        {
            router.route().failureHandler(failureContext ->
            {
                logger.error(failureContext.failure().getMessage());

                Global.sendExceptionMessage(failureContext);

            });

            router.post(CREATE_ROUTE).handler(this::create);

            router.get(READ_CREDENTIAL_ROUTE).handler(this::read);

            router.get(READ_ALL_ROUTE).handler(this::readAll);

            router.put(UPDATE_ROUTE).handler(this::update);

            router.delete(DELETE_CREDENTIAL_ROUTE).handler(this::delete);
        }

        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }

}
