/*
 * Copyright © 2022 92AK
 */

-- DECLARATION
-- dummy user
CREATE USER PUB
    IDENTIFIED BY GuestAnonymous2022
    TEMPORARY TABLESPACE temp
    QUOTA 10M ON system;

-- PERMISSIONS
