import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;

@SuppressWarnings("serial")
public class PrivCapi {
	private static final String pubkey = "f88a798f94df2461264f63af74a3ff4c5a24b86c";
	private static final String privkey = "3556907620710f4ca24ee37d2431b92b9c8338a2b76de5370091e2b5dd11282dcb89f2b05e189225";
	
	private static Object getSingleMarketTradesLock = new Object();
	
	public static ArrayList<coinData> coinDataList = new ArrayList<coinData>();
	public static LinkedList<myTrade> myTradesList = new LinkedList<myTrade>();
	public static ArrayList<ohlcDataList> ohlcFullList = new ArrayList<ohlcDataList>();
	
	
	
	public static class ohlcDataList implements Serializable {
		LinkedList<ohlcData> ohlcList;
		int marketid;
		
		public ohlcDataList(int mid) {
			this.marketid = mid;
		}
		
		public ohlcDataList(LinkedList<ohlcData> ohlcList, int mid) {
			this.ohlcList = ohlcList;
			this.marketid = mid;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof ohlcDataList ) {
				ohlcDataList ot = (ohlcDataList) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	
	public static class coinData implements Serializable {
		int currid;
		String name;
		String code;
		private double balance;
		
		public coinData(String code) {
			this.code = code;
		}
		
		public coinData(int cid, String name, String code) {
			this.currid = cid;
			this.name = name;
			this.code = code;
		}
		
		public double getbalance() {
			return this.balance;
		}
		
		public void setbalance(double b) {
			this.balance = b;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof coinData ) {
				coinData ot = (coinData) other;
				if (ot.code.equals(this.code) ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class myTrade implements Serializable {
		int marketid;
		int tradeid;
		long timestamp;
		String datetime;
		double price;
		double quantity;
		double fee;
		double total;
		String type;
		int orderid;
		String ordertype;
		
		public myTrade(int oid) {
			this.orderid = oid;
		}
		
		public myTrade(int mid, int tid, long ts, String dt, double p, double q, double f, double t, String ty, int oid, String oty) {
			this.marketid = mid;
			this.tradeid = tid;
			this.timestamp = ts;
			this.datetime = dt;
			this.price = p;
			this.quantity = q;
			this.fee = f;
			this.total = t;
			this.type = ty;
			this.orderid = oid;
			this.ordertype = oty;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof myTrade ) {
				myTrade ot = (myTrade) other;
				if (ot.orderid==this.orderid ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class ohlcData implements Serializable {
		long timestamp;
		String datetime;
		double open;
		double high;
		double low;
		double close;
		double volume;
		
		public ohlcData(long ts) {
			this.timestamp = ts;
		}
		
		public ohlcData(long ts, String dt, double o, double h, double l, double c, double v) {
			this.timestamp = ts;
			this.datetime = dt;
			this.open = o;
			this.high = h;
			this.low = l;
			this.close = c;
			this.volume = v;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof ohlcData ) {
				ohlcData ot = (ohlcData) other;
				if (ot.timestamp==this.timestamp ) return true;
				else return false;
			} else return false;
		}
	}
	
	
	private static PubCapi.tradeTick createTradeTick(String msg, int mid) {
		int tid=-1;
		long ts=0;
		String dt="";
		double p=0;
		double q=0;
		double to=0;
		String ty="";
		int s, e;
		
		try {
			s=msg.indexOf("tradeid")+10;
			e=msg.indexOf('"', s);
			tid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("datetime")+11;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			ts=DataC.convertDatetime(dt);
			
			s=msg.indexOf("tradeprice")+13;
			e=msg.indexOf('"', s);
			p=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity")+11;
			e=msg.indexOf('"', s);
			q=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("total")+8;
			e=msg.indexOf('"', s);
			to=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("ordertype")+12;
			e=msg.indexOf('"', s);
			ty=msg.substring(s, e);
			
			return new PubCapi.tradeTick(mid, tid, ts, dt, p, q, to, ty);
		} catch (Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Private SingleMarkedData's Trade tick data reading failed!");
		}
		
		return new PubCapi.tradeTick(-1, -1, 0, "", 0, 0, 0, "");
	}
	
	private static coinData createCoinData(String msg) {
		int cid=-1;
		String name="";
		String code="";
		int s, e;
		
		try {
			s=msg.indexOf("currencyid")+13;
			e=msg.indexOf('"', s);
			cid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("name")+7;
			e=msg.indexOf('"', s);
			name=msg.substring(s, e);
			
			s=msg.indexOf("code")+7;
			e=msg.indexOf('"', s);
			code=msg.substring(s, e);
			
			return new coinData(cid, name, code);
		} catch (Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Private SingleMarkedData's Trade tick data reading failed!");
		}
		
		return new coinData(-1, "", "");
	}
	
	private static myTrade createMyTrade(String msg) {
		int mid=-1;
		int tid=-1;
		long ts=0;
		String dt="";
		double p=0;
		double q=0;
		double f=0;
		double t=0;
		String ty="";
		int oid=-1;
		String oty="";
		int s, e;
		
		try {
			s=msg.indexOf("tradeid")+10;
			e=msg.indexOf('"', s);
			tid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("tradetype", e)+12;
			e=msg.indexOf('"', s);
			ty=msg.substring(s, e);
			
			s=msg.indexOf("datetime", e)+11;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			ts=DataC.convertDatetime(dt);
			
			s=msg.indexOf("marketid", e)+11;
			e=msg.indexOf('"', s);
			mid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("tradeprice", e)+13;
			e=msg.indexOf('"', s);
			p=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity", e)+11;
			e=msg.indexOf('"', s);
			q=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("fee", e)+6;
			e=msg.indexOf('"', s);
			f=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("total", e)+8;
			e=msg.indexOf('"', s);
			t=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("ordertype", e)+12;
			e=msg.indexOf('"', s);
			oty=msg.substring(s, e);
			
			s=msg.indexOf("order_id", e)+11;
			e=msg.indexOf('"', s);
			oid=Integer.parseInt(msg.substring(s, e) );
			
			return new myTrade(mid, tid, ts, dt, p, q, f, t, ty, oid, oty);
		} catch (Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Private MyTrade data reading failed!");
		}
		
		return new myTrade(-1, -1, 0, "", 0, 0, 0, 0, "", -1, "");
	}
	
	private static ohlcData createOHLCData(String msg) {
		long ts=-1;
		String dt="";
		double o=0;
		double h=0;
		double l=0;
		double c=0;
		double v=0;
		int s, e;
		
		try {
			s=msg.indexOf("timestamp")+11;
			e=msg.indexOf(',', s);
			ts=Long.parseLong(msg.substring(s, e) );
			
			s=msg.indexOf("date", e)+7;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			s=msg.indexOf("high", e)+6;
			e=msg.indexOf(',', s);
			h=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("low", e)+5;
			e=msg.indexOf(',', s);
			l=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("open", e)+6;
			e=msg.indexOf(',', s);
			o=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("close", e)+7;
			e=msg.indexOf(',', s);
			c=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("volume", e)+8;
			e=msg.indexOf('}', s);
			v=Double.parseDouble(msg.substring(s, e) );
			
			return new ohlcData(ts, dt, o, h, l, c, v);
		} catch (Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Private ohlc data reading failed!");
		}
		
		return new ohlcData(-1, "", 0, 0, 0, 0, 0);
	}
	
	
	public static void getBalances() {
		Window.mntmGetBalances.setEnabled(false);
		String fullmsg = getAuthmsg("method=getinfo");
		
		int si, ei;
		si=fullmsg.indexOf("success");
		
		if (si!=-1)
			if (fullmsg.charAt(si+10)=='1') {
				si=fullmsg.indexOf("balances_available")+21;
				ei=fullmsg.indexOf('}', si);
				String msg = new String(fullmsg.substring(si, ei) + ',');
				
				si=0;
				ei=msg.indexOf(',')+1;
				int s, e;
				
				while (ei!=0) {
					s = msg.indexOf('"', si)+1;
					e = msg.indexOf('"', s);
					
					String code = msg.substring(s, e);
					
					s = msg.indexOf('"', e+1)+1;
					e = msg.indexOf('"', s);
					
					double balance = -1;
					try { balance=Double.parseDouble(msg.substring(s, e) ); } catch (Exception exc) { Trader.addLogEntry(code + " coin balance reading failed!"); }
					
					if (balance!=-1 && coinDataList.contains(new coinData(code)) )
						coinDataList.get(coinDataList.indexOf(new coinData(code)) ).setbalance(balance);
					
					si=ei;
					ei=msg.indexOf(',', si)+1;
				}
			}
		
		Window.mntmGetBalances.setEnabled(true);
		Window.updateBalances();
	}
	
	public static void getCoinData() {
		Window.mntmGetCoinsData.setEnabled(false);
		String fullmsg = getAuthmsg("method=getcoindata");
		
		int si, ei;
		si=fullmsg.indexOf("success");
		
		if (si!=-1)
			if (fullmsg.charAt(si+10)=='1') {
				si=fullmsg.indexOf("return")+9;
				ei=fullmsg.indexOf('}', si)+1;
				
				while (si!=-1 && ei!=0) {
					coinData tempCD = createCoinData(fullmsg.substring(si, ei) );
					if (!coinDataList.contains(new coinData(tempCD.code) ) && tempCD.currid>0)
						coinDataList.add(tempCD);
					
					si=fullmsg.indexOf('{', ei);
					ei=fullmsg.indexOf('}', si)+1;
				}
			}
		
		Window.mntmGetCoinsData.setEnabled(true);
	}
	
	public static String getMarkets() {
		return getAuthmsg("method=getmarkets");
	}
	
	public static void getSingleMarketTrades(int mid) {
		String fullmsg = "";
		synchronized (PrivCapi.getSingleMarketTradesLock) {
			fullmsg = getAuthmsg("method=markettrades&marketid=" + mid);
		}
				
		int si, ei;
		si=fullmsg.indexOf("success");
		int tFLN = PubCapi.tradeFullList.indexOf(new PubCapi.tradeTickList(mid) );
		
		if (si!=-1)
			if (fullmsg.charAt(si+10)=='1' && tFLN!=-1) {
				si=fullmsg.indexOf("return")+9;
				ei=fullmsg.indexOf('}', si)+1;
				
				while (si>0 && ei>0) {
					PubCapi.tradeTick tempTT = createTradeTick(fullmsg.substring(si, ei), mid);
					synchronized (PubCapi.tradeFullList.get(tFLN) ) {
						if (!PubCapi.tradeFullList.get(tFLN).tradeList.contains(tempTT) && tempTT.tradeid!=-1) {
							PubCapi.tradeFullList.get(tFLN).tradeList.addFirst(tempTT);
						}
					}
					
					si=fullmsg.indexOf('{', ei);
					ei=fullmsg.indexOf('}', si)+1;
				}
				
				synchronized (PubCapi.tradeFullList.get(tFLN) ) {
					PubCapi.tradeFullList.get(tFLN).tradeList.sort(new Comparator<PubCapi.tradeTick>(){
						public int compare(PubCapi.tradeTick s1, PubCapi.tradeTick s2) {
							if (s1.tradeid>s2.tradeid) return 1;
							else if (s1.tradeid<s2.tradeid) return -1;
							else return 0;						
						}
					});
				}
				
				if (Window.selectedMarket==mid)
					Window.callChartsUpdate();
			}
	}
	
	public static void getAllMyTrades() {
		Window.mntmGetMytrades.setEnabled(false);
		String fullmsg = getAuthmsg("method=allmytrades");
		
		int si, ei;
		si=fullmsg.indexOf("success");
		
		if (si!=-1)
			if (fullmsg.charAt(si+10)=='1') {
				si=fullmsg.indexOf("return")+9;
				ei=fullmsg.indexOf('}', si)+1;
				
				while (si!=-1 && ei!=0) {
					myTrade tempT = createMyTrade(fullmsg.substring(si, ei));
					synchronized (PrivCapi.myTradesList) {
						if (!myTradesList.contains(new myTrade(tempT.orderid)) && tempT.orderid!=-1) {
							myTradesList.addFirst(tempT);
						}
					}
					
					si=fullmsg.indexOf('{', ei);
					ei=fullmsg.indexOf('}', si)+1;
				}
				
				synchronized (PrivCapi.myTradesList) {
					PrivCapi.myTradesList.sort(new Comparator<PrivCapi.myTrade>(){
						public int compare(PrivCapi.myTrade s1, PrivCapi.myTrade s2) {
							if (s1.timestamp>s2.timestamp) return 1;
							else if (s1.timestamp<s2.timestamp) return -1;
							else return 0;						
						}
					});
				}
			}
		
		Window.mntmGetMytrades.setEnabled(true);
		Window.updateMyTrades();
	}
	
	public static void getLongtermOHLCdata(int mid, long timespan) {
		String fullmsg = "";
		if (mid>0 && timespan>0) {
			Window.mntmGetSampleData.setEnabled(false);
			
			int ListN = -1;
			if (ohlcFullList.contains(new ohlcDataList(mid) ) )
				ListN = ohlcFullList.indexOf(new ohlcDataList(mid) );
			else synchronized (PrivCapi.ohlcFullList) {
				ListN = ohlcFullList.size();
				ohlcFullList.add(new ohlcDataList(new LinkedList<ohlcData>(), mid) );			
			}
			
			if (ListN!=-1) {
				long oldestts=System.currentTimeMillis()/1000;
				int si=0, ei=0;
				
				if (PrivCapi.ohlcFullList.get(ListN).ohlcList.isEmpty() ) {
					fullmsg = getAuthmsgv2("/markets/3/ohlc", "limit=100", "GET");
					
					si=fullmsg.indexOf("[{");
					ei=fullmsg.indexOf("}]");
					fullmsg = fullmsg.substring(si+1, ei+1);
					
					si=fullmsg.indexOf('{');
					ei=fullmsg.indexOf('}', si);
					while (si!=-1 && ei!=-1) {
						ohlcData tempD = createOHLCData(new String(fullmsg.substring(si, ei+1) ) );
						oldestts=tempD.timestamp;
						
						if (ohlcFullList.get(ListN).ohlcList.isEmpty() )
							synchronized (PrivCapi.ohlcFullList.get(ListN) ) {
								ohlcFullList.get(ListN).ohlcList.addFirst(tempD);
							}
						else if (oldestts<ohlcFullList.get(ListN).ohlcList.getFirst().timestamp )
							synchronized (PrivCapi.ohlcFullList.get(ListN) ) {
								ohlcFullList.get(ListN).ohlcList.addFirst(tempD);
							}
						
						si=fullmsg.indexOf('{', ei);
						ei=fullmsg.indexOf('}', si);
					}
				} else {
					oldestts=PrivCapi.ohlcFullList.get(ListN).ohlcList.getFirst().timestamp;
				}
				
				long newestts = ohlcFullList.get(ListN).ohlcList.getLast().timestamp;
				while (oldestts-60>=newestts-timespan) {
					fullmsg = getAuthmsgv2("/markets/3/ohlc", "limit=100&stop=" + Long.valueOf(oldestts+600), "GET");
					
					si=fullmsg.indexOf("[{");
					ei=fullmsg.indexOf("}]");
					fullmsg = fullmsg.substring(si+1, ei+1);
					
					si=fullmsg.indexOf('{');
					ei=fullmsg.indexOf('}', si);
					while (si!=-1 && ei!=-1 && oldestts-60>=newestts-timespan) {
						ohlcData tempD = createOHLCData(new String(fullmsg.substring(si, ei+1) ) );
						oldestts=tempD.timestamp;
						
						if (oldestts<ohlcFullList.get(ListN).ohlcList.getFirst().timestamp )
							synchronized (PrivCapi.ohlcFullList.get(ListN) ) {
								ohlcFullList.get(ListN).ohlcList.addFirst(tempD);
							}
						
						si=fullmsg.indexOf('{', ei);
						ei=fullmsg.indexOf('}', si);
					}
					
					if (Window.windowActive)
						Window.updateNeuralCChart(mid);
				}											
			}
			
			Window.mntmGetSampleData.setEnabled(true);
		}
	}
	
	
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
			URL authapiURL = new URL("https://api.cryptsy.com/api");
			HttpURLConnection uc = (HttpURLConnection) authapiURL.openConnection();
			
			uc.setDoOutput(true);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Key", pubkey);
			uc.setRequestProperty("Sign", encodeBySHA512(postmsg, privkey));
			uc.setRequestProperty("Content-Length", String.valueOf(postmsg.length()) );
			
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
			//e.printStackTrace();
			Trader.addLogEntry("Connecting to PrivCapi failed! Postdata:" + postData);
		}
				
		return returnmsg;
	}
	
	public static String getAuthmsgv2(String urlData, String postData, String method) {
		String returnmsg = "";
		String postmsg = postData + "&nonce=" + System.currentTimeMillis();
		String urlString = "https://api.cryptsy.com/api/v2" + urlData + "?" + postmsg;
		
		try {
			URL pubapiv2URL = new URL(urlString);
			HttpURLConnection uc = (HttpURLConnection) pubapiv2URL.openConnection();			
			
			uc.setRequestMethod(method);
			uc.setRequestProperty("Key", pubkey);
			uc.setRequestProperty("Sign", encodeBySHA512(postmsg, privkey));
			
			InputStreamReader in = new InputStreamReader(uc.getInputStream());
			BufferedReader br = new BufferedReader(in);
						
			returnmsg = br.readLine();
			
			br.close();
			in.close();
		} catch (Exception e) {
			//e.printStackTrace();
			Trader.addLogEntry("Connecting to PrivCapiv2 failed! Url: " + urlString);
		}
				
		return returnmsg;
	}
	
}