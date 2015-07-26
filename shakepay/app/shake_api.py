from flask import request, jsonify, Flask
import time
from flask.ext.sqlalchemy import SQLAlchemy
import os
from block_io import BlockIo


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
    user_id = db.Column(db.Integer, primary_key=True)
    location = db.Column(db.String(100))
    timestamp = db.Column(db.String(100))
    amount = db.Column(db.Integer)	
    def __init__(self, user_id, location, timestamp):
		self.user_id = user_id
		self.location = location
		self.timestamp = timestamp
		self.amount = amount


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

def db_insert_shake(shake): 
	db.session.add(shake)
	db.session.commit()

time_threshold = 5000
distance_threshold = 20

def get_distance(distance1, distance2): ##TODO
	return 0

def is_matching_shake(shake1, shake2): ##TODO 
	time_diff = abs(shake1.timestamp - shake2.timestamp)
	distance = get_distance(shake1.distance, shake2.distance)
	return time_diff < time_threshold and distance < distance_threshold 

def get_matching_shake(shake1):
	shakes = User.query.all()
	for shake2 in shakes: ##TODO handle multiple shakes
		if is_matching_shake(shake1, shake2):
			return shake2
	return None


#Note only one the shakes should call the api
def start_transaction(shake1, shake2): ##TODO
	pass

# /shake params-user_id, location, timestamp, amount
# 	
@app.route('/shake', methods=['GET'])
def shake_func():
	user_id = request.args.get('user_id')
	location = request.args.get('location')
	timestamp = request.args.get('timestamp')
	amount = request.args.get('amount') ##This will be none for one of the shakes
	shake = Shake(user_id, location, timestamp, amount)
	db_insert_shake(shake)
	initial_time_sec = get_time_in_sec()
	num_seconds_retry = 5
	while get_time_in_sec() - initial_time_sec < num_seconds_retry:
		matching_shake = get_matching_shake(shake)
		if matching_shake:
			return jsonify(start_transaction(shake, matching_shake))
	response = {'success' : false, 'message' : 'No opposite device found'}
	return jsonify(**response)



###bitcoin
def get_user_block_io(user_id):
	user = User.query.filter_by(user_id=user_id).first()
	return BlockIo(user.api_key, user.secret, 2) 

#returns created bitcoin address
def get_bitcoin_address(block_io):
	return block_io.get_new_address()

def get_total_balance(block_io):
	return block_io.get_balance()

def send_bitcoins(block_io, amount, to_address): #check format
	block_io.withdraw(amounts=amount, to_addresses=to_address)

@app.route('/user/<int:user_id>', methods=['GET'])
def api_balance(user_id):
	response = {'balance' : get_total_balance(get_user_block_io(user_id))}
	return jsonify(**response)

def main():
	app.run(host='0.0.0.0', debug=True, port=12342, use_reloader=True)

if __name__ == '__main__':
	main()
	