package org.hirewand;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserSingleton {

	static UserSingleton _this = null;
	static String emailaddress = null, userpassword = null, authkey = null, callbackurl = null;
	
	public JSONParser parser = new JSONParser(); // parses hirewand api response
	private HttpClient httpclient = new HttpClient();
	private PostMethod httppost = null;
	
	private String UPLOADURL = "http://www.hirewand.com/api/upload/", LOGINURL = "http://www.hirewand.com/api/signin/", PROFILEURL = "http://www.hirewand.com/api/getprofiles/";
	
	static UserSingleton get(){
		if(_this==null){ 
			_this = new UserSingleton();
		}
        return _this;
    }
	
	void login(String email, String password) throws HttpException, IOException{
		PostMethod httppost = new PostMethod(LOGINURL);
		try{
            NameValuePair[] data = {new NameValuePair("email", email),new NameValuePair("password", password)}; // login credentials
            httppost.setRequestBody(data);
            httpclient.executeMethod(httppost);
            int status = httppost.getStatusCode();
            if(status==200){
            	emailaddress = email;
            	userpassword = password;
            	InputStream responseStream = httppost.getResponseBodyAsStream();
            	String responseString = readResponseStream(responseStream);
				JSONObject resObj = convertToJSON(responseString);
				authkey = (String) resObj.get("authkey"); // authkey for the session, this key remains valid for 8 hrs of inactivity
            	System.out.println("Login successful");
            }
            else{
            	System.out.println("Login failed. Cause : "+status);
            }
        }
        catch(java.net.ConnectException e){
            System.out.println("Failed to Initialize. Connection refused");
        }
		catch(java.net.UnknownHostException e){
            System.out.println("Failed to Initialize. No internet connection");
        }
		catch(Exception e){
            System.out.println("Something went wrong. Please retry later");
        }
	}
	
	@SuppressWarnings("unchecked")
	String call(String function, HashMap params) throws HttpException, IOException{
		JSONObject jsonobj = new JSONObject();
		List<NameValuePair> paramlist = new LinkedList<NameValuePair>();
		InputStreamRequestEntity requestEntityStream = null;
		
		try{
			if(authkey==null){ 
				jsonobj.put("status","Fail");
				jsonobj.put("code","400");
				jsonobj.put("message","User not loggedin");
				return jsonobj.toJSONString();
			}
			else{
				switch(function){
					case "upload" : 
						if(params.get("filename")==null){
							jsonobj.put("status","Fail");
							jsonobj.put("code","400");
							jsonobj.put("message","Filename missing");
							return jsonobj.toJSONString();
						} 
						else if(params.get("resume")==null || params.get("resume") instanceof InputStream == false){
							jsonobj.put("status","Fail");
							jsonobj.put("code","400");
							jsonobj.put("message","File missing or invalid file");
							return jsonobj.toJSONString();
						}
						else{
							paramlist.add(new NameValuePair("filename", (String) params.get("filename")));
							if(params.get("callback")!=null) paramlist.add(new NameValuePair("callback", (String) params.get("callback")));
							else if(callbackurl!=null) paramlist.add(new NameValuePair("callback", (String) params.get("callbackurl")));
							requestEntityStream = (new InputStreamRequestEntity((InputStream) params.get("resume")));
							httppost = new PostMethod(UPLOADURL);
						}
						break; 
						
					case "profile":
						if(params.get("from") == null){
							jsonobj.put("status","Fail");
							jsonobj.put("code","400");
							jsonobj.put("message","from missing");
							return jsonobj.toJSONString();
						}
						else{
							paramlist.add(new NameValuePair("from", (String) params.get("from")));
							if(params.get("resSize")!=null) paramlist.add(new NameValuePair("resSize",(String) params.get("resSize")));
							if(params.get("since")!=null) paramlist.add(new NameValuePair("since",(String) params.get("since")));
							if(params.get("prettytype")!=null) paramlist.add(new NameValuePair("prettytype",(String) params.get("prettytype")));
							httppost = new PostMethod(PROFILEURL);
						}
						break;
				}
				if(requestEntityStream!=null) httppost.setRequestEntity((RequestEntity) requestEntityStream);
				paramlist.add(new NameValuePair("sessionless", "true"));
				paramlist.add(new NameValuePair("authkey", authkey));
				int i = 0;
				NameValuePair[] queryStringParameters = new NameValuePair[paramlist.size()];
				for(NameValuePair entry : paramlist){
					queryStringParameters[i++] = entry;
				}
				httppost.setQueryString(queryStringParameters);
				httpclient.executeMethod(httppost);
				int status = httppost.getStatusCode();
	            if(status==401){
	            	login(emailaddress,userpassword); // re-logging, authkey expired
	            	httpclient.executeMethod(httppost);
	            }
				return httppost.getResponseBodyAsString();
			}
		}
		finally{
			if(httppost!=null) httppost.releaseConnection();
		}
	}
	
	void setCallback(String callbackurl){
		this.callbackurl = callbackurl;
	}
	
	public String readResponseStream(InputStream responsestream) throws IOException{
		StringBuffer response = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(responsestream,"UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null){
            response.append(line).append(System.getProperty("line.separator"));
        }
        reader.close();
        return response.toString();
	}
	
	public JSONObject convertToJSON(String jsonstr) throws ParseException{
		return (JSONObject) parser.parse(jsonstr.trim());
	}
	
	public void printCallback(JSONObject requestJson) throws ParseException{
		for (Object key : requestJson.keySet()) {
	        String keyStr = (String)key;
	        Object keyvalue = requestJson.get(keyStr);

	        //Print key and value
	        System.out.println("key: "+ keyStr + " value: " + keyvalue);

	        //for nested objects iteration if required
	        if (keyvalue instanceof JSONObject)
	        	printCallback((JSONObject)keyvalue);
	    }
		
		
		/* SAMPLE JSON REQUEST ON CALLBACK
		 * {
		 *  	profile: structured form of the resume uploaded, 
		 *  	duplicate: true/false, // True, if the same resume was already present in the system 
		 *  	type: "newprofile",
		 *  	personid: personid, // unique hirewand id for resume (also returned in the response of successfull upload)
		 *  	accountid: accountid, // account id of the user
		 *  	message: "Profile created successfully"
		 * }		 
		 */
		
	}
	
	/*
	 * print particular fields in profile
	 * 
	 * arguments 
	 * 1. profilejson - Jsonarray of profile 
	 * 2. requiredfields - list of attributes to be printed 
	 * 
	 * */
	public void printProfile(List profileobject,List requiredfields){
		for (Object singleProfile : profileobject) {
			JSONObject prof = (JSONObject)singleProfile;
			for(Object singlefield : requiredfields){
				singlefield = (String)singlefield;
				//print the values
				//System.out.println(singlefield+" "+prof.get(singlefield));
			}
		}
	}
	
	//get latest profiles after a particular time
	/*
	 * get profile
	 * arguments 
	 * 1. since is time in milliseconds after which profile has to be retrieved
	 * 2. resSize is 100 or lesser 
	 * 
	 * save the last_res_date value for the next iteration (this is the resume_date_millis of the last resumes got)
	 * 
	 * returns List of profiles
	 * 
	 * */
	public List getProfiles(Long since,Long resSize){
		List profiles = new ArrayList();
		int totalsize = 0;
		
		//parameters for request
		HashMap paramMap = new HashMap();
		paramMap.put("resSize",String.valueOf(resSize));
		paramMap.put("since",String.valueOf(since));
		paramMap.put("from",String.valueOf(0));
		paramMap.put("prettytype","simple"); 

		int count = 0;
		do{
			try {
				//request hirewand for profiles
				String profilesbatch = call("profile",paramMap);
				
				//convert profile to jsonobject
				JSONObject profilejson = convertToJSON(profilesbatch);
				
				//take the profiles array
				JSONArray profilearray = (JSONArray) profilejson.get("result");
				totalsize = profilearray.size();
				count = count+totalsize;
				
				for (Object singleProfile : profilearray) {
					JSONObject prof = (JSONObject)singleProfile;
					paramMap.put("since",String.valueOf(prof.get("UpdateDateMS")));
					profiles.add(prof);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}while(totalsize == resSize);
		return profiles;
	}
	
	
	public static void main(String[] args) throws HttpException, IOException, ParseException {
		HashMap paramMap = new HashMap();
		
		UserSingleton user = UserSingleton.get(); // getting instance of User class
		/* ------- Log into Hirewand as user -------*/
		user.login("user email id","user password");

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
		
		
		/* ------- Callback from hirewand -------*/
		
		/* SAMPLE JSON REQUEST ON CALLBACK
		 * {
		 *  	profile: structured form of the resume uploaded, 
		 *  	duplicate: true/false, // True, if the same resume was already present in the system 
		 *  	type: "newprofile",
		 *  	personid: personid, // unique hirewand id for resume (also returned in the response of successfull upload)
		 *  	accountid: accountid, // account id of the user
		 *  	message: "Profile created successfully"
		 * }		 
		 */
		
		String sampleJsonString = "{"
				   + "\"profile\":{" 
				   + "  \"name\": [" 
				   + "    {" 
				   + "      \"v\": \"Candidate name\","
				   + "      \"c\" : \"confidence (quality of extraction), 0 to 100, integer\""
				   + "    }"  
				   + "  ],"
				   + "  \"emails\": [" 
				   + "    {" 
				   + "      \"v\": \"Candidate email address\","
				   + "      \"c\" : \"confidence (quality of extraction), 0 to 100, integer\""
				   + "    }"  
				   + "  ],"
				   + "  \"phone\": [" 
				   + "    {" 
				   + "      \"v\": \"Candidate phone number\","
				   + "      \"c\" : \"confidence (quality of extraction), 0 to 100, integer\""
				   + "    }"  
				   + "  ]" // 3 keys are given as an example, complete structure of profile is available in the documentation
				   + "},"
				   + "\"duplicate\": false,"
				   + "\"type\": \"newprofile\","
				   + "\"personid\": \"123456789\","
				   + "\"accountid\": \"123\","
				   + "\"message\": \"Profile created successfully\","
				   + "}"; 
		
		JSONObject sampleJson = user.convertToJSON(sampleJsonString);
		
		/* ------ Calling printCallback to print the json received ------*/
		//user.printCallback(sampleJson);
		
		/* ------ Get profiles list -------*/
		List profiles = user.getProfiles(Long.valueOf("0"),Long.valueOf("100"));
		
		/* ------ print profile attributes -------*/
		//user.printProfile(profile list,list of attributes to be printed); 
		//example list of attributes to be printed - ["PersonId","UpdateDate"]
	}

}
