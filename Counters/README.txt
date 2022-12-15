Steps for execution:
--------------------
1. Once the zip file is extracted scoure code can be found inside src folder

2. Open terminal and cd to .../src directory

3. In terminal enter the following commands:
	a) javac Counters.java
	b) java Counters

4. You will prompted to enter input file name, no. of counter arrays, no. of counters

5. Once the execution completes following three files will be generated:
	a) CountMinOutput.txt -- Report for Count min
	b) CounterSketchOutput.txt -- Report for Counter sketch
	c) ActiveCounter.txt -- Report for Active Counter
	
Report Description
------------------

1. CountMinOutput.txt:
	a) first line mentions avg. error for count min.
	b) rest of the 100 lines shows actual and estimated value of flow size for flow with largest estimated flow size.

2. CounterSketchOutput.txt:
	a) first line mentions avg. error for counter sketch.
	b) rest of the 100 lines shows actual and estimated value of flow size for flow with largest estimated flow size.
	
3. ActiveCounter.txt:
	a) Final Active counter state.
	