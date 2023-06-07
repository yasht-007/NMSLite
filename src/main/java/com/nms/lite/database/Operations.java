package com.nms.lite.database;

import java.util.List;

public interface Operations<T>
{
    void create(T data);
    T read(long id);
    List<T> readAll();
    void update(T data);
    boolean delete(long id);
}
