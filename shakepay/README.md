HackDelhi 2015 Hackathon @ 91Springboard

	       .__            __                              
	  _____|  |__ _____  |  | __ ____ ___________  ___.__.
	 /  ___/  |  \\__  \ |  |/ // __ \\____ \__  \<   |  |
	 \___ \|   Y  \/ __ \|    <\  ___/|  |_> > __ \\___  |
	/____  >___|  (____  /__|_ \\___  >   __(____  / ____|
	     \/     \/     \/     \/    \/|__|       \/\/     


# Shakepay


## What is it ?
	
	- ShakePay allows you to make bitcoin transactions by just your phone with a shake. 		


## How to test

	- As of now there are two users in the database, User #1 and User #2.

	- Run `python shake_api.py`

	- To see the wallet info of both users:
		- `http://localhost:5000/user/1`
		- `http://localhost:5000/user/2`

	- To initiate a transaction:

		- `http://localhost:5000/shake?user_id=1&amount=0.0002&long=21.6&lat=28.5`
			- user_id = User ID by which the outbound transfer will be done
			- amount = amount to be transferred
			- lat, long = Latitude na Longitude of the location of the user

		- To receive the transaction (Both sender and receiver should be present at the same location to shake and initiate the transaction)

			- `http://localhost:5000/shake?user_id=2&long=21.6&lat=28.5`

		- Errors: 

			- ` "message": "No opposite device found" ` : Devices are not in the same range

			- `"message": "Matching request found but transaction failed"` : Only one person is shaking the device

	- To test run on mobile device, test with two user ID, with URL = http://IP-address:5000

		- Type the amount to be sent by one user, and shake both devices, and transaction will be completed with appropriate response

## Built With

	- Flask on the backend for the server and serving the API's

	- ShakeJS being used for mimicing the API requests on the phone

	- CoinSecure API

	- block.io API

## What's next for ShakePay

	- Build Android/iOS app for it.