package com.fromtushar.soulmessenger;

public class Users {

    String mail;
    String userName;
    String password;
    String profilepic;
    String status;
    String userId;

    public Users() {}

    public Users(String userId, String userName, String mail,
                 String password, String profilepic, String status) {
        this.userId = userId;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.profilepic = profilepic;
        this.status = status;
    }

    public String getMail() { return mail; }
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getProfilepic() { return profilepic; }
    public String getStatus() { return status; }

    public void setUserId(String id) { this.userId = id; }
    public void setUserName(String name) { this.userName = name; }
    public void setMail(String email) { this.mail = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(String status) { this.status = status; }
    public void setProfilepic(String imageUrl) { this.profilepic = imageUrl; }
}
