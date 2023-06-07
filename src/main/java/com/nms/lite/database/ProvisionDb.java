package com.nms.lite.database;


import io.vertx.core.MultiMap;

import java.util.ArrayList;
import java.util.List;

public class ProvisionDb
{
    private static ProvisionDb provisionInstance = null;
    private final MultiMap provisionDb = MultiMap.caseInsensitiveMultiMap();
    private final List<String> provisionList = new ArrayList<>();

    private ProvisionDb()
    {

    }

    public static ProvisionDb getInstance()
    {
        if (provisionInstance == null)
        {
            provisionInstance = new ProvisionDb();
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

    public synchronized void readAll()
    {

    }

    public synchronized boolean delete(String provisionId)
    {
        provisionDb.remove(provisionId);

        return true;
    }

    public synchronized boolean containsIp(String ip)
    {
        return provisionList.contains(ip);
    }

}

