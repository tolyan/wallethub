/*
Basic idea is to iterate thru tuples with cursor, extract strings separated with delimiter and insert them into new table.
Prepared statement is used in order to create temporary table from input table passed in as argument. This temporary table is
used with cursor. Another temporary table is used to store results. Usage of temporary tables helps to avoid conflicts in multi-user 
environment.

Computational complexity:
There is a iteration thru m delimited words inside main loop that goes for n rows, thus time is O(n m).
New table is created for n splitted words, thus space is linearly dependent on n - O(n)
*/

DROP PROCEDURE IF EXISTS EXPLODE;

DELIMITER //

CREATE PROCEDURE EXPLODE(tbl VARCHAR(64), delimiterr VARCHAR(32))
  BEGIN

      DECLARE id INT DEFAULT 0;
      DECLARE name VARCHAR(250);
      DECLARE finds INT DEFAULT 0;
      DECLARE i INT DEFAULT 0;
      DECLARE result VARCHAR(50);
      DECLARE done INT DEFAULT 0;
      DECLARE cur CURSOR FOR SELECT vw_myproc.id, vw_myproc.name FROM vw_myproc;
      DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

      DROP TABLE IF EXISTS vw_myproc;
      SET @sel = CONCAT('CREATE TEMPORARY TABLE vw_myproc AS SELECT ', tbl, '.id, ', tbl, '.name FROM ', tbl);
      PREPARE stm from @sel;
      EXECUTE stm;
      DEALLOCATE PREPARE stm;
      DROP TABLE IF EXISTS new_tbl;
      CREATE TEMPORARY TABLE new_tbl(id INT, name VARCHAR(250));
      OPEN cur;

        read_loop: LOOP
          FETCH cur INTO id, name;
          IF done THEN
              LEAVE read_loop;
          END IF;

          IF LOCATE(delimiterr, name) > 0 THEN

              SET finds = (SELECT LENGTH(name) - LENGTH(REPLACE(name, delimiterr, '')) + 1 );
              SET i = 1;

              WHILE i <= finds DO

                  SET result = (SELECT REPLACE(SUBSTRING(SUBSTRING_INDEX(name, delimiterr, i),
                                                        LENGTH(SUBSTRING_INDEX(name, delimiterr, i - 1)) + 1),
                                                         delimiterr, ''));
                  IF result <> '' THEN
                    INSERT INTO new_tbl(id, name) VALUES (id, result);
                  END IF;
                  SET i = i + 1;

              END WHILE;
          ELSE
              INSERT INTO new_tbl VALUES (id, name);
          END IF;

        END LOOP read_loop;
        CLOSE cur;
        DROP TABLE vw_myproc;
        SELECT * FROM new_tbl;
        DROP TABLE new_tbl;
  END;
  //

  DELIMITER ;




