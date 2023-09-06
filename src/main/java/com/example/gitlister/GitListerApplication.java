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
    private static final String auth = "Bearer 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";

    @GetMapping("/")
    public String getDefault() {
        return "Hello";
    }

    @GetMapping(value = "/{name}")
    public Object getUser(@PathVariable("name") String name){
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", auth);
        HttpEntity<Void> he = new HttpEntity<>(header);
        final String uri = "https://api.github.com/users/"+name+"/repos";
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> result_str = rt.exchange(uri, HttpMethod.GET, he, String.class);
        if(result_str.getHeaders().getContentType() == MediaType.APPLICATION_XML){
            JSONObject ans_obj = new JSONObject();
            ans_obj.put("status", 406);
            ans_obj.put("message", "Wrong header type");
            JSONArray ans = new JSONArray();
            ans.put(ans_obj);
            return ans.toString();
        }
        JSONArray result = new JSONArray(result_str.getBody());
        System.out.println("OK");
        if (result.isEmpty())
        {
            JSONObject ans_obj = new JSONObject();
            ans_obj.put("status", 404);
            ans_obj.put("message", "Requested user does not exist");
            JSONArray ans = new JSONArray();
            ans.put(ans_obj);
            return ans.toString();
        }
        var ans = buildAnswer(parseUser(result, rt));
        return ans.toString();
    }

    private JSONArray buildAnswer(List<Repo> repositories) {
        JSONArray ans = new JSONArray();
        for(var repo: repositories) {
            JSONObject r = new JSONObject();
            r.put("name", repo.getName());
            r.put("user", repo.getLogin());
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
    public List<Repo> parseUser(JSONArray data, RestTemplate rt){

        List<Repo> output = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.getJSONObject(i);
            boolean fork = entry.getBoolean("fork");
            if(!fork){
                String name = entry.getString("name");
                String user = entry.getJSONObject("owner").getString("login");
                Repo repository = new Repo(name, user);
                System.out.println(name);
                HttpHeaders header = new HttpHeaders();
                header.set("Authorization", auth);
                HttpEntity<Void> he = new HttpEntity<>(header);
                String uri = "https://api.github.com/repos/"+user+"/"+name+"/branches";
                ResponseEntity<String> result_str = rt.exchange(uri, HttpMethod.GET, he, String.class);
                JSONArray branches_arr = new JSONArray(result_str.getBody());
                for(int j = 0; j < branches_arr.length(); j++){
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
