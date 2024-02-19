DROP PROCEDURE IF EXISTS insert_userrightspecifics;
CREATE PROCEDURE insert_userrightspecifics ( IN userId long,IN orgId long, IN userrightId int,coalist varchar(10000), coaamountlimitfrom varchar(10000), coaamountlimitto varchar(10000))
BEGIN
	   
    DECLARE v_PARTICULARS_ID LONG DEFAULT 0;   
    DECLARE x INT DEFAULT 0; 
    DECLARE y INT DEFAULT 0; 
    SET y = 1;  
 
    IF NOT coalist IS NULL 
    THEN 
           SELECT LENGTH(coalist) - LENGTH(REPLACE(coalist, ',', '')) INTO @noOfCommas; 
          delete from USER_has_RIGHTS_for_CHART_OF_ACCOUNTS where USER_ID=userId and USER_RIGHT_ID=userrightId;
           IF  @noOfCommas = 0 
          THEN 
                  INSERT INTO USER_has_RIGHTS_for_CHART_OF_ACCOUNTS
                   (USER_ID,USER_RIGHT_ID,SPECIFICS_ID,SPECIFICS_PARTICULARS_ID,AMOUNT,AMOUNT_TO) 
                   VALUES(userId,v_rightId,@coaId,v_PARTICULARS_ID,@amtFrom,@amtTo); 
          ELSE 
                SET x = @noOfCommas + 1; 
                WHILE y  <=  x DO 
                   SELECT SPLIT_STR(coalist, ',', y) INTO @coaId; 
                   SELECT SPLIT_STR(coaamountlimitfrom, ',', y) INTO @amtFrom; 
                   SELECT SPLIT_STR(coaamountlimitto, ',', y) INTO @amtTo; 
                   select  PARTICULARS_ID into v_PARTICULARS_ID from SPECIFICS where id=@coaId and ORGANIZATION_ID=orgId;  
                   
                   INSERT INTO USER_has_RIGHTS_for_CHART_OF_ACCOUNTS
                   (USER_ID,USER_RIGHT_ID,SPECIFICS_ID,SPECIFICS_PARTICULARS_ID,AMOUNT,AMOUNT_TO,PRESENT_STATUS,CREATED_BY, CREATED_AT) 
                   VALUES(userId,userrightId,@coaId,v_PARTICULARS_ID,@amtFrom,@amtTo, 1, 1, now()); 
                   
                   SET  y = y + 1; 
                END WHILE; 
        END IF; 
    END IF; 

END;
