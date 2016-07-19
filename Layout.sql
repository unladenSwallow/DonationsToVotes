USE dotes2votes;

SET FOREIGN_KEY_CHECKS =0;

#Roles table 
DROP TABLE IF EXISTS ROLES;
CREATE TABLE ROLES (
	ROLE_ID				integer				NOT NULL, #to add: ZEROFILL AUTO_INCREMENT
	Roletype			enum('elected official',
							 'industry',
							 'private citizen',
							 'nonprofit',
							 'interest group',
                             'pac',
                             'other'
                        ) DEFAULT 'other'	NULL,
    RoleTitle			char (255) 			NOT NULL,
    Chamber				enum('House',
							 'Senate'
						)					NULL DEFAULT NULL,
    District			integer	(2)			NULL,
    Description			char (255)			NULL,
    CONSTRAINT			ROLES_PK			PRIMARY KEY (ROLE_ID),
    CONSTRAINT			DISTRICT_LIMIT		CHECK (District > 0 AND District < 50)
);

DROP TABLE IF EXISTS ENTITIES;
CREATE TABLE ENTITIES (
	ENT_ID				integer				NOT NULL,
	ROLE_ID				integer				NULL,
    EntityName			char (255)			NULL,
    Location			char (255)			NULL, #TO-DO
	Party				enum('Republican',
							 'Democrat',
							 'Independent',
							 'Libertarian',
                             'Socialist',
                             'Other',
                             'None'
						) DEFAULT 'None'	NOT NULL,
	CONSTRAINT 		    ENTITIES_PK 		PRIMARY KEY (ENT_ID),
	CONSTRAINT 		    ENTITIES_ROLE_FK	FOREIGN KEY (ROLE_ID)
											REFERENCES  ROLES (ROLE_ID)
);

#DROP TABLE IF EXISTS DISTRICTS;
#CREATE TABLE DISTRICTS (
	#???
#);

DROP TABLE IF EXISTS BILLS;
CREATE TABLE BILLS (
	BILL_NAME			char (16)			NOT NULL,
    FullName			char (255)			NOT NULL,
    Topic				char (255)			NOT NULL,
	CONSTRAINT 		    BILL_PK   			PRIMARY KEY (BILL_NAME)
);

DROP TABLE IF EXISTS KEYWORDS;
CREATE TABLE KEYWORDS (
	BILL_NAME			char (16)			NOT NULL,
	Keyword				char (32)			NOT NULL,
    CONSTRAINT			KEYWORDS_PK			PRIMARY KEY (BILL_NAME, keyword),
	CONSTRAINT 		    KEYWORDS_BILL_FK 	FOREIGN KEY (BILL_NAME)
											REFERENCES  BILLS (BILL_NAME)
);

DROP TABLE IF EXISTS VOTES;
CREATE TABLE VOTES (
	POL_ID				integer				NOT NULL,
	BILL_NAME			char (16)			NOT NULL,
	Vote				enum ('yea',
							  'nay',
							  'absent',
							  'excused',
							  'other',
                              'unknown'
						) DEFAULT 'unknown'	NOT NULL,
    Sponsor				bool				NOT NULL,
    CONSTRAINT			VOTES_PK			PRIMARY KEY (POL_ID, BILL_NAME),
	CONSTRAINT 		    VOTES_ENT_FK	 	FOREIGN KEY (POL_ID)
											REFERENCES  ENTITIES (ENT_ID),
    CONSTRAINT 		    VOTES_BILL_FK 		FOREIGN KEY (BILL_NAME)
											REFERENCES  BILLS (BILL_NAME)
);

DROP TABLE IF EXISTS DONATIONS;
CREATE TABLE DONATIONS (
	FROM_ID				integer				NOT NULL,
	TO_ID				integer				NOT NULL,
	DonationDate		date				NULL,
    Amount				decimal(9,2)		NULL,
    CONSTRAINT			DONATIONS_PK		PRIMARY KEY (FROM_ID, TO_ID, DonationDate),
	CONSTRAINT 		    DONATIONS_FROM_FK	FOREIGN KEY (FROM_ID)
											REFERENCES  ENTITIES (ENT_ID),
	CONSTRAINT 		    DONATIONS_TO_FK		FOREIGN KEY (TO_ID)
											REFERENCES  ENTITIES (ENT_ID)
);

SET FOREIGN_KEY_CHECKS =1;