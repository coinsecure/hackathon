##
## Created : Fri Jul 10 19:10:06 IST 2015
##
## Copyright (C) 2015 Sriram Karra <karra.etc@gmail.com>
## All Rights Reserved
##
## Licenced under Affero GPL (AGPL) version 3
##

import copy, logging, sys, time
from   datetime import datetime
import demjson

_BUY  = 0
_SELL = 1
SATOSHIS   = 100000000                    # Num Satoshis in one BTC
SATOSHIS_F = 100000000.0

ONEDAY = 86400000                         # Delta in Unix timestamps for 1 Day
DEFAULT_LTG_THRESHOLD = 3                 # Num years BTC has to be held to LTG

def dt_from_ts_ms (ts_ms):
    """
    Convert a Unix timestamp since the Epoch, when it also has a millisecond
    value built in
    """

    millisec = ts_ms % 1000
    return datetime.fromtimestamp(ts_ms/1000).replace(microsecond=millisec * 1000)

def disp_time_from_ts_ms (ts_ms):
    """
    Convert a Unix timestamp since the Epoch, when it also has a millisecond
    value built in
    """

    dt = dt_from_ts_ms(ts_ms)
    return dt.strftime("%Y-%m-%d %H:%M:%S")

def ts_ms_from_dt (dt):
    return long(time.mktime(dt.timetuple())*1000)

class TxnType(object):
    def __init__ (self, typ):
        self.typ = typ

    def __str__ (self):
        return " BUY" if self.typ == _BUY else "SELL"

BUY = TxnType(_BUY)
SELL = TxnType(_SELL)

class Txn(object):
    def __init__ (self, typ, **kwargs):
        self.buy_or_sell = typ

        self.time      = kwargs.get(u'time')
        self.vol       = kwargs.get('vol', 0.0)
        self.rate      = kwargs.get('rate', 0.0)
        self.rate_spec = kwargs.get('rateSpecified', 0.0)
        self.order_id  = kwargs.get('orderID')
        self.trade_id  = kwargs.get('tradeID')
        self.fiat      = kwargs.get('fiat', 0)

    def __repr__ (self):
        ret = '\n'
        ret += 'Trade Type : %s ; ' % self.buy_or_sell
        ret += 'Trade ID : %s ; ' % (self.trade_id)
        ret += 'Timestamp : %s ; ' % (self.time)
        ret += 'BTC : %f ; ' % (self.vol / SATOSHIS_F)
        ret += 'Amt (INR) : %.2f ; ' % (self.fiat / 100.0)
        ret += 'Txn Rate : %s ; ' % (self.rate)
        ret += 'Order ID : %s ; ' % (self.order_id)
        ret += 'Order Rate : %s ; ' % (self.rate_spec)

        return ret

class CGTxn(object):
    """
    Represent a single sell transaction, corresponding buy, and gain / loss
    made """

    def __init__ (self, **kwargs):
        self.buy_id    = kwargs.get(u'buy_trade_id')
        self.sell_id   = kwargs.get(u'sell_trade_id')
        self.buy_time  = kwargs.get(u'buy_time')
        self.sell_time = kwargs.get(u'sell_time')

        # Note that this will either correspond to buy_vol or sell_vol but not
        # necessarily both.
        self.vol       = kwargs.get('vol', 0.0)
        self.open_vol  = 0.0
        self.close_vol = 0.0

        self.buy_rate  = kwargs.get('buy_rate', 0.0)
        self.sell_rate = kwargs.get('sell_rate', 0.0)

        self.ltg_threshold = kwargs.get('ltg_threshold', None)
        if not self.ltg_threshold:
            self.ltg_threshold = DEFAULT_LTG_THRESHOLD
        else:
            self.ltg_threshold = int(self.ltg_threshold)

        self.gain = self.vol/SATOSHIS_F * (self.sell_rate - self.buy_rate)/100.0
        self.stg  = self.apply_stg_rules()   # True if short term gain/loss

    def apply_stg_rules (self):
        """
        Returns True if the current transaction is a Short Term Gain/Loss txn
        and False otherwise.

        As per current rules assuming a btc is held for more than 3
        years then it is long term otherwise it will be considered short term.
        """

        sellt = dt_from_ts_ms(self.sell_time)
        buyt  = dt_from_ts_ms(self.buy_time)
        delta = sellt - buyt

        return delta.days <= (365 * self.ltg_threshold)

    def __repr__ (self):
        ret = '\n'
        ret += 'Sell ID : %s ; ' % (self.sell_id)
        ret += 'Buy ID : %s ; ' % self.buy_id
        ret += 'Sell Time : %s ; ' % (self.sell_time)
        ret += 'BTC : %f ; ' % (self.vol / SATOSHIS_F)
        ret += 'Gain (INR) : %.2f ; ' % (self.gain)
        ret += 'STG : %s' % (self.stg)

        return ret

