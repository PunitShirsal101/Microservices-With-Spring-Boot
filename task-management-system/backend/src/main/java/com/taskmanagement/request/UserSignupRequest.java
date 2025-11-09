package com.taskmanagement.request;

public class UserSignupRequest {
    private String email;
    private String password;
    private String fullName;
    private String mobile;
    private String role;

    public UserSignupRequest() {}

    public UserSignupRequest(String email, String password, String fullName, String mobile, String role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.mobile = mobile;
        this.role = role;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}