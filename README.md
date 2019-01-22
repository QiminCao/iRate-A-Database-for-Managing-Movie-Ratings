# iRate: A Database for Managing Movie Ratings

## Project Overview
> iRate is a social media application that allows registered theater customers to rate and review a movie that they have watched, and for other customers to endorse the reviews.
>
#### Team members
- Karen Cao
- Alice He 
#### Technologies
- Java
- MySQL
***

## Detailed description of the project
> iRate is a social media application that encourages customers to rate a movie that they have watched at the theater in the past 7 days and write a short review on the movie. It also supports the function that other registered customers can endorse one review of a movie as "helpful" every day. The writer of the top endorsed review of a movie written 3 days earlier will receive a free movie ticket as a reward. Customers who endorsed one or more reviews as "helpful" on a specific day will be selected to receive a free concession item as a reward.
>

### The problem that we are trying to solve:
- Record the registered customer information of the theater. <br> 
Constraint: a customer has to provide a valid email address to register. If a customer is deleted, all of his or her attendances, reviews, and endorsements are also deleted.

- Record the movie information playing at the theater.<br> 
Constraint: If a movie is deleted from the database, all of its reviews and attendances are also deleted.

- Record the attendance information (a movie seen by a customer on a given date), in order to verify attendance when creating a review.<br> 
- Record the reviews created by customers. <br> 
Constraints: A customer can only review a movie once. And a customer can only write the review within 7 days of the most recent attendance of the movie.

- Record the endorsement information. <br> 
Constraints: a customer cannot endorse his or her own reviews. And endorsing is closed for all reviews of the movie created three days ago.
***

## Design
### 1. Entities
- Customer
- Movie
- Review
### 2. Relationship
- Attendance, Endorsement
- Customer and Attendance: one to many relationship, a customer can have many attendance records.
- Movie and Attendance: many to one relationship, a movie can have many customer attendances.
- Customer and Review: one to many relationship, a customer can write many reviews, but one review for one movie.
- Movie and Review: one to many relationship, a movie can have many reviews on it.
- Customer and Endorsement: one to many relationship, a customer can make many endorsements, but one endorsment for each day.
- Review and Endorsement: one to many relationship, a review can have many endorsements.
***

