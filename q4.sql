/*Basic idea is to create temporary table for set of dates in the given range and then use it as an input
for joining of agregated data about bugs by count. Arbitrary date in far future is a kind of poor man's 
positive infinity in case of bug's close date null value.

Computational complexity: temporary table results in O(m) for space and time where m is amount of day. 
Agregate function requires sorting and mysql uses quicksort, so average case time complexity is O(n log n) and space is O(log n)
where n is amount of tuples in bugs table.
Overal time and space complexity depends on ratio between date range size and amount of tuples in bugs table. 
 
*/

DROP PROCEDURE IF EXISTS COUNT_BUGS;

DELIMITER //

CREATE PROCEDURE COUNT_BUGS(dateStart DATE, dateEnd DATE)
this_proc:BEGIN
  IF dateEnd < dateStart THEN LEAVE this_proc;
  END IF;

  DROP TABLE IF EXISTS dates;
  CREATE TEMPORARY TABLE dates(datee DATE);
  WHILE dateStart <= dateEnd DO
      INSERT INTO dates(datee) VALUES (dateStart);
      SET dateStart = date_add(dateStart, INTERVAL 1 DAY);
  END WHILE;

  SELECT d.datee, COUNT(b.id) FROM dates AS d LEFT JOIN bugs AS b
   ON d.datee >= b.open_date AND d.datee <= IFNULL(b.close_date, STR_TO_DATE('3000-1-1', '%Y-%m-%d'))
   WHERE d.datee IS NOT NULL
   GROUP BY d.datee;


  DROP TABLE dates;
END;
  //

DELIMITER ;
