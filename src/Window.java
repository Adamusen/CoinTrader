import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.axis.*;
//import org.jfree.chart.plot.*;
//import org.jfree.chart.renderer.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
//import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.util.ShapeUtilities;
//import org.jfree.ui.RectangleAnchor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;


public class Window {
	public static final JFrame frame = new JFrame("Trader");
	public static final JTabbedPane Main_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	public static boolean windowActive = true;
	public static Object chartUpdateLock = new Object();
	
	public static DefaultListModel<String> MarketsListModel = new DefaultListModel<String>();
	public static DefaultOHLCDataset CandlestickChartDataset;
	public static JFreeChart CandlestickChart = ChartFactory.createCandlestickChart("Candlestick chart", "Date", "Price", CandlestickChartDataset, true);
	public static XYSeriesCollection TradeChartDataset = new XYSeriesCollection();
	public static JFreeChart TradeChart = ChartFactory.createXYLineChart("Trade history", "Time", "Price", TradeChartDataset);
	public static XYSeriesCollection TickerChartDataset = new XYSeriesCollection( );
	public static JFreeChart TickerChart = ChartFactory.createXYLineChart("Price history", "Time", "Price", TickerChartDataset);
	//public static JFreeChart OrderDepthChart = new JFreeChart("Orderbook Depth", new XYPlot());
	public static DefaultOHLCDataset NeuralCCDataset;
	public static JFreeChart NeuralCC = ChartFactory.createCandlestickChart("Sample Candlestick chart", "Date", "Price", NeuralCCDataset, true);
	
	public static int selectedMarket = 0;
	
	public static JList<String> MarketsList_JList = new JList<String>(MarketsListModel);
	public static DefaultTableModel MarketListTableModel = new MyDefaultTableModel();
	public static JTable MarketListTable = new JTable(MarketListTableModel);
	public static JTextArea Balances_textArea = new JTextArea();
	public static JTextArea MyTrades_textArea = new JTextArea();
	public static JTextArea LogTextArea = new JTextArea();
	
