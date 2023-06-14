package com.nms.lite.database;

import static com.nms.lite.utility.Constant.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryStore implements Operations
{
    private static DiscoveryStore discoveryInstance = null;
    private final ConcurrentHashMap<Long, JsonObject> discoveryDb = new ConcurrentHashMap<>();

    private DiscoveryStore()
    {

    }

    public static DiscoveryStore getInstance()
    {
        if (discoveryInstance == null)
        {
            discoveryInstance = new DiscoveryStore();
        }

        return discoveryInstance;
    }

    @Override
    public void create(JsonObject data)
    {
        discoveryDb.put(data.getLong(ID), data);
    }

    @Override
    public JsonObject read(long id)
    {
        return discoveryDb.get(id);
    }

    @Override
    public JsonArray readAll()
    {
        return new JsonArray(discoveryDb.values().stream().toList());
    }

    @Override
    public void update(JsonObject data)
    {
        discoveryDb.put(data.getLong(ID), data);
    }

    @Override
    public void delete(long id)
    {
        discoveryDb.remove(id);

    }
}
