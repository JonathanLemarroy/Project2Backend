CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users(
    "username" citext primary key,
    "password" VARCHAR(64) NOT NULL,
    "first_name" VARCHAR(64),
    "last_name" VARCHAR(64),
    "gender" VARCHAR(64),
    "age" INT,
    "height" INT,
    "weight" INT,
    "admin" BOOLEAN
);

CREATE TABLE IF NOT EXISTS exercises(  
    "exercise_name" VARCHAR(64) primary key,
    "description" VARCHAR(64) NOT NULL,
    "type" VARCHAR(64) NOT NULL,
    "video_link" VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS routines(  
    "routine_id" SERIAL PRIMARY KEY,
    "username" citext NOT NULL,
    "routine_name" VARCHAR(64) NOT NULL,
    "date_scheduled" INT,
    "date_completed" INT,
    FOREIGN KEY ("username") REFERENCES users("username") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_exercises(
    "routine_exercise_id" SERIAL PRIMARY KEY,
    "exercise_name" VARCHAR (64),
    "routine_id" INT,
    "duration" INT,
    "reps" INT,
    "weight" INT,
    FOREIGN KEY ("exercise_name") REFERENCES exercises("exercise_name") ON DELETE CASCADE,
    FOREIGN KEY ("routine_id") REFERENCES routines("routine_id") ON DELETE CASCADE
);