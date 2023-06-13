package com.nms.lite.database;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface Operations
{
    void create(JsonObject data);

    JsonObject read(long id);

    JsonArray readAll();

    void update(JsonObject data);

    boolean delete(long id);
}
