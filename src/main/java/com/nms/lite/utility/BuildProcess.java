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

import java.util.concurrent.TimeUnit;

public class BuildProcess
{
    private static final Logger logger = LoggerFactory.getLogger(BuildProcess.class);

    public Future<JsonObject> build(List<String> command, long timeout, Vertx vertx)
    {
        Promise<JsonObject> promise = Promise.promise();

        var processBuilder = new ProcessBuilder(command);

        vertx.executeBlocking(task ->
        {

            BufferedReader inputReader = null;

            BufferedReader errorReader = null;

            Process process = null;

            try
            {
                process = processBuilder.start();

                boolean completed = process.waitFor(timeout, TimeUnit.MILLISECONDS);

                if (!completed)
                {
                    process.destroyForcibly();
                }

                int exitCode = process.waitFor();

                if (exitCode != Constant.PROCESS_ABNORMAL_TERMINATION_CODE)
                {
                    JsonObject result = new JsonObject();

                    inputReader = process.inputReader();

                    errorReader = process.errorReader();

                    StringBuilder output = new StringBuilder();

                    StringBuilder errorOutput = new StringBuilder();

                    String read;

                    while ((read = inputReader.readLine()) != null)
                    {
                        output.append(read).append(Constant.NEW_LINE);
                    }

                    while ((read = errorReader.readLine()) != null)
                    {
                        errorOutput.append(read).append(Constant.NEW_LINE);
                    }

                    if (!command.contains(Constant.FPING))
                    {
                        Base64.Decoder decoder = Base64.getMimeDecoder();

                        if (output.toString().length() > 0)
                        {
                            output = new StringBuilder(new String(decoder.decode(output.toString())));

                        }

                        if (errorOutput.toString().length() > 0)
                        {
                            errorOutput = new StringBuilder(new String(decoder.decode(errorOutput.toString())));
                        }

                    }

                    result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                    result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                    result.put(Constant.PROCESS_STATUS, Constant.PROCESS_NORMAL);

                    result.put(Constant.STATUS_RESULT, output.toString());

                    result.put(Constant.STATUS_ERROR, errorOutput.toString());

                    task.complete(result);

                }

                else
                {
                    JsonObject errorResult = new JsonObject();

                    errorResult.put(Constant.STATUS, Constant.STATUS_FAIL);

                    errorResult.put(Constant.PROCESS_STATUS, Constant.PROCESS_ABNORMAL);

                    errorResult.put(Constant.STATUS_RESULT, Constant.EMPTY_STRING);

                    errorResult.put(Constant.STATUS_MESSAGE, Constant.PROCESS_ABNORMALLY_TERMINATED);

                    task.fail(errorResult.encode());

                }

            }

            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            finally
            {
                try
                {
                    if (inputReader != null)
                    {
                        inputReader.close();
                    }

                    if (errorReader != null)
                    {
                        errorReader.close();
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