# Git Lister
### Recruitment task for <i>Atipera</i>
## Introduction
The task was to create an API to fetch data from GitHub (via their API) based on the username and parse to show ony appropriate information. Those informations being:
1. Name of the repository
2. Login of the owner
3. List of all branches with:
   1. Name of the branch
   2. SHA of last commit.

As an additional requirement, fetched repositories were not to be forked from other users.

## Solution
App was designed using Java and Spring. Controller class is responsible for handling REST. Additional classes (Repo and Branch) are used as data placeholders. Class Application is a default Spring Boot app, handling everything engine related.

App supports to get methods: one without username, and one with. Empty request will return a welcome page, with short instruction. Filled request will return requested data, in JSON format, showed as String. 

Beside get requests, app uses four other functions:
* handleError, which takes code of an error and message and prepares error information for display
* fetchData, which takes uri of a request, builds a request for GitHub API and return fetched data
* parseUsers, which takes Array of fetched data, cleans it up and combines with the information about branches
* buildAnswer, which takes List of my internal objects, and creates JSON object based on it

Class Repo contains definition for repository, taking two arguments in the constructor: name and login, as well as creates internal List containing branches.

Class Branch contains definition for branch, taking two arguments in the constructor: name and SHA

Both inner classes features full getter and setter functionality.

## Requirements
* Coded using Java 17 on Amazon Coretto
* Spring Boot web in version 5
* Additional dependencies: json from org.json, version 20230618
* Coded on Jetbrains IntelliJ IDEA Ultimate 2023.2.1
* Under Windows 10
* Run on Xampp Tomcat

Coded by Karol Wesołowski
