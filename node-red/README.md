# Evaluation lab - Node-RED

## Group number: 21

## Group members

- Aristei Francesco 10804304
- Mazzola Dario 10650009
- Repole Giampiero 10543357

## Problem addressed

You are to implement a Telegram bot using Node-RED capable of answering four queries.
- Return the average temperature in Milano or Hamburg based on the last ten readings of sensor.community.
- Return the maximum and minimum humidity in Milano or Hamburg based on the last ten readings of sensor.community.
- Return the list of user names the bot interacted with, until the time of the query.
- Confirm if the bot ever interacted with a specific user.

User: “What is the average temperature in Milano?

Bot: “According to sensor.community, the average temperature in Milano is 7C!”

User: “Who did you talk to?”

Bot: “In my whole life, I talked to Luca, Alessandro, and Pietro until now.”

- You must properly format the output string!

User: “Have you ever talked to Luca?”

Bot: ”Yes, I talked to Luca before”

User names are explicitly communicated to the bot.
Example, from the user: 

“Hi, my name is Luca!”

- User names are unique.
- You can format the queries the way you like.
- But you must format answers too!
- You can assume the bot is installed on a Node-RED machine that is up 24/7

## Description of message flows

For the first two queries we used 2 separates mqtt nodes: 1st one to subscribe to the topic "/smartcity/milan", 2nd to "/smartcity/hamburg".

The data published by the nodes are sent to 2 different function nodes where we filter the content of our interest (temperature and humidity).

Then we passed these filtered data to other function nodes that saves the last 10 values(a queue for temperature values and another queue for the humidity values) using the flow module to have them shared among the whole Node-Red instance.

At this point we set a telegram receiver node to get the queries of the users and we compute the output using a function node based on the query text.

In the query function node we used the flow module to access the shared data and compute the results accordingly.

Last we used various text nodes to manage each type of query result and these are finally sent to the telegram sender which outputs the results on the client telegram chat.

To keep track of the users that interact with the bot in the same Node-Red instance, as asked in the last 2 queries, we continued to use the flow module saving their usernames.

## Extensions 
node-red-contrib-chatbot

## Bot URL 
http://t.me/NSDS_group21_bot
