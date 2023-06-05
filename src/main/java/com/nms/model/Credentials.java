package com.nms.model;

import io.vertx.core.json.JsonObject;
import com.nms.utility.Constant;

public class Credentials
{
    private final long id;
    private final String name;
    private String username;
    private String password;

    public Credentials(long id, String name, String username, String password)
    {
        this.id = id;

        this.name = name;

        this.username = username;

        this.password = password;
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

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();

        return object.put(Constant.CREDENTIALS_ID, getId()).put(Constant.CREDENTIALS_NAME, getName()).put(Constant.USERNAME, getUsername()).put(Constant.PASSWORD, getPassword());
    }
}
