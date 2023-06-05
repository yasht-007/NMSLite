package model;

public class Credentials
{
    private final long id;
    private final String name;
    private String username;
    private String password;

    public Credentials(long id, String name, String username, String password)
    {
        this.id = id;

        this.name = name;

        this.username = username;

        this.password = password;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "Credentials{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
