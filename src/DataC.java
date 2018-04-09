import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;


@SuppressWarnings("unchecked")
public class DataC {
	public static final long datetimeconvconst = 21600;
	private static final long tickerElExpTime = 3600*24; //seconds
	private static final long tradeElExpTime = 3600*24*7; //seconds
	private static final long orderBookElExpTime = 3600*4; //seconds
	
	
	public static long convertDatetime (String dt) {
		try {
			SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = datef.parse(dt);			
			return date.getTime() / 1000 + datetimeconvconst;
		} catch (Exception e) { }		
		return 0;
	}
	
	
	public static void startPubCapiListsMonitor() {
		(new Thread(new PubCapiListsMonitor())).start();
	}
	
	private static class PubCapiListsMonitor implements Runnable {
		public void run() {
			try { Thread.sleep(3600000); } catch (Exception e) { e.printStackTrace(); }
			while (true) {
				ArrayList<PubCapi.tickerTickList> wTickerL = new ArrayList<PubCapi.tickerTickList>();
				ArrayList<PubCapi.tradeTickList> wTradeL = new ArrayList<PubCapi.tradeTickList>();
				//ArrayList<PubCapi.orderBookList> wOrderBookL = new ArrayList<PubCapi.orderBookList>();
				long currTime = System.currentTimeMillis() / 1000;
				
				for (int i=0;i<PubCapi.tickerFullList.size();i++) {
					if (!PubCapi.tickerFullList.get(i).tickList.isEmpty() ) {
						int wTickerLindex = wTickerL.size();
						wTickerL.add(new PubCapi.tickerTickList(new LinkedList<PubCapi.tickerTick>(), PubCapi.tickerFullList.get(i).marketid));
						synchronized (PubCapi.tickerFullList.get(i) ) {
							boolean reachedDest = false;
							while (!reachedDest) {
								if (PubCapi.tickerFullList.get(i).tickList.getFirst().pctimestamp+tickerElExpTime<currTime) {
									wTickerL.get(wTickerLindex).tickList.add(PubCapi.tickerFullList.get(i).tickList.getFirst());
									PubCapi.tickerFullList.get(i).tickList.removeFirst();
								} else reachedDest = true;
							}
						}						
					}
				}
				
				for (int i=0;i<PubCapi.tradeFullList.size();i++) {
					if (!PubCapi.tradeFullList.get(i).tradeList.isEmpty() ) {
						int wTradeLindex = wTradeL.size();
						wTradeL.add(new PubCapi.tradeTickList(new LinkedList<PubCapi.tradeTick>(), PubCapi.tradeFullList.get(i).marketid));
						synchronized (PubCapi.tradeFullList.get(i) ) {
							boolean reachedDest = false;
							while (!reachedDest) {
								if (PubCapi.tradeFullList.get(i).tradeList.getFirst().pctimestamp+tradeElExpTime<currTime) {
									wTradeL.get(wTradeLindex).tradeList.add(PubCapi.tradeFullList.get(i).tradeList.getFirst());
									PubCapi.tradeFullList.get(i).tradeList.removeFirst();
								} else reachedDest = true;
							}
						}						
					}
				}
				
				for (int i=0;i<PubCapi.orderBookFullList.size();i++) {
					if (!PubCapi.orderBookFullList.get(i).orderBookList.isEmpty() ) {
						//int wOrderBookLindex = wOrderBookL.size();
						//wOrderBookL.add(new PubCapi.orderBookList(new LinkedList<PubCapi.orderBook>(), PubCapi.orderBookFullList.get(i).marketid));
						synchronized (PubCapi.orderBookFullList.get(i) ) {
							boolean reachedDest = false;
							while (!reachedDest) {
								if (PubCapi.orderBookFullList.get(i).orderBookList.getFirst().pctimestamp+orderBookElExpTime<currTime) {
									//wOrderBookL.get(wOrderBookLindex).orderBookList.add(PubCapi.orderBookFullList.get(i).orderBookList.getFirst());
									PubCapi.orderBookFullList.get(i).orderBookList.removeFirst();
								} else reachedDest = true;
							}
						}
					}
				}
				
				writeTickerFullListFiles(wTickerL);
				writeTradeFullListFiles(wTradeL);
				//writeOrderBookFullListFiles(wOrderBookL);
				
				try { Thread.sleep(3600000); } catch (Exception e) { e.printStackTrace(); }
			}
		}		
	}
	
	
	
