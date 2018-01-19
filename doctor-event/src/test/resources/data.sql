--insert into doctor_pig_dailies(farm_id,sum_at,sow_ph_start,sow_ph_reserve_in,sow_cf_start,boar_start,sow_ph_end) values
--(1,'2017-12-01',2,4,2,2,3),(1,'2017-12-02',2,3,2,2,4),
--(2,'2017-12-01',2,4,2,2,3),(2,'2017-12-02',2,3,2,2,4),
--(3,'2017-12-01',2,4,2,1,4),
--(1,'2017-11-03',2,4,2,1,4),
--(1,'2017-11-06',2,4,2,1,4),
--(1,'2017-11-30',2,4,2,1,4);

--org 2 pig_type 4 type 1 12
-- org 2 pig_type 3 type 2 11
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (1,'2017-11-01',11,4,0,1,1);
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (1,'2017-11-01',1,4,0,1,1);
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (1,'2017-11-01',11,4,0,2,2);
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (1,'2017-11-01',1,4,1,1,4);
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (1,'2017-11-01',2,4,1,2,4);
insert into doctor_warehouse_material_apply(farm_id,apply_date,quantity,unit_price,apply_type,type,pig_barn_id)values (2,'2017-11-02',9,4,0,2,3);

insert into doctor_barns(id,pig_type)values (1,4),(2,3),(3,3),(4,7);

insert into doctor_farms(id,org_id)values (1,2),(2,4);
