package com.nms.lite.database;

import io.vertx.core.MultiMap;
import java.util.*;

public class ProvisionStore
{
    private static ProvisionStore provisionInstance = null;
    private final MultiMap provisionDb = MultiMap.caseInsensitiveMultiMap();
    private final List<String> provisionList = new ArrayList<>();

    private ProvisionStore()
    {

    }

    public static ProvisionStore getInstance()
    {
        if (provisionInstance == null)
        {
            provisionInstance = new ProvisionStore();
        }

        return provisionInstance;
    }

    public synchronized void create(String ip, String provisionId, String credentialsId, String provisionData)
    {
        provisionDb.add(provisionId, provisionData);

        provisionDb.add(provisionId, credentialsId);

        provisionDb.add(provisionId, ip);

        provisionList.add(ip);
    }

    public synchronized List<String> read(String provisionId)
    {
        return provisionDb.getAll(provisionId);
    }

    public synchronized Set<String> readAll()
    {
        return provisionDb.names();
    }

    public synchronized boolean delete(String provisionId)
    {
        provisionList.remove(provisionDb.getAll(provisionId).get(2));

        provisionDb.remove(provisionId);

        return true;
    }

    public synchronized boolean containsIp(String ip)
    {
        return provisionList.contains(ip);
    }

}

