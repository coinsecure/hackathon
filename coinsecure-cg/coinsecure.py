## 
## Created : Sun Jul 12 22:25:55 IST 2015
## 
## Copyright (C) 2015 Sriram Karra <karra.etc@gmail.com>
## All Rights Reserved
##
## Licenced under Affero GPL (AGPL) version 3
##

import demjson, jsonpickle, jinja2, logging, os, time, urllib2, webapp2
from hashlib import sha256 as hash_func
from datetime import datetime

from google.appengine.ext import ndb

from google.appengine.api   import urlfetch
from google.appengine.ext   import ndb

import cg
from   cg import ts_ms_from_dt, Txn, Portfolio, BUY, SELL

TEMPLATES_DIR = os.path.join(os.path.dirname(__file__), 'templates')
JENV = jinja2.Environment(
    loader=jinja2.FileSystemLoader(TEMPLATES_DIR),
    extensions=['jinja2.ext.autoescape'],
    autoescape=False)

URL_BASE = "https://api.coinsecureis.cool/v0/"
URL_BUYS = "auth/completeduserbids"
URL_SELLS = "auth/completeduserasks"

jsonpickle.set_preferred_backend('demjson')

class StatsSummary(ndb.Model):
    # id = ndb.StringProperty()
    home         = ndb.IntegerProperty()
    transactions = ndb.IntegerProperty()
    cg_actual    = ndb.IntegerProperty()
    cg_proj      = ndb.IntegerProperty()
    other        = ndb.IntegerProperty()

class CSError(Exception):
    pass

class CSHandler(webapp2.RequestHandler):
    def fetch_trades (self, url, apikey, body):
        ret = {}
        req = urllib2.Request(url, body,
                              {'Content-Type': 'application/json'})
        resp = urllib2.urlopen(req)
        content = resp.read()

        c = demjson.decode(content)
        if 'error' in c:
            raise CSError(c[u'error'])

        return demjson.decode(content)

    def fetch_buys (self, apikey):
        if apikey == "test":
            with open("data/test_buys.json") as f:
                js = demjson.decode(f.read())
        else:
            body = demjson.encode({'apiKey' : apikey})
            url = URL_BASE + URL_BUYS
            js = self.fetch_trades(url, apikey, body)

        ret = []
        for txn in js['result']:
            ret.append(Txn(BUY, **txn))

        return ret

    def fetch_sells (self, apikey):
        if apikey == "test":
            with open("data/test_sells.json") as f:
                js = demjson.decode(f.read())
        else:
            body = demjson.encode({'apiKey' : apikey})
            url = URL_BASE + URL_SELLS
            js = self.fetch_trades(url, apikey, body)

        ret = []
        for txn in js['result']:
            ret.append(Txn(SELL, **txn))

        return ret

    def hash_it (self, s):
        return hash_func(s).hexdigest()

    def record_stat (self, apikey, action):
        key = self.hash_it(self.hash_it(apikey)) if apikey else "Empty"
        acc_key = ndb.Key(StatsSummary, key)
        acc = acc_key.get()

        if not acc:
            acc = StatsSummary(id=key, home=0, transactions=0, cg_actual=0,
                               cg_proj=0, other=0)

        if action == 'home':
            acc.home += 1
        elif action == 'transactions':
            acc.transactions += 1
        elif action == 'cg-actual':
            acc.cg_actual += 1
        elif action == 'cg-proj':
            acc.cg_proj += 1
        else:
            acc.other += 1

        acc.put()

class TradesHandler(CSHandler):
    """
    Fetch the asks and bids using the specified API Key and return it as a
    json
    """

    def get (self):
        apikey = self.request.get('apikey', None)
        self.record_stat(apikey, 'transactions')

        txns  = False
        buys  = []
        sells = []
        error = False
        errmsg = ''

        if apikey in [None, ""]:
            pass
        else:
            txns  = True
            try:
                buys  = self.fetch_buys(apikey)
                sells = self.fetch_sells(apikey)
            except Exception, e:
                errmsg = str(e)
                logging.error("Error in TradesHandler: %s", e)
                error = True

        template = JENV.get_template('transactions.html')
        self.response.write(template.render({
            'txns'  : txns,
            'buys'  : buys,
            'sells' : sells,
            'cgmod' : cg,
            'error' : error,
            'errmsg' : errmsg
            }))

