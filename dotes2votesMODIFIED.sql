CREATE DATABASE IF NOT EXISTS dotes2votes;
USE dotes2votes;

SET FOREIGN_KEY_CHECKS = 0;

#Roles table
#The roles table functions to identify what an entity does in the world. Mostly this is
#to distinguish politicians from private citizens and corporations. 
DROP TABLE IF EXISTS ROLES;
CREATE TABLE ROLES (
	
    #PRIMARY KEY: Unique identifier but the number is useless
	ROLE_ID				integer				NOT NULL, #to add: ZEROFILL AUTO_INCREMENT
    
    #Enum describing how to categorize this person; we may need to expand this
	Roletype			enum('elected official', #note that this isn't necesarily a congressperson!
							 'unelected official', #judges, etc.
							 'industry', #a company, corporation, etc.
							 'private citizen',
							 'nonprofit',
							 'interest group',
                             'pac',
                             'other'
                        ) DEFAULT 'other'	NULL,

	#Simple title, like Senator, Governor, CEO, etc. Not specific (e.g., CEO of Microsoft)
    RoleTitle			char (255) 			NOT NULL,
    
    #House, Senate, or null
    Chamber				enum('House',
							 'Senate'
						)					NULL DEFAULT NULL,
    
    #District number from 1 to 49
    District			integer	(2)			NULL,

	#A string describing what this role does. Purely informatory.
    Description			char (255)			NULL,
    
    #CONSTRAINTS
    CONSTRAINT			ROLES_PK			PRIMARY KEY (ROLE_ID),
    CONSTRAINT			DISTRICT_LIMIT		CHECK (District > 0 AND District < 50)
);

#Entities table
#This is people, organizations, companies--anything legally defined (blame Scalia)
#as a person. This is differentiated from roles in order to allow everyone to play
#politician, private citizen, donor, etc.
DROP TABLE IF EXISTS ENTITIES;
CREATE TABLE ENTITIES (

    #PRIMARY KEY: Unique identifier but the number is useless
	ENT_ID				integer				NOT NULL,

    #FOREIGN KEY: The entity's role
	ROLE_ID				integer				NULL,

	#A name, e.g. "Maria Cantwell", "Boeing"; please use a single space to separate first and last names
    EntityName			char (255)			NULL,
    
	#The political party--anything with fewer than 10 adherents should probably just be "other"
	Party				enum('Republican',
							 'Democrat',
							 'Independent',
							 'Libertarian',
                             'Socialist',
                             'Other',
                             'None'
						) DEFAULT 'None'	NOT NULL,
	
    #CONSTRAINTS
	CONSTRAINT 		    ENTITIES_PK 		PRIMARY KEY (ENT_ID),
	CONSTRAINT 		    ENTITIES_ROLE_FK	FOREIGN KEY (ROLE_ID)
											REFERENCES  ROLES (ROLE_ID)
);

#Bills table
#Table of all the bills, their names, summaries, and any other relevant info.
#Note that bills don't need a house-senate enum because the first two letters
#of a bill denote that.
DROP TABLE IF EXISTS BILLS;
CREATE TABLE BILLS (

	#PRIMARY KEY The usually 7 character name that denotes a bill (e.g. HB 3708, SB 1202)
	BILL_NAME			char (32)			NOT NULL,
    
    #The technical full name of the bill
    FullName			char (255)			NOT NULL,
    
    #The full summary of the bill (this may be quite long)
    Summary				text				NULL,

	#CONSTRAINTS
	CONSTRAINT 		    BILL_PK   			PRIMARY KEY (BILL_NAME)
);

#Keywords table
#Called Topic or Summary on the websites, this is a one-to-few words indicator
#of a politically sensitive issue--note that the offical legislative site structures
#bills to be searchable in this fashion.
DROP TABLE IF EXISTS KEYWORDS;
CREATE TABLE KEYWORDS (

	#COMPOSITE KEY: FOREIGN KEY: The bill name this topic appears in
	BILL_NAME			char (32)			NOT NULL,

	#COMPOSITE KEY: Keyword that describes in short a politically relevant issue (also subject, topic).
	Keyword				char (255)			NOT NULL,
    
    #If the Keyword links to a bill without a summary (i.e., NO BILL), then it will redirect to this keyword.
    Redirect			char (255)			NULL,
    
    #CONSTRAINTS
    CONSTRAINT			KEYWORDS_PK			PRIMARY KEY (BILL_NAME, keyword),
	CONSTRAINT 		    KEYWORDS_BILL_FK 	FOREIGN KEY (BILL_NAME)
											REFERENCES  BILLS (BILL_NAME)
);

#Votes table
#Contains a list of all the votes on all the bills.
DROP TABLE IF EXISTS VOTES;
CREATE TABLE VOTES (

	#COMPOSITE KEY: FOREIGN KEY: The house or senate voting member
	POL_ID				integer				NOT NULL,

	#COMPOSITE KEY: FOREIGN KEY: The bill name the person vote on.
	BILL_NAME			char (32)			NOT NULL,
    
    #How the person voted on this bill
	Vote				enum ('yea',
							  'nay',
							  'absent',
							  'excused',
							  'other',
                              'unknown'
						) DEFAULT 'unknown'	NOT NULL,
                        
	#Whether this person is also a sponsor of the bill
    Sponsor				bool				NOT NULL,
    
    #CONSTRAINTS
    CONSTRAINT			VOTES_PK			PRIMARY KEY (POL_ID, BILL_NAME),
	CONSTRAINT 		    VOTES_ENT_FK	 	FOREIGN KEY (POL_ID)
											REFERENCES  ENTITIES (ENT_ID),
    CONSTRAINT 		    VOTES_BILL_FK 		FOREIGN KEY (BILL_NAME)
											REFERENCES  BILLS (BILL_NAME)
);

#Donations table
#Records donations from any entity to any other entity
DROP TABLE IF EXISTS DONATIONS;
CREATE TABLE DONATIONS (
	
    #COMPOSITE KEY: FOREIGN KEY: The person giving the money
	FROM_ID				varchar (32)				NOT NULL		DEFAULT 0,
    
    #COMPOSITE KEY: FOREIGN KEY: The person recieving the money
	TO_ID				varchar (32)				NOT NULL		DEFAULT 0,
    
    #COMPOSITE KEY: The date the donation was filed on
	DonationDate		date				NOT NULL,
    
    #The amount of the donation
    Amount				decimal(9,2)		NULL,
    
    #CONSTRAINTS
    CONSTRAINT			DONATIONS_PK		PRIMARY KEY (FROM_ID, TO_ID, DonationDate)
	#CONSTRAINT 		    DONATIONS_FROM_FK	FOREIGN KEY (FROM_ID)
											#REFERENCES  ENTITIES (ENT_ID),
	#CONSTRAINT 		    DONATIONS_TO_FK		FOREIGN KEY (TO_ID)
											#REFERENCES  ENTITIES (ENT_ID)
);

SET FOREIGN_KEY_CHECKS =1;