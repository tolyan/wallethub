/*
The set of delimiters of words is not set, so I assumed it as such ' ,.;:-!?/()[]{}@><', though it can be passed as argument to function as well.
The algorithm is following: convert all characters into lower case with MySQL builint function and 
then iterate thru string like it's an array of characters while capitalizing first letters after sequence of delilmiters.
Digits are considered as part of alphabet, thus letter following digit without delimiters will not be capitalized.

Computational complexity: there is one loop of iteration thru string (array of characters), so execution time is linearly dependent on 
lengh of the string - O(n) and for space it's the same O(n).
*/

DROP FUNCTION IF EXISTS INITCAP;

DELIMITER //

-- Capitalises the first letter of each word.
CREATE FUNCTION INITCAP(str VARCHAR(255)) RETURNS VARCHAR(255) deterministic
BEGIN
  DECLARE delims CHAR(18) DEFAULT ' ,.;:-!?/()[]{}@><';
  DECLARE charr CHAR(1);
  DECLARE s VARCHAR(255);
  DECLARE i INT DEFAULT 1;
  DECLARE isDelim INT DEFAULT 1;

  SET s = LCASE(str);
  WHILE i < LENGTH(str) DO
    BEGIN
      SET charr = SUBSTRING(s, i, 1);
      IF LOCATE(charr, delims) > 0 THEN
        SET isDelim = 1;
      ELSEIF isDelim=1 THEN
        BEGIN
          IF charr >= 'a' AND charr <= 'z' THEN
             BEGIN
               SET s = CONCAT(LEFT(s,i-1),UCASE(charr),SUBSTRING(s,i+1));
               SET isDelim = 0;
             END;
          ELSEIF charr >= '0' AND charr <= '9' THEN
            SET isDelim = 0;
          END IF;
        END;
      END IF;
      SET i = i+1;
    END;
  END WHILE;
  RETURN s;
END //

DELIMITER ; 
