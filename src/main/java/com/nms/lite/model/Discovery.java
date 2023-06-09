package com.nms.lite.model;

import io.vertx.core.json.JsonObject;
import com.nms.lite.utility.Constant;

public class Discovery
{

    private long id;
    private String name;
    private String ip;
    private int port;
    private long credentialsId;
    private boolean discovered;

    public Discovery()
    {

    }

    public Discovery(long id, String name, String ip, int port, long credentialsId)
    {

        this.id = id;

        this.name = name;

        this.ip = ip;

        this.port = port;

        this.credentialsId = credentialsId;

        this.discovered = false;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public long getCredentialsId()
    {
        return credentialsId;
    }

    public boolean getDiscovered()
    {
        return discovered;
    }

    public void setDiscovered(boolean discovered)
    {
        this.discovered = discovered;
    }

    public void setCredentialsId(long credentialsId)
    {
        this.credentialsId = credentialsId;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();

        return object.put(Constant.ID, getId()).put(Constant.NAME, getName()).put(Constant.IP_ADDRESS, getIp()).put(Constant.PORT_NUMBER, getPort()).put(Constant.CREDENTIALS_ID, getCredentialsId()).put(Constant.DISCOVERED, getDiscovered());
    }
}
