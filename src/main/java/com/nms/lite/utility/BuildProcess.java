package com.nms.lite.utility;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BuildProcess
{
    public JsonObject build(List<String> command, long timeout)
    {
        BufferedReader inputReader;

        BufferedReader errorReader;

        Process process;

        JsonObject result = new JsonObject();

        try
        {
            var processBuilder = new ProcessBuilder(command);

            process = processBuilder.start();

            if (process.waitFor(timeout, TimeUnit.SECONDS))
            {
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

                result.put(Constant.STATUS, Constant.STATUS_SUCCESS);

                result.put(Constant.PROCESS_STATUS, Constant.PROCESS_NORMAL);

                result.put(Constant.STATUS_RESULT, output.toString());

                result.put(Constant.STATUS_ERROR, errorOutput.toString());

            }

            else
            {
                result.put(Constant.STATUS, Constant.STATUS_FAIL);

                result.put(Constant.PROCESS_STATUS, Constant.PROCESS_ABNORMAL);

            }

        }

        catch (Exception exception)
        {
            result.put(Constant.STATUS, Constant.STATUS_FAIL);

            result.put(Constant.PROCESS_STATUS, Constant.EMPTY_STRING);

        }

        return result;
    }
}
