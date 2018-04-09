import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

public class PrivPapi {
	private static final String pubkey = "DCC2820D-YCCJYJ7Q-MPVE0XO5-T1F47QXA";
	private static final String privkey = "497abb4f5543fae2c05e5130117e18d0c9078d6ce85a2a3d94cf1adcee1dc2c9b8c45d8dd497b304dbe345c2721185a164ed196918fdd3f0935dae56e3961c45";
	
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