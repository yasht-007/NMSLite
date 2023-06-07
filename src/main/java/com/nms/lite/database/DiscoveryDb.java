package com.nms.lite.database;

import com.nms.lite.model.Discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public List<Discovery> readAll()
    {
        return new ArrayList<>(discoveryDb.values());
    }
    @Override
    public void update(Discovery data)
    {
        discoveryDb.put(data.getId(),data);
    }

    @Override
    public boolean delete(long id)
    {
        discoveryDb.remove(id);

        return true;
    }
}
