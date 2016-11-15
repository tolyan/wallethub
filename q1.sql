/*Domain of rank value is unclear, I assume that domain of rank equals to natural numbers domain 
and ranking of tuples with equal votes field values is done by other means (identity field value in our case).
In order to solve this issue we need to solve 2 subissues: 
sort by votes and generate some sort of sequence containing natural numbers that can be reflected on votes number.
Sorting by votes is done with trivial ORDER BY clause and natural numbers sequence is done with help of incrementing local variable.
Subselect is used in order to reset variable value with every query.

Computational complexity: MySQL uses quicksort as basis for sorting chunks of data and subselect is a constant time factor, 
thus average case performance is O(n log n) where n is the number of votes tuples. For space it is O(log n).
*/

SELECT @rank := @rank + 1 as rank, name, votes FROM votes, (SELECT @rank := 0 ) ranked order by votes desc;
