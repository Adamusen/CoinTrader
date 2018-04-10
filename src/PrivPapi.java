import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

public class PrivPapi {
	private static final String pubkey = "0";
	private static final String privkey = "0";
	
	private static String encodeBySHA512(String msg, String key) {
		String result = null;
		Mac sha512_HMAC;

		try {
			sha512_HMAC = Mac.getInstance("HmacSHA512");      
			SecretKeySpec secretkey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
			sha512_HMAC.init(secretkey);
			byte[] mac_data = sha512_HMAC.doFinal(msg.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(mac_data.length * 2);
			for(byte b: mac_data)
				sb.append(String.format("%02x", b & 0xff));
			result = sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
		
	public static String getAuthmsg(String postData) {
		String returnmsg = "";
		String postmsg = postData + "&nonce=" + System.currentTimeMillis();
		
		try {
			URL authapiURL = new URL("https://poloniex.com/tradingApi");
			HttpURLConnection uc = (HttpURLConnection) authapiURL.openConnection();
			
			uc.setDoOutput(true);		
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Key", pubkey);
			uc.setRequestProperty("Sign", encodeBySHA512(postmsg, privkey));
			
			OutputStreamWriter out = new OutputStreamWriter(uc.getOutputStream());
			out.write(postmsg);
			out.flush();
			out.close();
			
			InputStreamReader in = new InputStreamReader(uc.getInputStream());
			BufferedReader br = new BufferedReader(in);
						
			returnmsg = br.readLine();
			
			br.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnmsg;
	}
	
	
}
