package com.nms.lite.database;

import com.nms.lite.model.Credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CredentialStore implements Operations<Credentials>
{
    private static CredentialStore credentialInstance = null;
    private final ConcurrentHashMap<Long, Credentials> credentialDb = new ConcurrentHashMap<>();

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
    public List<Credentials> readAll()
    {
        return new ArrayList<>(credentialDb.values());
    }

    @Override
    public void update(Credentials data)
    {
        credentialDb.put(data.getId(),data);
    }

    @Override
    public boolean delete(long id)
    {
        credentialDb.remove(id);

        return true;
    }
}