	public static JMenuItem mntmConnectPusher = new JMenuItem("Connect Pusher");
	public static JMenuItem mntmDisconnectPusher = new JMenuItem("Disconnect Pusher");
	public static JMenuItem mntmReloadMarketList = new JMenuItem("Reload MarketList");
	public static JMenuItem mntmGetCoinsData = new JMenuItem("Get Coins Data");
	public static JMenuItem mntmGetBalances = new JMenuItem("Get Balances");
	public static JMenuItem mntmGetMytrades = new JMenuItem("Get MyTrades");
	public static JMenuItem mntmGetSampleData = new JMenuItem("Get Sample Data");
	
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public static void createWindow() {
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("data/Icons/trayicon.jpg"));
		frame.setSize(874, 480);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		frame.getContentPane().add(Main_tabbedPane);
		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				windowActive = true;
				updateMarketsListModel();
			}
			
			public void windowDeactivated(WindowEvent e) {
				windowActive = false;
			}
		});
		
		JPanel MarketsMain_JPanel = new JPanel();
		MarketsMain_JPanel.setLayout(new BorderLayout(0, 0));
		Main_tabbedPane.addTab("Markets", null, MarketsMain_JPanel, null);	
		
		JScrollPane Markets_scrollPane = new JScrollPane();				
		MarketsList_JList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false && !MarketsList_JList.isSelectionEmpty() ) {
					selectedMarket = Integer.parseInt(MarketsList_JList.getSelectedValue().substring(0, MarketsList_JList.getSelectedValue().indexOf(':')));
					
					CandlestickChart.getXYPlot().getRangeAxis().setAutoRange(true);
					CandlestickChart.getXYPlot().getDomainAxis().setAutoRange(true);
					TradeChart.getXYPlot().getRangeAxis().setAutoRange(true);
					TradeChart.getXYPlot().getDomainAxis().setAutoRange(true);
					TickerChart.getXYPlot().getRangeAxis().setAutoRange(true);
					TickerChart.getXYPlot().getDomainAxis().setAutoRange(true);
					
					callChartsUpdate();
				}			
			}
		});
		MarketsList_JList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Markets_scrollPane.setPreferredSize(new Dimension(130, 0));
		Markets_scrollPane.setViewportView(MarketsList_JList);
		
		MarketsMain_JPanel.add(Markets_scrollPane, BorderLayout.WEST);
		JTabbedPane Markets_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		MarketsMain_JPanel.add(Markets_tabbedPane, BorderLayout.CENTER);
		
		initCharts();		
	    CandlestickChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {
	    	public void axisChanged(AxisChangeEvent event) {
	    		double dAxisLength = CandlestickChart.getXYPlot().getDomainAxis().getRange().getLength();
	    		updateCandelstickChartDataset(calcCCtTS(dAxisLength) );
	    	}
	    });
	    	    
	    ChartPanel MarketsCandelstick_ChartPanel = new ChartPanel(CandlestickChart);
	    Markets_tabbedPane.addTab("Candlestick", null, MarketsCandelstick_ChartPanel, null);    
		ChartPanel MarketsTradeView_ChartPanel = new ChartPanel(TradeChart);
		Markets_tabbedPane.addTab("Trades", null, MarketsTradeView_ChartPanel, null);	
		ChartPanel MarketsTicker_ChartPanel = new ChartPanel(TickerChart);
		Markets_tabbedPane.addTab("Tickers", null, MarketsTicker_ChartPanel, null);		
		//ChartPanel MarketsOrderDepth_ChartPanel = new ChartPanel(OrderDepthChart);
		//Markets_tabbedPane.addTab("OrderDepth", null, MarketsOrderDepth_ChartPanel, null);
		
		JScrollPane MarketList_scrollPane = new JScrollPane();
		Main_tabbedPane.addTab("MarketList", null, MarketList_scrollPane, null);
		Main_tabbedPane.setSelectedIndex(1);
		initMarketListTableModel();		
		MarketList_scrollPane.setViewportView(MarketListTable);
		MarketListTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();
					int column = target.getSelectedColumn();
					
					if (column == 7) {
						if (!PubCapi.subscribedChannels.contains(new PubCapi.subscribedChannelData(PubCapi.simplyestMarketDataList.get(row).marketid) ) ) {
							PubCapi.subscribePusherChannel(PubCapi.simplyestMarketDataList.get(row).marketid );
							updateMarketListTableModel();
							updateMarketsListModel();
						} else {
							PubCapi.unsubscribePusherChannel(PubCapi.simplyestMarketDataList.get(row).marketid );
							updateMarketListTableModel();
							updateMarketsListModel();
						}			    	  
					} else {
						PubCapi.getSingleMarketDataForMarketsList(PubCapi.simplyestMarketDataList.get(row).marketid );
					}
				}
			}
		});
		
		JPanel BsandTs_JPanel = new JPanel();
		Main_tabbedPane.addTab("Bal. and Th.", null, BsandTs_JPanel, null);
		BsandTs_JPanel.setLayout(new BorderLayout(0, 0));		
		JScrollPane Balances_scrollPane = new JScrollPane();
		BsandTs_JPanel.add(Balances_scrollPane, BorderLayout.WEST);
		Font bfont = new Font(Balances_textArea.getFont().getFontName(), Font.BOLD, 16);
		Balances_textArea.setFont(bfont);
		Balances_scrollPane.setPreferredSize(new Dimension(180, 0));
		Balances_scrollPane.setViewportView(Balances_textArea);	
		Balances_textArea.setEditable(false);
		JScrollPane MyTrades_scrollPane = new JScrollPane();
		BsandTs_JPanel.add(MyTrades_scrollPane, BorderLayout.CENTER);
		MyTrades_scrollPane.setViewportView(MyTrades_textArea);
		MyTrades_textArea.setEditable(false);
		
		JTabbedPane NeuralTrainer_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		Main_tabbedPane.addTab("Neural Trainer", null, NeuralTrainer_tabbedPane, null);
		ChartPanel NeuralCandelstick_ChartPanel = new ChartPanel(NeuralCC);
		NeuralTrainer_tabbedPane.addTab("Candlestick", null, NeuralCandelstick_ChartPanel, null);
				
		JScrollPane Log_scrollPane = new JScrollPane();
		Log_scrollPane.setViewportView(LogTextArea);
		LogTextArea.setEditable(false);
		Main_tabbedPane.addTab("Log", null, Log_scrollPane, null);

				
		initMenuBar();
		
		frame.setVisible(true);
	}
	
	private static void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);		
		
		JMenu mnMainMenu = new JMenu("Main Menu");
		menuBar.add(mnMainMenu);		
		JMenuItem mntmGarbageCollect = new JMenuItem("Collect Garbage");
		mntmGarbageCollect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				System.gc();
			}
			
		});
		mnMainMenu.add(mntmGarbageCollect);		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Trader.onExit();
			}
		});
		
		JSeparator sp1MainMenu = new JSeparator();
		mnMainMenu.add(sp1MainMenu);
		mnMainMenu.add(mntmExit);
		
		JMenu mnPublicApi = new JMenu("Public Api");
		menuBar.add(mnPublicApi);
		mntmConnectPusher.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PubCapi.reconnectPusher();
			}
		});
		mntmDisconnectPusher.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				PubCapi.disconnectPusher();
			}
		});
		mntmDisconnectPusher.setEnabled(false);
		mnPublicApi.add(mntmConnectPusher);	
		mnPublicApi.add(mntmDisconnectPusher);		
		JSeparator sp1PublicApi = new JSeparator();
		mnPublicApi.add(sp1PublicApi);	
		mntmReloadMarketList.setEnabled(true);
		mntmReloadMarketList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PubCapi.startGetAllMarketsData();
			}
		});
		mnPublicApi.add(mntmReloadMarketList);
		
		JMenu mnPrivateApi = new JMenu("Private Api");
		menuBar.add(mnPrivateApi);
					
		mntmGetCoinsData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread () {
					public void run() {
						PrivCapi.getCoinData();
					}
				}.start();
			}
		});
		mntmGetBalances.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread () {
					public void run() {
						PrivCapi.getBalances();
					}
				}.start();
			}
		});
		mntmGetMytrades.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread () {
					public void run() {
						PrivCapi.getAllMyTrades();
					}
				}.start();
			}
		});
		
		mntmGetCoinsData.setEnabled(false);
		mnPrivateApi.add(mntmGetCoinsData);
		JSeparator sp1PrivateApi = new JSeparator();
		mnPrivateApi.add(sp1PrivateApi);
		mntmGetBalances.setEnabled(false);
		mnPrivateApi.add(mntmGetBalances);
		mntmGetMytrades.setEnabled(false);
		mnPrivateApi.add(mntmGetMytrades);
		
		JMenu mnNeuralNet = new JMenu("Neural net");
		menuBar.add(mnNeuralNet);
		
		mntmGetSampleData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread () {
					public void run() {
						PrivCapi.getLongtermOHLCdata(3, 86400*365);
					}
				}.start();			
			}
		});
		mnNeuralNet.add(mntmGetSampleData);
		
		JMenuItem mntmSaveSampleData = new JMenuItem("Save Sample Data");
		mntmSaveSampleData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//DataC.writeNeuralSampleDataFile();
			}
		});
		mnNeuralNet.add(mntmSaveSampleData);
		
		JMenuItem mntmLoadSampleData = new JMenuItem("Load Sample Data");
		mntmLoadSampleData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread () {
					public void run() {
						DataC.readNeuralSampleDataFile();
					}
				}.start();
			}
		});
		mnNeuralNet.add(mntmLoadSampleData);
	}
	
	
	public static void updateMarketsListModel() {
		ArrayList<PubCapi.subscribedChannelData> tempsCL = PubCapi.subscribedChannels;
		tempsCL.sort(new Comparator<PubCapi.subscribedChannelData>(){
			public int compare(PubCapi.subscribedChannelData s1, PubCapi.subscribedChannelData s2) {
				if (s1.marketid>s2.marketid) return 1;
				else if (s1.marketid<s2.marketid) return -1;
				else return 0;						
			}
		});
		
		int si = MarketsList_JList.getSelectedIndex();
		MarketsListModel.clear();
		
		for (int i=0;i<tempsCL.size();i++) {
			if (tempsCL.get(i).marketid>0) {
				int mid = tempsCL.get(i).marketid;
				String marketLabel = "";
				if (PubCapi.simplyestMarketDataList.contains(new PubCapi.simplyestMarketData(mid) ) )
					marketLabel = PubCapi.simplyestMarketDataList.get(PubCapi.simplyestMarketDataList.indexOf(new PubCapi.simplyestMarketData(mid) ) ).label;
				String Name = mid + ": " + marketLabel;
				
				MarketsListModel.addElement(Name);
			}
		}
		
		if (si<MarketsListModel.getSize() && si!=-1)
			MarketsList_JList.setSelectedIndex(si);
		else MarketsList_JList.setSelectedIndex(0);
	}
	
	private static void initCharts() {
		NumberAxis yAxis = (NumberAxis) TradeChart.getXYPlot().getRangeAxis();
	    yAxis.setAutoRangeIncludesZero(false);
	    yAxis.setNumberFormatOverride(new DecimalFormat("#0.00000000"));
	    yAxis = (NumberAxis) CandlestickChart.getXYPlot().getRangeAxis();
	    yAxis.setAutoRangeIncludesZero(false);
	    yAxis.setNumberFormatOverride(new DecimalFormat("#0.00000000"));
	    yAxis = (NumberAxis) TickerChart.getXYPlot().getRangeAxis();
	    yAxis.setAutoRangeIncludesZero(false);
	    yAxis.setNumberFormatOverride(new DecimalFormat("#0.00000000"));
	    yAxis = (NumberAxis) NeuralCC.getXYPlot().getRangeAxis();
	    yAxis.setAutoRangeIncludesZero(false);
	    yAxis.setNumberFormatOverride(new DecimalFormat("#0.00000000"));
	    
	    DateAxis candlestickXAxis = (DateAxis) CandlestickChart.getXYPlot().getDomainAxis();
	    candlestickXAxis.setLowerMargin(0.0);
	    candlestickXAxis.setUpperMargin(0.0);
	    
	    try {
	    	DateAxis xAxis = (DateAxis) CandlestickChart.getXYPlot().getDomainAxis().clone();
	    	xAxis.setLowerMargin(0.0);
	        xAxis.setUpperMargin(0.0);
	    	TradeChart.getXYPlot().setDomainAxis(xAxis);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
		renderer.setSeriesPaint( 0 , Color.RED );
	    renderer.setSeriesPaint( 1 , Color.BLUE);
	    renderer.setSeriesStroke( 0 , new BasicStroke( 1f ) );
	    renderer.setSeriesStroke( 1 , new BasicStroke( 1f ) );
	    renderer.setBaseLinesVisible(false);
	    renderer.setSeriesShape(0, ShapeUtilities.createDiamond( 2.5f ));
	    renderer.setSeriesShape(1, ShapeUtilities.createDiamond( 2.5f ));
	    TradeChart.getXYPlot().setRenderer(renderer);
	    
	    try {
	    	DateAxis xAxis = (DateAxis) CandlestickChart.getXYPlot().getDomainAxis().clone();
	    	xAxis.setLowerMargin(0.0);
	        xAxis.setUpperMargin(0.0);
		    TickerChart.getXYPlot().setDomainAxis(xAxis);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    renderer = new XYLineAndShapeRenderer( );
		renderer.setSeriesPaint( 0 , Color.RED );
	    renderer.setSeriesPaint( 1 , Color.BLUE);
	    renderer.setSeriesStroke( 0 , new BasicStroke( 1f ) );
	    renderer.setSeriesStroke( 1 , new BasicStroke( 1f ) );
	    renderer.setBaseLinesVisible(true);
	    renderer.setSeriesShape(0, ShapeUtilities.createDiamond( 1f ));
	    renderer.setSeriesShape(1, ShapeUtilities.createDiamond( 1f ));
	    TickerChart.getXYPlot().setRenderer(renderer);
	    
	    /*OrderDepthChart.setAntiAlias(false);
	    XYPlot orderDepthPlot = OrderDepthChart.getXYPlot();
	    try {
	    	DateAxis xAxis = (DateAxis) CandlestickChart.getXYPlot().getDomainAxis().clone();
	    	xAxis.setLowerMargin(0.0);
	        xAxis.setUpperMargin(0.0);
	    	orderDepthPlot.setDomainAxis(xAxis);
	    	
	    	yAxis = (NumberAxis) CandlestickChart.getXYPlot().getRangeAxis().clone();
		    yAxis.setAutoRangeIncludesZero(false);
		    yAxis.setUpperMargin(0.0);
		    orderDepthPlot.setRangeAxis(yAxis);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	    
	    XYBlockRenderer renderer2 = new XYBlockRenderer();
        renderer2.setBlockAnchor(RectangleAnchor.CENTER);
        orderDepthPlot.setRenderer(0, renderer2);
        orderDepthPlot.setRenderer(1, TickerChart.getXYPlot().getRenderer());
        orderDepthPlot.setBackgroundPaint(Color.lightGray);
        orderDepthPlot.setRangeGridlinePaint(Color.white);
        OrderDepthChart.removeLegend();
        OrderDepthChart.setBackgroundPaint(Color.white);*/
	    
	    
	    (new Thread(new updateChartsThread())).start();
	}
	
	public static void callChartsUpdate() {
		synchronized (chartUpdateLock) {
			chartUpdateLock.notify();
		}
	}
	
	private static class updateChartsThread implements Runnable {
		public void run() {
			synchronized (chartUpdateLock) {
				try { chartUpdateLock.wait(10000); } catch (Exception e) { e.printStackTrace(); }
			}			
			while (true) {
				if (windowActive) {
					updateCharts();
					synchronized (chartUpdateLock) {
						try { chartUpdateLock.wait(10000); } catch (Exception e) { e.printStackTrace(); }
					}			
				} else synchronized (chartUpdateLock) {
					try { chartUpdateLock.wait(); } catch (Exception e) { e.printStackTrace(); }
				}
			}
		}
	}
	
	private static void updateCharts() {
		int ListN = PubCapi.tradeFullList.indexOf(new PubCapi.tradeTickList(selectedMarket) );
		if (ListN!=-1) {
			if (CandlestickChart.getXYPlot().getDomainAxis().isAutoRange()) {
				double dAxisLength = (PubCapi.tradeFullList.get(ListN).tradeList.getLast().timestamp - PubCapi.tradeFullList.get(ListN).tradeList.getFirst().timestamp)*1000;
				updateCandelstickChartDataset(calcCCtTS(dAxisLength) );
			} else {
				double dAxisLength = CandlestickChart.getXYPlot().getDomainAxis().getRange().getLength();
	    		updateCandelstickChartDataset(calcCCtTS(dAxisLength) );
			}			
		}			
		updateTradeChartDataset();
		updateTickerChartDataset();
		//updateOrderDepthChartDataset();
	}
	
	private static long calcCCtTS(double dAxisLength) {
		if (dAxisLength<21600000)
			return 300000;
		else if (dAxisLength<86400000)
			return 900000;
		else if (dAxisLength<604800000)
			return 10800000;
		else return 43200000;
	}
	
	private static void updateCandelstickChartDataset(long tickTimeSpan) {
		int ListN = PubCapi.tradeFullList.indexOf(new PubCapi.tradeTickList(selectedMarket) );
		if (ListN!=-1) {
			if (!PubCapi.tradeFullList.get(ListN).tradeList.isEmpty() ) {
				ArrayList<OHLCDataItem> dataItems = new ArrayList<OHLCDataItem>();
				
				try {					
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = df.parse(PubCapi.tradeFullList.get(ListN).tradeList.getFirst().datetime);
					
					date.setTime((date.getTime() / tickTimeSpan) * tickTimeSpan);
					Date nextdate= new Date();
					nextdate.setTime(date.getTime()+tickTimeSpan);
					
					double open=PubCapi.tradeFullList.get(ListN).tradeList.getFirst().price;
					double high=PubCapi.tradeFullList.get(ListN).tradeList.getFirst().price;
					double low=PubCapi.tradeFullList.get(ListN).tradeList.getFirst().price;
					double close=0;
					double volume=0;
					
					int i=0;
					while (i<PubCapi.tradeFullList.get(ListN).tradeList.size() ) {
						PubCapi.tradeTick tt = PubCapi.tradeFullList.get(ListN).tradeList.get(i);
						Date d = df.parse(tt.datetime);
						if (!d.after(nextdate) ) {
							if (tt.price>high)
								high=tt.price;
							if (tt.price<low)
								low=tt.price;
							close=tt.price;
							volume+=tt.quantity;
							i++;
						} else {
							OHLCDataItem item = new OHLCDataItem(new Date(date.getTime()+DataC.datetimeconvconst*1000), open, high, low, close, volume);
							dataItems.add(item);
							
							open = tt.price;
							high = tt.price;
							low = tt.price;
							close = tt.price;
							volume = 0;
							
							date.setTime(date.getTime()+tickTimeSpan);
							nextdate.setTime(date.getTime()+tickTimeSpan);
						}
					}
					
					OHLCDataItem item = new OHLCDataItem(new Date(date.getTime()+DataC.datetimeconvconst*1000), open, high, low, close, volume);
					dataItems.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String title = PubCapi.simplyestMarketDataList.get(PubCapi.simplyestMarketDataList.indexOf(new PubCapi.simplyestMarketData(selectedMarket) ) ).label;
				CandlestickChartDataset = new DefaultOHLCDataset(title, dataItems.toArray(new OHLCDataItem[dataItems.size()]) );
				CandlestickChart.getXYPlot().setDataset(CandlestickChartDataset);
			}			
		}
	}
	
	private static void updateTradeChartDataset() {
		int ListN = PubCapi.tradeFullList.indexOf(new PubCapi.tradeTickList(selectedMarket) );
		if (ListN!=-1) {
			final XYSeries buySeries = new XYSeries("Buys");
			final XYSeries sellSeries = new XYSeries("Sells");
			
			for (int i=0;i<PubCapi.tradeFullList.get(ListN).tradeList.size();i++) {
				PubCapi.tradeTick tt = PubCapi.tradeFullList.get(ListN).tradeList.get(i);
				if (tt.type.equals("Buy")) {
					buySeries.add(tt.timestamp*1000, tt.price);
				} else {
					sellSeries.add(tt.timestamp*1000, tt.price);
				}
			}
			
			TradeChartDataset.removeAllSeries();
			TradeChartDataset.addSeries(buySeries);
			TradeChartDataset.addSeries(sellSeries);
		}
	}
	
	private static void updateTickerChartDataset() {
		int ListN = PubCapi.tickerFullList.indexOf(new PubCapi.tickerTickList(selectedMarket));
		if (ListN!=-1) {
			final XYSeries buySeries = new XYSeries("Topbuys");
			final XYSeries sellSeries = new XYSeries("Topsells");				
			
			for (int i=0;i<PubCapi.tickerFullList.get(ListN).tickList.size();i++) {
				PubCapi.tickerTick tt = PubCapi.tickerFullList.get(ListN).tickList.get(i);
				buySeries.add(tt.timestamp*1000, tt.topbuyprice);
				sellSeries.add(tt.timestamp*1000, tt.topsellprice);
			}
			
			TickerChartDataset.removeAllSeries();
			TickerChartDataset.addSeries(buySeries);
			TickerChartDataset.addSeries(sellSeries);		
		}				
	}
	
	/*private static void updateOrderDepthChartDataset() {
		final int ListN = PubCapi.orderBookFullList.indexOf(new PubCapi.orderBookList(selectedMarket));
		if (ListN!=-1)
			if (!PubCapi.orderBookFullList.get(ListN).orderBookList.isEmpty() ) {
				final int oBLD = PubCapi.orderBookListDepth; 
				final int xAxisTC = 2*oBLD*PubCapi.orderBookFullList.get(ListN).orderBookList.size();
				//double maxt = 0.00000001;
				
				double[] xvalues = new double[xAxisTC];     
		        double[] yvalues = new double[xAxisTC];     
		        double[] zvalues = new double[xAxisTC]; 
		        for (int i=0;i<xAxisTC/oBLD/2;i++) { 
		        	long ts = PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).pctimestamp;
		        	
		        	for (int j=0;j<2*oBLD;j++) {		            	
		            	int n = 2*oBLD*i+j;
		            	
		            	if (j<oBLD) {
		            		if (j<PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Buyorders.size() ) {
		            			PubCapi.orderData oD = PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Buyorders.get(j);
			            		//if (oD.total>maxt) maxt=oD.total;
			            		
			            		xvalues[n] = ts*1000;
			                    yvalues[n] = oD.price;
			                    zvalues[n] = oD.total;
		            		} else {
		            			PubCapi.orderData oD = PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Buyorders.getLast();
		            			
		            			xvalues[n] = ts*1000;
			                    yvalues[n] = oD.price;
			                    zvalues[n] = oD.total;
		            		}
		            	} else if (j>=oBLD) {
		            		if (j-oBLD<PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Sellorders.size() ) {
		            			PubCapi.orderData oD = PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Sellorders.get(j-oBLD);
			            		//if (oD.total>maxt) maxt=oD.total;
			            		
			            		xvalues[n] = ts*1000;
			                    yvalues[n] = oD.price;
			                    zvalues[n] = oD.total;
		            		} else {
		            			PubCapi.orderData oD = PubCapi.orderBookFullList.get(ListN).orderBookList.get(i).Sellorders.getLast();
		            			
		            			xvalues[n] = ts*1000;
			                    yvalues[n] = oD.price;
			                    zvalues[n] = oD.total;
		            		}
		            		
		            	}
		                
		            } 
		        }	        
		        
		        DefaultXYZDataset dataset = new DefaultXYZDataset(); 
		        dataset.addSeries("Series 1",  
		                new double[][] { xvalues, yvalues, zvalues });
		        
		        //GrayPaintScale paintScale = new GrayPaintScale(0, maxt);
		        LookupPaintScale paintScale = new LookupPaintScale();
			    paintScale.add(0.0, Color.GREEN);
		        paintScale.add(0.001, Color.YELLOW);
		        paintScale.add(0.01, Color.ORANGE);
		        paintScale.add(0.1, Color.RED);
		        paintScale.add(1.0, Color.BLACK);
		        XYBlockRenderer renderer = (XYBlockRenderer) OrderDepthChart.getXYPlot().getRenderer(0);
		        
		        renderer.setBlockWidth(1000.0 * 60.0);
		        double worstbuyprice = PubCapi.orderBookFullList.get(ListN).orderBookList.getLast().Buyorders.getLast().price;
		        double worstsellprice = PubCapi.orderBookFullList.get(ListN).orderBookList.getLast().Sellorders.getLast().price;
		        renderer.setBlockHeight((worstsellprice-worstbuyprice)/100);
		        renderer.setPaintScale(paintScale);
		        OrderDepthChart.getXYPlot().setDataset(0, dataset);
			}		
	}*/
	
	public static void updateNeuralCChart(int mid) {
		int ListN = PrivCapi.ohlcFullList.indexOf(new PrivCapi.ohlcDataList(mid) );
		if (ListN!=-1)
			if (!PrivCapi.ohlcFullList.get(ListN).ohlcList.isEmpty() ) {
				int size = PrivCapi.ohlcFullList.get(ListN).ohlcList.size();
				if (size > 60*24*7) size = 60*24*7;
				OHLCDataItem[] dataItems = new OHLCDataItem[size];
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				for (int i=0;i<size;i++) {
					try {
						Date date = df.parse(PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).datetime);
						double open = PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).open;
						double high = PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).high;
						double low = PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).low;
						double close = PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).close;
						double volume = PrivCapi.ohlcFullList.get(ListN).ohlcList.get(i).volume;
											
						OHLCDataItem item = new OHLCDataItem(new Date(date.getTime()+DataC.datetimeconvconst*1000), open, high, low, close, volume);
						dataItems[i] = item;
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
				
				NeuralCCDataset = new DefaultOHLCDataset("Market: " + mid, dataItems);
				NeuralCC.getXYPlot().setDataset(NeuralCCDataset);
			}				
	}
	
	
	@SuppressWarnings("serial")
	private static class MyDefaultTableModel extends DefaultTableModel {
		@Override
	    public boolean isCellEditable(int row, int column) {
	        return false;
	    }
	}
	
	private static void initMarketListTableModel() {
		String[] MarketListTableColumnNames = {"id", "label", "PName", "SName", "lt. price", "lt. volume", "lt. time", "PApi"};	
		MarketListTableModel.setColumnIdentifiers(MarketListTableColumnNames);
		
		MarketListTable.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(35);
		MarketListTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(40);
		MarketListTable.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(90);
		MarketListTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(120);
		MarketListTable.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(130);
		MarketListTable.getTableHeader().getColumnModel().getColumn(3).setPreferredWidth(130);
		MarketListTable.getTableHeader().getColumnModel().getColumn(4).setPreferredWidth(130);
		MarketListTable.getTableHeader().getColumnModel().getColumn(5).setPreferredWidth(130);
		MarketListTable.getTableHeader().getColumnModel().getColumn(6).setPreferredWidth(140);
		MarketListTable.getTableHeader().getColumnModel().getColumn(7).setPreferredWidth(50);
		MarketListTable.getTableHeader().getColumnModel().getColumn(7).setMaxWidth(60);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		
		MarketListTable.getTableHeader().getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		MarketListTable.getTableHeader().getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		MarketListTable.getTableHeader().getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		MarketListTable.getTableHeader().getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
	}
		
	public static void updateMarketListTableModel() {
		MarketListTableModel.setRowCount(0);
		
		NumberFormat ltpf = new DecimalFormat("#0.00000000");
		NumberFormat ltvf = new DecimalFormat("#0.0000");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for (int i=0;i<PubCapi.simplyestMarketDataList.size();i++) {
			double ltp = PubCapi.simplyestMarketDataList.get(i).lasttradeprice;
			double ltv = PubCapi.simplyestMarketDataList.get(i).lasttradevolume * ltp;
			Date ltt = new Date();
			try { ltt = df.parse(PubCapi.simplyestMarketDataList.get(i).lasttradetime); } catch (Exception e) { }
			ltt.setTime(ltt.getTime()+DataC.datetimeconvconst*1000);
			String PapiS = "";
			if (PubCapi.subscribedChannels.contains(new PubCapi.subscribedChannelData(PubCapi.simplyestMarketDataList.get(i).marketid) ) ) {
				PapiS = "Yes";
			}
			
			Object[] newRow = {
				PubCapi.simplyestMarketDataList.get(i).marketid,
				PubCapi.simplyestMarketDataList.get(i).label,
				PubCapi.simplyestMarketDataList.get(i).primaryname,
				PubCapi.simplyestMarketDataList.get(i).secondaryname,
				ltpf.format(ltp).replace(',', '.'),
				ltvf.format(ltv).replace(',', '.'),
				df.format(ltt),
				PapiS
			};			
			MarketListTableModel.addRow(newRow);
		}
	}
	
	
	public static void updateBalances() {
		String balances = "";
		NumberFormat bf = new DecimalFormat("#0.00000000");
		
		for (int i=0;i<PrivCapi.coinDataList.size();i++)
			if (PrivCapi.coinDataList.get(i).getbalance()!=0) {
				balances = balances + PrivCapi.coinDataList.get(i).code + "=" + bf.format(PrivCapi.coinDataList.get(i).getbalance() ).replaceAll(",", ".") + "\n";
			}
		
		Balances_textArea.setText(balances);
	}
	
	public static void updateMyTrades() {
		String myTrades = "";
		NumberFormat nf = new DecimalFormat("#0.00000000");
		
		for (int i=0;i<PrivCapi.myTradesList.size();i++) {
			PrivCapi.myTrade tt = PrivCapi.myTradesList.get(i);
			
			String market = String.valueOf(tt.marketid);
			int index = PubCapi.simplyestMarketDataList.indexOf(new PubCapi.simplyestMarketData(tt.marketid));
			if (index!=-1)
				market = PubCapi.simplyestMarketDataList.get(index).label;
			
			myTrades = myTrades + tt.datetime + ": on market:" + market + " " + tt.ordertype + " quantity=" +
					nf.format(tt.quantity).replaceAll(",", ".") + " total=" + nf.format(tt.total).replaceAll(",", ".") + " orderid=" + tt.orderid + "\n";
		}
		
		MyTrades_textArea.setText(myTrades);
	}
	
	
	public static void createTray() {
		final PopupMenu popup = new PopupMenu();	
		
		TrayIcon trayIcon = null;
		try {
			trayIcon = new TrayIcon(ImageIO.read(new File("data/Icons/trayicon.jpg")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		trayIcon.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		frame.setVisible(true);
        	}
		});
		
        final SystemTray tray = SystemTray.getSystemTray();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		Trader.onExit();
        	}
        });
        
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
        	e.printStackTrace();
            Trader.addLogEntry("TrayIcon could not be added.");
        }
	}
	
}