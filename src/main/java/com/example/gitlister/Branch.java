package com.example.gitlister;

public class Branch {
    private String name;
    private String SHA;

    public Branch(String name, String SHA) {
        this.name = name;
        this.SHA = SHA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSHA() {
        return SHA;
    }

    public void setSHA(String SHA) {
        this.SHA = SHA;
    }
}