class CGError(Exception):
    pass

class Portfolio(object):
    def __init__ (self, buys, sells, ltg_threshold=None):
        self.buys = buys
        self.sells = sells
        self.ltg_threshold = ltg_threshold

        ## FIXME: Ensure buys and sells are sorted by timestamp

    def cg (self, start_date, end_date):
        """Compute short term capital gain and long term capital gain for sell
        txns made in the specified date range.

        Will return a json in the following format:
        {
          'error' : < 'success' | 'failed' >,
          'error_msg' : < error message if any >,
          'start_balance'  : <balance before start_date in satoshis >,
          'end_balance'  : <balance after end_date in satoshis >,
          'short_gain' : < short term capital gains in INR >,
          'long_gain'  : < long term capital gains in INR >,
          'txn_log' : [ cg_txns ]
          }
        """
        self.temp_buys  = copy.deepcopy(self.buys)
        self.temp_sells = copy.deepcopy(self.sells)

        self.stg  = 0
        self.ltg  = 0
        self.cgtxns = []
        err = False
        errmsg = None

        bal = sum([b.vol for b in self.temp_buys])
        self.start_balance = bal
        self.end_balance   = bal

        for sell in self.temp_sells:
            try:
                ret = self.match_one_sell(sell, start_date, end_date)
                if not ret:
                    break
            except CGError, e:
                err    = True
                errmsg = str(e)

        ## A quick sanity check
        bal = sum([b.vol for b in self.temp_buys])
        assert(bal == self.end_balance)

        return {'error' : err,
                'error_msg' : errmsg,
                'short_gain' : self.stg,
                'long_gain' : self.ltg,
                'txn_log' : self.cgtxns,
                'vol' : self.start_balance - self.end_balance,
                'start_balance' : self.start_balance,
                'end_balance' : self.end_balance
                }

    def match_one_sell (self, sell, start_date, end_date):
        if sell.time > end_date:
            logging.debug('Sell date exceeds end_date terminating.')
            return False

        for buy in self.temp_buys:
            if buy.vol <= 0:
                continue

            if sell.time < buy.time:
                raise CGError("You sold more than you bought!")

            vol = buy.vol if buy.vol < sell.vol else sell.vol

            cgtxn = CGTxn(buy_trade_id  = buy.trade_id,
                          sell_trade_id = sell.trade_id, vol = vol,
                          buy_time  = buy.time, sell_time = sell.time,
                          buy_rate = buy.rate, sell_rate = sell.rate,
                          ltg_threshold = self.ltg_threshold)

            if sell.time > start_date:
                self.cgtxns.append(cgtxn)

                if cgtxn.stg:
                    self.stg += cgtxn.gain
                else:
                    self.ltg += cgtxn.gain

            cgtxn.open_vol = buy.vol
            buy.vol  -= vol
            sell.vol -= vol
            cgtxn.close_vol = buy.vol

            if start_date > sell.time:
                self.start_balance -= vol
            if end_date > sell.time:
                self.end_balance -= vol

            assert(sell.vol >= 0)

            if sell.vol == 0:
                return True

        assert(False)                     # We should not run out of buys

    def gen_sells (self, vol, price, ts):
        """Add a single Sell transaction for specified vol and price. Useful
        for making gain projections.

        vol in Satoshis, and price in Paise per BTC. ts is Unix timestamp"""

        sell = Txn(SELL, **{
            'time' : ts,
            'vol' : vol,
            'rate' : price,
            'orderID' : 'gowron-order',
            'tradeID' : 'gowron-trade',
            'fiat' : price / (vol / SATOSHIS_F),
            'rateSpecified' : price
            })

        self.sells.append(sell)

def build_txns (buys_json, sells_json):
    # FIXME: We will need to ensure it is sorted in chronological order
    buys = []
    for txn in buys_json['result']:
        buys.append(Txn(BUY, **txn))

    sells = []
    for txn in sells_json['result']:
        sells.append(Txn(SELL, **txn))

    return buys, sells

def main ():
    buys_fn = sys.argv[1]
    sells_fn = sys.argv[2]

    with open(buys_fn, "r") as f:
        buys_json = demjson.decode(f.read())


    with open(sells_fn, "r") as f:
        sells_json = demjson.decode(f.read())

    buys, sells = build_txns(buys_json, sells_json)

    p = Portfolio(buys, sells)
    s = time.mktime(datetime(2015, 04, 01).timetuple())*1000
    e = time.mktime(datetime(2016, 03, 31).timetuple())*1000
    print p.cg(s, e)

if __name__ == "__main__":
    main()
