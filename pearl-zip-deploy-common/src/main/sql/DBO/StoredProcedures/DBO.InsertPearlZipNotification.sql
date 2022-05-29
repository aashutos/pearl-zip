/*
 * Copyright © 2022 92AK
 */

/*
 *  Insert message with a given topic into the DBO.PearlZipNotifications table
 */
-- Currently unsupported by Cockroach DB PostGres SQL DB (Oracle DB implementation)
CREATE OR REPLACE PROCEDURE DBO.InsertPearlZipNotification (
	PARAM_TOPIC IN VARCHAR,
	PARAM_MESSAGE IN VARCHAR
)
AS
BEGIN
	DECLARE
        TOPIC_ID_EXCEPTION EXCEPTION;
        PRAGMA EXCEPTION_INIT (TOPIC_ID_EXCEPTION, -20001);
        TOPIC_ID INTEGER := -1;
    BEGIN
        SELECT ID INTO TOPIC_ID FROM DBO.TOPIC WHERE TOPIC = DBO.InsertPearlZipNotification.PARAM_TOPIC;

        IF TOPIC_ID < 0 THEN
            raise_application_error(-20001,'Topic was not found. No insertion will occur');
        END IF;

        INSERT INTO DBO.PEARLZIPNOTIFICATIONS(TOPICID,MESSAGE,CREATIONTIMESTAMP)
        VALUES (TOPIC_ID, PARAM_MESSAGE, CURRENT_TIMESTAMP);
        dbms_output.put_line('Insertion complete.');

        COMMIT;
    END;
END;
