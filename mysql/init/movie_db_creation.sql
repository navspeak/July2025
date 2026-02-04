DROP DATABASE IF EXISTS movie;
CREATE DATABASE movie;
USE movie;

CREATE TABLE movie_main
(mov_id INT, movie_title VARCHAR(50), duration INT,
language VARCHAR(30), country VARCHAR(50), release_date DATE, gross INT);

INSERT INTO movie_main VALUES
(210, 'The Amityville Horror',90, 'English','USA','2005-04-15', 64255243),
(211, 'The Dark Knight',152, 'English','USA','2008-07-14', 533316061),
(212, 'The Lion King',73, 'English','USA','1994-06-24', 422783777),
(213, 'The Host',110, 'Korean','South Korea','2006-07-27', 2201412),
(214, 'Fateless',134, 'Hungarian','Hungary','2005-02-08', 195888),
(215, 'Princess Mononoke',134, 'Japanese','Japan','1997-07-12', 2298191),
(216, 'Akira',124, 'Japanese','Japan','1988-07-16', 439162),
(217, 'Spirited Away',125, 'Japanese','Japan','2001-07-20', 10049886),
(218, 'Fight Club',151, 'English','USA','1999-10-15', 37023395),
(219, 'Mean Girls',97, 'English','USA','2004-04-30', 86049418),
(220, 'American Psycho',102, 'English','USA','2000-04-14', 15047419);

CREATE TABLE movie_info
(mov_id INT, movie_title TEXT, imdb REAL, genres TEXT);

INSERT INTO movie_info VALUES
(210,'The Amityville Horror', 6, 'Drama|Horror|Mystery|Thriller'),
(211,'The Dark Knight', 9, 'Action|Crime|Drama|Thriller'),
(212,'The Lion King',8.5, 'Adventure|Animation|Drama|Family|Musical'),
(213,'The Host', 7,'Action|Drama|Horror|Sci-Fi'),
(214,'Fateless',7.1,'Drama|Romance|War'),
(215,'Princess Mononoke',8.4,'Adventure|Animation|Fantasy'),
(216,'Akira',8.1,'Action|Animation|Sci-Fi'),
(217,'Spirited Away', 8.6,'Adventure|Animation|Family|Fantasy'),
(218,'Fight Club', 8.8,'Drama'),
(219,'Mean Girls',7,'Comedy'),
(220,'American Psycho',7.6,'Crime|Drama');
