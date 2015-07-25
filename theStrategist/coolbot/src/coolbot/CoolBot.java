package coolbot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class CoolBot{
	
	static JFrame frame = new JFrame("The Strategist Bot");
	static JScrollPane loadedPanel=null;
	
	static JTextField apiKey=new JTextField(45);
	static ActionListener aListener;
	static String apiKeyVal=null;
	static long fiatbalance=-1,coinbalance=-1;
	 
	static BlockingQueue<String> orderqueue = new ArrayBlockingQueue<String>(1024);
	static int executingorder=0;
	
	
	public static void main(String args[])
	{
		
		JPanel menupanel = new JPanel();
		menupanel.setLayout(new GridLayout(1,5));
		menupanel.setPreferredSize(new Dimension(800,40));
	
		JButton bLogin = new JButton("Login");
		JButton bTrade = new JButton("Trade");
		JButton bProfile = new JButton("Profile");
		JButton bExchange = new JButton("Exchange");
		JButton bHelp = new JButton("Help");
		
		
		
		
		aListener=new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource()==bLogin) {
					addLoginPanel();
				}
				if(e.getSource()==bTrade) {
					if(apiKeyVal!=null)
					setBalance();
					addTradePanel();
				}
				if(e.getSource()==bProfile) {
					if(fiatbalance!=-1)
					addProfilePanel();
					else
					{
						showApiError();
					}
				}
				if(e.getSource()==bExchange) {
					addExchangePanel();
				}
				if(e.getSource()==bHelp) {
					addHelpPanel();
				}
			}
		};
		

		
		
		menupanel.add(bLogin);
		menupanel.add(bTrade);
		menupanel.add(bProfile);
		menupanel.add(bExchange);
		menupanel.add(bHelp);
		
		bLogin.addActionListener(aListener);
		bTrade.addActionListener(aListener);
		bProfile.addActionListener(aListener);
		bExchange.addActionListener(aListener);
		bHelp.addActionListener(aListener);
		
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(menupanel,BorderLayout.NORTH);
		
		//add login panel
		addLoginPanel();
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {
		    	try {
					 
					File file = new File("orders.txt");
		 
					// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}
		 
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					int waitingtime=0;
					while(true)
					{
						
						if(executingorder==0) break;
							Thread.sleep(1000);
							waitingtime++;
							if(waitingtime==5) break;
						
					}
					while(orderqueue.size()!=0)
					{
					String temporder=orderqueue.poll();
					if(temporder!=null)
					bw.write(temporder+"\n");
					}
					bw.close();
		 
					 
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		            System.exit(0);
		        
		    }
		});
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		
		
		//load saved orders
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("orders.txt"));
 
			while ((sCurrentLine = br.readLine()) != null) {
				
				orderqueue.add(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	
	}
	
	public static void monitor(int orderscount)
	{
		System.out.println("Monitor executing");
		if(orderscount!=0)
		{
		executingorder++;
		System.out.println(new Date());
		//check for pending orders and try to place them if possible
		String currentorder=orderqueue.poll();
		System.out.println(currentorder);
		String orderarray[]=currentorder.split(" ");
		
		String mode=orderarray[0];
		if(mode.equals("Ghost"))
		{
			String type=orderarray[3];
			Long rate=Long.parseLong(orderarray[1]);
			Long qty=Long.parseLong(orderarray[2]);
			String url="";
			boolean tradedone=false;
			//check if matching order exists and execute
			try {
			if(type.equals("buy"))
			{
				//get min sell price
				Long minsell = new JSONArray(makePost("https://api.coinsecureis.cool/v0/lowestask",null)).getJSONObject(0).getLong("lowestAsk");
				System.out.println("minsell"+minsell);
				if(minsell<=rate)
				{
					//place order
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",rate);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createbid";
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.print("result"+makePost(url,json));
					tradedone=true;
				}
				
				
			}
			else if(type.equals("sell"))
			{
				//get max buy price
				Long maxbuy = new JSONArray(makePost("https://api.coinsecureis.cool/v0/highestbid",null)).getJSONObject(0).getLong("highestBid");
				System.out.println("maxbuy="+maxbuy);
				if(maxbuy>=rate)
				{
					//place order
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",rate);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createask";	
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.print("result"+makePost(url,json));
					tradedone=true;
				}
				
			}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e){System.out.println(e);}
			
			//if matching order does not exists add the order back to queue
			if(!tradedone)
			{
				orderqueue.add(currentorder);
			}
			executingorder--;
			monitor(orderscount-1);
		}
		
		else if(mode.equals("MeFirst"))
		 {
			//order=mode rate qty diff type currentorderid currentrate 
			String type=orderarray[4];
			long rate=Long.parseLong(orderarray[1]);
			long qty=Long.parseLong(orderarray[2]);
			long diff=Long.parseLong(orderarray[3]);
			String currentorderid="";
			long currentrate=-1;
			boolean orderpending=false;
			long pendingqty=-1;
			if(orderarray.length>=6)
			{
				
				currentorderid=orderarray[5];currentrate=Long.parseLong(orderarray[6]);
				
				
				//check if order has been executed
				String url="";
				if(type.equals("buy"))
				{url="https://api.coinsecureis.cool/v0/auth/alluserbids";}
				else if(type.equals("sell"))
				{url="https://api.coinsecureis.cool/v0/auth/alluserasks";}
				try {
					JSONObject json=new JSONObject();
					json.put("apiKey",apiKeyVal);
					//System.out.println("allbids"+makePost(url,json));
					JSONArray allbuyorders = new JSONArray(makePost(url,json));
					
					
					for(int i=0;i<allbuyorders.length();i++)
					{
						String tempid=allbuyorders.getJSONObject(i).getString("orderID");
						if(currentorderid.equals(tempid)) {orderpending=true;pendingqty=allbuyorders.getJSONObject(i).getLong("vol");break;}
					}
										
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			if(!orderpending && !currentorderid.equals("")) 
				{
				//order has been already completely executed
					executingorder--;
					monitor(orderscount-1);
				}
			else
				{
				//find min rate and cancel if order rate is higher and place new order	
				if(orderpending)
					{qty=pendingqty;}
				
				String url="";
				boolean tradedone=false;
				//check if matching order exists and execute
				try {
				if(type.equals("buy"))
				{
					//get min buy price
					JSONArray allbuyorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/allbids",null)).getJSONObject(0).getJSONArray("allbids").getJSONArray(0);
					//System.out.println("allbuyorders"+allbuyorders);
									
					//find the highest order in range bid+difference 
					
					long maxexchangerate=rate;long maxallowedrate=rate+diff;
					for(int i=0;i<allbuyorders.length();i++)
					{
						long temprate=allbuyorders.getJSONObject(i).getLong("rate");
						
						if(temprate>maxexchangerate && temprate<=maxallowedrate)
						{
							maxexchangerate=temprate;
						}
					}
					if(maxexchangerate!=rate){maxexchangerate+=1;}
					
					
					//if current order maxexchangerate != new maxexchangerate
					//delete previous order and place new order
					if(currentrate!=maxexchangerate)
						{
						//cancel order
						JSONObject json=new JSONObject();
						try {
							json.put("apiKey",apiKeyVal);
							json.put("orderID",currentorderid);
							url="https://api.coinsecureis.cool/v0/auth/cancelbid";
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(new JSONArray(makePost(url,json)).getInt(0)>0)
							{
							//place order
							json=new JSONObject();
							try {
								json.put("apiKey",apiKeyVal);
								json.put("rate",maxexchangerate);
								json.put("vol",qty);
								url="https://api.coinsecureis.cool/v0/auth/createbid";
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							currentorder="MeFirst"+" "+rate+" "+qty+" "+diff+" "+"buy"+" "+makePost(url,json)+" "+maxexchangerate;
							}
						}
					else
						{
						currentorder="MeFirst"+" "+rate+" "+qty+" "+diff+" "+"buy"+" "+currentorderid+" "+maxexchangerate;
						}
					
					
					
				}
				else if(type.equals("sell"))
				{
					//get min sell price
					JSONArray allsellorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/allasks",null)).getJSONObject(0).getJSONArray("allasks").getJSONArray(0);
					//System.out.println("allbuyorders"+allbuyorders);
									
					//find the highest order in range bid+difference 
					
					long minexchangerate=rate;long minallowedrate=rate-diff;
					for(int i=0;i<allsellorders.length();i++)
					{
						long temprate=allsellorders.getJSONObject(i).getLong("rate");
						
						if(temprate<minexchangerate && temprate>=minallowedrate)
						{
							minexchangerate=temprate;
						}
					}
					if(minexchangerate!=rate){minexchangerate-=1;}
					
					
					//if current order minexchangerate != new minexchangerate
					//delete previous order and place new order
					if(currentrate!=minexchangerate)
						{
						//cancel order
						JSONObject json=new JSONObject();
						try {
							json.put("apiKey",apiKeyVal);
							json.put("orderID",currentorderid);
							url="https://api.coinsecureis.cool/v0/auth/cancelask";
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(new JSONArray(makePost(url,json)).getInt(0)>0)
							{
							//place order
							json=new JSONObject();
							try {
								json.put("apiKey",apiKeyVal);
								json.put("rate",minexchangerate);
								json.put("vol",qty);
								url="https://api.coinsecureis.cool/v0/auth/createask";
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							currentorder="MeFirst"+" "+rate+" "+qty+" "+diff+" "+"sell"+" "+makePost(url,json)+" "+minexchangerate;
							}
						}
					else
						{
						currentorder="MeFirst"+" "+rate+" "+qty+" "+diff+" "+"sell"+" "+currentorderid+" "+minexchangerate;
						}
					
					
				}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(Exception e){System.out.println(e);}
				
				executingorder--;
				monitor(orderscount-1);
			 }
		}
		
		else if(mode.equals("SIP"))
		{
			//order=SIP quantity/amount duration(min) ordertype orderbase lastexecuted
			
			Long qtyORamt=Long.parseLong(orderarray[1]);
			int duration=Integer.parseInt(orderarray[2]);
			String type=orderarray[3];
			String base=orderarray[4];
			Long lastexecuted=Long.parseLong(orderarray[5]);
			
			//check if SIP duration has passed
			long unixTime = System.currentTimeMillis() / 1000L;
			if(unixTime>=lastexecuted+duration*60)
			{
			try {
				
			//get last traded rate
				JSONObject lasttrade = new JSONArray(makePost("https://api.coinsecureis.cool/v0/lasttrade",null)).getJSONObject(0).getJSONArray("lasttrade").getJSONObject(0);
				JSONArray lasttradearray=null;
				try
				{lasttradearray=lasttrade.getJSONArray("bid").getJSONArray(0);}
				catch(JSONException e)
				{lasttradearray=lasttrade.getJSONArray("ask").getJSONArray(0);}
				long lasttradeprice=lasttradearray.getJSONObject(0).getLong("rate");
				
			//get qty
				long qty=-1;
				if(base.equals("quantity"))
				{qty=qtyORamt;}
				else if(base.equals("amount"))
				{qty=qtyORamt/lasttradeprice;}
				
				String url="";	
			if(type.equals("buy"))
			{
				
					//place order
					
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",lasttradeprice);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createbid";
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					makePost(url,json);
				
			}
			else if(type.equals("sell"))
			{
					//place order
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",lasttradeprice);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createask";	
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					makePost(url,json);
					
				
				
			}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			catch(Exception e){System.out.println(e);}
			
			//order=SIP quantity/amount duration(min) ordertype orderbase lastexecuted		
			currentorder ="SIP"+" "+qtyORamt+" "+duration+" "+type+" "+base+" "+System.currentTimeMillis() / 1000L;
			
		}
		
			orderqueue.add(currentorder);
			executingorder--;
			monitor(orderscount-1);
		
		}
		
		else if(mode.equals("Range"))
		{
			//order=Range buyrate sellrate qty type
			String type=orderarray[4];
			Long buyrate=Long.parseLong(orderarray[1]);
			Long sellrate=Long.parseLong(orderarray[2]);
			Long qty=Long.parseLong(orderarray[3]);
			String url="";
			
			try {
				
			//get last trade rate
				JSONObject lasttrade = new JSONArray(makePost("https://api.coinsecureis.cool/v0/lasttrade",null)).getJSONObject(0).getJSONArray("lasttrade").getJSONObject(0);
				JSONArray lasttradearray=null;
				try
				{lasttradearray=lasttrade.getJSONArray("bid").getJSONArray(0);}
				catch(JSONException e)
				{lasttradearray=lasttrade.getJSONArray("ask").getJSONArray(0);}
				long lasttradeprice=lasttradearray.getJSONObject(0).getLong("rate");
			if(type.equals("begin"))
			{
				//set type if possible
				if(lasttradeprice>=sellrate)
				{type="sell";}
				if(lasttradeprice<=buyrate)
				{type="buy";}
				
			}
			if(type.equals("buy")&&(lasttradeprice<=buyrate))
			{
				//place order
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",lasttradeprice);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createbid";
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					makePost(url,json);
					type="sell";
					currentorder="Range"+" "+buyrate+" "+sellrate+" "+qty+" "+type;

				
			}
			else if(type.equals("sell")&&(lasttradeprice>=sellrate))
			{
				
					//place order
					JSONObject json=new JSONObject();
					try {
						json.put("apiKey",apiKeyVal);
						json.put("rate",lasttradeprice);
						json.put("vol",qty);
						url="https://api.coinsecureis.cool/v0/auth/createask";	
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					makePost(url,json);
					type="buy";
					currentorder="Range"+" "+buyrate+" "+sellrate+" "+qty+" "+type;
				
			}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e){System.out.println(e);}
			
			orderqueue.add(currentorder);
			executingorder--;
			monitor(orderscount-1);
		}
		else if(mode.equals("Price"))
		{
			//sell all bitcoins if price>bookprofit or price<stoploss and cancell all orders
			//order=Price bookprofitrate stoplossrate
			
			long bookprofitrate=Long.parseLong(orderarray[1]);
			long stoplossrate=Long.parseLong(orderarray[2]);
			long qty=coinbalance;
			String url="";
			
			try {
				
			//get last trade rate
				JSONObject lasttrade = new JSONArray(makePost("https://api.coinsecureis.cool/v0/lasttrade",null)).getJSONObject(0).getJSONArray("lasttrade").getJSONObject(0);
				JSONArray lasttradearray=null;
				try
				{lasttradearray=lasttrade.getJSONArray("bid").getJSONArray(0);}
				catch(JSONException e)
				{lasttradearray=lasttrade.getJSONArray("ask").getJSONArray(0);}
				long lasttradeprice=lasttradearray.getJSONObject(0).getLong("rate");
			
			if((lasttradeprice>=bookprofitrate)||(lasttradeprice<=stoplossrate))
			{
				//cancel all pending placed orders
					
				JSONObject json=new JSONObject();
				try {
					json.put("apiKey",apiKeyVal);
					
					//cancel all bids
					JSONArray allbuyorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/alluserbids",json));
										
					for(int i=0;i<allbuyorders.length();i++)
					{
						cancelorder(allbuyorders.getJSONObject(i).getString("orderID"),"buy");
					}
					
					//cancel all asks
					JSONArray allsellorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/alluserasks",json));
										
					for(int i=0;i<allsellorders.length();i++)
					{
						cancelorder(allsellorders.getJSONObject(i).getString("orderID"),"sell");
					}
				//get coinsbalance
					String urlcoin="https://api.coinsecureis.cool/v0/auth/coinbalance";
					coinbalance=Long.parseLong(new JSONArray(makePost(urlcoin,json)).get(0).toString());
				
				//place sell all order
					json=new JSONObject();
					json.put("apiKey",apiKeyVal);
					json.put("rate",lasttradeprice);
					json.put("vol",coinbalance);
					url="https://api.coinsecureis.cool/v0/auth/createask";	
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					makePost(url,json);
					orderqueue=null;
					executingorder--;
					
				
			}
			else
			{
				orderqueue.add(currentorder);
				executingorder--;
				monitor(orderscount-1);
			}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e){System.out.println(e);}
			
			
			
			
		}
		else
		{
			orderqueue.add(currentorder);
			executingorder--;
			monitor(orderscount-1);
		}
		
		
		
		
		}
		
	}
	
	
	
	public static void cancelorder(String orderid,String type) {
		
		JSONObject json=new JSONObject();
		String url="";
		try
		{
		json.put("apiKey",apiKeyVal);
		json.put("orderID",orderid);
		
		if(type.equals("buy"))
			{
			url="https://api.coinsecureis.cool/v0/auth/cancelbid";	
			}
		if(type.equals("sell"))
			{
			url="https://api.coinsecureis.cool/v0/auth/cancelask";	
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		makePost(url,json);
	}

	public static void addLoginPanel()
	{
		if(loadedPanel !=null)
			{frame.getContentPane().remove(loadedPanel);}
		
		JPanel loginPanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		loginPanel.setLayout(slayout);
		loginPanel.setPreferredSize(new Dimension(800,600));

		JLabel apiKeyLabel=new JLabel("Api Key(Read+Write)");
		loginPanel.add(apiKeyLabel);
		slayout.putConstraint(SpringLayout.WEST, apiKeyLabel,350,SpringLayout.WEST, loginPanel);
		slayout.putConstraint(SpringLayout.NORTH, apiKeyLabel,200,SpringLayout.NORTH, loginPanel);
		
		
		loginPanel.add(apiKey);
		slayout.putConstraint(SpringLayout.WEST, apiKey,10,SpringLayout.EAST, apiKeyLabel);
		slayout.putConstraint(SpringLayout.NORTH, apiKey,200,SpringLayout.NORTH, loginPanel);
		
		JButton submitlogin = new JButton("Submit");
		submitlogin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(apiKey.getText().equals("")) {showinputError();return;}
				apiKeyVal=(apiKey.getText());
				
				setBalance();
				if(fiatbalance!=-1)
				{
					addTradePanel();
					Date today=new Date();
					
					Timer timer = new Timer ();
					TimerTask hourlyTask = new TimerTask () {
					    @Override
					    public void run () {
					        // your code here...
					    	
					    	monitor(orderqueue.size());
					    }
					};

					// schedule the task to run starting now and then every minute...
					timer.schedule (hourlyTask, today, 1000*60);
				}
				else
				{
					JOptionPane.showMessageDialog(frame,
						    "Network/Invalid API error, please check and submit again",
						    "Network/Invalid API error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		loginPanel.add(submitlogin,BorderLayout.SOUTH);
		slayout.putConstraint(SpringLayout.NORTH, submitlogin,50,SpringLayout.SOUTH, apiKeyLabel);
		slayout.putConstraint(SpringLayout.WEST, submitlogin,600,SpringLayout.WEST, loginPanel);
		
		JScrollPane loginscrollPane = new JScrollPane(loginPanel);
		frame.getContentPane().add(loginscrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=loginscrollPane;
	}
	
	
	static JPanel temploadedPanel=null;
	public static void addTradePanel()
	{
		//frame.getLayoutComponent(BorderLayout.CENTER)
		//frame.remove(((BorderLayout)frame.getLayout()).getLayoutComponent(BorderLayout.CENTER));
		//frame.remove(frame.getContentPane());
		
		if(loadedPanel !=null)
		{
		 
		frame.remove(loadedPanel);
		
		
		}
		/*
		if(temploadedPanel !=null)
			{
			System.out.println("temploadedPanel "+temploadedPanel);
			frame.remove(temploadedPanel);
			}
		*/
		JPanel tradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		
		tradePanel.setLayout(slayout);
		//tradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel vanilaDesc=new JLabel("Simple buy/sell");
		JButton vanila=new JButton("Vanila Mode");
		vanila.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addVanilaTradePanel();
				
			}
		});
		tradePanel.add(vanila);
		tradePanel.add(vanilaDesc);
		if(fiatbalance>-1)
		{
		JLabel fiatBalance=new JLabel("Fiat Balance: ");
		tradePanel.add(fiatBalance);
		slayout.putConstraint(SpringLayout.WEST, fiatBalance,50,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, fiatBalance,10,SpringLayout.NORTH, tradePanel);
		
		JLabel fiatBalanceValue=new JLabel(displayfiat(fiatbalance));
		tradePanel.add(fiatBalanceValue);
		slayout.putConstraint(SpringLayout.WEST, fiatBalanceValue,10,SpringLayout.EAST, fiatBalance);
		slayout.putConstraint(SpringLayout.NORTH, fiatBalanceValue,10,SpringLayout.NORTH, tradePanel);
		
		JLabel coinBalance=new JLabel("Coin Balance: ");
		tradePanel.add(coinBalance);
		slayout.putConstraint(SpringLayout.WEST, coinBalance,100,SpringLayout.EAST, fiatBalanceValue);
		slayout.putConstraint(SpringLayout.NORTH, coinBalance,10,SpringLayout.NORTH, tradePanel);
		
		JLabel coinBalanceValue=new JLabel(displaycoin(coinbalance));
		tradePanel.add(coinBalanceValue);
		slayout.putConstraint(SpringLayout.WEST, coinBalanceValue,10,SpringLayout.EAST, coinBalance);
		slayout.putConstraint(SpringLayout.NORTH, coinBalanceValue,10,SpringLayout.NORTH, tradePanel);
		
		slayout.putConstraint(SpringLayout.EAST, vanila,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, vanila,50,SpringLayout.NORTH, fiatBalance);
		
		slayout.putConstraint(SpringLayout.WEST, vanilaDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, vanilaDesc,50,SpringLayout.NORTH, fiatBalance);
		}
		else
		{
		slayout.putConstraint(SpringLayout.EAST, vanila,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, vanila,50,SpringLayout.NORTH, tradePanel);	
		
		slayout.putConstraint(SpringLayout.WEST, vanilaDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, vanilaDesc,50,SpringLayout.NORTH, tradePanel);
		}
		
		
		JButton ghost=new JButton("Ghost Mode");

		ghost.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addGhostTradePanel();
				
			}
		});
		tradePanel.add(ghost);
		slayout.putConstraint(SpringLayout.EAST, ghost,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, ghost,20,SpringLayout.SOUTH, vanila);
		
		JLabel ghostDesc=new JLabel("Buy/sell orders are not shown on coinsecure website");
		tradePanel.add(ghostDesc);
		slayout.putConstraint(SpringLayout.WEST, ghostDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, ghostDesc,20,SpringLayout.SOUTH, vanila);
		
		JButton meFirst=new JButton("MeFirst Mode");
		meFirst.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addMeFirstTradePanel();
				
			}
		});
		tradePanel.add(meFirst);
		slayout.putConstraint(SpringLayout.EAST, meFirst,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, meFirst,20,SpringLayout.SOUTH, ghost);
		JLabel meFirstDesc=new JLabel("Order is dynamically updated to execute first and maximize return");
		tradePanel.add(meFirstDesc);
		slayout.putConstraint(SpringLayout.WEST, meFirstDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, meFirstDesc,20,SpringLayout.SOUTH, ghost);
		
		JButton rainbow=new JButton("Rainbow Mode");
		rainbow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addRainbowTradePanel();
				
			}
		});
		tradePanel.add(rainbow);
		slayout.putConstraint(SpringLayout.EAST, rainbow,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rainbow,20,SpringLayout.SOUTH, meFirst);
		JLabel rainbowDesc=new JLabel("Buy/sell orders are split into smaller orders");
		tradePanel.add(rainbowDesc);
		slayout.putConstraint(SpringLayout.WEST, rainbowDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rainbowDesc,20,SpringLayout.SOUTH, meFirst);
		
		JButton sip=new JButton("SIP Mode");
		sip.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addSIPTradePanel();
				
			}
		});
		tradePanel.add(sip);
		slayout.putConstraint(SpringLayout.EAST, sip,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, sip,20,SpringLayout.SOUTH, rainbow);
		JLabel sipDesc=new JLabel("Buy/sell at periodic intervals");
		tradePanel.add(sipDesc);
		slayout.putConstraint(SpringLayout.WEST, sipDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, sipDesc,20,SpringLayout.SOUTH, rainbow);
		
		JButton range=new JButton("Range Mode");
		range.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addRangeTradePanel();
				
			}
		});
		tradePanel.add(range);
		slayout.putConstraint(SpringLayout.EAST, range,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, range,20,SpringLayout.SOUTH, sip);
		JLabel rangeDesc=new JLabel("Buy/sell based on price range");
		tradePanel.add(rangeDesc);
		slayout.putConstraint(SpringLayout.WEST, rangeDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rangeDesc,20,SpringLayout.SOUTH, sip);
		
		JButton price=new JButton("Price Mode");
		price.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fiatbalance<0) showApiError();
				else
				addPriceTradePanel();
				
			}
		});
		
		tradePanel.add(price);
		slayout.putConstraint(SpringLayout.EAST, price,150,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, price,20,SpringLayout.SOUTH, range);
		JLabel priceDesc=new JLabel("Buy/sell orders based on price (StopLoss/BookProfit)");
		tradePanel.add(priceDesc);
		slayout.putConstraint(SpringLayout.WEST, priceDesc,200,SpringLayout.WEST, tradePanel);
		slayout.putConstraint(SpringLayout.NORTH, priceDesc,20,SpringLayout.SOUTH, range);
		
			
		
		
		JScrollPane tradescrollPane = new JScrollPane(tradePanel);
		frame.add(tradescrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=tradescrollPane;
		temploadedPanel=tradePanel;
	}
	
	public static void addVanilaTradePanel()
	{
		
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel vanilaTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		vanilaTradePanel.setLayout(slayout);
		vanilaTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("Vanila Mode");
		vanilaTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,150,SpringLayout.WEST, vanilaTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, vanilaTradePanel);
		
		JLabel orderType=new JLabel("Order Type");
		vanilaTradePanel.add(orderType);
		slayout.putConstraint(SpringLayout.EAST, orderType,150,SpringLayout.WEST, vanilaTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderType,50,SpringLayout.NORTH, orderMode);
		
		JRadioButton buyoption=new JRadioButton("Buy");
		vanilaTradePanel.add(buyoption);
		slayout.putConstraint(SpringLayout.WEST, buyoption,10,SpringLayout.EAST, orderType);
		slayout.putConstraint(SpringLayout.NORTH, buyoption,50,SpringLayout.NORTH, orderMode);
		
		JRadioButton selloption=new JRadioButton("Sell");
		vanilaTradePanel.add(selloption);
		slayout.putConstraint(SpringLayout.WEST, selloption,10,SpringLayout.EAST, buyoption);
		slayout.putConstraint(SpringLayout.NORTH, selloption,50,SpringLayout.NORTH, orderMode);
		
		ButtonGroup typeGroup=new ButtonGroup();
		typeGroup.add(buyoption);
		typeGroup.add(selloption);
		
		JLabel quantity=new JLabel("Quantity(in BTC)");
		vanilaTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,150,SpringLayout.WEST, vanilaTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,10,SpringLayout.SOUTH, orderType);
		
		JTextField quantityVal=new JTextField(20);
		vanilaTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,10,SpringLayout.SOUTH, orderType);
		
		JLabel rate=new JLabel("Rate per Bitcoin(in Rs.)");
		vanilaTradePanel.add(rate);
		slayout.putConstraint(SpringLayout.EAST, rate,150,SpringLayout.WEST, vanilaTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rate,10,SpringLayout.SOUTH, quantity);
		
		JTextField rateVal=new JTextField(20);
		vanilaTradePanel.add(rateVal);
		slayout.putConstraint(SpringLayout.WEST, rateVal,10,SpringLayout.EAST, rate);
		slayout.putConstraint(SpringLayout.NORTH, rateVal,10,SpringLayout.SOUTH, quantity);
		
		JButton submitVanila=new JButton("Submit");
		submitVanila.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(!(buyoption.isSelected()||selloption.isSelected())||rateVal.getText().equals("")||quantityVal.getText().equals(""))
						{showinputError();return;}
				String url="";
				if(buyoption.isSelected())
				{
				url="https://api.coinsecureis.cool/v0/auth/createbid";
				}
				else if(selloption.isSelected())
				{
					url="https://api.coinsecureis.cool/v0/auth/createask";	
				}
				JSONObject json=new JSONObject();
				try {
					json.put("apiKey",apiKeyVal);
					json.put("rate",inputfiat(rateVal.getText()));
					json.put("vol",inputcoin(quantityVal.getText()));
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String result=makePost(url,json);
				if(result!=null && !result.contains("error"))
				{
					showsuccessMessage();
					buyoption.setSelected(false);
					selloption.setSelected(false);
					rateVal.setText("");
					quantityVal.setText("");
					
				}
				else
				{
					if(result==null)
					showApiError();
					else
					showexchangeError(result);
				}
			}
		});
		vanilaTradePanel.add(submitVanila);
		slayout.putConstraint(SpringLayout.WEST, submitVanila,150,SpringLayout.WEST, vanilaTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitVanila,10,SpringLayout.SOUTH, rate);
		
				
		JScrollPane vanilascrollPane = new JScrollPane(vanilaTradePanel);
		frame.getContentPane().add(vanilascrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=vanilascrollPane;
		
	}
	
	public static void addGhostTradePanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel ghostTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		ghostTradePanel.setLayout(slayout);
		ghostTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("Ghost Mode");
		ghostTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,150,SpringLayout.WEST, ghostTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, ghostTradePanel);

		JLabel orderType=new JLabel("Order Type");
		ghostTradePanel.add(orderType);
		slayout.putConstraint(SpringLayout.EAST, orderType,150,SpringLayout.WEST, ghostTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderType,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton buyoption=new JRadioButton("Buy");
		ghostTradePanel.add(buyoption);
		slayout.putConstraint(SpringLayout.WEST, buyoption,10,SpringLayout.EAST, orderType);
		slayout.putConstraint(SpringLayout.NORTH, buyoption,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton selloption=new JRadioButton("Sell");
		ghostTradePanel.add(selloption);
		slayout.putConstraint(SpringLayout.WEST, selloption,10,SpringLayout.EAST, buyoption);
		slayout.putConstraint(SpringLayout.NORTH, selloption,50,SpringLayout.SOUTH, orderMode);
		
		ButtonGroup typeGroup=new ButtonGroup();
		typeGroup.add(buyoption);
		typeGroup.add(selloption);
		
		JLabel quantity=new JLabel("Quantity(in BTC)");
		ghostTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,150,SpringLayout.WEST, ghostTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,10,SpringLayout.SOUTH, orderType);
		
		JTextField quantityVal=new JTextField(20);
		ghostTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,10,SpringLayout.SOUTH, orderType);
		
		JLabel rate=new JLabel("Rate per Bitcoin(in Rs.)");
		ghostTradePanel.add(rate);
		slayout.putConstraint(SpringLayout.EAST, rate,150,SpringLayout.WEST, ghostTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rate,10,SpringLayout.SOUTH, quantity);
		
		JTextField rateVal=new JTextField(20);
		ghostTradePanel.add(rateVal);
		slayout.putConstraint(SpringLayout.WEST, rateVal,10,SpringLayout.EAST, rate);
		slayout.putConstraint(SpringLayout.NORTH, rateVal,10,SpringLayout.SOUTH, quantity);
		
		JButton submitGhost=new JButton("Submit");
		submitGhost.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!(buyoption.isSelected()||selloption.isSelected())||rateVal.getText().equals("")||quantityVal.getText().equals(""))
				{showinputError();return;}
				
				String order="Ghost"+" "+inputfiat(rateVal.getText())+" "+inputcoin(quantityVal.getText());
				if(buyoption.isSelected())
				{
					order=order+" "+"buy";
				}
				else if(selloption.isSelected())
				{
					order=order+" "+"sell";	
				}
				orderqueue.add(order);
				showsuccessMessage("Your order has been added");
				buyoption.setSelected(false);
				selloption.setSelected(false);
				rateVal.setText("");
				quantityVal.setText("");
			}
		});
		ghostTradePanel.add(submitGhost);
		slayout.putConstraint(SpringLayout.WEST, submitGhost,150,SpringLayout.WEST, ghostTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitGhost,10,SpringLayout.SOUTH, rate);
		
				
		JScrollPane ghostscrollPane = new JScrollPane(ghostTradePanel);
		frame.getContentPane().add(ghostscrollPane,BorderLayout.CENTER);
		frame.pack();
		loadedPanel=ghostscrollPane;
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	public static void addMeFirstTradePanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel meFirstTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		meFirstTradePanel.setLayout(slayout);
		meFirstTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("MeFirst Mode");
		meFirstTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, meFirstTradePanel);


		JLabel orderType=new JLabel("Order Type");
		meFirstTradePanel.add(orderType);
		slayout.putConstraint(SpringLayout.EAST, orderType,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderType,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton buyoption=new JRadioButton("Buy");
		meFirstTradePanel.add(buyoption);
		slayout.putConstraint(SpringLayout.WEST, buyoption,10,SpringLayout.EAST, orderType);
		slayout.putConstraint(SpringLayout.NORTH, buyoption,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton selloption=new JRadioButton("Sell");
		meFirstTradePanel.add(selloption);
		slayout.putConstraint(SpringLayout.WEST, selloption,10,SpringLayout.EAST, buyoption);
		slayout.putConstraint(SpringLayout.NORTH, selloption,50,SpringLayout.SOUTH, orderMode);
		
		ButtonGroup typeGroup=new ButtonGroup();
		typeGroup.add(buyoption);
		typeGroup.add(selloption);
		
		JLabel quantity=new JLabel("Quantity(in BTC)");
		meFirstTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,10,SpringLayout.SOUTH, orderType);
		
		JTextField quantityVal=new JTextField(20);
		meFirstTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,10,SpringLayout.SOUTH, orderType);
		
		JLabel rate=new JLabel("Rate per Bitcoin(in Rs.)");
		meFirstTradePanel.add(rate);
		slayout.putConstraint(SpringLayout.EAST, rate,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rate,10,SpringLayout.SOUTH, quantity);
		
		JTextField rateVal=new JTextField(20);
		meFirstTradePanel.add(rateVal);
		slayout.putConstraint(SpringLayout.WEST, rateVal,10,SpringLayout.EAST, rate);
		slayout.putConstraint(SpringLayout.NORTH, rateVal,10,SpringLayout.SOUTH, quantity);
		
		JLabel difference=new JLabel("Max price diff(in Rs.)");
		meFirstTradePanel.add(difference);
		slayout.putConstraint(SpringLayout.EAST, difference,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, difference,10,SpringLayout.SOUTH, rate);
		
		JTextField differenceVal=new JTextField(20);
		meFirstTradePanel.add(differenceVal);
		slayout.putConstraint(SpringLayout.WEST, differenceVal,10,SpringLayout.EAST, difference);
		slayout.putConstraint(SpringLayout.NORTH, differenceVal,10,SpringLayout.SOUTH, rate);
		
		JButton submitMeFirst=new JButton("Submit");
		submitMeFirst.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(!(buyoption.isSelected()||selloption.isSelected())||rateVal.getText().equals("")||quantityVal.getText().equals("")||differenceVal.getText().equals(""))
				{showinputError();return;}
				
				String order="MeFirst"+" "+inputfiat(rateVal.getText())+" "+inputcoin(quantityVal.getText())+" "+inputfiat(differenceVal.getText());
				if(buyoption.isSelected())
				{
					order=order+" "+"buy";
				}
				else if(selloption.isSelected())
				{
					order=order+" "+"sell";	
				}
				orderqueue.add(order);
				showsuccessMessage("Your order has been added");
				buyoption.setSelected(false);
				selloption.setSelected(false);
				rateVal.setText("");
				quantityVal.setText("");
				differenceVal.setText("");
			}
		});
		meFirstTradePanel.add(submitMeFirst);
		slayout.putConstraint(SpringLayout.WEST, submitMeFirst,150,SpringLayout.WEST, meFirstTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitMeFirst,10,SpringLayout.SOUTH, difference);
		
			
		JScrollPane meFirstscrollPane = new JScrollPane(meFirstTradePanel);
		frame.getContentPane().add(meFirstscrollPane,BorderLayout.CENTER);
		frame.pack();
		loadedPanel=meFirstscrollPane;
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
	}
	
	public static void addRainbowTradePanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel rainbowTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		rainbowTradePanel.setLayout(slayout);
		rainbowTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("Rainbow Mode");
		rainbowTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, rainbowTradePanel);

		JLabel orderType=new JLabel("Order Type");
		rainbowTradePanel.add(orderType);
		slayout.putConstraint(SpringLayout.EAST, orderType,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderType,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton buyoption=new JRadioButton("Buy");
		rainbowTradePanel.add(buyoption);
		slayout.putConstraint(SpringLayout.WEST, buyoption,10,SpringLayout.EAST, orderType);
		slayout.putConstraint(SpringLayout.NORTH, buyoption,50,SpringLayout.SOUTH, orderMode);
		
		JRadioButton selloption=new JRadioButton("Sell");
		rainbowTradePanel.add(selloption);
		slayout.putConstraint(SpringLayout.WEST, selloption,10,SpringLayout.EAST, buyoption);
		slayout.putConstraint(SpringLayout.NORTH, selloption,50,SpringLayout.SOUTH, orderMode);
		
		ButtonGroup typeGroup=new ButtonGroup();
		typeGroup.add(buyoption);
		typeGroup.add(selloption);
		
		JLabel quantity=new JLabel("Total Quantity(in BTC)");
		rainbowTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,10,SpringLayout.SOUTH, orderType);
		
		JTextField quantityVal=new JTextField(20);
		rainbowTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,10,SpringLayout.SOUTH, orderType);
		
		JLabel rate=new JLabel("Base Rate per Bitcoin(in Rs.)");
		rainbowTradePanel.add(rate);
		slayout.putConstraint(SpringLayout.EAST, rate,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rate,10,SpringLayout.SOUTH, quantity);
		
		JTextField rateVal=new JTextField(20);
		rainbowTradePanel.add(rateVal);
		slayout.putConstraint(SpringLayout.WEST, rateVal,10,SpringLayout.EAST, rate);
		slayout.putConstraint(SpringLayout.NORTH, rateVal,10,SpringLayout.SOUTH, quantity);
		
		JLabel rateIncrement=new JLabel("Rate Increment(in Rs.)");
		rainbowTradePanel.add(rateIncrement);
		slayout.putConstraint(SpringLayout.EAST, rateIncrement,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, rateIncrement,10,SpringLayout.SOUTH, rate);
		
		JTextField rateIncrementVal=new JTextField(20);
		rainbowTradePanel.add(rateIncrementVal);
		slayout.putConstraint(SpringLayout.WEST, rateIncrementVal,10,SpringLayout.EAST, rateIncrement);
		slayout.putConstraint(SpringLayout.NORTH, rateIncrementVal,10,SpringLayout.SOUTH, rate);
		
		JLabel ordersCount=new JLabel("No of orders");
		rainbowTradePanel.add(ordersCount);
		slayout.putConstraint(SpringLayout.EAST, ordersCount,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, ordersCount,10,SpringLayout.SOUTH, rateIncrement);
		
		JTextField ordersCountVal=new JTextField(20);
		rainbowTradePanel.add(ordersCountVal);
		slayout.putConstraint(SpringLayout.WEST, ordersCountVal,10,SpringLayout.EAST, ordersCount);
		slayout.putConstraint(SpringLayout.NORTH, ordersCountVal,10,SpringLayout.SOUTH, rateIncrement);
		
		JButton submitRainbow=new JButton("Submit");
		submitRainbow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String url="";
				if(!(buyoption.isSelected()||selloption.isSelected())||rateVal.getText().equals("")||quantityVal.getText().equals("")||ordersCountVal.getText().equals("")||rateIncrementVal.getText().equals(""))
				{showinputError();return;}
				if(buyoption.isSelected())
				{
				url="https://api.coinsecureis.cool/v0/auth/createbid";
				}
				else if(selloption.isSelected())
				{
					url="https://api.coinsecureis.cool/v0/auth/createask";	
				}
				int orderscount=0;
				for(int i=0;i < Integer.parseInt(ordersCountVal.getText());i++)
				{
				JSONObject json=new JSONObject();
				try {
					long voltemp=inputcoin(quantityVal.getText())/Integer.parseInt(ordersCountVal.getText());
					long ratetemp=inputfiat(rateVal.getText())+i*inputfiat(rateIncrementVal.getText());
					json.put("apiKey",apiKeyVal);
					json.put("rate",ratetemp);
					json.put("vol",voltemp);
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String result=makePost(url,json);
				if(result!=null && !result.contains("error"))
					{orderscount++;}
				}
				showsuccessMessage(orderscount+" orders placed successfully");
				rateVal.setText("");
				quantityVal.setText("");
				rateIncrementVal.setText("");
				ordersCountVal.setText("");
			}
		});
		rainbowTradePanel.add(submitRainbow);
		slayout.putConstraint(SpringLayout.WEST, submitRainbow,200,SpringLayout.WEST, rainbowTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitRainbow,10,SpringLayout.SOUTH, ordersCount);
		
				
		JScrollPane rainbowscrollPane = new JScrollPane(rainbowTradePanel);
		frame.getContentPane().add(rainbowscrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=rainbowscrollPane;
		
	}
	
	public static void addSIPTradePanel()
	{
		//buy sell at fixed intervals for current price 
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel SIPTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		SIPTradePanel.setLayout(slayout);
		SIPTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("SIP Mode");
		SIPTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, SIPTradePanel);

		
		JLabel orderType=new JLabel("Order Type");
		SIPTradePanel.add(orderType);
		slayout.putConstraint(SpringLayout.EAST, orderType,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderType,50,SpringLayout.NORTH, orderMode);
		
		JRadioButton buyoption=new JRadioButton("Buy");
		SIPTradePanel.add(buyoption);
		slayout.putConstraint(SpringLayout.WEST, buyoption,10,SpringLayout.EAST, orderType);
		slayout.putConstraint(SpringLayout.NORTH, buyoption,50,SpringLayout.NORTH, orderMode);
		
		JRadioButton selloption=new JRadioButton("Sell");
		SIPTradePanel.add(selloption);
		slayout.putConstraint(SpringLayout.WEST, selloption,10,SpringLayout.EAST, buyoption);
		slayout.putConstraint(SpringLayout.NORTH, selloption,50,SpringLayout.NORTH, orderMode);
		
		ButtonGroup typeGroup=new ButtonGroup();
		typeGroup.add(buyoption);
		typeGroup.add(selloption);
		
		JLabel orderBase=new JLabel("Order Base");
		SIPTradePanel.add(orderBase);
		slayout.putConstraint(SpringLayout.EAST, orderBase,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderBase,10,SpringLayout.SOUTH, orderType);
		
		JRadioButton amountoption=new JRadioButton("Amount(Rupees)");
		SIPTradePanel.add(amountoption);
		slayout.putConstraint(SpringLayout.WEST, amountoption,10,SpringLayout.EAST, orderBase);
		slayout.putConstraint(SpringLayout.NORTH, amountoption,10,SpringLayout.SOUTH, orderType);
		
		JRadioButton quantityoption=new JRadioButton("Quantity(BTC)");
		SIPTradePanel.add(quantityoption);
		slayout.putConstraint(SpringLayout.WEST, quantityoption,10,SpringLayout.EAST, amountoption);
		slayout.putConstraint(SpringLayout.NORTH, quantityoption,10,SpringLayout.SOUTH, orderType);
		
		ButtonGroup baseGroup=new ButtonGroup();
		baseGroup.add(amountoption);
		baseGroup.add(quantityoption);
		
		JLabel quantity=new JLabel("Amount/Quantity");
		SIPTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,10,SpringLayout.SOUTH, orderBase);
		
		JTextField quantityVal=new JTextField(20);
		SIPTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,10,SpringLayout.SOUTH, orderBase);
		
		
		JLabel duration=new JLabel("Buy every(in minutes)");
		SIPTradePanel.add(duration);
		slayout.putConstraint(SpringLayout.EAST, duration,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, duration,10,SpringLayout.SOUTH, quantity);
		
		JTextField durationVal=new JTextField(20);
		SIPTradePanel.add(durationVal);
		slayout.putConstraint(SpringLayout.WEST, durationVal,10,SpringLayout.EAST, duration);
		slayout.putConstraint(SpringLayout.NORTH, durationVal,10,SpringLayout.SOUTH, quantity);
		
		JButton submitSIP=new JButton("Submit");
		submitSIP.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!(buyoption.isSelected()||selloption.isSelected())||!(amountoption.isSelected()||quantityoption.isSelected())||durationVal.getText().equals("")||quantityVal.getText().equals(""))
				{showinputError();return;}
				String order="";
				if(amountoption.isSelected())
				{
					order="SIP"+" "+inputfiat(quantityVal.getText())+" "+durationVal.getText();
				}
				else if(quantityoption.isSelected())
				{
					order="SIP"+" "+inputcoin(quantityVal.getText())+" "+durationVal.getText();
				}
				if(buyoption.isSelected())
				{
					order=order+" "+"buy";
				}
				else if(selloption.isSelected())
				{
					order=order+" "+"sell";	
				}
				if(amountoption.isSelected())
				{
					order=order+" "+"amount";
				}
				else if(quantityoption.isSelected())
				{
					order=order+" "+"quantity";	
				}
				order=order+" "+0;	
				orderqueue.add(order);
				showsuccessMessage("Your order has been added");
				durationVal.setText("");
				quantityVal.setText("");
			}
		});
		SIPTradePanel.add(submitSIP);
		slayout.putConstraint(SpringLayout.WEST, submitSIP,150,SpringLayout.WEST, SIPTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitSIP,10,SpringLayout.SOUTH, duration);
		
			
		JScrollPane SIPscrollPane = new JScrollPane(SIPTradePanel);
		frame.getContentPane().add(SIPscrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=SIPscrollPane;
		
	}
	
	public static void addRangeTradePanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel rangeTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		rangeTradePanel.setLayout(slayout);
		rangeTradePanel.setPreferredSize(new Dimension(800,600));
		
		JLabel orderMode=new JLabel("Range Mode");
		rangeTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,200,SpringLayout.WEST, rangeTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, rangeTradePanel);

		
		
		JLabel quantity=new JLabel("Quantity");
		rangeTradePanel.add(quantity);
		slayout.putConstraint(SpringLayout.EAST, quantity,200,SpringLayout.WEST, rangeTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, quantity,50,SpringLayout.SOUTH, orderMode);
		
		JTextField quantityVal=new JTextField(20);
		rangeTradePanel.add(quantityVal);
		slayout.putConstraint(SpringLayout.WEST, quantityVal,10,SpringLayout.EAST, quantity);
		slayout.putConstraint(SpringLayout.NORTH, quantityVal,50,SpringLayout.SOUTH, orderMode);
		
		JLabel buyrate=new JLabel("Buy Rate per Bitcoin(in Rs.)");
		rangeTradePanel.add(buyrate);
		slayout.putConstraint(SpringLayout.EAST, buyrate,200,SpringLayout.WEST, rangeTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, buyrate,10,SpringLayout.SOUTH, quantity);
		
		JTextField buyrateVal=new JTextField(20);
		rangeTradePanel.add(buyrateVal);
		slayout.putConstraint(SpringLayout.WEST, buyrateVal,10,SpringLayout.EAST, buyrate);
		slayout.putConstraint(SpringLayout.NORTH, buyrateVal,10,SpringLayout.SOUTH, quantity);
		
		JLabel sellrate=new JLabel("Sell Rate per Bitcoin(in Rs.)");
		rangeTradePanel.add(sellrate);
		slayout.putConstraint(SpringLayout.EAST, sellrate,200,SpringLayout.WEST, rangeTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, sellrate,10,SpringLayout.SOUTH, buyrate);
		
		JTextField sellrateVal=new JTextField(20);
		rangeTradePanel.add(sellrateVal);
		slayout.putConstraint(SpringLayout.WEST, sellrateVal,10,SpringLayout.EAST, sellrate);
		slayout.putConstraint(SpringLayout.NORTH, sellrateVal,10,SpringLayout.SOUTH, buyrate);
		
		JButton submitRange=new JButton("Submit");
		submitRange.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(buyrateVal.getText().equals("")||sellrateVal.getText().equals("")||quantityVal.getText().equals(""))
				{showinputError();return;}
				String order="Range"+" "+inputfiat(buyrateVal.getText())+" "+inputfiat(sellrateVal.getText())+" "+inputcoin(quantityVal.getText())+" "+"begin";
				orderqueue.add(order);
				showsuccessMessage("Your order has been added");
				buyrateVal.setText("");
				sellrateVal.setText("");
				quantityVal.setText("");
			}
		});
		rangeTradePanel.add(submitRange);
		slayout.putConstraint(SpringLayout.WEST, submitRange,200,SpringLayout.WEST, rangeTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitRange,10,SpringLayout.SOUTH, sellrate);
		
			
		JScrollPane rangescrollPane = new JScrollPane(rangeTradePanel);
		frame.getContentPane().add(rangescrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=rangescrollPane;
		
	}
	
	public static void addPriceTradePanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel priceTradePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		priceTradePanel.setLayout(slayout);
		priceTradePanel.setPreferredSize(new Dimension(800,600));

		JLabel orderMode=new JLabel("Price Mode");
		priceTradePanel.add(orderMode);
		slayout.putConstraint(SpringLayout.WEST, orderMode,200,SpringLayout.WEST, priceTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, orderMode,50,SpringLayout.NORTH, priceTradePanel);

		
		JLabel profitRate=new JLabel("BookProfit Rate per Bitcoin(in Rs.)");
		priceTradePanel.add(profitRate);
		slayout.putConstraint(SpringLayout.EAST, profitRate,200,SpringLayout.WEST, priceTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, profitRate,50,SpringLayout.SOUTH, orderMode);
		
		JTextField profitRateVal=new JTextField(20);
		priceTradePanel.add(profitRateVal);
		slayout.putConstraint(SpringLayout.WEST, profitRateVal,10,SpringLayout.EAST, profitRate);
		slayout.putConstraint(SpringLayout.NORTH, profitRateVal,50,SpringLayout.SOUTH, orderMode);
		
		JLabel stoplossRate=new JLabel("StopLoss Rate per Bitcoin(in Rs.)");
		priceTradePanel.add(stoplossRate);
		slayout.putConstraint(SpringLayout.EAST, stoplossRate,200,SpringLayout.WEST, priceTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, stoplossRate,10,SpringLayout.SOUTH, profitRate);
		
		JTextField stoplossRateVal=new JTextField(20);
		priceTradePanel.add(stoplossRateVal);
		slayout.putConstraint(SpringLayout.WEST, stoplossRateVal,10,SpringLayout.EAST, stoplossRate);
		slayout.putConstraint(SpringLayout.NORTH, stoplossRateVal,10,SpringLayout.SOUTH, profitRate);
		
		JButton submitPrice=new JButton("Submit");
		submitPrice.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(profitRateVal.getText().equals("")||stoplossRateVal.getText().equals(""))
				{showinputError();return;}
				String order="Price"+" "+inputfiat(profitRateVal.getText())+" "+inputfiat(stoplossRateVal.getText());
				orderqueue.add(order);
				showsuccessMessage("Your order has been added");
				profitRateVal.setText("");
				stoplossRateVal.setText("");
				
			}
		});
		priceTradePanel.add(submitPrice);
		slayout.putConstraint(SpringLayout.WEST, submitPrice,200,SpringLayout.WEST, priceTradePanel);
		slayout.putConstraint(SpringLayout.NORTH, submitPrice,10,SpringLayout.SOUTH, stoplossRate);
		
		
		JScrollPane pricescrollPane = new JScrollPane(priceTradePanel);
		frame.getContentPane().add(pricescrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=pricescrollPane;
		
	}
	
	public static void addProfilePanel()
	{
		if(loadedPanel !=null)
			{frame.getContentPane().remove(loadedPanel);}
		
		JPanel profilePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		profilePanel.setLayout(slayout);
		int height=350;
		
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.put("apiKey",apiKeyVal);
			
		JLabel fiatHeading=new JLabel("Fiat Balances:");
		profilePanel.add(fiatHeading);
		slayout.putConstraint(SpringLayout.WEST, fiatHeading,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, fiatHeading,10,SpringLayout.NORTH, profilePanel);
		
		JLabel fiatAvailable=new JLabel("Available to trade:");
		profilePanel.add(fiatAvailable);
		slayout.putConstraint(SpringLayout.WEST, fiatAvailable,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, fiatAvailable,10,SpringLayout.SOUTH, fiatHeading);
		
		
		JLabel fiatAvailableVal=new JLabel(displayfiat(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/fiatbalance",jsonobj)).get(0).toString())));
		profilePanel.add(fiatAvailableVal);
		slayout.putConstraint(SpringLayout.WEST, fiatAvailableVal,5,SpringLayout.EAST, fiatAvailable);
		slayout.putConstraint(SpringLayout.NORTH, fiatAvailableVal,10,SpringLayout.SOUTH, fiatHeading);
		
		JLabel fiatInTrade=new JLabel("In Trade:");
		profilePanel.add(fiatInTrade);
		slayout.putConstraint(SpringLayout.WEST, fiatInTrade,50,SpringLayout.EAST, fiatAvailableVal);
		slayout.putConstraint(SpringLayout.NORTH, fiatInTrade,10,SpringLayout.SOUTH, fiatHeading);
		
		JLabel fiatInTradeVal=new JLabel(displayfiat(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/intradefiatbalance",jsonobj)).get(0).toString())));
		profilePanel.add(fiatInTradeVal);
		slayout.putConstraint(SpringLayout.WEST, fiatInTradeVal,5,SpringLayout.EAST, fiatInTrade);
		slayout.putConstraint(SpringLayout.NORTH, fiatInTradeVal,10,SpringLayout.SOUTH, fiatHeading);
		
		JLabel fiatTotal=new JLabel("Total:");
		profilePanel.add(fiatTotal);
		slayout.putConstraint(SpringLayout.WEST, fiatTotal,50,SpringLayout.EAST, fiatInTradeVal);
		slayout.putConstraint(SpringLayout.NORTH, fiatTotal,10,SpringLayout.SOUTH, fiatHeading);
		
		JLabel fiatTotalVal=new JLabel(displayfiat(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/actualfiatbalance",jsonobj)).get(0).toString())));
		profilePanel.add(fiatTotalVal);
		slayout.putConstraint(SpringLayout.WEST, fiatTotalVal,5,SpringLayout.EAST, fiatTotal);
		slayout.putConstraint(SpringLayout.NORTH, fiatTotalVal,10,SpringLayout.SOUTH, fiatHeading);
		
		JLabel coinHeading=new JLabel("Coin Balances:");
		profilePanel.add(coinHeading);
		slayout.putConstraint(SpringLayout.WEST, coinHeading,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, coinHeading,10,SpringLayout.SOUTH, fiatAvailable);
		
		JLabel coinAvailable=new JLabel("Available to trade:");
		profilePanel.add(coinAvailable);
		slayout.putConstraint(SpringLayout.WEST, coinAvailable,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, coinAvailable,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel coinAvailableVal=new JLabel(displaycoin(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/coinbalance",jsonobj)).get(0).toString())));
		profilePanel.add(coinAvailableVal);
		slayout.putConstraint(SpringLayout.WEST, coinAvailableVal,5,SpringLayout.EAST, coinAvailable);
		slayout.putConstraint(SpringLayout.NORTH, coinAvailableVal,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel coinInTrade=new JLabel("In Trade:");
		profilePanel.add(coinInTrade);
		slayout.putConstraint(SpringLayout.WEST, coinInTrade,50,SpringLayout.EAST, coinAvailableVal);
		slayout.putConstraint(SpringLayout.NORTH, coinInTrade,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel coinInTradeVal=new JLabel(displaycoin(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/intradecoinbalance",jsonobj)).get(0).toString())));
		profilePanel.add(coinInTradeVal);
		slayout.putConstraint(SpringLayout.WEST, coinInTradeVal,5,SpringLayout.EAST, coinInTrade);
		slayout.putConstraint(SpringLayout.NORTH, coinInTradeVal,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel coinTotal=new JLabel("Total:");
		profilePanel.add(coinTotal);
		slayout.putConstraint(SpringLayout.WEST, coinTotal,50,SpringLayout.EAST, coinInTradeVal);
		slayout.putConstraint(SpringLayout.NORTH, coinTotal,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel coinTotalVal=new JLabel(displaycoin(Long.parseLong(new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/actualcoinbalance",jsonobj)).get(0).toString())));
		profilePanel.add(coinTotalVal);
		slayout.putConstraint(SpringLayout.WEST, coinTotalVal,5,SpringLayout.EAST, coinTotal);
		slayout.putConstraint(SpringLayout.NORTH, coinTotalVal,10,SpringLayout.SOUTH, coinHeading);
		
		JLabel feesHeading=new JLabel("Fees:");
		profilePanel.add(feesHeading);
		slayout.putConstraint(SpringLayout.WEST, feesHeading,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, feesHeading,10,SpringLayout.SOUTH, coinAvailable);
		
		JLabel fiatFees=new JLabel("Fiat Fees:");
		profilePanel.add(fiatFees);
		slayout.putConstraint(SpringLayout.WEST, fiatFees,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, fiatFees,10,SpringLayout.SOUTH, feesHeading);
		
		JLabel fiatFeesVal=new JLabel((new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/fiatfeepercentage",jsonobj)).get(0).toString())+" %");
		profilePanel.add(fiatFeesVal);
		slayout.putConstraint(SpringLayout.WEST, fiatFeesVal,5,SpringLayout.EAST, fiatFees);
		slayout.putConstraint(SpringLayout.NORTH, fiatFeesVal,10,SpringLayout.SOUTH, feesHeading);
		
		JLabel coinFees=new JLabel("Coin Fees:");
		profilePanel.add(coinFees);
		slayout.putConstraint(SpringLayout.WEST, coinFees,50,SpringLayout.EAST, fiatFeesVal);
		slayout.putConstraint(SpringLayout.NORTH, coinFees,10,SpringLayout.SOUTH, feesHeading);
		
		JLabel coinFeesVal=new JLabel((new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/coinfeepercentage",jsonobj)).get(0).toString())+" %");
		profilePanel.add(coinFeesVal);
		slayout.putConstraint(SpringLayout.WEST, coinFeesVal,5,SpringLayout.EAST, coinFees);
		slayout.putConstraint(SpringLayout.NORTH, coinFeesVal,10,SpringLayout.SOUTH, feesHeading);
		
		JLabel pendingOrders=new JLabel("Pending Orders:");
		profilePanel.add(pendingOrders);
		slayout.putConstraint(SpringLayout.WEST, pendingOrders,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, pendingOrders,30,SpringLayout.SOUTH, fiatFees);
		
		
		JSONObject json=new JSONObject();
		int linesadded=0;
		
			json.put("apiKey",apiKeyVal);
		//get all pending buy orders:
		JSONArray allbuyorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/alluserbids",json));
		JLabel order;JButton cancel;
		for(int i=0;i<allbuyorders.length();i++)
		{
			String orderdetails="Buy"+"      "+displayfiat(allbuyorders.getJSONObject(i).getLong("rate"))+"      "+displaycoin(allbuyorders.getJSONObject(i).getLong("vol"))+"      "+allbuyorders.getJSONObject(i).getString("time");
			System.out.println(orderdetails);
			order=new JLabel(orderdetails);
			profilePanel.add(order);
			slayout.putConstraint(SpringLayout.WEST, order,10,SpringLayout.WEST, profilePanel);
			slayout.putConstraint(SpringLayout.NORTH, order,40*(i+1),SpringLayout.SOUTH, pendingOrders);
			cancel=new JButton("Cancel");
			String orderid=allbuyorders.getJSONObject(i).getString("orderID");
			cancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
						cancelorder(orderid,"buy");
						addProfilePanel();
						
					}
			});
			profilePanel.add(cancel);
			slayout.putConstraint(SpringLayout.WEST, cancel,10,SpringLayout.EAST, order);
			slayout.putConstraint(SpringLayout.NORTH, cancel,40*(i+1),SpringLayout.SOUTH, pendingOrders);
			linesadded++;
			height+=50;
		}
		int j=allbuyorders.length();
		//get all pending sell orders:
		
		JSONArray allsellorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/auth/alluserasks",json));
		for(int i=0;i<allsellorders.length();i++)
		{
			String orderdetails="Sell"+"      "+displayfiat(allsellorders.getJSONObject(i).getLong("rate"))+"      "+displaycoin(allsellorders.getJSONObject(i).getLong("vol"))+"      "+allsellorders.getJSONObject(i).getString("time");
			
			order=new JLabel(orderdetails);
			profilePanel.add(order);
			slayout.putConstraint(SpringLayout.WEST, order,10,SpringLayout.WEST, profilePanel);
			slayout.putConstraint(SpringLayout.NORTH, order,40*(i+1+j),SpringLayout.SOUTH, pendingOrders);
			cancel=new JButton("Cancel");
			String orderid=allbuyorders.getJSONObject(i).getString("orderID");
			cancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
						cancelorder(orderid,"sell");
						addProfilePanel();
						
					}
			});
			profilePanel.add(cancel);
			slayout.putConstraint(SpringLayout.WEST, cancel,10,SpringLayout.EAST, order);
			slayout.putConstraint(SpringLayout.NORTH, cancel,40*(i+1+j),SpringLayout.SOUTH, pendingOrders);
			linesadded++;
			height+=50;
		}
		
		
		//list monitor orders
		JLabel monitorOrders=new JLabel("Monitor Orders(unit is paisa and satoshi):");
		profilePanel.add(monitorOrders);
		slayout.putConstraint(SpringLayout.WEST, monitorOrders,10,SpringLayout.WEST, profilePanel);
		slayout.putConstraint(SpringLayout.NORTH, monitorOrders,40*(linesadded+1)+30,SpringLayout.SOUTH, pendingOrders);
		
		
		
		int waitingtime=0;
		while(true)
		{	
			if(executingorder==0) break;
			try {
				Thread.sleep(1000);
				waitingtime++;
				if(waitingtime==5) break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		Iterator<String> iter=orderqueue.iterator();
		//JLabel order=null;JButton cancel=null;
		int k=0;
		while(iter.hasNext())
		{
			String temporder=iter.next();
			
			String[] temporderarray=temporder.split(" ");
			String orderdetails="";
			for(int i=0;i<temporderarray.length;i++)
			{
				orderdetails+=temporderarray[i]+" ";
			}
			
			order=new JLabel(orderdetails);
			profilePanel.add(order);
			slayout.putConstraint(SpringLayout.WEST, order,10,SpringLayout.WEST, profilePanel);
			slayout.putConstraint(SpringLayout.NORTH, order,40*(k+1),SpringLayout.SOUTH, monitorOrders);
			cancel=new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					   int waitingtime=0;
						while(!orderqueue.remove(temporder))
						{
							try {
								Thread.sleep(1000);
								waitingtime++;
								if(waitingtime==5) break;
							} catch (InterruptedException ie) {
								// TODO Auto-generated catch block
								ie.printStackTrace();
							} 
						}
						addProfilePanel();
						
					}
			});
			profilePanel.add(cancel);
			slayout.putConstraint(SpringLayout.WEST, cancel,10,SpringLayout.EAST, order);
			slayout.putConstraint(SpringLayout.NORTH, cancel,40*(k+1),SpringLayout.SOUTH, monitorOrders);
			k++;
			height+=50;
		}
		}
		catch(JSONException e){System.out.println(e);}
		
		profilePanel.setPreferredSize(new Dimension(800,height));
		JScrollPane profilescrollPane = new JScrollPane(profilePanel);
		frame.getContentPane().add(profilescrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=profilescrollPane;
	}
	
	public static void addExchangePanel()
	{
		if(loadedPanel !=null)
			{frame.getContentPane().remove(loadedPanel);}
		
		JPanel exchangePanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		int height=350;
		exchangePanel.setLayout(slayout);
		
		
		JLabel last24hr=new JLabel("Last 24 hrs:");
		exchangePanel.add(last24hr);
		slayout.putConstraint(SpringLayout.WEST, last24hr,10,SpringLayout.WEST, exchangePanel);
		slayout.putConstraint(SpringLayout.NORTH, last24hr,10,SpringLayout.NORTH, exchangePanel);
		JLabel data=null;
		JSONObject jsonobj = new JSONObject();
		try {
			//jsonobj.put("apiKey",apiKeyVal);
			
			//get ticker data
			JSONArray tickerdata=new JSONArray(makeGet("https://api.coinsecureis.cool/v0/noauth/ticker"));
		
			String label="";
			for(int p=0;p<=tickerdata.length();p++)
			{
				
				if(p==0)
				{
					//last24hrs
					JSONObject t= tickerdata.getJSONArray(p).getJSONObject(0).getJSONObject("last24hrs");
					data=new JLabel("Fiat Amt: "+displayfiat(t.getLong("fiat"))+"     BTC Vol: "+displaycoin(t.getLong("vol"))+"     Fiat Diff: "+displayfiat(Long.parseLong(t.getString("difffiat")))+"     BTC Vol Diff: "+displaycoin(Long.parseLong(t.getString("diffvol"))));
					exchangePanel.add(data);
					slayout.putConstraint(SpringLayout.WEST, data,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, data,10,SpringLayout.SOUTH, last24hr);
					
				}
				else if(p==1 || p==2)
				{
					//last 24 hr rate max min
					JSONObject t= tickerdata.getJSONArray(p).getJSONObject(0);
					if(p==1)
					{
					label="Max rate: "+displayfiat(t.getLong("max24Hr"))+"     Difference: "+displayfiat(Long.parseLong(t.getString("diff")));
					}
					if(p==2)
					{
					JLabel rate24hr=new JLabel("Max/Min Rates (24 hrs):");
					exchangePanel.add(rate24hr);
					slayout.putConstraint(SpringLayout.WEST, rate24hr,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, rate24hr,60,SpringLayout.NORTH, last24hr);
					label=label+"          Min rate: "+displayfiat(t.getLong("min24Hr"))+"     Difference: "+displayfiat(Long.parseLong(t.getString("diff")));
					data=new JLabel(label);
					exchangePanel.add(data);
					slayout.putConstraint(SpringLayout.WEST, data,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, data,10,SpringLayout.SOUTH, rate24hr);
					label="";
					}
				}
				else if(p==3)
				{
					//lasttrade
					JLabel lasttrade=new JLabel("Last Trade:");
					exchangePanel.add(lasttrade);
					slayout.putConstraint(SpringLayout.WEST, lasttrade,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, lasttrade,110,SpringLayout.SOUTH, last24hr);
					
					JSONObject t= tickerdata.getJSONArray(p).getJSONObject(0).getJSONArray("lasttrade").getJSONObject(0);
					JSONObject dataobject=null;String type="Buy";
					try
					{dataobject=t.getJSONArray("ask").getJSONArray(0).getJSONObject(0);}
					catch(JSONException je)
					{type="Sell";dataobject=t.getJSONArray("bid").getJSONArray(0).getJSONObject(0);}
					Date date = new Date(dataobject.getLong("time"));
			        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			        format.setTimeZone(TimeZone.getTimeZone("IST"));
			        String formatted = format.format(date);
					data=new JLabel(type+"      Rate: "+displayfiat(dataobject.getLong("rate"))+"     Vol: "+displaycoin(dataobject.getLong("volume"))+"     Time: "+formatted);
					exchangePanel.add(data);
					slayout.putConstraint(SpringLayout.WEST, data,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, data,10,SpringLayout.SOUTH, lasttrade);
					
				}
				else if(p==4 || p==5)
				{
					//current rate max min
					JSONObject t= tickerdata.getJSONObject(p);
					if(p==4)
					{
					label="Highest Bid: "+displayfiat(t.getLong("highestBid"));
					}
					if(p==5)
					{
					JLabel currentrates=new JLabel("Current Best Rates:");
					exchangePanel.add(currentrates);
					slayout.putConstraint(SpringLayout.WEST, currentrates,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, currentrates,180,SpringLayout.SOUTH, last24hr);
					label=label+"             Lowest Ask: "+displayfiat(t.getLong("lowestAsk"));
					data=new JLabel(label);
					exchangePanel.add(data);
					slayout.putConstraint(SpringLayout.WEST, data,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, data,10,SpringLayout.SOUTH, currentrates);
					label="";
					}
				}
				else if(p==6)
				{
					//sum
					JLabel sum=new JLabel("Total orders sum:");
					exchangePanel.add(sum);
					slayout.putConstraint(SpringLayout.WEST, sum,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, sum,250,SpringLayout.SOUTH, last24hr);
					
					JSONObject t= tickerdata.getJSONObject(p);
					
					data=new JLabel("All ask Coin Sum: "+displaycoin(t.getLong("allaskCoinSum"))+"          All bid Fiat Sum: "+displayfiat(t.getLong("allbidFiatSum")));
					exchangePanel.add(data);
					slayout.putConstraint(SpringLayout.WEST, data,10,SpringLayout.WEST, exchangePanel);
					slayout.putConstraint(SpringLayout.NORTH, data,10,SpringLayout.SOUTH, sum);
				}
			}
		
		JLabel pendingOrders=new JLabel("Open Orders:");
		exchangePanel.add(pendingOrders);
		slayout.putConstraint(SpringLayout.WEST, pendingOrders,10,SpringLayout.WEST, exchangePanel);
		slayout.putConstraint(SpringLayout.NORTH, pendingOrders,330,SpringLayout.SOUTH, last24hr);
		
		
		
		int linesadded=0;
		
			
		//get all pending buy orders:
		JSONArray allbuyorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/allbids",null)).getJSONObject(0).getJSONArray("allbids").getJSONArray(0);
		JLabel order;
		for(int i=0;i<allbuyorders.length();i++)
		{
			String orderdetails="Buy"+"      "+displayfiat(allbuyorders.getJSONObject(i).getLong("rate"))+"      "+displaycoin(allbuyorders.getJSONObject(i).getLong("volume"));
			System.out.println(orderdetails);
			order=new JLabel(orderdetails);
			exchangePanel.add(order);
			slayout.putConstraint(SpringLayout.WEST, order,10,SpringLayout.WEST, exchangePanel);
			slayout.putConstraint(SpringLayout.NORTH, order,20*(i+1),SpringLayout.SOUTH, pendingOrders);
			
			linesadded++;
			height+=30;
		}
		
		int j=allbuyorders.length();
		//get all pending sell orders:
		
		JSONArray allsellorders = new JSONArray(makePost("https://api.coinsecureis.cool/v0/allasks",null)).getJSONObject(0).getJSONArray("allasks").getJSONArray(0);
		for(int i=0;i<allsellorders.length();i++)
		{
			String orderdetails="Sell"+"      "+displayfiat(allsellorders.getJSONObject(i).getLong("rate"))+"      "+displaycoin(allsellorders.getJSONObject(i).getLong("volume"));
			System.out.println(orderdetails);
			order=new JLabel(orderdetails);
			exchangePanel.add(order);
			slayout.putConstraint(SpringLayout.WEST, order,10,SpringLayout.WEST, exchangePanel);
			slayout.putConstraint(SpringLayout.NORTH, order,20*(i+1+j),SpringLayout.SOUTH, pendingOrders);
			linesadded++;
			height+=30;
		}
		
		
		
		}
		catch(JSONException e){System.out.println(e);}
		catch(Exception ex){System.out.println(ex);}
		
		
		exchangePanel.setPreferredSize(new Dimension(800,height));
		JScrollPane scrollPane = new JScrollPane(exchangePanel);
		
		

		
			
		JScrollPane exchangescrollPane = new JScrollPane(exchangePanel);
		frame.getContentPane().add(exchangescrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=exchangescrollPane;
	}
	
	public static void addHelpPanel()
	{
		if(loadedPanel !=null)
		{frame.getContentPane().remove(loadedPanel);}
		JPanel helpPanel = new JPanel();
		SpringLayout slayout=new SpringLayout();
		
		helpPanel.setLayout(slayout);
		helpPanel.setPreferredSize(new Dimension(800,800));

		JLabel helpMessage=new JLabel("<html>Vanila Mode<br/>Place simple buy and sell orders.<br/><br/>Ghost Mode<br/>"
				+ "Orders placed are not shown on the coinsecure exchange.<br/>The bot monitors the orders on the exchange and when "
				+ "it sees that the user order can be executed, it places the order on the exchange.<br/><br/>"
				+ "MeFirst Mode<br/>It is useful when you want to guard against users who prevent execution of your order<br/>"
				+ " by placing their order just lower/higher than yours.<br/>Eg: You placed a buy order at rate Rs.20000 now another user places buy order at Rs.20001<br/>"
				+ "then his order is executed before yours. To prevent this the bot takes the base rate and a difference.<br/>"
				+ "The bot will place the order at base rate and then monitor for any other order placed in difference from base rate<br/>"
				+ "and in case of another order the bot will cancel the previous order and place a new order better than that of <br/>"
				+ "the other user.In previous example the bot will place an order at rate Rs. 20001.01 and keep  on increasing till the difference<br/> which is the upper limit.<br/>"
				+ "<br/>Rainbow Mode<br/>It can be used to break the order into smaller orders of increasing/decreasing rate.Eg: If you place<br/>"
				+ "A sell order of 1 BTC at Rs.20000,set incement at Rs.10 and split into 5 orders<br/>The bot will place 5 "
				+ "orders starting at .2 BTC for Rs.20000, then .2 BTC at Rs.20010 and so on.<br/><br/>SIP Mode<br/>This mode"
				+ "allows you to place order which will be executed at interval of input minutes.<br/><br/>Range Mode<br/>"
				+ "This mode allows user to place two order one buy and other sell.The bot will buy bitcoin at the lower buy <br/>"
				+ "price and sell bitcoins at higher sell price. Buy and sell orders are executed in order.<br/>It can be useful in case"
				+ "you expect the bitcoin price to fluctuate in a range.Eg: If the bitcoin price is fluctuation beween Rs.20000 and <br/>"
				+ "Rs.19000, the bot will buy at Rs.19000 and sell at Rs.20000<br/><br/>Price Mode<br/>Price mode can be used to "
				+ "protect the user in case of large drop in bitcoin price or book profit in case of steep rise in price.<br/>"
				+ "If the condition for price order is met, then all the orders of the user is cancelled and a buy/sell order<br/>"
				+ "is placed for all the bitcoins in ser account.<br/><br/><br/>IMPORTANT:<br/> Please use the bot at your own risk."
				+ "The software is provided as is without any guarantee/warranty on its features<br/> or functionality.Please "
				+ "test the bot at coinsecure testnet website so that you properly understand the bot before using it on the real<br/>"
				+ "live exchange.Also keep monitoring your orders in Profile page to ensure you have not placed any unintended order.  </html>");
		helpPanel.add(helpMessage);
		slayout.putConstraint(SpringLayout.WEST, helpMessage,10,SpringLayout.WEST, helpPanel);
		slayout.putConstraint(SpringLayout.NORTH, helpMessage,10,SpringLayout.NORTH, helpPanel);
		
					
		JScrollPane helpscrollPane = new JScrollPane(helpPanel);
		frame.getContentPane().add(helpscrollPane,BorderLayout.CENTER);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		loadedPanel=helpscrollPane;
		
	}
	public static void setBalance()
	{
		if(apiKeyVal.equals(null) || apiKeyVal.equals(""))
		{}
		else
		{
			JSONObject json = new JSONObject();
			try {
				json.put("apiKey",apiKeyVal);
			
			String urlfiat="https://api.coinsecureis.cool/v0/auth/fiatbalance";
			fiatbalance=Long.parseLong(new JSONArray(makePost(urlfiat,json)).get(0).toString());
			String urlcoin="https://api.coinsecureis.cool/v0/auth/coinbalance";
			coinbalance=Long.parseLong(new JSONArray(makePost(urlcoin,json)).get(0).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String makePost(String url,JSONObject json) 
	{
		HttpClient httpclient = HttpClients.createDefault();
        HttpResponse response;
        
        StringBuffer result = new StringBuffer();
        try {
            HttpPost post = new HttpPost(url);
            if(json!=null)
            {
            StringEntity se = new StringEntity( json.toString());  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            }
            response = httpclient.execute(post);
            int rCode=response.getStatusLine().getStatusCode();
            /*Checking response */
            System.out.println("rCode "+rCode);
            
            if(response!=null&&rCode==200){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        		
        		String line = "";
        		while ((line = rd.readLine()) != null) {
        			result.append(line);
        		}

        	//System.out.println("makepost"+result.toString());
        	JSONObject obj = new JSONObject(result.toString());
        	return (obj.getJSONArray("result").toString());
            }

        } catch(Exception e) {
            e.printStackTrace();
            
        }
      
        return null;
        }
		
	public static String makeGet(String url) 
	{
		HttpClient httpclient = HttpClients.createDefault();
        HttpResponse response;
        
        StringBuffer result = new StringBuffer();
        try {
            HttpGet get = new HttpGet(url);
            
            response = httpclient.execute(get);
            int rCode=response.getStatusLine().getStatusCode();
            /*Checking response */
            System.out.println("rCode "+rCode);
            
            if(response!=null&&rCode==200){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        		
        		String line = "";
        		while ((line = rd.readLine()) != null) {
        			result.append(line);
        		}

        	//System.out.println("makepost"+result.toString());
        	JSONObject obj = new JSONObject(result.toString());
        	return (obj.getJSONArray("result").toString());
            }

        } catch(Exception e) {
            e.printStackTrace();
            
        }
       
        return null;
        }
	
	public static void showApiError()
	{
		JOptionPane.showMessageDialog(frame,
			    "Please enter you API key and login.",
			    "API error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showinputError()
	{
		JOptionPane.showMessageDialog(frame,
			    "Please enter all fields.",
			    "Input error",
			    JOptionPane.ERROR_MESSAGE);
	}
	public static void showsuccessMessage()
	{
		JOptionPane.showMessageDialog(frame,
				"Your order has been placed successfully.",
			    "Order success",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showsuccessMessage(String tmessage)
	{
		JOptionPane.showMessageDialog(frame,
				tmessage,
			    "Order Info",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void showexchangeError(String tempresult)
	{
		System.out.println("tempresult="+tempresult);
		String temperror="Exchange error";
		try {
			temperror = new JSONArray(tempresult).getJSONObject(0).getString("error");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(frame,
				temperror,
			    "Exchange error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public static String displayfiat(Long fbalance)
	{
		return("Rs "+(double)fbalance/100);
	}
	
	public static String displaycoin(Long cbalance)
	{
		return((double)cbalance/100000000+" BTC");
	}
	
	public static long inputfiat(String tfiat)
	{
		return (long) (Double.parseDouble(tfiat)*100);
	}
	
	public static long inputcoin(String tcoin)
	{
		return (long) (Double.parseDouble(tcoin)*100000000);
	}
}
