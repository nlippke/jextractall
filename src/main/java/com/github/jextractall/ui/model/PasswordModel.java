package com.github.jextractall.ui.model;

public class PasswordModel {
    private String password;
    private boolean remember;
    
    public PasswordModel(String password, boolean remember) {
        this.password = password;
        this.remember = remember;
    }
    
    public String getPassword() {
        return password;
    }
    
    public boolean remember() {
        return remember;
    }
}
