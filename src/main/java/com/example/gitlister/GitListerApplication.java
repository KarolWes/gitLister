package com.example.gitlister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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
    @GetMapping(value = "/{name}")
    public String getUser(@RequestHeader Map<String, String> header, @PathVariable("name") String name){
        header.put("Authorization", "Bearer ");
        final String uri = "https://api.github.com/users/"+name+"/repos";
        RestTemplate rt = new RestTemplate();
        String result_str = rt.getForObject(uri, String.class);
        JSONArray result = new JSONArray(result_str);
        System.out.println("OK");
        if (result.isEmpty())
        {
            return """
                    {
                        “status”: 404
                        “Message”: Requested user does not exist
                    }""";
        }

        var repositories = parseUser(header, result, rt);
        return buildAnswer(repositories).toString();
    }

    private JSONArray buildAnswer(List<Repo> repositories) {
        StringBuilder ans = new StringBuilder();
        boolean first = true;
        ans.append("[\n");
        for(var repo: repositories){
            if (!first){
                ans.append(",\n");

            }
            first = false;
            ans.append("{\n");
            ans.append("\"name\": ");
            ans.append(repo.name);
            ans.append(",\n");
            ans.append("\"user\": ");
            ans.append(repo.login);
            ans.append(",\n");
            ans.append("\"branches\": [\n");
            boolean inner_first = true;
            for(int i = 0; i < repo.branches.size(); i++){
                var br = repo.branches.get(i);
                if(!inner_first){
                    ans.append(",");
                }
                inner_first = false;
                ans.append("{\n\"name\": ");
                ans.append(br.name);
                ans.append(",\n");
                ans.append("\"sha\": ");
                ans.append(br.SHA);
                ans.append("}\n");
            }
            ans.append("]\n}");
        }
        ans.append("\n]");
        System.out.println(ans);
        return new JSONArray(ans.toString());
    }

    @GetMapping(value = "/")
    public String getDefault(){
        return "To run analysis, put username in the search bar";
    }
    public List<Repo> parseUser(@RequestHeader Map<String, String> header, JSONArray data, RestTemplate rt){
        header.put("Authorization", "Bearer");
        List<Repo> output = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.getJSONObject(i);
            boolean fork = entry.getBoolean("fork");
            if(!fork){
                String name = entry.getString("name");
                String user = entry.getJSONObject("owner").getString("login");
                Repo repository = new Repo(name, user);
                System.out.println(name);
                String uri = "https://api.github.com/repos/"+user+"/"+name+"/branches";
                String result_str = rt.getForObject(uri, String.class);
                JSONArray branches_arr = new JSONArray(result_str);
                for(int j = 0; j < branches_arr.length(); j++){
                    JSONObject branch = branches_arr.getJSONObject(j);
                    String branch_name = branch.getString("name");
                    String SHA = branch.getJSONObject("commit").getString("sha");
                    Branch br = new Branch(branch_name, SHA);
                    repository.branches.add(br);
                }
                output.add(repository);
            }
        }
        return output;
    }
}