class CGActualHandler(CSHandler):
    def get (self):
        apikey    = self.request.get('apikey', None)

        self.record_stat(apikey, 'cg-actual')

        date_from = self.request.get('from', None)
        date_to   = self.request.get('to', None)
        debug     = self.request.get('debug', False)
        ltg_threshold = self.request.get('ltg_threshold', False)

        error  = False
        errmsg = ""
        buys   = []
        sells  = []

        if not (apikey or date_from or date_to):
            template = JENV.get_template('cg-actual.html')
            self.response.write(template.render({
                'cgs' : None,
                'error' : False
                }))
            return

        if not (apikey and date_from and date_to):
            template = JENV.get_template('cg-actual.html')
            self.response.write(template.render({
                'cgs' : None,
                'error' : True,
                'errmsg' : "All form fields are mandatory"
                }))
            return

        try:
            buys  = self.fetch_buys(apikey)
            sells = self.fetch_sells(apikey)
            error = False
        except Exception, e:
            error = True
            logging.error(str(e))
            errmsg = str(e)

        p = Portfolio(buys, sells, ltg_threshold=int(ltg_threshold))

        dt = datetime.strptime(date_from, "%Y-%m-%d")
        from_ts = ts_ms_from_dt(dt)
        dt = datetime.strptime(date_to, "%Y-%m-%d")
        to_ts = ts_ms_from_dt(dt) + cg.ONEDAY

        cgs = p.cg(from_ts, to_ts)
        result = {
            'cgs' : cgs,
            'cgmod' : cg,
            'error' : error,
            'errmsg' : errmsg
            }

        if debug:
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(jsonpickle.encode(result))
        else:
            template = JENV.get_template('cg-actual.html')
            self.response.write(template.render(result))

class CGProjHandler(CSHandler):
    ## FIXME: Lot of code duplication betwen this and the CGActual
    ## handler. ...
    def get (self):
        apikey = self.request.get('apikey', None)

        self.record_stat(apikey, 'cg-proj')

        sell_qty   = long(self.request.get('sell_qty', 0))
        price  = float(self.request.get('sell_price', 0.0))
        sell_price = long(price * 100)  # Paisa

        debug     = self.request.get('debug', False)
        ltg_threshold = self.request.get('ltg_threshold', False)

        error  = False
        errmsg = ""
        buys   = []
        sells  = []

        if not apikey:
            template = JENV.get_template('cg-proj.html')
            self.response.write(template.render({
                'cgs' : None,
                'error' : False
                }))
            return

        if not (apikey and sell_qty and sell_price):
            template = JENV.get_template('cg-proj.html')
            self.response.write(template.render({
                'cgs' : None,
                'error' : True,
                'errmsg' : "All form fields are mandatory"
                }))
            return

        try:
            buys  = self.fetch_buys(apikey)
            sells = self.fetch_sells(apikey)
            error = False
        except Exception, e:
            error = True
            logging.error(str(e))
            errmsg = str(e)

        p = Portfolio(buys, sells, ltg_threshold=int(ltg_threshold))

        dt = datetime(1980, 01, 01)
        from_ts = ts_ms_from_dt(dt)
        dt = datetime.now()
        to_ts = ts_ms_from_dt(dt)

        cgs = p.cg(from_ts, to_ts)
        if p.end_balance < sell_qty:
            error = True
            errmsg = ("Cannot sell %f BTC as your current balance (%f BTC) " +
                      "is insufficient." ) % (sell_qty / cg.SATOSHIS_F,
                                              p.end_balance / cg.SATOSHIS_F)
        else:
            p.gen_sells(sell_qty, sell_price, to_ts - 1)
            cgs = p.cg(to_ts-2, to_ts)

        result = {
            'cgs' : cgs,
            'cgmod' : cg,
            'error' : error,
            'errmsg' : errmsg
            }

        if debug:
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(jsonpickle.encode(result))
        else:
            template = JENV.get_template('cg-proj.html')
            self.response.write(template.render(result))

class MainPageHandler(CSHandler):
    def get (self):
        self.record_stat(None, 'home')

        template = JENV.get_template('index.html')
        self.response.write(template.render())

app = webapp2.WSGIApplication([('/', MainPageHandler),
                               ('/transactions', TradesHandler),
                               ('/cgActual', CGActualHandler),
                               ('/cgProj', CGProjHandler),
                               ])

