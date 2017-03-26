# Hirewand for Java
HireWand is a platform for parsing resumes and screening/shortlisting profiles for a given requirement. More details is found at www.hirewand.com. The APIs are available at api.hirewand.com. This project is the java wrapper over the HTTP APIs. The goal of this project is to make the use of the APIs very simple.

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

Note : All of the above jars are included in /lib folder in the project

## Installation
Include all the dependencies (present inside lib)

## Usage
- Initialize the HWSingleton instance at the start of your application by calling login function with user login credentials.
- Use HWSingleton singleton instance to interact with Hirewand. 
<br />
<br />

#### Constructor <br />
HWSingleton get()
Get the signleton instance of HWSingleton   

#### Login <br />
```java 
void login(String email, String password) throws HWHTTPException <br />
```
Creates a connection with Hirewand. This needs to be done only at the start of your application.<br/>

#### Pushing resume to HireWand (for parsing and indexing)<br />

__Function to call:__<br />

```java 
String call(String function, HashMap params) throws InvalidRequestException, HWHTTPException<br/></sub>
```

This function is used to make any call to HireWand supported functions, the below example is for upload of a resume, for indexing and parsing.<br />

__Parameters for Upload:__<br />

function: "upload"  (Calls the HireWand supported function to upload the resume)<br />
params: {filename:<name of the file being uploaded, with extension>, resume: <binary stream of the resume>}<br />
HashMap of parameters required for the call, in this case for "upload" function.<br />

__Returns:__<br /> 

json with the person id that needs to be stored for future reference to this profile in HireWand.<br />
The structure of this json is documented at https://docs.google.com/spreadsheets/d/1kE3ygWLt4Xe0uUELXxwV7NbdbLnQxhjVbo9JgiYNVJQ/edit?pref=2&pli=1#gid=1056523406<br />

#### Fetching the parsed profiles: <br />

__Function to call:__<br />

```java 
String call_list(String function, HashMap params) throws InvalidRequestException, HWHTTPException;
```

This function is used to make any call to HireWand supported functions. There the function returns a list of objects.<br />

__Parameters to get the latest profiles parsed:__<br />

function: "profiles"<br />
Calls the HireWand supported function to get the latest profiles parsed.<br />
params: {size:<number of profiles to return, takes values between 1-100>, since: <Long value, time in milliseconds, returns profiles created after this time>}<br />
HashMap of parameters required for the call, in this case for "upload" function.<br />

__Returns:__<br /> 

List of profile objects. <br />
A profile object is a map with the structure documented at https://docs.google.com/spreadsheets/d/1kE3ygWLt4Xe0uUELXxwV7NbdbLnQxhjVbo9JgiYNVJQ/edit?pref=2&pli=1#gid=0<br />

### Exception handling : 

Types of exceptions:
1. HWHTTPException
2. InvalidRequestException

Function to call:
String getMessage()
   Returns the cause of this exception<br />

int getCode()
   Returns the status code for this exception

Exception status codes :	
1. 401 : No internet connect
   - Check your internet connection and try again
2. 401 : Login failure
   - Check your login credentials and try again
3. 500 : Invalid response from Hirewand
   - Try again in sometime
4. 503 : Connection refused
   - Try again in sometime
5. 701 : Call parameter missing
   - Check the values in hashmap of params you are passing to the functions
6. 702 : Invalid call parameter
   - Check the data type of params you are passing to the functions

## Example

```
HashMap paramMap = new HashMap();
try{
	HWSingleton hw = HWSingleton.get(); // getting instance of User class
	/* ------- Log into Hirewand as user -------*/
	hw.login("your email id","your password");

	/* ------- Optional: Set a callback for all the request to hirewand -------*/
	hw.setCallback("Publically accessible callback url"); // if you have different callback for every resume, callback can be sent in paramMap to call function,
															// example paramMap.put("callback","Publically accessible callback url");

	File file = new File("Path to resume"); // file object of resume
	InputStream stream = new FileInputStream(file); // stream of resume

	/* ------- Create a HashMap with all the parameters to be send i.e Resume's filename, Stream of resume & callback (if required)*/
	paramMap.put("filename","filename with extension (like abc.doc)");
	paramMap.put("resume",stream);

	/* ------- Upload resume to hirewand -------*/
	String resp = hw.call("upload",paramMap);

	/* ------- Print the response from hirewand -------*/
	System.out.println (new JSONParser().parse(resp)); // reading response received

	/* SAMPLE RESPONSE FROM HIREWAND ON SUCCESS
	 * {
	 *    status : 'success',
	 *    message : 'file uploaded successfully',
	 *    personid : '56adas6d5a4sda56das5d6' // unique hirewand id for resume (mapping to internal key is recommended)
	 * }		 
	 * 
	 * SAMPLE RESPONSE FROM HIREWAND ON FAILURE
	 * {
	 *    status : 'fail',
	 *    message : Reason for failure,
	 * }	
	 * */

	/* ------- Get profiles list --------*/
		HashMap profilesParamMap = new HashMap();
		profilesParamMap.put("size", 50);
		profilesParamMap.put("since", 1456830717016L); // adding UpdateDateMS of the last profile received
		List profiles = hw.call_list("profiles", profilesParamMap);
		for(Object profile : profiles){ //iterating over the result set
			System.out.println(profile);
		}


	/* UpdateDateMS inside each profile can be used to get next batch of profiles */
}
catch(InvalidRequestException e){
	System.out.println(e.getMessage());
	System.out.println(e.getCode());
}
catch(HWHTTPException e){
	System.out.println(e.getMessage());
	System.out.println(e.getCode());
}
catch(Exception e){
	System.out.print("Exception");
	e.printStackTrace();
}
```

## Profile json
Profile json structure can be viewed at https://docs.google.com/spreadsheets/d/1kE3ygWLt4Xe0uUELXxwV7NbdbLnQxhjVbo9JgiYNVJQ/edit?pref=2&pli=1#gid=0



