Assignment 3 for 159.355 "Concurrent Systems"

The goal for this assignment was to create a scenario where 25 socially distancing families need to do their shopping, but only one can be at the store at a time.
Each family is represented as its own thread, and all threads are running concurrently.

There are two different options of communication, shown in different files for each section (see 'Files for each section' below).
One option is a Permission based Ricart Agrawala algorithm, which only lets a family go shopping if every other family first confirms that they are not currently shopping.
The other option is a Token passing Ricart Agrawala algorithm, which has the lowest token numbered family go shopping first, then tells the family with the next lowest token.
Once all families have finished shopping, the program stops. 




Files for each section:
Permission based Ricart Agrawala algorithm:
	PermissionBased.java
	PmBuffer.java
	Family.java
Token passing Ricart Agrawala algorithm:
	TokenPassing.java
	TpBuffer.java
	TokenFamily.java

Run permission based through PermissionBased.java
Run token passing through TokenPassing.java
