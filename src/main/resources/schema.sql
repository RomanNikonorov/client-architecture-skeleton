create table client_profile (
    client_id varchar(64) primary key,
    segment varchar(64) not null,
    risk_score integer not null
);
