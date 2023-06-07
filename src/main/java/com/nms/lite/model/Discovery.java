package com.nms.lite.model;

import io.vertx.core.json.JsonObject;
import com.nms.lite.utility.Constant;

public class Discovery
{

    private final long id;
    private final String name;
    private String ip;
    private int port;
    private long credentialProfileId;
    private boolean discovered = false;

    public Discovery(long id, String name, String ip, int port, long credentialProfileId)
    {

        this.id = id;

        this.name = name;

        this.ip = ip;

        this.port = port;

        this.credentialProfileId = credentialProfileId;

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

    public long getCredentialProfileId()
    {
        return credentialProfileId;
    }

    public boolean getDiscovered()
    {
        return discovered;
    }

    public void setDiscovered(boolean discovered)
    {
        this.discovered = discovered;
    }

    public void setCredentialProfileId(long credentialProfileId)
    {
        this.credentialProfileId = credentialProfileId;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();

        return object.put(Constant.DISCOVERY_ID, getId()).put(Constant.DISCOVERY_NAME, getName()).put(Constant.IP_ADDRESS, getIp()).put(Constant.PORT_NUMBER, getPort()).put(Constant.CREDENTIALS_ID, getCredentialProfileId()).put(Constant.DISCOVERED,getDiscovered());
    }
}
