package com.nms.lite.utility;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;

import java.util.List;

import java.util.concurrent.TimeUnit;

public class BuildProcess
{
    public JsonObject build(List<String> command, long timeout)
    {
        BufferedReader inputReader = null;

        BufferedReader errorReader = null;

        Process process = null;

        JsonObject result = new JsonObject();

        try
        {

            var processBuilder = new ProcessBuilder(command);

            process = processBuilder.start();

            boolean completed = process.waitFor(timeout, TimeUnit.MILLISECONDS);

            if (!completed)
            {
                process.destroyForcibly();
            }

            int exitCode = process.waitFor();

            if (exitCode != Constant.PROCESS_ABNORMAL_TERMINATION_CODE)
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

                result.put(Constant.STATUS_CODE, Constant.STATUS_CODE_OK);

                result.put(Constant.PROCESS_STATUS, Constant.PROCESS_NORMAL);

                result.put(Constant.STATUS_RESULT, output.toString());

                result.put(Constant.STATUS_ERROR, errorOutput.toString());

            }

            else
            {
                result.put(Constant.STATUS, Constant.STATUS_FAIL);

                result.put(Constant.PROCESS_STATUS, Constant.PROCESS_ABNORMAL);

                result.put(Constant.STATUS_RESULT, Constant.EMPTY_STRING);

                result.put(Constant.STATUS_MESSAGE, Constant.PROCESS_ABNORMALLY_TERMINATED);

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
                exception.printStackTrace();
            }
        }

        return result;
    }
}
