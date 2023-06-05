package com.nms.database;

import com.nms.model.Discovery;

import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryDb implements Operations<Discovery>
{
    private static DiscoveryDb discoveryInstance = null;
    private final ConcurrentHashMap<Long, Discovery> discoveryDb = new ConcurrentHashMap<>();

    private DiscoveryDb() {

    }
    public static DiscoveryDb getInstance()
    {
        if (discoveryInstance == null)
        {
            discoveryInstance = new DiscoveryDb();
        }

        return discoveryInstance;
    }

    @Override
    public void create(Discovery data)
    {
        discoveryDb.put(data.getId(),data);
    }

    @Override
    public Discovery read(long id)
    {
        return discoveryDb.get(id);
    }

    @Override
    public void update(Discovery data)
    {

    }

    @Override
    public boolean delete(long id)
    {
        return false;
    }
}
