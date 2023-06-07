package com.nms.lite.model;

import io.vertx.core.json.JsonObject;
import com.nms.lite.utility.Constant;

import java.io.Serializable;

public class Credentials implements Serializable
{
    private final long id;
    private final String name;
    private String username;
    private String password;
    private int counter;

    public Credentials(long id, String name, String username, String password)
    {
        this.id = id;

        this.name = name;

        this.username = username;

        this.password = password;

        counter = 0;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public int getCounter()
    {
        return counter;
    }

    public void incrementCounter()
    {
        counter++;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();

        return object.put(Constant.CREDENTIALS_ID, getId()).put(Constant.CREDENTIALS_NAME, getName()).put(Constant.USERNAME, getUsername()).put(Constant.PASSWORD, getPassword()).put(Constant.CREDENTIAL_COUNTER,getCounter());
    }
}
