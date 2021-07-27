package com.serverless;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.parser.JSONParser;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);
		
		try {
			
			URL url = new URL("https://api.covid19api.com/summary");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.connect();
			//Getting the response code
			int responsecode = conn.getResponseCode();
			
			if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            }			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String strTemp = "";
			String inputRawStr = "";
			String outputRawStr = "";
			while (null != (strTemp = br.readLine())) {
				inputRawStr +=strTemp;
			}
                
			conn.disconnect();
			
			//Using the JSON simple library parse the string into a json object
		    JSONParser parse = new JSONParser();
		    JSONObject data_obj = (JSONObject) parse.parse(inputRawStr);
		    
		  //Get the required object from the above created object
		    JSONObject obj = (JSONObject) data_obj.get("Global");

		    //Get the required data using its key
//		    System.out.println(obj.get("TotalRecovered"));
		    JSONArray arr = (JSONArray) data_obj.get("Countries");

//sample    {
//		    	"ID": "8aa57ff4-4dd1-4eee-beae-f50cbe382153",
//		    	"Country": "India",
//		    	"CountryCode": "IN",
//		    	"Slug": "india",
//		    	"NewConfirmed": 29689,
//		    	"TotalConfirmed": 31440951,
//		    	"NewDeaths": 415,
//		    	"TotalDeaths": 421382,
//		    	"NewRecovered": 42363,
//		    	"TotalRecovered": 30621469,
//		    	"Date": "2021-07-27T21:07:32.922Z",
//		    	"Premium": {}
//		   	}
            for (int i = 0; i < arr.size(); i++) {

                JSONObject new_obj = (JSONObject) arr.get(i);
                
                if (new_obj.get("Slug").equals("india")) {
//                    System.out.println("Total Recovered: " + new_obj.get("TotalRecovered"));
                	outputRawStr += "COVID19 Update as on "+ new_obj.get("Date") +"\n";                	
                	outputRawStr += "Country :" + new_obj.get("Country")+"\n";             
                	outputRawStr += "Total Confirmed : " + new_obj.get("TotalConfirmed") +"\n";
                    outputRawStr += "Total Recovered : " + new_obj.get("TotalRecovered");
                    break;
                }
            }
		    
		    
			return ApiGatewayResponse.builder()
					.setStatusCode(responsecode)
					.setRawBody(outputRawStr)
					.setHeaders(Collections.singletonMap("Powered-By", "AWS Lambda & serverless"))
					.build();
		    
	  } catch(MalformedURLException e) {
		  e.printStackTrace();
	  } catch (Exception e) {
            e.printStackTrace();
      }
	  return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setRawBody("Raj, function NOT executed successfully!")
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
