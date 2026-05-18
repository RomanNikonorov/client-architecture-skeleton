insert into client_profile (client_id, segment, risk_score)
values ('client-001', 'premium', 17);

insert into client_profile (client_id, segment, risk_score)
values ('client-002', 'mass', 41);

insert into credit_policy (client_id, max_limit, blocked)
values ('client-001', 500000, false);

insert into credit_policy (client_id, max_limit, blocked)
values ('client-002', 120000, true);
