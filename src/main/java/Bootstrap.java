import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import service.DatabaseService;
import service.ListenerService;
import utility.Constant;

public class Bootstrap extends AbstractVerticle
{
    public static void main(String[] args)
    {
        var vertx = Vertx.vertx();

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
        vertx.deployVerticle(ListenerService.class.getName())

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
}
