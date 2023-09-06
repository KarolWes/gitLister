package com.example.gitlister;

import java.util.ArrayList;
import java.util.List;

public class Repo {
    String name;
    String login;
    List<Branch> branches;

    public Repo(String name, String login) {
        this.name = name;
        this.login = login;
        this.branches = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }
}
