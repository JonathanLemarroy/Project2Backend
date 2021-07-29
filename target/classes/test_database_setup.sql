CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS test_users(
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

CREATE TABLE IF NOT EXISTS test_exercises(  
    "exercise_name" VARCHAR(64) primary key,
    "description" VARCHAR(64) NOT NULL,
    "type" VARCHAR(64) NOT NULL,
    "video_link" VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS test_routines(  
    "routine_id" SERIAL PRIMARY KEY,
    "username" citext NOT NULL,
    "routine_name" VARCHAR(64) NOT NULL,
    "date_scheduled" INT,
    "date_completed" INT,
    FOREIGN KEY ("username") REFERENCES test_users("username") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS test_routine_exercises(
    "routine_exercise_id" SERIAL PRIMARY KEY,
    "exercise_name" VARCHAR (64),
    "routine_id" INT,
    "duration" INT,
    "reps" INT,
    "weight" INT,
    FOREIGN KEY ("exercise_name") REFERENCES test_exercises("exercise_name") ON DELETE CASCADE,
    FOREIGN KEY ("routine_id") REFERENCES test_routines("routine_id") ON DELETE CASCADE
);