## Data Model and Schema
![image](https://github.com/QiminCao/iRate-A-Database-for-Managing-Movie-Ratings/blob/master/iRate_ER_model.png)
### Entity Tables: Customer, Movie, Review
#### 1. Customer: contains the information of registered customers of the theater.
- ##### The customer table contains five attributes: 
 
  (1) uid (primary key): a gensym customer ID as INT.<br>
  (2) first_name: the first name of the customer as VARCHAR(100) and NOT NULL.<br>
  (3) last_name: the last name of the customer as VARCHAR(100) and NOT NULL<br>
  (4) email: the email address of a customer as VARCHAR(100) and NOT NULL. The email address is set to be unique, which means that one email address can only belong to one customer.<br>
  (5) join_date: the date the customer joined the theater as DATE and NOT NULL. 
- ##### A trigger customer_limit before insert on the customer table for each row is created to check whether the email entered is valid or not. If the email address is not a valid format, the record cannot be inserted into the table.

#### 2. Movie: a record of a movie playing at the theater.
- ##### The movie table contains two attributes: 
  (1) id (primary key): a gensym movie ID as INT.<br> 
  (2) title: the title of the movie as VARCHAR(100) and NOT NULL

#### 3. Review: a review of a particular movie attended by a customer within 7 days.
- ##### The review table contains six attributes:
  (1) customer_uid: a foreign key references uid in the customer table.<br>
  (2) movie_id: a foreign key references id in the movie table.<br>
  (3) id (primary key): a gensym review ID as INT.<br> 
  (4) rating: the rating of a movie, it is an ENUM type with six values: 0 stars, 1 stars, 2 stars, 3 stars, 4 stars, 5 stars. The field cannot be null.<br>
  (5) review: the content of the review with the maximum length of 1000 characters as VARCHAR(1000).<br>
  (6) review_date: the date that the review is written as DATE.<br>

    The customer_uid is a foreign key references uid in Customer table. When a customer is deleted in the customer table, all his or her reviews are deleted. This is achieved by ON DELETE CASCADE.
    
    The movie_id is a foreign key references id in Movie table. When a movie is deleted in the movie table, all of its reviews are deleted. This is achieved by ON DELETE CASCADE.
    
    The customer_uid and movie_id together is required to be unique, to garantee that a customer can only write a review on a movie once.

- ##### A trigger review_limit before insert on the review table for each row is created to check the following constraints:
    (1) If the customer has not watched the movie yet, he or she cannot write a review for the movie;<br>
    (2) The date of the review written must be within 7 days of the most recent attendance of the movie. 

### Relationship Tables: Attendance, Endorsement
#### 4. Attendance: a record of a movie seen by a customer on a given date.
- ##### The attendance table contains three attributes:
    (1) customer_uid: a foreign key references uid in the customer table.<br>
    (2) movie_id: a foreign key references id in the movie table.<br>
    (3) watch_date: the date the customer attended a specific movie as DATE.<br>

    All these three attributes form the primary key for the attendance table.  
    
    The customer_uid is a foreign key reference uid in the Customer table. When a customer is deleted in the customer table, all his or her attendances are deleted. This is achieved by ON DELETE CASCADE.
    
    The movie_id is a foreign key references id in Movie table. When a movie is deleted in the movie table, all of its attendance is deleted. This is achieved by ON DELETE CASCADE.

#### 5. Endorsement: an endorsement of a movie review by a customer.
- ##### The endorsement table contains three attributes:
    (1) customer_uid: a foreign key references uid in the customer table.<br>
    (2) review_id: a foreign key references id in the review table.<br>
    (3) endorse_date: the date that a review is endorsed as DATE.

    The customer_uid is a foreign key references uid in Customer table. When a customer is deleted in the customer table, all his or her endorsements are deleted. This is achieved by ON DELETE CASCADE.
    
    The movie_id is a foreign key references id in Movie table. When a movie is deleted in the movie table, all of its endorsements are deleted. This is achieved by ON DELETE CASCADE.
- ##### A trigger endorse_limit before insert on the review table for each row is created to check the following constraints:
    (1) A customer cannot endorse his or her own review;<br>
    (2) The endorsement is closed for all reviews of movies written three days ago. So a customer cannot endorse a review written more than three days ago;<br>
    (3) If the customer has endorsed one review of a particular movie on the current endorse_date, he or she cannot endorse second time on the same date.

### Implementation
- In this project, we developed and documented a data model to represent entities and relationships in the social media application. 
- Provided DDL for creating five tables: Customer, Movie, Attendance, Review, Endorsement. 
- Provided DML to parse the datasets from the data file (.txt), and editing entries in the tables. 
- Provided DQL for making commonly used queries to get information about the status of reviews and endorsements from the database, in order to get the winner of the review and candidates to receive a free concession item.
  - In order to easily retrieve the winner of the top endorsed review of a movie written three days earlier to receive a free movie ticket, we created a stored procedure in MySQL called findTopEndorsedReview. When we want to get this information on a particular day, we can just call the procedure with a date as input.
  - In order to easily retrieve the candidates who endorsed one or more movie reviews as "helpful" on a given day to receive a free concession item, we created a stored procedure called findCustomerForFreeConcessionItem. When we want to get this information on a particular day, we can just call the procedure with a date as input.
***

## Setup & Run & Output
### How to run the program
##### 1. Download iRate project to a local folder, unzip it and import iRate as project to IntelliJ IDEA Ultimate. If you are using Eclipse, please make sure the connection and environment is correct.

##### 2. Make sure MySQL and Java environment are set up on your machine.
     
Verify MySQL is installed in terminal: 
~~~
mysql -u username
~~~

If mysql is installed, go to 3. Otherwise, you can use Homebrew to install MySQL in terminal: 
~~~
brew install mysql
~~~
     
##### 3. Connecting to a database MySQL in IDEA:

- Open the Database tool window (**View | Tool Windows | Database**) and click the **Data Source Properties** icon.
- In the **Data Sources and Drivers** dialog, click the **Add** icon and select **MySQL**.
- At the bottom of the data source settings area, click the **Download missing driver files** link.
- Specify **Database** is "iRate" and Leave the **Host** and **Port** as default.
- If you have your own  MYSQL username and password, use them to specify **User** and **Password**. In the meantime, you need to navigate to the iRate.java placed in the src file, change the attributes USER to your username and PSW to your password. If you don't have your own MYSQL username and password, specify **User** is "root" and leave **Password** blank.

- To ensure that the connection to the data source is successful, click **Test Connection**.

##### 4. Click [mysql-connector-java-8.0.12.jar](https://github.ccs.neu.edu/2018FACS5200SV/project-1-qimin/blob/master/mysql-connector-java-8.0.12.jar) and download it. Add this jar file to project library.
- Open the Project Structure window  (**File | Project Structure**) and click the **Modules** icon.
- In the **Dependencies** dialog, click the **Add** icon and add "mysql-connector-java-8.0.12.jar".
- Click **Apply** and then **OK**.

##### 5. Navigate to Main.java and run it.


### How to interpret the output
All of the results of our program will output to the console. Basically, there are two parts of the program output. One part is built iRate scheme and another is tested iRate. Every part of the output is clearly labeled. The steps of every part are listed as below:

##### Part 1: Build iRate schema
- Connecting to database<br>
- Creating database<br>
- Dropping and recreating tables in the iRate database<br>
- Dropping and recreating triggers in the iRate database<br>
- Dropping and recreating procedures in the iRate database 

##### Part 2: Test iRate 
- Deleting and repopulating tables in the iRate database<br>
- Testing invalid email<br>
- Testing review constraints<br>
- Testing endorsement constraints<br>
- Find the writer of the top rated review procedure call<br>
- Testing delete constraints<br>
- Find the writer of the top rated review procedure call after deleting records <br>
 Find candidates to receive free concession items
***

## Testing strategy
> Tests include both things that should work and that should not. These things are separated into 7 methods, they are insertData, testEmail, testReviewLimit, testEndorsementLimit, testDelete, callFindTopRatedReview and callFindCustomerForFreeConcessionItem.
> 

#### 1. insertData 
Insert valid data to tables. Every valid data should be inserted into corresponding tables. After inserting, the number of rows in table should like below:

Table Customer: count: 11 <br>
Table Movie: count: 8<br>
Table Attendance: count: 14<br>
Table Review: count: 10<br>
Table Endorsement: count: 13

#### 2. testEmail
Test insert invalid email to the Customer table, includes three invalid email statement:
- Test email missing "." operator, e.g. ikaren@gmailcom
- Test email missing "@" operator, e.g. ialice?gmail.com
- Test email missing "." and "@" operator, e.g. ikaren@gmailcom

Any of these invalid emails cannot be inserted into Customer table and it will throw a warning message when it is inserted.

#### 3. testReviewLimit
Test insert review into Review table that violates the constraints of review includes four invalid review statements:
 - Test a customer can only have one review on one movie.
 - Test a customer can not review a movie if he/she did not watch that movie.
 - Test insert review after 7 days reviews created.
 - Test insert review before a customer attends a movie.
 
Any insertion violates these constraints cannot be inserted into Review table and it will throw a warning message when it is inserted.

#### 4. testEndorsementLimit
Test insert endorsement into Endorsement table that violates the constraints of endorsement includes three invalid endorsement statements:
- Test a customer cannot endorse his/her own review.
- Test a customer can not endorse the review that is created 3 days ago.
- Test a customer can not endorse a review twice on the same day.

Any insertion violates these constraints cannot be inserted into Endorsement table and it will throw a warning message when it is inserted.

#### 5. testDelete
Test delete constraints, include three delete statements:

- If a customer is deleted, all of his or her attendances, reviews and endorsements are deleted.
- If a movie is deleted, all of its attendances and reviews are deleted.
- If a review is deleted, all endorsements are also deleted.

#### 6. callFindTopRatedReview 
Test callFindTopRatedReview function to find the writer of the top rated review of a movie written three days earlier. The chosen customer receives a free movie ticket.

#### 7. callFindCustomerForFreeConcessionItem 
Test callFindCustomerForFreeConcessionItem function to find all the candidates who voted one or more reviews as "helpful" on a given day. The chosen customer receive a free concession item.
***


## Major design decisions, limitations, and future steps
### Major design decisions
#### 1. Data preprocessing:
We wrote a function to parse the datasets in files (.txt), and insert data into tables row by row.
#### 2. Data model and schema:
- We designed five tables to represent the three entities(customer, movie, and review) and two relations (attendance and endorsement).
- To support the foreign key constraints, we used ON DELETE CASCADE.
- To support all the other constraints in the project, such as a customer can only review a movie once, we implemented three triggers on before insert on the tables (Customer, Review, and Endorsement) to check all this constraints before insertion. If any invalid data in the data to be inserted, the triggers will be triggered.
#### 3. Commonly used queries
To support commonly used queries to get the winner of the review and candidates to receive a free concession item, implements two stored procedures. So every time, to get these information, we can make procedure calls.
### Limitations
Currently, we do not provide middle-ware, and front-end integration in this project. Thus, we do not support the actually users to enter information througth GUI interface.
### Future plan
- To support large dataset and fast query, we can implement index in tables.
- To actually create a web-based application, we can implement the front-end to the database. Thus, it allows a movie theater to operates this promotional application. 
