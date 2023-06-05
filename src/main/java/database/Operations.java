package database;

public interface Operations<T>
{
    void create(T data);
    T read(long id);
    void update(T data);
    boolean delete(long id);

}