	public static void writeEverythingToFiles () {
		writeSubscribedPusherChannelsFile();
		writeSimplyestMarketDataListFile();
		writeCoinDataListFile();
		writeMyTradesListFile();
		
		writeTickerFullListFiles(PubCapi.tickerFullList);		
		writeTradeFullListFiles(PubCapi.tradeFullList);
		writeOrderBookFullListFiles(PubCapi.orderBookFullList);
	}
	
	private static void writeSubscribedPusherChannelsFile() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		File markets = new File("data/Markets");
		if (!markets.exists() ) {
			markets.mkdirs();
		}
		
		try {
			File subPCs = new File("data/Markets/SubscribedChannels.jdat");
			if (!subPCs.exists() ) {
				subPCs.createNewFile();
			}
			
			fout = new FileOutputStream(subPCs, false);
			oos = new ObjectOutputStream(fout);
			
			ArrayList<PubCapi.subscribedChannelData> tempSCs = new ArrayList<PubCapi.subscribedChannelData>();
			for (int i=0;i<PubCapi.subscribedChannels.size();i++) {
				if (PubCapi.subscribedChannels.get(i).marketid>0)
					tempSCs.add(new PubCapi.subscribedChannelData(PubCapi.subscribedChannels.get(i).marketid) );
			}
			
			tempSCs.sort(new Comparator<PubCapi.subscribedChannelData>(){
				public int compare(PubCapi.subscribedChannelData s1, PubCapi.subscribedChannelData s2) {
					if (s1.marketid>s2.marketid) return 1;
					else if (s1.marketid<s2.marketid) return -1;
					else return 0;						
				}
			});
			
			oos.writeObject(tempSCs);
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				oos.close();
				fout.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void writeSimplyestMarketDataListFile() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		if (!PubCapi.simplyestMarketDataList.isEmpty() ) {
			File markets = new File("data/Markets");
			if (!markets.exists() ) {
				markets.mkdirs();
			}
			
			try {
				File marketsList = new File("data/Markets/MarketsList.jdat");
				if (!marketsList.exists() ) {
					marketsList.createNewFile();
				}
				
				fout = new FileOutputStream(marketsList, false);
				oos = new ObjectOutputStream(fout);
				oos.writeObject(PubCapi.simplyestMarketDataList);
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				try {
					oos.close();
					fout.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private static void writeCoinDataListFile() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		if (!PrivCapi.coinDataList.isEmpty() ) {
			File markets = new File("data/Markets");
			if (!markets.exists() ) {
				markets.mkdirs();
			}
			
			try {
				File coinDataList = new File("data/Markets/CoinDataList.jdat");
				if (!coinDataList.exists() ) {
					coinDataList.createNewFile();
				}
				
				fout = new FileOutputStream(coinDataList, false);
				oos = new ObjectOutputStream(fout);
				oos.writeObject(PrivCapi.coinDataList);
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				try {
					oos.close();
					fout.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private static void writeMyTradesListFile() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		if (!PrivCapi.myTradesList.isEmpty() ) {
			File markets = new File("data/Markets");
			if (!markets.exists() ) {
				markets.mkdirs();
			}
			
			try {
				File myTradesList = new File("data/Markets/MyTradesList.jdat");
				if (!myTradesList.exists() ) {
					myTradesList.createNewFile();
				}
				
				fout = new FileOutputStream(myTradesList, false);
				oos = new ObjectOutputStream(fout);
				oos.writeObject(PrivCapi.myTradesList);
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				try {
					oos.close();
					fout.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private static void writeTickerFullListFiles(ArrayList<PubCapi.tickerTickList> writableTL) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		File tickers = new File("data/Markets/Tickers");
		if (!tickers.exists() ) {
			tickers.mkdirs();
		}
		
		for (int i=0;i<writableTL.size();i++) {
			if (!writableTL.get(i).tickList.isEmpty() && writableTL.get(i).marketid>0) {
				try {
					File ticker = new File("data/Markets/Tickers/Ticker_" + writableTL.get(i).marketid + ".jdat");
					if (!ticker.exists() ) {
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!writableTL.get(i).tickList.isEmpty() ) {
								if (writableTL.get(i).tickList.getFirst().pctimestamp+tickerElExpTime*7<currTime)
									writableTL.get(i).tickList.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
						
						if (!writableTL.get(i).tickList.isEmpty()) {
							ticker.createNewFile();
							
							fout = new FileOutputStream(ticker, false);
							oos = new ObjectOutputStream(fout);
							oos.writeObject(writableTL.get(i).tickList);
							
							oos.close();
							fout.close();
						}					
					} else {
						fin = new FileInputStream(ticker);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.tickerTick> tempTL;
						tempTL = (LinkedList<PubCapi.tickerTick>) ois.readObject();
						
						ois.close();
						fin.close();												
						
						long lastTickTime = tempTL.getLast().timestamp;
						for (int j=0;j<writableTL.get(i).tickList.size();j++) {
							if (writableTL.get(i).tickList.get(j).timestamp>lastTickTime)
								tempTL.add(writableTL.get(i).tickList.get(j));
						}
						
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!tempTL.isEmpty() ) {
								if (tempTL.getFirst().pctimestamp+tickerElExpTime*7<currTime)
									tempTL.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
						
						if (!tempTL.isEmpty() ) {
							fout = new FileOutputStream(ticker, false);
							oos = new ObjectOutputStream(fout);
							
							oos.writeObject(tempTL);
							
							oos.close();
							fout.close();
						} else ticker.delete();
					}								
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}		
		
	}
	
	private static void writeTradeFullListFiles(ArrayList<PubCapi.tradeTickList> writableTL) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		File trades = new File("data/Markets/Trades");
		if (!trades.exists() ) {
			trades.mkdirs();
		}
		
		for (int i=0;i<writableTL.size();i++) {
			if (!writableTL.get(i).tradeList.isEmpty() && writableTL.get(i).marketid>0) {
				try {
					File trade = new File("data/Markets/Trades/Trade_" + writableTL.get(i).marketid + ".jdat");
					if (!trade.exists() ) {
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!writableTL.get(i).tradeList.isEmpty() ) {
								if (writableTL.get(i).tradeList.getFirst().pctimestamp+tradeElExpTime*4<currTime)
									writableTL.get(i).tradeList.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
						
						if (!writableTL.get(i).tradeList.isEmpty()) {
							trade.createNewFile();
							
							fout = new FileOutputStream(trade, false);
							oos = new ObjectOutputStream(fout);
							oos.writeObject(writableTL.get(i).tradeList);
							
							oos.close();
							fout.close();
						}					
					} else {
						fin = new FileInputStream(trade);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.tradeTick> tempTL;
						tempTL = (LinkedList<PubCapi.tradeTick>) ois.readObject();
						
						ois.close();
						fin.close();						
						
						long lastTradeId = tempTL.getLast().tradeid;
						for (int j=0;j<writableTL.get(i).tradeList.size();j++) {
							if (writableTL.get(i).tradeList.get(j).tradeid>lastTradeId)
								tempTL.add(writableTL.get(i).tradeList.get(j));
						}
						
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!tempTL.isEmpty() ) {
								if (tempTL.getFirst().pctimestamp+tradeElExpTime*4<currTime)
									tempTL.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
						
						if (!tempTL.isEmpty() ) {
							fout = new FileOutputStream(trade, false);
							oos = new ObjectOutputStream(fout);
							
							oos.writeObject(tempTL);
							
							oos.close();
							fout.close();
						} else trade.delete();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
		
	}
	
	private static void writeOrderBookFullListFiles(ArrayList<PubCapi.orderBookList> writableOBL) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		File orderBooks = new File("data/Markets/OrderBooks");
		if (!orderBooks.exists() ) {
			orderBooks.mkdirs();
		}
		
		for (int i=0;i<writableOBL.size();i++) {
			if (!writableOBL.get(i).orderBookList.isEmpty() && writableOBL.get(i).marketid>0) {
				try {
					File orderBook = new File("data/Markets/OrderBooks/OrderBook_" + writableOBL.get(i).marketid + ".jdat");
					if (!orderBook.exists() ) {
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!writableOBL.get(i).orderBookList.isEmpty() ) {
								if (writableOBL.get(i).orderBookList.getFirst().pctimestamp+orderBookElExpTime<currTime)
									writableOBL.get(i).orderBookList.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
						
						if (!writableOBL.get(i).orderBookList.isEmpty()) {
							orderBook.createNewFile();
							
							fout = new FileOutputStream(orderBook, false);
							oos = new ObjectOutputStream(fout);
							oos.writeObject(writableOBL.get(i).orderBookList);
							
							oos.close();
							fout.close();
						}					
					} else {
						fin = new FileInputStream(orderBook);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.orderBook> tempOBL;
						tempOBL = (LinkedList<PubCapi.orderBook>) ois.readObject();
						
						ois.close();
						fin.close();
						
						long lastTickTime = tempOBL.getLast().pctimestamp;
						for (int j=0;j<writableOBL.get(i).orderBookList.size();j++) {
							if (writableOBL.get(i).orderBookList.get(j).pctimestamp>lastTickTime)
								tempOBL.add(writableOBL.get(i).orderBookList.get(j));
						}
						
						long currTime = System.currentTimeMillis() / 1000;
						boolean reachedDest = false;
						while (!reachedDest) {
							if (!tempOBL.isEmpty() ) {
								if (tempOBL.getFirst().pctimestamp+orderBookElExpTime<currTime)
									tempOBL.removeFirst();
								else reachedDest = true;
							} else reachedDest = true;							
						}
																		
						if (!tempOBL.isEmpty() ) {
							fout = new FileOutputStream(orderBook, false);
							oos = new ObjectOutputStream(fout);
							
							oos.writeObject(tempOBL);
							
							oos.close();
							fout.close();
						} else orderBook.delete();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}		
		
	}
	
	
	public static void writeNeuralWeightsFile(float[] Ws) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		File neural = new File("data/Neural");
		if (!neural.exists() ) {
			neural.mkdirs();
		}
		
		if (Ws.length!=0) {
			try {
				File wsfile = new File("data/Neural/Weights.jdat");
				if (!wsfile.exists() )
					wsfile.createNewFile();
					
				fout = new FileOutputStream(wsfile, false);
				oos = new ObjectOutputStream(fout);
				oos.writeObject(Ws);
					
				oos.close();
				fout.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
	
	public static void writeNeuralSampleDataFile() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		File neural = new File("data/Neural");
		if (!neural.exists() ) {
			neural.mkdirs();
		}
		
		for (int j=0;j<PrivCapi.ohlcFullList.size();j++) {
			if (!PrivCapi.ohlcFullList.get(j).ohlcList.isEmpty() && PrivCapi.ohlcFullList.get(j).marketid>0) {
				int mid = PrivCapi.ohlcFullList.get(j).marketid;
				
				try {
					File samplefile = new File("data/Neural/OHLCSample_" + mid + ".jdat");
					if (!samplefile.exists() )
						samplefile.createNewFile();
						
					fout = new FileOutputStream(samplefile, false);
					oos = new ObjectOutputStream(fout);
					oos.writeObject(PrivCapi.ohlcFullList.get(j).ohlcList);
						
					oos.close();
					fout.close();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}
	
	
	
	public static void readEverythingFromFiles() {
		readCoinDataListFile();
		readMyTradesListFile();
		readSubscribedPusherChannelsFile();
		
		readTickerFullListFiles();
		readTradeFullListFiles();
		readOrderBookFullListFiles();		
		
		readSimplyestMarketDataListFile();	
		
		Window.updateMarketsListModel();
	}
	
	private static void readSubscribedPusherChannelsFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File subPCs = new File("data/Markets/SubscribedChannels.jdat");
			if (subPCs.exists() ) {
				fin = new FileInputStream(subPCs);
				ois = new ObjectInputStream(fin);
				
				PubCapi.subscribedChannels = (ArrayList<PubCapi.subscribedChannelData>) ois.readObject();
				for (int i=0;i<PubCapi.subscribedChannels.size();i++) {
					PubCapi.subscribePusherChannel(PubCapi.subscribedChannels.get(i).marketid );
					PubCapi.startOrderBookGetting(PubCapi.subscribedChannels.get(i).marketid );
				}				
				
				ois.close();
				fin.close();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private static void readSimplyestMarketDataListFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File marketsList = new File("data/Markets/MarketsList.jdat");
			if (marketsList.exists() ) {
				fin = new FileInputStream(marketsList);
				ois = new ObjectInputStream(fin);
				
				PubCapi.simplyestMarketDataList = (LinkedList<PubCapi.simplyestMarketData>) ois.readObject();
				
				ois.close();
				fin.close();
				
				if (!PubCapi.simplyestMarketDataList.isEmpty() ) {
					Window.updateMarketListTableModel();
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private static void readCoinDataListFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File coinDataList = new File("data/Markets/CoinDataList.jdat");
			if (coinDataList.exists() ) {
				fin = new FileInputStream(coinDataList);
				ois = new ObjectInputStream(fin);
				
				PrivCapi.coinDataList = (ArrayList<PrivCapi.coinData>) ois.readObject();
				
				ois.close();
				fin.close();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		Window.updateBalances();
	}
	
	private static void readMyTradesListFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File myTradesList = new File("data/Markets/MyTradesList.jdat");
			if (myTradesList.exists() ) {
				fin = new FileInputStream(myTradesList);
				ois = new ObjectInputStream(fin);
				
				PrivCapi.myTradesList = (LinkedList<PrivCapi.myTrade>) ois.readObject();
				
				ois.close();
				fin.close();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		Window.updateMyTrades();
	}
	
	private static void readTickerFullListFiles() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File tickersFolder = new File("data/Markets/Tickers");
			if (tickersFolder.exists() ) {
				FilenameFilter filter = new FilenameFilter() {
					@Override
		            public boolean accept(File dir, String name) {
						if (name.contains("Ticker_") && name.contains(".jdat") )
							return true;
						else return false;
					}
				};
				
				File[] tickerFiles = tickersFolder.listFiles(filter);
				int i=0;
				while (i<tickerFiles.length) {
					String filename = tickerFiles[i].getName();
					int mid = Integer.parseInt(filename.substring(filename.indexOf("Ticker_")+7, filename.indexOf(".jdat") ) );
								
					if (PubCapi.subscribedChannels.contains(new PubCapi.subscribedChannelData(mid))) {
						fin = new FileInputStream(tickerFiles[i]);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.tickerTick> tempTL;
						tempTL = (LinkedList<PubCapi.tickerTick>) ois.readObject();
										
						ois.close();
						fin.close();
							
						int tFLi = PubCapi.tickerFullList.size();
						PubCapi.tickerFullList.add(new PubCapi.tickerTickList(new LinkedList<PubCapi.tickerTick>(), mid));
						long currTime = System.currentTimeMillis() / 1000;
						for (int j=0;j<tempTL.size();j++) {
							if (tempTL.get(j).pctimestamp+tickerElExpTime>currTime)
								PubCapi.tickerFullList.get(tFLi).tickList.add(tempTL.get(j));	
						}
					} else 
						tickerFiles[i].delete();
										
					i++;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private static void readTradeFullListFiles() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File tradesFolder = new File("data/Markets/Trades");
			if (tradesFolder.exists() ) {
				FilenameFilter filter = new FilenameFilter() {
					@Override
		            public boolean accept(File dir, String name) {
						if (name.contains("Trade_") && name.contains(".jdat") )
							return true;
						else return false;
					}
				};
				
				File[] tradeFiles = tradesFolder.listFiles(filter);
				int i=0;
				while (i<tradeFiles.length) {
					String filename = tradeFiles[i].getName();
					int mid = Integer.parseInt(filename.substring(filename.indexOf("Trade_")+6, filename.indexOf(".jdat") ) );
					
					if (PubCapi.subscribedChannels.contains(new PubCapi.subscribedChannelData(mid))) {
						fin = new FileInputStream(tradeFiles[i]);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.tradeTick> tempTL;
						tempTL = (LinkedList<PubCapi.tradeTick>) ois.readObject();
						
						ois.close();
						fin.close();
						
						int tFLi = PubCapi.tradeFullList.size();
						PubCapi.tradeFullList.add(new PubCapi.tradeTickList(new LinkedList<PubCapi.tradeTick>(), mid));
						long currTime = System.currentTimeMillis() / 1000;
						for (int j=0;j<tempTL.size();j++) {
							if (tempTL.get(j).pctimestamp+tradeElExpTime>currTime)
								PubCapi.tradeFullList.get(tFLi).tradeList.add(tempTL.get(j));
						}
					} else
						tradeFiles[i].delete();				
					
					i++;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private static void readOrderBookFullListFiles() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File orderBooksFolder = new File("data/Markets/OrderBooks");
			if (orderBooksFolder.exists() ) {
				FilenameFilter filter = new FilenameFilter() {
					@Override
		            public boolean accept(File dir, String name) {
						if (name.contains("OrderBook_") && name.contains(".jdat") )
							return true;
						else return false;
					}
				};
				
				File[] orderBookFiles = orderBooksFolder.listFiles(filter);
				int i=0;
				while (i<orderBookFiles.length) {
					String filename = orderBookFiles[i].getName();
					int mid = Integer.parseInt(filename.substring(filename.indexOf("OrderBook_")+10, filename.indexOf(".jdat") ) );
					
					if (PubCapi.subscribedChannels.contains(new PubCapi.subscribedChannelData(mid))) {
						fin = new FileInputStream(orderBookFiles[i]);
						ois = new ObjectInputStream(fin);
						
						LinkedList<PubCapi.orderBook> tempTL;
						tempTL = (LinkedList<PubCapi.orderBook>) ois.readObject();
						
						ois.close();
						fin.close();
						
						int obFLi = PubCapi.orderBookFullList.size();
						PubCapi.orderBookFullList.add(new PubCapi.orderBookList(new LinkedList<PubCapi.orderBook>(), mid));
						long currTime = System.currentTimeMillis() / 1000;
						for (int j=0;j<tempTL.size();j++) {
							if (tempTL.get(j).pctimestamp+orderBookElExpTime>currTime)
								PubCapi.orderBookFullList.get(obFLi).orderBookList.add(tempTL.get(j));
						}
					} else
						orderBookFiles[i].delete();
					
					i++;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	
	public static float[] readNeuralWeightsFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File wsfile = new File("data/Neural/Weights.jdat");
			if (wsfile.exists() ) {
				fin = new FileInputStream(wsfile);
				ois = new ObjectInputStream(fin);
				
				float[] ret = (float[]) ois.readObject();
				
				ois.close();
				fin.close();
				return ret;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return new float[0];
	}
	
	public static void readNeuralSampleDataFile() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			File samplefile = new File("data/Neural/OHLCSample_" + 3 + ".jdat");
			if (samplefile.exists() ) {
				fin = new FileInputStream(samplefile);
				ois = new ObjectInputStream(fin);
				
				LinkedList<PrivCapi.ohlcData> ret = (LinkedList<PrivCapi.ohlcData>) ois.readObject();
				
				ois.close();
				fin.close();
				
				int ListN = PrivCapi.ohlcFullList.indexOf(new PrivCapi.ohlcDataList(3) );
				if (ListN!=-1)
					synchronized (PrivCapi.ohlcFullList) {
						PrivCapi.ohlcFullList.set(ListN, new PrivCapi.ohlcDataList(ret, 3) );
					}					
				else
					synchronized (PrivCapi.ohlcFullList) {
						PrivCapi.ohlcFullList.add(new PrivCapi.ohlcDataList(ret, 3) );
					}
				
				Window.updateNeuralCChart(3);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
