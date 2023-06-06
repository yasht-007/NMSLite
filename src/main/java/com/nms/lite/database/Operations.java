package com.nms.lite.database;

public interface Operations<T>
{
    void create(T data);
    T read(long id);
    T[] readAll();
    void update(T data);
    boolean delete(long id);
}
