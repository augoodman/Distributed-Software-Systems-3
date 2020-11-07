# Assignment 3 README

## Description

Implements a Rebus Puzzle game over a network.
TiledRebusTCP uses TCP to transfer data.
TiledRebusUDP was not completed in time for submission.
Youtube link: 

## Command Line Arguments
First spin up the game server using:
gradle runServer -Pport=<port>
On the client, start the game by using:
gradle runClient -Phost=<hot> -Pport=<port>


## Sequence Diagram
Client                 Server
|                      |
|client protocol header|
|--------------------->|
|                      |
|server protocol header|
|<---------------------|
|                      |
|image sent            |
|<---------------------|
|                      |
|q/a sent              |
|<---------------------|
X                      X

## Protocol Description
The header has two components.
For the client the first component contains a client identifier "c".
The second component contains the dimension size wanted for the puzzle.
For the server the first component contains a server identifier "s".
The second component contains the number of questions to be sent to the client.
For this implementation, this number is a square of the dimension.

### Robustness
The client uses a user generated value to tell the server the dimension 
of the game desired.
The server randomly chooses one of three images and sends it to the client.
The client cuts up the image into tiles by given dimension.
The server calculates the minimum number of quesions required for this session
and, one by one, sends question/answer pairs to the client.
For each question, the user may anwer the given question correctly and receive
a new tile to fill in the image or they may guess the rebus puzzle solution at
any time to win the game.  Three incorrect guess during a game results in a
loss.  Number of questions answered correctly and incorrectly are displayed at
the end of the game.