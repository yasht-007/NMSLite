package service;

import database.CredentialDb;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import model.Credentials;
import utility.Constant;
import utility.KeyGen;

public class DatabaseService extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        EventBus eventBus = vertx.eventBus();

        eventBus.<JsonObject>localConsumer(Constant.CREATE_CREDENTIALS).handler(message ->
        {

            create(Constant.CREDENTIALS, message);

        });

        eventBus.<JsonObject>localConsumer(Constant.READ_CREDENTIALS).handler(message ->
        {

            var data = message.body();

//            read(Constant.CREDENTIALS, data,eventBus);

        });

        promise.complete();
    }

    private void create(String type, Message<JsonObject> message)
    {
        var data = message.body();

        vertx.executeBlocking(promise ->
        {

            switch (type)
            {
                case Constant.CREDENTIALS ->
                {

                    CredentialDb credentialDb = CredentialDb.getInstance();

                    long credId = KeyGen.getUniqueKeyForName(data.getString("credentialsName"));

                    if (credentialDb.read(credId) != null)
                    {
                        promise.fail(Constant.CREDENTIALS + Constant.DATA_ALREADY_EXISTS);
                    }

                    else
                    {
                        Credentials credentials = new Credentials(credId, data.getString("credentialsName"), data.getString("username"), data.getString("password"));

                        credentialDb.create(credentials);

                        promise.complete(Constant.CREDENTIALS + Constant.CREATE_SUCCESS);
                    }
                }

//                case Constant.DISCOVERY):
            }

        }, handler ->
        {
            if (handler.succeeded())
            {
                switch (handler.result().toString())
                {
                    case Constant.CREDENTIALS + Constant.CREATE_SUCCESS ->
                    {

                        JsonObject result = new JsonObject();

                        result.put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_SUCCESS);

                        result.put(Constant.HTTP_STATUS_MESSAGE, Constant.CREATE_SUCCESS);

                        message.reply(result);

                    }

                    case Constant.DISCOVERY + Constant.CREATE_SUCCESS ->
                    {

                    }
                }

            }

            else
            {
                switch (handler.cause().getMessage())
                {
                    case Constant.CREDENTIALS + Constant.DATA_ALREADY_EXISTS ->
                    {

                        JsonObject result = new JsonObject();

                        result.put(Constant.HTTP_STATUS, Constant.HTTP_STATUS_FAIL);

                        result.put(Constant.HTTP_STATUS_MESSAGE, handler.cause().getMessage());

                        message.reply(result);

                    }

                    case Constant.DISCOVERY + Constant.DATA_ALREADY_EXISTS ->
                    {

                    }
                }
            }
        });
    }

//    private void read(String type, JsonObject data)
//    {
//        switch (type)
//        {
//            case Constant.CREDENTIALS:
//                System.out.println();
//
//            case Constant.DISCOVERY):
//        }
//    }
}