package com.nms.lite.database;

import com.nms.lite.model.Credentials;

import java.util.concurrent.ConcurrentHashMap;

public class CredentialDb implements Operations<Credentials>
{
    private static CredentialDb credentialInstance = null;
    private final ConcurrentHashMap<Long, Credentials> credentialDb = new ConcurrentHashMap<>();

    private CredentialDb()
    {

    }

    public static CredentialDb getInstance()
    {
        if (credentialInstance == null)
        {
            credentialInstance = new CredentialDb();
        }

        return credentialInstance;
    }

    @Override
    public void create(Credentials data)
    {
        credentialDb.put(data.getId(), data);
    }

    @Override
    public Credentials read(long id)
    {
        return credentialDb.get(id);
    }

    @Override
    public Credentials[] readAll()
    {
        return null;
    }

    @Override
    public void update(Credentials data)
    {
        credentialDb.put(data.getId(),data);
    }

    @Override
    public boolean delete(long id)
    {
        return false;
    }
}
