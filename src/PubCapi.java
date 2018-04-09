import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;


@SuppressWarnings("serial")
public class PubCapi {
	private static Pusher pusher = new Pusher("cb65d0a7a72cd94adf1f");
	private static final String eventName = "message";
	private static final long orderBookRefTime = 60000; //millisecond
	private static Object orderBookGettingLock = new Object();
	public static final int orderBookListDepth = 16;
	public static boolean pusherConnect = false;
	
	public static ArrayList<subscribedChannelData> subscribedChannels = new ArrayList<subscribedChannelData>();
	public static ArrayList<tickerTickList> tickerFullList = new ArrayList<tickerTickList>();
	public static ArrayList<tradeTickList> tradeFullList = new ArrayList<tradeTickList>();
	public static ArrayList<orderBookList> orderBookFullList = new ArrayList<orderBookList>();
	public static LinkedList<simplyestMarketData> simplyestMarketDataList = new LinkedList<simplyestMarketData>();
	
	
	
	
	
	public static class subscribedChannelData implements Serializable {
		public int marketid;
		public Object oBGThreadLock;
		
		public subscribedChannelData(int mid) {
			this.marketid = mid;
		}
		
		public subscribedChannelData(int mid, Object obj) {
			this.marketid = mid;
			this.oBGThreadLock = obj;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof subscribedChannelData ) {
				subscribedChannelData ot = (subscribedChannelData) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class tickerTickList implements Serializable {
		public LinkedList<tickerTick> tickList;
		public int marketid;
		
		public tickerTickList(int mid) {
			this.marketid = mid;
		}
		
		public tickerTickList(LinkedList<tickerTick> tickL, int mid) {
			this.tickList = tickL;
			this.marketid = mid;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof tickerTickList ) {
				tickerTickList ot = (tickerTickList) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class tradeTickList implements Serializable {
		public LinkedList<tradeTick> tradeList;
		public int marketid;
		
		public tradeTickList(int mid) {
			this.marketid = mid;
		}
		
		public tradeTickList(LinkedList<tradeTick> tradeL, int mid) {
			this.tradeList = tradeL;
			this.marketid = mid;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof tradeTickList ) {
				tradeTickList ot = (tradeTickList) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class orderBookList implements Serializable {
		public LinkedList<orderBook> orderBookList;
		public int marketid;
		
		public orderBookList(int mid) {
			this.marketid = mid;
		}
				
		public orderBookList(LinkedList<orderBook> obL, int mid) {
			this.orderBookList = obL;
			this.marketid = mid;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof orderBookList ) {
				orderBookList ot = (orderBookList) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	
	public static class tickerTick implements Serializable {
		public int marketid;
		public long pctimestamp;
		public long timestamp;
		public String datetime;
		public double topsellprice;
		public double topsellquantity;
		public double topbuyprice;
		public double topbuyquantity;
		
		public tickerTick(int mid, long ts, String dt, double tsp, double tsq, double tbp, double tbq) {
			this.marketid=mid;
			this.pctimestamp=System.currentTimeMillis() / 1000;
			this.timestamp=ts;
			this.datetime=dt;
			this.topsellprice=tsp;
			this.topsellquantity=tsq;
			this.topbuyprice=tbp;
			this.topbuyquantity=tbq;
		}		
	}
	
	public static class tradeTick implements Serializable {
		public int marketid;
		public int tradeid;
		public long pctimestamp;
		public long timestamp;
		public String datetime;
		public double price;
		public double quantity;
		public double total;
		public String type;
		
		public tradeTick(int tid) {
			this.tradeid=tid;
		}
		
		public tradeTick(int mid, int tid, long ts, String dt, double p, double q, double to, String ty) {
			this.marketid=mid;
			this.tradeid=tid;
			this.pctimestamp=System.currentTimeMillis() / 1000;
			this.timestamp=ts;
			this.datetime=dt;
			this.price=p;
			this.quantity=q;
			this.total=to;
			this.type=ty;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof tradeTick ) {
				tradeTick ot = (tradeTick) other;
				if (ot.tradeid==this.tradeid ) return true;
				else return false;
			} else return false;
		}
	}
	
	public static class orderBook implements Serializable {
		public LinkedList<orderData> Sellorders;
		public LinkedList<orderData> Buyorders;
		public long pctimestamp;
		
		public orderBook(LinkedList<orderData> sellos, LinkedList<orderData> buyos) {
			this.Sellorders = sellos;
			this.Buyorders = buyos;		
			this.pctimestamp = System.currentTimeMillis() / 1000;
		}
	}
	
	public static class orderData implements Serializable {
		public double price;
		public double quantity;
		public double total;
		
		public orderData(double p, double q, double t) {
			this.price = p;
			this.quantity = q;
			this.total = t;
		}
	}
	
	public static class simplyestMarketData implements Serializable {
		public int marketid;
		public String label;
		public double lasttradeprice;
		public double lasttradevolume;
		public String lasttradetime;
		public String primaryname;
		public String secondaryname;
		
		public simplyestMarketData(int mid) {
			this.marketid=mid;
		}
		
		public simplyestMarketData(int mid, String lab, double ltp, double ltv, String ltt, String pn, String sn) {
			this.marketid=mid;
			this.label=lab;
			this.lasttradeprice=ltp;
			this.lasttradevolume=ltv;
			this.lasttradetime=ltt;
			this.primaryname=pn;
			this.secondaryname=sn;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof simplyestMarketData ) {
				simplyestMarketData ot = (simplyestMarketData) other;
				if (ot.marketid==this.marketid ) return true;
				else return false;
			} else return false;
		}
	}
	
	
	private static tickerTick createTickerTick(String msg) {
		int mid=-1;
		long ts=0;
		String dt="";
		double tsp=0;
		double tsq=0;
		double tbp=0;
		double tbq=0;
		int s, e;
		
		try {
			s=msg.indexOf("marketid")+11;
			e=msg.indexOf('"', s);
			mid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("timestamp")+11;
			e=msg.indexOf(',', s);
			ts=Long.parseLong(msg.substring(s, e) );
			
			s=msg.indexOf("datetime")+11;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			s=msg.indexOf("topsell");
			s=msg.indexOf("price",s)+8;
			e=msg.indexOf('"', s);
			tsp=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity",e)+11;
			e=msg.indexOf('"', s);
			tsq=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("topbuy",e);
			s=msg.indexOf("price",s)+8;
			e=msg.indexOf('"', s);
			tbp=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity",e)+11;
			e=msg.indexOf('"', s);
			tbq=Double.parseDouble(msg.substring(s, e) );
			
			return new tickerTick(mid, ts, dt, tsp, tsq, tbp, tbq);
		} catch(Exception exc) {
			//exc.printStackTrace();
			//Trader.addLogEntry("Pushapi's Ticker tick data reading failed!");
		}
				
		return new tickerTick(-1, 0, "", 0, 0, 0, 0);
	}
	
	private static tradeTick createTradeTick(String msg) {
		int mid=-1;
		int tid=-1;
		long ts=0;
		String dt="";
		double p=0;
		double q=0;
		double to=0;
		String ty="";
		int s, e;
		
		try {
			s=msg.indexOf("marketid")+11;
			e=msg.indexOf('"', s);
			mid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("tradeid")+10;
			e=msg.indexOf('"', s);
			tid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("timestamp")+11;
			e=msg.indexOf(',', s);
			ts=Long.parseLong(msg.substring(s, e) );
			
			s=msg.indexOf("datetime")+11;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			s=msg.indexOf("price")+8;
			e=msg.indexOf('"', s);
			p=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity")+11;
			e=msg.indexOf('"', s);
			q=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("total")+8;
			e=msg.indexOf('"', s);
			to=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("type")+7;
			e=msg.indexOf('"', s);
			ty=msg.substring(s, e);
		} catch(Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Pushapi's Trade tick data reading failed!");
		}
				
		return new tradeTick(mid, tid, ts, dt, p, q, to, ty);
	}
	
	private static tradeTick createTradeTick(String msg, int mid) {
		int tid=-1;
		long ts=0;
		String dt="";
		double p=0;
		double q=0;
		double to=0;
		String ty="";
		int s, e;
		
		try {
			s=msg.indexOf("id")+5;
			e=msg.indexOf('"', s);
			tid=Integer.parseInt(msg.substring(s, e) );
			
			s=msg.indexOf("time")+7;
			e=msg.indexOf('"', s);
			dt=msg.substring(s, e);
			
			ts=DataC.convertDatetime(dt);
			
			s=msg.indexOf("price")+8;
			e=msg.indexOf('"', s);
			p=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity")+11;
			e=msg.indexOf('"', s);
			q=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("total")+8;
			e=msg.indexOf('"', s);
			to=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("type")+7;
			e=msg.indexOf('"', s);
			ty=msg.substring(s, e);
			
			return new tradeTick(mid, tid, ts, dt, p, q, to, ty);
		} catch(Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("Public SingleMarkedData's Trade tick data reading failed!");
		}
		
		return new tradeTick(-1, -1, 0, "", 0, 0, 0, "");
	}
	
	private static orderData createOrderData(String msg)
	{
		double p=0;
		double q=0;
		double t=0;
		int s, e;
		
		try {
			s=msg.indexOf("price")+8;
			e=msg.indexOf('"', s);
			p=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("quantity")+11;
			e=msg.indexOf('"', s);
			q=Double.parseDouble(msg.substring(s, e) );
			
			s=msg.indexOf("total")+8;
			e=msg.indexOf('"', s);
			t=Double.parseDouble(msg.substring(s, e) );
		} catch(Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("SingleMarkedData's orderBook's orderData reading failed!");
		}
		
		return new orderData(p, q, t);
	}
	
	public static simplyestMarketData createSimplyestMarketData(String msg) {
		int mid=-1;
		String lab="";
		double ltp=0;
		double ltv=0;
		String ltt="";
		String pn="";
		String sn="";
		int s, e;
		
		try {
			s=msg.indexOf("marketid")+11;
			e=msg.indexOf('"', s);
			try { mid=Integer.parseInt(msg.substring(s, e) ); } catch (Exception exc) {}
			
			s=msg.indexOf("label", e)+8;
			e=msg.indexOf('"', s);
			lab=msg.substring(s, e);
			
			s=msg.indexOf("lasttradeprice", e)+17;
			e=msg.indexOf('"', s);
			try { ltp=Double.parseDouble(msg.substring(s, e) ); } catch (Exception exc) {}
						
			s=msg.indexOf("volume", e)+9;
			e=msg.indexOf('"', s);
			try { ltv=Double.parseDouble(msg.substring(s, e) ); } catch (Exception exc) {}
						
			s=msg.indexOf("lasttradetime", e)+16;
			e=msg.indexOf('"', s);
			ltt=msg.substring(s, e);
			if (ltt.equals("ull,") ) ltt="null";
			
			s=msg.indexOf("primaryname", e)+14;
			e=msg.indexOf('"', s);
			pn=msg.substring(s, e);
			
			s=msg.indexOf("secondaryname", e)+16;
			e=msg.indexOf('"', s);
			sn=msg.substring(s, e);
		} catch (Exception exc) {
			exc.printStackTrace();
			Trader.addLogEntry("One of Markets List's market data reading failed!");
		}
		
		return new simplyestMarketData(mid, lab, ltp, ltv, ltt, pn, sn);
	}
	
	
	
	public static void startGetAllMarketsData() {
		Window.mntmReloadMarketList.setEnabled(false);
		(new Thread(new GetAllMarketsDataThread())).start();
		Trader.addLogEntry("Getting marketList data from Pubapi started");
	}
	
	private static class GetAllMarketsDataThread implements Runnable {	
		public void run() {
			URL allmarketURL = null;
			URLConnection uc = null;
			InputStreamReader in = null;
			BufferedReader br = null;
			
			try {			
				allmarketURL = new URL("http://pubapi2.cryptsy.com/api.php?method=marketdatav2");
				uc = allmarketURL.openConnection();
				in = new InputStreamReader(uc.getInputStream());
				br = new BufferedReader(in);
				
				LinkedList<simplyestMarketData> tempSMDL = new LinkedList<simplyestMarketData>();
				
				final int readL = 50;
				String charinit = "";
				for(int i=0;i<readL;i++) {
					charinit+="!";
				}
				char[] prevshortmsg;
				char[] shortmsg = charinit.toCharArray();
				String longmsg, fullmsg = new String("");
				int ret, index=0;
				boolean found=false;
				
				do {
					prevshortmsg = shortmsg;
					shortmsg = charinit.toCharArray();
					ret=br.read(shortmsg);
					
					longmsg = new String(prevshortmsg);
					longmsg += new String(shortmsg);
					if (longmsg.contains("!") ) longmsg = longmsg.replaceAll("!", "");
									
					if (found) {
						fullmsg += new String(shortmsg).replaceAll("!", "");
						if (fullmsg.indexOf("recenttrades", index)!=-1 ) {					
							tempSMDL.add(createSimplyestMarketData(fullmsg) );
							found=false;
						}
					}
					
					if (longmsg.contains("marketid") ) {
						fullmsg = new String(longmsg);
						index = fullmsg.indexOf("marketid");
						found=true;
					}					
				} while (ret!=-1);			
				
				tempSMDL.sort(new Comparator<simplyestMarketData>(){
					public int compare(simplyestMarketData s1, simplyestMarketData s2) {
						if (s1.marketid>s2.marketid) return 1;
						else if (s1.marketid<s2.marketid) return -1;
						else return 0;						
					}
				});
				
				simplyestMarketDataList = tempSMDL;
				Window.updateMarketListTableModel();
				
				Trader.addLogEntry("Getting marketList data from Pubapi finished");
			} catch (Exception ex) {
				ex.printStackTrace();
				Trader.addLogEntry("Getting marketList data from Pubapi failed!");
			} finally {
				try {
					br.close();
					in.close();
				} catch (Exception exc){
					exc.printStackTrace();
				}
			}
			Window.mntmReloadMarketList.setEnabled(true);
			Window.updateMarketsListModel();
		}
	}
	
	public static void startOrderBookGetting(int mid) {
		if (subscribedChannels.contains(new subscribedChannelData(mid) ) ) {
			int LN = subscribedChannels.indexOf(new subscribedChannelData(mid) );
			subscribedChannels.get(LN).oBGThreadLock = new Object();
			(new Thread(new OrderBookGettingThread(mid, LN))).start();
		}
	}
	
	private static class OrderBookGettingThread implements Runnable {
		private int marketid;
		private int sCLN;
		private long lastgetTime=0;
		
		public OrderBookGettingThread(int mid, int LN) {
			this.marketid = mid;
			this.sCLN = LN;
		}
		
		public void run() {
			synchronized (subscribedChannels.get(sCLN).oBGThreadLock) {
				try { subscribedChannels.get(sCLN).oBGThreadLock.wait(new Double(Math.random()*orderBookRefTime).longValue() ); } catch (Exception e) { e.printStackTrace(); }
			}
				while (subscribedChannels.contains(new subscribedChannelData(marketid) ) ) {
					long currTime = System.currentTimeMillis();
					
					if (currTime-lastgetTime>5000) {
						getSingleMarketOrderBook(marketid);			
						lastgetTime = currTime;
					}
					
					synchronized (subscribedChannels.get(sCLN).oBGThreadLock) {
						try { subscribedChannels.get(sCLN).oBGThreadLock.wait(orderBookRefTime); } catch (Exception e) { e.printStackTrace(); }
					}
				}
		}
	}
	
	
	public static void getSingleMarketDataForMarketsList(int mid) {
		URL singlemarketURL = null;
		URLConnection uc = null;
		InputStreamReader in = null;
		BufferedReader br = null;
		
		try {
			singlemarketURL = new URL("http://pubapi2.cryptsy.com/api.php?method=singlemarketdata&marketid=" + mid);
			uc = singlemarketURL.openConnection();
			in = new InputStreamReader(uc.getInputStream());
			br = new BufferedReader(in);
			
			String msg=br.readLine();
			
			int sMDLindex = simplyestMarketDataList.indexOf(new simplyestMarketData(mid) );
			simplyestMarketData tempsMD = createSimplyestMarketData(msg);
			simplyestMarketDataList.set(sMDLindex, tempsMD);
			
			NumberFormat ltpf = new DecimalFormat("#0.00000000");
			NumberFormat ltvf = new DecimalFormat("#0.0000");
			double ltp = tempsMD.lasttradeprice;
			double ltv = tempsMD.lasttradevolume * ltp;
			String PapiS = "";
			if (subscribedChannels.contains(new subscribedChannelData(tempsMD.marketid) ) ) {
				PapiS = "Yes";
			}
			Object[] newRow = {
					tempsMD.marketid,
					tempsMD.label,
					tempsMD.primaryname,
					tempsMD.secondaryname,
					ltpf.format(ltp).replace(',', '.'),
					ltvf.format(ltv).replace(',', '.'),
					tempsMD.lasttradetime,
					PapiS
				};
			
			Window.MarketListTableModel.removeRow(sMDLindex);
			Window.MarketListTableModel.insertRow(sMDLindex, newRow);
		} catch (Exception ex) {
			ex.printStackTrace();
			Trader.addLogEntry("Getting SingleMarketData for marketList qmarket:" + mid + " from Pubapi failed!");
		} finally {
			try {
				br.close();
				in.close();
			} catch (Exception exc){
				exc.printStackTrace();
			}
		}
	}
	
	private static void getSingleMarketTrades(int mid) {
		URL singlemarketURL = null;
		URLConnection uc = null;
		InputStreamReader in = null;
		BufferedReader br = null;
		
		try {
			singlemarketURL = new URL("http://pubapi2.cryptsy.com/api.php?method=singlemarketdata&marketid=" + mid);
			uc = singlemarketURL.openConnection();
			in = new InputStreamReader(uc.getInputStream());
			br = new BufferedReader(in);
			
			String msg=br.readLine();
			
			int si=msg.indexOf("recenttrades")+15;
			int ei=msg.indexOf("sellorders")-3;
			int tFLN = tradeFullList.indexOf(new tradeTickList(mid) );
			
			if (tFLN!=-1 && si!=-1 && ei!=-1) {
				String alltrades = new String(msg.substring(si, ei));
				
				si=0; ei=0;
				while (ei<alltrades.length()-5 && si!=-1 && ei!=-1) {
					si=alltrades.indexOf('{', ei);
					ei=alltrades.indexOf('}', si);
					
					tradeTick tempTT = createTradeTick(alltrades.substring(si, ei), mid);
					synchronized (PubCapi.tradeFullList.get(tFLN) ) {
						if (!tradeFullList.get(tFLN).tradeList.contains(tempTT) && tempTT.tradeid!=-1) {
							tradeFullList.get(tFLN).tradeList.addFirst(tempTT);
						}
					}
				}
				
				synchronized (PubCapi.tradeFullList.get(tFLN) ) {
					tradeFullList.get(tFLN).tradeList.sort(new Comparator<tradeTick>(){
						public int compare(tradeTick s1, tradeTick s2) {
							if (s1.tradeid>s2.tradeid) return 1;
							else if (s1.tradeid<s2.tradeid) return -1;
							else return 0;						
						}
					});
				}
				
				if (Window.selectedMarket==mid)
					Window.callChartsUpdate();
			}			
		} catch (Exception ex) {
			//ex.printStackTrace();
			Trader.addLogEntry("Getting SingleMarketTrades market:" + mid + " from Pubapi failed!");
		} finally {
			try {
				br.close();
				in.close();
			} catch (Exception exc){ }
		}
	}
	
	private static void getSingleMarketOrderBook(int mid) {
 		URL singlemarketURL = null;
		URLConnection uc = null;
		InputStreamReader in = null;
		BufferedReader br = null;
		String msg = "";
		
		try {
			synchronized (PubCapi.orderBookGettingLock) {
				singlemarketURL = new URL("http://pubapi2.cryptsy.com/api.php?method=singleorderdata&marketid=" + mid);
				uc = singlemarketURL.openConnection();
				in = new InputStreamReader(uc.getInputStream());
				br = new BufferedReader(in);
				
				msg = br.readLine();
				
				br.close();
				in.close();
			}
			
			int succindex=msg.indexOf("succes")+9;
			if (msg.charAt(succindex)=='1') {
				int si=msg.indexOf("sellorders")+13;
				int ei=msg.indexOf("buyorders")-3;
				String sellorders = new String(msg.substring(si, ei));
				
				si=ei+15;
				ei=msg.length()-4;
				String buyorders = new String(msg.substring(si, ei));
				
				si=0; ei=0;
				LinkedList<orderData> tempSOL = new LinkedList<orderData>();
				int lastei=sellorders.lastIndexOf('}');
				int gotN=0;
				while (ei<lastei && ei!=-1 && ei!=-1 && gotN<orderBookListDepth) {
					si=sellorders.indexOf('{',ei);
					ei=sellorders.indexOf('}',si);					
					tempSOL.add(createOrderData(sellorders.substring(si, ei) ) );
					gotN++;
				}
				
				si=0; ei=0;
				LinkedList<orderData> tempBOL = new LinkedList<orderData>();
				lastei=buyorders.lastIndexOf('}');
				gotN=0;
				while (ei<lastei && ei!=-1 && ei!=-1 && gotN<orderBookListDepth) {
					si=buyorders.indexOf('{',ei);
					ei=buyorders.indexOf('}',si);					
					tempBOL.add(createOrderData(buyorders.substring(si, ei) ) );	
					gotN++;
				}
				
				orderBook tempoB = new orderBook(tempSOL, tempBOL);
				if (orderBookFullList.contains(new orderBookList(mid) ) ) {
					int oBFLN=orderBookFullList.indexOf(new orderBookList(mid) );
					synchronized (orderBookFullList.get(oBFLN) ) {
						orderBookFullList.get(oBFLN).orderBookList.add(tempoB);
					}
				} else {
					int oBFLN;
					synchronized (orderBookFullList) {
						oBFLN=orderBookFullList.size();
						orderBookFullList.add(new orderBookList(new LinkedList<orderBook>(), mid) );
					}
					synchronized (orderBookFullList.get(oBFLN) ) {
						orderBookFullList.get(oBFLN).orderBookList.add(tempoB);
					}					
				}
			}			
		} catch (Exception ex) {
			//ex.printStackTrace();
			Trader.addLogEntry("Getting SingleMarketOrderBook market:" + mid + " from Pubapi failed!");
		}
 	}
	
	
	public static void connectPusherFirst() {
		pusherConnect = true;
		pusher.connect(new ConnectionEventListener() {
			int conntrys = 0;
			
		    @Override
		    public void onConnectionStateChange(ConnectionStateChange change) {
		        Trader.addLogEntry("Pusher connection state changed to " + change.getCurrentState() + " from " + change.getPreviousState());
		        
		        if (change.getCurrentState().toString().equals("CONNECTED") ) {
		        	Window.mntmConnectPusher.setEnabled(false);
		        	Window.mntmDisconnectPusher.setEnabled(true);
		        	conntrys=0;
		        }
		        
		        if (change.getCurrentState().toString().equals("DISCONNECTED") ) {
		        	Window.mntmConnectPusher.setEnabled(true);
		        	Window.mntmDisconnectPusher.setEnabled(false);
		        	if (pusherConnect && conntrys<3) {
		        		if (conntrys>0) { try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); } }
		        		reconnectPusher();
		        		conntrys++;
		        	}
		        }
		        
		        if (change.getCurrentState().toString().equals("DISCONNECTING") ) {
		        	Window.mntmConnectPusher.setEnabled(true);
		        	Window.mntmDisconnectPusher.setEnabled(false);
		        }
		    }

		    @Override
		    public void onError(String message, String code, Exception e) {
		    	Trader.addLogEntry("There was a problem connecting the Pusher! Code: " + code);
		        //e.printStackTrace();
		    }
		}, ConnectionState.ALL);
	}
	
	public static void reconnectPusher() {
		pusherConnect = true;
		pusher.connect();
	}
	
	public static void disconnectPusher() {
		pusherConnect = false;
		pusher.disconnect();
	}
	
	public static void subscribePusherChannel(int chNumber) {
		subscribeTickerChannel(chNumber);
		subscribeTradeChannel(chNumber);
		if (!subscribedChannels.contains(new subscribedChannelData(chNumber) ) ) {
			subscribedChannels.add(new subscribedChannelData(chNumber) );
			startOrderBookGetting(chNumber);		
		}
	}
	
	public static void unsubscribePusherChannel(int chNumber) {
		String tickerchName = new String("ticker." + chNumber);
		String tradechName = new String("trade." + chNumber);
		if (subscribedChannels.contains(new subscribedChannelData(chNumber) ) ) {
			int index = subscribedChannels.indexOf(new subscribedChannelData(chNumber) );
			subscribedChannels.get(index).marketid=-10;
			synchronized (subscribedChannels.get(index).oBGThreadLock) {
				subscribedChannels.get(index).oBGThreadLock.notify();
			}
			//Trader.addLogEntry("Unsubscribed from Pusher ticker channel: " + tickerchName);
			//Trader.addLogEntry("Unsubscribed from Pusher trade channel: " + tradechName);
		}
		pusher.unsubscribe(tickerchName);
		pusher.unsubscribe(tradechName);
	}
	
	private static void subscribeTickerChannel(int chNumber) {
		String chName = "ticker." + chNumber;
		
		@SuppressWarnings("unused")
		Channel channel = pusher.subscribe(chName, new ChannelEventListener() {
			int ListN;
			
		    @Override
		    public void onSubscriptionSucceeded(String channelName) {
		    	int marketid = -1;
	    		try {
	    			marketid = Integer.parseInt(channelName.substring(channelName.indexOf('.')+1, channelName.length() ) );
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			Trader.addLogEntry("Couldn't read marketid from Pushapi channel name!");
	    		}
	    		
		    	synchronized (PubCapi.tickerFullList) {
		    		
		    		if (tickerFullList.contains(new tickerTickList(marketid)) ) {
		    			ListN=tickerFullList.indexOf(new tickerTickList(marketid) );
		    		} else {
		    			ListN=tickerFullList.size();
		    			tickerFullList.add(new tickerTickList(new LinkedList<tickerTick>(), marketid) );
		    		}
		        }
		    	
		    	//Trader.addLogEntry("Subscribed to Pusher ticker channel: " + channelName);
		    }

		    @Override
		    public void onEvent(String channelName, String event, String data) {
		    	tickerTick tick = createTickerTick(data);
		    	if (tick.marketid==tickerFullList.get(ListN).marketid)
		    		synchronized (PubCapi.tickerFullList.get(ListN) ) {
		    			tickerFullList.get(ListN).tickList.add(tick);
		    		}
	    		
	    		//Trader.addLogEntry(data);
		    }		    
		}, eventName);
	}
	
	private static void subscribeTradeChannel(int chNumber) {
		String chName = "trade." + chNumber;
		
		@SuppressWarnings("unused")
		Channel channel = pusher.subscribe(chName, new ChannelEventListener() {
			int ListN;
			
		    @Override
		    public void onSubscriptionSucceeded(String channelName) {
		    	int marketid = -1;
	    		try {
	    			marketid = Integer.parseInt(channelName.substring(channelName.indexOf('.')+1, channelName.length() ) );
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			Trader.addLogEntry("Couldn't read marketid from Pushapi channel name!");
	    		}
		    	
		    	synchronized (PubCapi.tradeFullList) {		    				    			    		
		    		if (tradeFullList.contains(new tradeTickList(marketid)) ) {
		    			ListN=tradeFullList.indexOf(new tradeTickList(marketid) );
		    		} else {
		    			ListN=tradeFullList.size();
		    			tradeFullList.add(new tradeTickList(new LinkedList<tradeTick>(), marketid) );
		    		}		    		
		        }
		    	
		    	//Trader.addLogEntry("Subscribed to Pusher trade channel: " + channelName);
		    	
				PubCapi.getSingleMarketTrades(marketid);
				final int finalmarketid=marketid;		
				(new Thread() {
					int mid = finalmarketid;
					public void run() {
						PrivCapi.getSingleMarketTrades(mid);
					}		  
				}).start();
		    }

		    @Override
		    public void onEvent(String channelName, String event, String data) {
		    	synchronized (PubCapi.tradeFullList.get(ListN) ) {
		    		tradeFullList.get(ListN).tradeList.add(createTradeTick(data) );
		    	}
		    	
		    	int marketid = -1;
	    		try {
	    			marketid = Integer.parseInt(channelName.substring(channelName.indexOf('.')+1, channelName.length() ) );
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			Trader.addLogEntry("Couldn't read marketid from Pushapi channel name!");
	    		}
	    		
	    		if (marketid!=-1 && subscribedChannels.contains(new subscribedChannelData(marketid) ) ) {
	    			int index = subscribedChannels.indexOf(new subscribedChannelData(marketid) );
	    			synchronized (subscribedChannels.get(index).oBGThreadLock) {
	    				subscribedChannels.get(index).oBGThreadLock.notify();
	    			}
	    		}
	    		
		    	//Trader.addLogEntry(data);
		    }		    
		}, eventName);
	}
	
}