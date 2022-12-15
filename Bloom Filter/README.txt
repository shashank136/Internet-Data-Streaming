Steps for execution:
--------------------
1. Once the zip file is extracted scoure code can be found inside src folder

2. Open terminal and cd to .../src directory

3. In terminal enter the following commands:
	a) javac XBloomFilters.java
	b) java XBloomFilters

4. You will prompted to enter no. of elements, no. of bits, no. of hashes, no. of elements to add and remove, no. of sets

5. Once the execution completes following three files will be generated:
	a) BloomFilter.txt -- Report for Bloom Filter
	b) CodedBloomFilter.txt -- Report for Coded Bloom Filter
	c) CountingBloomFilter.txt -- Report for Counting Bloom Filter
	
Report Description
------------------

1. BloomFilter.txt:
	a) first line mentions number of elements of setA found successfully during lookup
	b) second line mentions number of elemnets of setB found successfully during lookup

2. CodedBloomFilter.txt:
	a) Number of elements that have correct look up
	
3. CountingBloomFilter.txt:
	a) Number of elements of Set A found during look up in Counting Bloom Filter
	