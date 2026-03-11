package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class User {
    protected Long id;
    protected String username;
    protected String email;
    protected String password;
    protected List<User> friends = new ArrayList<User>();
    protected String imagePath;

    public User(Long id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void login(){
        System.out.println(username+" s-a conectat la retea.");
    }
    public void logout(){
        System.out.println(username+" s-a delogat din retea.");
    }
    public void sendMessage(User receiver, String message){
        System.out.println(username + " a trimis mesaj către " + receiver.username + ": " + message);
    }
    public void receiveMessage(String sender, String message){
        System.out.println(username + " a primit mesaj de la " + sender + ": " + message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        // Verificăm ID-ul și Clasa (ca să nu fie Duck(1) == Persoana(1))
        return Objects.equals(getId(), user.getId()) &&
                Objects.equals(this.getClass(), user.getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), this.getClass());
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
