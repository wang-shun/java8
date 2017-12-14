insert into doctor_warehouse_material_handle(id,type,quantity,warehouse_id,material_id)values (1,1,10,1,1);

insert into doctor_warehouse_handle_detail(id,material_handle_id,material_purchase_id,quantity)values (1,1,1,10);

insert into doctor_warehouse_purchase(id,quantity,handle_quantity)values (1,10,0);

insert into doctor_warehouse_stock(id,quantity,warehouse_id,sku_id)values (15,15,1,1);