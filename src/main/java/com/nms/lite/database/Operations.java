package com.nms.lite.database;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public interface Operations
{
    void create(JsonObject data);

    JsonObject read(long id);

    JsonArray readAll();

    void update(JsonObject data);

    void delete(long id);
}
