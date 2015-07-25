# theStrategist
Bot Details:
The Bot is based on Coinsecure Testnet and an API from https://coinsecureis.cool/api is required to trade with the bot.
It supports the following strategies:

Vanila Mode
Place simple buy and sell orders.

Ghost Mode
Orders placed are not shown on the Coinsecure exchange.The bot monitors the orders on the exchange and when it sees that the user order can be executed, it places the order on the exchange.

MeFirst Mode
It is useful when you want to guard against users who prevent execution of your order by placing their order just lower/higher than yours. Eg: You placed a buy order at rate Rs.20000 now another user places buy order at Rs.20001 then his order is executed before yours. To prevent this the bot takes the base rate and a difference.The bot will place the order at base rate and then monitor for any other order placed in difference from base rate and in case of another order the bot will cancel the previous order and place a new order better than that of the other user.In previous example the bot will place an order at rate Rs. 20001.01 and keep  on increasing till the difference which is the upper limit

Rainbow Mode
It can be used to break the order into smaller orders of increasing/decreasing rate.Eg: If you place a sell order of 1 BTC at Rs.20000,set incement at Rs.10 and split into 5 orders, the bot will place 5 orders starting at .2 BTC for Rs.20000, then .2 BTC at Rs.20010 and so on.

SIP Mode
This mode allows you to place order which will be executed at interval of input minutes.

Range Mode
This mode allows user to place two order one buy and other sell.The bot will buy bitcoin at the lower buy price and sell bitcoins at higher sell price. Buy and sell orders are executed in order.It can be useful in case you expect the bitcoin price to fluctuate in a range .Eg: If the bitcoin price is fluctuation between Rs.20000 and Rs.19000, the bot will buy at Rs.19000 and sell at Rs.20000

Price Mode
Price mode can be used to protect the user in case of large drop in bitcoin price or book profit in case of steep rise in price.If the condition for price order is met, then all the orders of the user is cancelled and a buy/sell order is placed for all the bitcoins in user account.

Executable Jar: https://www.dropbox.com/s/dcnnmosc0oj3s1m/theStrategist.jar?dl=0
