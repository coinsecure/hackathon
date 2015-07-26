from flask import request, jsonify, Flask, render_template, url_for
import time
from flask.ext.sqlalchemy import SQLAlchemy
import os
from block_io import BlockIo
from flask import Response
import math

app = Flask(__name__)
basedir = os.path.abspath(os.path.dirname(__file__))

##db setup##
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:////tmp/test.db'
db = SQLAlchemy(app)
app = Flask(__name__)
app.config['SECRET_KEY'] = 'hard to guess string'
app.config['SQLALCHEMY_DATABASE_URI'] =\
        'sqlite:///' + os.path.join(basedir, 'data.sqlite')
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = True
db = SQLAlchemy(app)

##Model##
class Shake(db.Model):
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    user_id = db.Column(db.Integer)
    lat = db.Column(db.Float)
    long = db.Column(db.Float)
    timestamp = db.Column(db.Integer)
    amount = db.Column(db.Float)
    done = db.Column(db.Boolean)
    def __init__(self, user_id, lat, long, timestamp, amount):
            self.user_id = user_id
            self.lat = lat
            self.long = long
            self.timestamp = timestamp
            self.amount = amount
            self.done = False

    def __str__(self):
        return "user_id %d lat %f long %f timestamp %d amount %f " % (self.user_id, self.lat, self.long, self.timestamp, self.amount)

class User(db.Model):
    user_id = db.Column(db.Integer, primary_key=True)
    api_key = db.Column(db.String(200))
    secret = db.Column(db.String(200))
    def __init__(self, user_id, api_key, secret):
        self.user_id = user_id
        self.api_key = api_key
        self.secret = secret


def get_time_in_sec():
    return int(round(time.time()))

def db_insert(obj): 
    db.session.add(obj)
    db.session.commit()

time_threshold = 7
distance_threshold = 10 #in meters

##adapted from http://stackoverflow.com/questions/1502590/calculate-distance-between-two-points-in-google-maps-v3
def rad(x):
    return (x*math.pi)/180

def get_distance(lat1, long1, lat2, long2): 
    R = 6378137
    dLat = rad(lat2 - lat1)
    dLong = rad(long2 - long1)
    a = math.sin(dLat / 2) * math.sin(dLat / 2) + math.cos(rad(lat1)) * math.cos(rad(lat2)) * math.sin(dLong / 2) * math.sin(dLong / 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    d = R * c
    print "%f %f %f %f - %f\n" % (lat1, long1, lat2, long2, d)
    return d

def is_matching_shake(shake1, shake2): ##TODO 
    if shake1.amount * shake2.amount >= 0:  ##have same sign
        return False
    if shake1.id == shake2.id:
        return False
    time_diff = abs(shake1.timestamp - shake2.timestamp)
    distance = get_distance(shake1.lat, shake1.long, shake2.lat, shake2.long)
    return time_diff <= time_threshold and distance <= distance_threshold

def get_matching_shake(shake1):
    shakes = Shake.query.all()
    for shake2 in shakes: ##TODO handle multiple shakes
        if is_matching_shake(shake1, shake2):
            print "matching\n" + str(shake1) +"\n" + str(shake2) +"\n"
            return shake2
    return None


#Note only one the shakes should call the api
def start_transaction(shake1, shake2): ##TODO
    giver = shake1
    receiver = shake2
    if shake2.amount >= 0:
        receiver, giver = giver, receiver
    ##TODO check balance etc.
    sender_block_io = get_user_block_io(giver.user_id)
    receiver_block_io = get_user_block_io(receiver.user_id)
    receiver_address = get_bitcoin_address(receiver_block_io)
    amount = giver.amount
    return send_bitcoins(sender_block_io, amount, receiver_address)

# /shake params-user_id, location, amount
#   
@app.route('/shake', methods=['GET'])
def shake_func():
    user_id = request.args.get('user_id')
    lat = request.args.get('lat')
    long = request.args.get('long') 
    timestamp = initial_time_sec = get_time_in_sec()
    amount = request.args.get('amount') ##This will be none for one of the shakes
    if not amount:
        amount = -1.0
    shake = Shake(user_id, lat, long, timestamp, amount)
    db_insert(shake)
    #transaction is only done by giver
    num_seconds_retry = 8
    response = None
    if amount < 0:  #receiver
        time.sleep(num_seconds_retry)
        matching_shake = get_matching_shake(shake)
        if matching_shake:
            if matching_shake.done:
                response = {'success' : True, 'message' : 'Received bitcoins'}
            else:
                response = {'success' : False, 'message' : 'Matching request found but transaction failed'}
        else:
            response = {'success' : False, 'message' : 'No opposite device found'}
    else :
        while get_time_in_sec() - initial_time_sec < num_seconds_retry:
            matching_shake = get_matching_shake(shake)
            if matching_shake:
                response = start_transaction(shake, matching_shake)
                #persist the fact that transaction was a success
                shake.done = matching_shake.done = True
                db.session.commit()
                break
        else:
            response = {'success' : False, 'message' : 'No opposite device found'}
    return jsonify(**response)

@app.route('/users/create')
def create_user():
    user_id = request.args.get('user_id')
    api_key = request.args.get('api_key')
    secret = request.args.get('secret')
    user = User(user_id, api_key, secret)
    db.insert(user)

@app.route('/db/refresh')
def refresh_db():
    db.engine.execute("drop table user;")
    db.engine.execute("drop table shake;")
    db.create_all()
    db.engine.execute("insert into user values(1, '47d3-6b6f-2c49-69c9', 'hooli123');");
    db.engine.execute("insert into user values(2, 'c142-800f-049c-5c49', 'hooli123');");
    return "done"

###bitcoin
def get_user_block_io(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    return BlockIo(user.api_key, user.secret, 2) 

#returns created bitcoin address
def get_bitcoin_address(block_io):
    return block_io.get_new_address()['data']['address']

def get_total_balance(block_io):
    return block_io.get_balance()

def send_bitcoins(block_io, amount, to_address): #check format
    print "sending bitcoins amount-%f to-%s\n" % (amount, to_address)
    return block_io.withdraw(amounts=amount, to_addresses=to_address)

@app.route('/user/<int:user_id>', methods=['GET'])
def api_balance(user_id):
    response = {'balance' : get_total_balance(get_user_block_io(user_id))}
    return jsonify(**response)

@app.route('/db/create')
def create_db():
    db.create_all()
    return "done"



@app.route('/')
def shaker():
    return render_template('a.html')

def main():
    app.run(host='0.0.0.0', debug=True, port=5000, use_reloader=True, threaded=True)

if __name__ == '__main__':
    main()
