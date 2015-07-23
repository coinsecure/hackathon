from flask import Flask,current_app,render_template
import urllib,requests,json

app=Flask(__name__,static_url_path="")
app.Debug=True
@app.route('/')
def index():
	source="https://api.coinsecure.in/v0/noauth/ticker"
	print('Yo')
	resp=requests.get(url=source)
	data=json.loads(resp.text)
	highest_bid=data['result'][4]['highestBid']
	print(highest_bid)
	lowest_ask=data['result'][5]['lowestAsk']
	print(lowest_ask)
	Total_ask=data['result'][6]['allaskCoinSum']
	return  render_template('index.html',**locals())

@app.route('/balance')
def balance():
	source="https://api.coinsecureis.cool/v0/auth/coinbalance"
	headers={'Content-Type': 'application/json'}
	param={  "apiKey": "fNFZd2A2kL0tdqwHTqeJOD2mAOVzLBSbb87Qvr1C"}
	r=requests.post(source,headers,param)
	data=json.loads(r.text)
	coin_balance=data['result'][0]
	UserBidSource="https://api.coinsecureis.cool/v0/auth/alluserbids"
	r1=requests.post(UserBidSource,headers,param)
	data=json.loads(r1.text)
	if not (data['result']):
		coin_bids="No Bids"
	CoinDepositAddress="https://api.coinsecureis.cool/v0/auth/getcoinaddresses"
	r2=requests.post(CoinDepositAddress,headers,param)
	data=json.loads(r2.text)
	add=[]
	for i in data['result']:
		add.append(i['address'])
	
	return render_template('balance.html',**locals())
@app.route('/transfer')
def transfer():
	return render_template('transfer.html')

@app.route('/bid')
def service():
	source="https://api.coinsecure.in/v0/allbids"
	resp=requests.post(url=source)
	data=json.loads(resp.text)
	total=0
	rate=[]
	volume=[]
	sumi=[]
	
	for i in data['result'][0]['allbids'][0]:
		rate.append(i['rate'])
		volume.append(i['volume'])
		sumi.append(i['sum'])
		total+=i['sum']
	
	return render_template('bids.html',**locals())

@app.route('/pay')
def paylol():
	return render_template('pay.html')
	



if __name__ == '__main__':
    app.run()