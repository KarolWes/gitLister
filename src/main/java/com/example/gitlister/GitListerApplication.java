package com.example.gitlister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import org.json.*;


@SpringBootApplication
public class GitListerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitListerApplication.class, args);
    }
}

@RestController
class GitListerController{
    // Put you authentication key here
    private static final String auth = "Bearer ";
    private static final boolean addAuth = false;

    @GetMapping("/")
    public String getDefault() {
        // default page
        return "Hello. To analyse, type username in the search bar";
    }

    @GetMapping(value = "/{name}")
    public Object getUser(@PathVariable("name") String name){
        // main functionality
        var result_str = fetchData("https://api.github.com/users/"+name+"/repos");
        if(result_str.getHeaders().getContentType() == MediaType.APPLICATION_XML){
            // if header is Aplication/XML (could be changed to anything but json)
            return handleError(406, "Wrong header type").toString();
        }
        // convert fetched data to JSON Array
        JSONArray result = new JSONArray(result_str.getBody());
        if (result.isEmpty())
        {
            // if returned value is an empty array (i.e. use not exist)
            return handleError(404, "Requested user does not exist").toString();
        }
        // if everything is correct, parse and return nice answer
        var ans = buildAnswer(parseUser(result));
        return ans.toString();
    }

    private ResponseEntity<String> fetchData(String uri){
        // authentication header
        HttpHeaders header = new HttpHeaders();
        if(addAuth){
            header.set("Authorization", auth);
        }
        HttpEntity<Void> he = new HttpEntity<>(header);
        // fetching data from github api
        RestTemplate rt = new RestTemplate();
        return rt.exchange(uri, HttpMethod.GET, he, String.class);
    }
    private JSONObject handleError(int code, String message){
        JSONObject ans_obj = new JSONObject();
        ans_obj.put("status", code);
        ans_obj.put("message", message);
//            JSONArray ans = new JSONArray();
//            ans.put(ans_obj);
        return ans_obj;
    }

    private JSONArray buildAnswer(List<Repo> repositories) {
        // function takes list of my own objects and coverts it to JSON
        JSONArray ans = new JSONArray();
        for(var repo: repositories) {
            JSONObject r = new JSONObject();
            r.put("name", repo.getName());
            r.put("user", repo.getLogin());
            // branches
            JSONArray branches = new JSONArray();
            for(var branch: repo.getBranches()) {
                JSONObject b = new JSONObject();
                b.put("name", branch.getName());
                b.put("sha", branch.getSHA());
                branches.put(b);
            }
            r.put("branches", branches);
            ans.put(r);
        }
        return ans;
    }
    public List<Repo> parseUser(JSONArray data){
        // takes users form JSON array, adds info about repositories
        List<Repo> output = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.getJSONObject(i);
            boolean fork = entry.getBoolean("fork");
            if(!fork){
                //if is not forked from other user
                String name = entry.getString("name");
                String user = entry.getJSONObject("owner").getString("login");
                Repo repository = new Repo(name, user);
                System.out.println(name);
                // fetch branches
                // headers
                var result_str = fetchData("https://api.github.com/repos/"+user+"/"+name+"/branches");
                JSONArray branches_arr = new JSONArray(result_str.getBody());
                for(int j = 0; j < branches_arr.length(); j++){
                    //parse
                    JSONObject branch = branches_arr.getJSONObject(j);
                    String branch_name = branch.getString("name");
                    String SHA = branch.getJSONObject("commit").getString("sha");
                    Branch br = new Branch(branch_name, SHA);
                    repository.getBranches().add(br);
                }
                output.add(repository);
            }
        }
        return output;
    }
}
