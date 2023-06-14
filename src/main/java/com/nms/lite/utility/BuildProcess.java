package com.nms.lite.utility;

import com.nms.lite.Bootstrap;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.Base64;
import java.util.List;

import static com.nms.lite.utility.Constant.*;

import java.util.concurrent.TimeUnit;

public class BuildProcess
{
    private static final Logger logger = LoggerFactory.getLogger(BuildProcess.class);

    public static Future<JsonObject> build(List<String> command, long timeout, boolean base64Encoded)
    {
        Promise<JsonObject> promise = Promise.promise();

        var processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        Bootstrap.vertx.executeBlocking(task ->
        {
            BufferedReader inputReader = null;

            Process process = null;

            try
            {
                process = processBuilder.start();

                if (!process.waitFor(timeout, TimeUnit.MILLISECONDS))
                {
                    process.destroyForcibly();
                }

                if (process.waitFor() != PROCESS_ABNORMAL_TERMINATION_CODE)
                {
                    inputReader = process.inputReader();

                    StringBuilder output = new StringBuilder();

                    String read;

                    while ((read = inputReader.readLine()) != null)
                    {
                        output.append(read).append(NEW_LINE);
                    }

                    if (base64Encoded)
                    {
                        Base64.Decoder decoder = Base64.getMimeDecoder();

                        if (output.toString().length() > 0)
                        {
                            output = new StringBuilder(new String(decoder.decode(output.toString())));
                        }
                    }

                    task.complete(new JsonObject().put(STATUS, STATUS_SUCCESS).put(PROCESS_STATUS, PROCESS_NORMAL).put(STATUS_RESULT, output.toString()));
                }

                else
                {
                    task.fail(new JsonObject().put(STATUS, STATUS_FAIL).put(PROCESS_STATUS, PROCESS_ABNORMAL).put(STATUS_MESSAGE, PROCESS_ABNORMALLY_TERMINATED).encode());
                }

            }

            catch (Exception exception)
            {
                logger.error(exception.getMessage());

                task.fail(exception.getMessage());
            }

            finally
            {
                try
                {
                    if (inputReader != null)
                    {
                        inputReader.close();
                    }

                    if (process != null && process.isAlive())
                    {
                        process.destroyForcibly();
                    }
                }

                catch (Exception exception)
                {
                    logger.error(exception.getMessage());
                }
            }

        }, handler ->
        {
            if (handler.succeeded())
            {
                promise.complete((JsonObject) handler.result());
            }

            else
            {
                promise.complete(new JsonObject(handler.cause().getMessage()));
            }
        });


        return promise.future();
    }
}