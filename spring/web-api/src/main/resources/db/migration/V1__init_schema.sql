create table dogs
(
    id   bigserial primary key,
    name text not null
);

insert into dogs (name)
values ('Rex'),
       ('Barky'),
       ('Max'),
       ('Lucky'),
       ('Spot'),
       ('Bella'),
       ('Buck');
