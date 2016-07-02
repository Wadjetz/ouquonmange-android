package fr.oqom.ouquonmange.models;

public class SignUpUser {
    public String username;
    public String email;
    public String password;

    public SignUpUser(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
