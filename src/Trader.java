import java.util.LinkedList;


public class Trader {
	public static LinkedList<String> Log = new LinkedList<String>();
		
	public static void addLogEntry(String entry) {
		String currtime = new java.sql.Timestamp(System.currentTimeMillis()).toString();
		Log.add(currtime + " \"" + entry + "\"");
		Window.LogTextArea.append(currtime + " \"" + entry + "\"\n");
		System.out.println(currtime + " \"" + entry + "\"");
	}
	
	public static void init() {
		Window.createWindow();
		Window.createTray();
		
		//PubCapi.startGetAllMarketsData();
		PubCapi.connectPusherFirst();
		
		DataC.readEverythingFromFiles();
		DataC.startPubCapiListsMonitor();
		
		PrivCapi.getCoinData();
		PrivCapi.getBalances();
		PrivCapi.getAllMyTrades();
	}
	
	public static void onExit() {
		Window.frame.setVisible(false);
		PubCapi.disconnectPusher();
		//try { Thread.sleep(200); } catch (Exception e) { e.printStackTrace(); }
		DataC.writeEverythingToFiles();
		System.exit(0);
	}
	
	
	
	public static void main (String args[]) {
		init();
		//Neural.test3();
		//LSTM.test2();
		
		//System.out.println( );
		
		//PrivCapi.getLongtermOHLCdata(3, 7*86400);
		
		/*long st = System.currentTimeMillis();
		String msg = PrivCapi.getAuthmsgv2("/markets/3/ohlc", "stop=1439024872", "GET");
		//String msg = PrivCapi.getAuthmsg("method=getinfo");
		System.out.println(msg);
		long ct = System.currentTimeMillis();
		System.out.println(ct-st);*/
		
		/*for (int i=0;i<3;i++) {
			(new Thread() {
				  public void run() {
					  long startTime = System.currentTimeMillis();
					  System.out.println(PrivCapi.getInfo() );
					  long currTime = System.currentTimeMillis();
					  System.out.println(currTime - startTime);
				  }
			}).start();
		}*/
		
		//try { Thread.sleep(3000); } catch (Exception e) { }
		
		/*long startTime = System.currentTimeMillis();
		System.out.println(PrivCapi.getMyTrades() );
		long currTime = System.currentTimeMillis();
		System.out.println(currTime - startTime);*/
	}	
}