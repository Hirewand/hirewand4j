# Hirewand for Java
A Java wrapper for Hirewand's HTTP APIs

## Requirements
You need to have a recent version of Java installed. 

## Dependencies
  - commons-httpclient-3.1.jar
	- commons-logging-1.1.3.jar
	- commons-codec-1.9.jar
	- commons-cli-1.2.jar
	- commons-collections-3.2.1.jar
	- commons-io-2.4.jar
	- commons-lang3-3.3.2.jar
	- commons-pool2-2.2.jar
	- json-simple-1.1.1.jar

Note : All of the above jars are included in the project

## Installation
Include all the dependencies (present inside lib)

## Usage
- Initialize the UserSingleton instance at the start of your app proceeded by calling login function with user login credentials. 
- From there on the same UserSingleton class can be used to send the resume by simply calling the call function.
- HireWand will callback the registered url.
- The response json can be parsed to get the profile details.

## Structured form on resume
Profile structure can be viewed at https://docs.google.com/spreadsheets/d/1kE3ygWLt4Xe0uUELXxwV7NbdbLnQxhjVbo9JgiYNVJQ



