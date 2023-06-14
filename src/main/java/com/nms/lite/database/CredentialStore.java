package com.nms.lite.database;

import static com.nms.lite.utility.Constant.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.ConcurrentHashMap;

public class CredentialStore implements Operations
{
    private static CredentialStore credentialInstance = null;
    private final ConcurrentHashMap<Long, JsonObject> credentialDb = new ConcurrentHashMap<>();

    private CredentialStore()
    {

    }

    public static CredentialStore getInstance()
    {
        if (credentialInstance == null)
        {
            credentialInstance = new CredentialStore();
        }

        return credentialInstance;
    }

    @Override
    public void create(JsonObject data)
    {
        credentialDb.put(data.getLong(ID), data);
    }

    @Override
    public JsonObject read(long id)
    {
        return credentialDb.get(id);
    }

    @Override
    public JsonArray readAll()
    {
        return new JsonArray(credentialDb.values().stream().toList());
    }

    @Override
    public void update(JsonObject data)
    {
        credentialDb.put(data.getLong(ID), data);
    }

    @Override
    public void delete(long id)
    {
        credentialDb.remove(id);

    }
}
