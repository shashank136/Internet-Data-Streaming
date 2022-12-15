Steps for execution:
--------------------
1. Once the zip file is extracted scoure code can be found inside src folder

2. Open terminal and cd to .../src directory

3. In terminal enter the following commands:
	a) javac XHashTables.java
	b) java XHashTables

4. You will prompted to enter no. of entries in hashtable, no. of flows, no. of hashes, no. of cukoo steps, no. of segments

5. Once the execution completes following three files will be generated:
	a) multiHashTable.txt -- Report for Multi-Hash Table
	b) cukooHashTable.txt -- Report for Cukoo-Hash Table
	c) dLeftHashTable.txt -- Report for D-Left Hash Table
	
Report Description
------------------

1. This file contains the total no of filled flow entries in the hash table and also what flowIds were filled in what indexes of the hash table. If you find any entry as zero it means that the flowId was not present in that index of HashTable
	