## 
## Created : Sun Jul 19 15:20:29 IST 2015
## 
## Copyright (C) 2015 Sriram Karra <karra.etc@gmail.com>
## All Rights Reserved
##
## Licenced under Affero GPL (AGPL) version 3
##

## Generate Test Transactions and generate buy and sell json files in the
## same format as the Coinsecure API. Output is written to data/test_buys.json
## and data/test_sells.json. If the files already exist, they will be
## overwritten

## Logic:
##
## Buys
## - 1 buy per day
## - Date range: 01-01-2014 to 30-06-2015 (covering 3 financial years)
## - 0.01 BTC per txn
## - Random price between 5,000 - 20,000 INR / BTC

## Sells
## - 1 sell every 3 days
## - random amount less than cumulative balance till that date

import cg, demjson

import datetime, random

START_DATE = datetime.datetime(2014, 01, 01)
END_DATE   = datetime.datetime(2015, 06, 30)
DAYS = (END_DATE - START_DATE).days

BUY_VOL = long(0.01 * cg.SATOSHIS)
RATE_MIN = 500000
RATE_MAX = 2500000
SELL_SATOSHI_MUL = 100000            # onlky sell in multiples of 100K S

def gen_buy (nbuy, day):
    rate = random.randint(RATE_MIN, RATE_MAX)
    fiat = long(rate * (BUY_VOL / cg.SATOSHIS_F))

    return {
        "time": cg.ts_ms_from_dt(day),
        "vol": BUY_VOL,
        "rate": rate,
        "orderID": "test_buy_order_%03d" % nbuy,
        "fiat": fiat,
        "tradeID": "test_buy_trade_%03d" % nbuy,
        "rateSpecified": rate
    }

def gen_sell (nsell, day, total_buy_vol, total_sell_vol):
    ## FIXME: To fix thes
    balance = total_buy_vol - total_sell_vol
    vol = random.randint(1, balance/SELL_SATOSHI_MUL) * SELL_SATOSHI_MUL
    rate = random.randint(RATE_MIN, RATE_MAX)
    fiat = long(rate * (vol / cg.SATOSHIS_F))

    return {
        "time": cg.ts_ms_from_dt(day),
        "vol": vol,
        "rate": rate, 
        "orderID": "test_sell_order_%03d" % nsell,
        "fiat": fiat,
        "tradeID": "test_sell_trade_%03d" % nsell,
        "rateSpecified": rate
    }

def gen_txns ():
    buys = []
    sells = []
    nsells = 0
    total_buy_vol = 0
    total_sell_vol = 0

    for nday in range(0, DAYS):
        day = START_DATE + datetime.timedelta(days=nday)
        buy = gen_buy(nday, day)
        buys.append(buy)
        total_buy_vol += buy['vol']

        if ((nday - 1) % 3) == 0:
            sell = gen_sell(nsells, day, total_buy_vol, total_sell_vol)
            sells.append(sell)
            nsells += 1
            total_sell_vol += sell['vol']

    return buys, sells

def save_to_file (fn, js):
    with open(fn, "w") as f:
        f.write(demjson.encode(js, compactly=False))

def main ():
    buys, sells = gen_txns()    
    save_to_file('test_buys.json', { "result" : buys})
    save_to_file('test_sells.json', { "result" : sells })

if __name__ == "__main__":
    main()
