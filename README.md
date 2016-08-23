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

Note : All of the above jars are included in the project

## Installation
Include all the dependencies (present inside lib)

## Usage
 - Initialize the UserSingleton instance at the start of your app proceeded by calling login function with user login credentials. 
 - From there on the same UserSingleton class can be used to interact with Hirewand HTTP APIs.
<br />
<br />
 #### Constructor <br />
      UserSingleton get()
      Constructs object of UserSingleton    

 #### Login <br />
    void login(String email, String password) throws HttpException, IOException
    Creates HTTP connection with Hirewand. Gets authentication key which remains active for 8 hours of inactivity

 #### Send resume for parsing<br />
    String call(String function, HashMap params) throws HttpException, IOException <br />
    <br />

    Makes call to Hirewand and return (String) response from the server <br />
    Parameters : <br />
      function : type of call to be made. <br />
                1. upload : for uploading resume into hirewand <br />
                <br />
      params : HashMap<k,v> of parameters required for the call <br />
               Parameters mandatory for resume upload : <br />
                1. filename : name of the file uploading <br />
                2. resume : Stream of the file <br />
    <br />
 #### Get profiles <br />
    List call_list(String function, HashMap params) throws Exception <br /> 
    
    Makes call to Hirewand and return (List) response from the server <br />
    Parameters : <br />
      	function : type of call to be made. <br />
		1. profiles : for receiving profiles of the uploaded resumes <br />
		<br />
      	params : HashMap<k,v> of parameters required for the call <br />
               	Parameters mandatory for resume upload : <br />
		<br />
                1. size : (Integer) number of profiles (1-100) <br />
                2. since : (Long) time in milliseconds, returns the profiles created after this time <br />


## Example

  ```
  HashMap paramMap = new HashMap();
		
	UserSingleton user = UserSingleton.get(); // getting instance of User class
	/* ------- Log into Hirewand as user -------*/
	user.login("your email id","your password");

	/* ------- Set a callback for all the request to hirewand -------*/
	user.setCallback("Publically accessible callback url"); // if you have different callback for every resume, callback can be sent in paramMap to call function,
															// example paramMap.put("callback","Publically accessible callback url");
	
	File file = new File("Path to resume"); // file object of resume
	InputStream stream = new FileInputStream(file); // stream of resume
	
	/* ------- Create a HashMap with all the parameters to be send i.e Resume's filename, Stream of resume & callback (if required)*/
	paramMap.put("filename","filename with extension (like abc.doc)");
	paramMap.put("resume",stream);
	
	/* ------- Upload resume to hirewand -------*/
	String resp = user.call("upload",paramMap);
	
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
	List profiles = user.call_list("profiles", profilesParamMap);
	for(Object profile : profiles){ //iterating over the result set
		System.out.println(profile);
	}
	/* UpdateDateMS inside each profile can be used to get next batch of profiles */

  ```

## Profile json
Profile json structure can be viewed at https://docs.google.com/spreadsheets/d/1kE3ygWLt4Xe0uUELXxwV7NbdbLnQxhjVbo9JgiYNVJQ



