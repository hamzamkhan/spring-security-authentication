package com.hamzamustafakhan.authenticationapi.utils;

public enum Roles {
    ADMIN(1, "Admin"),
    USER(2, "User");
    private int id;
    private String label;

    Roles(int id, String label){
        this.id= id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getId() {
        return id;
    }
}
