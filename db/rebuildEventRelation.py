#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 更新猪事件和猪群事件之间的关联关系

import torndb


mysql_host_from = '127.0.0.1:3306'
mysql_user_from = 'root'
mysql_passwd_from = 'anywhere'

mysql_host_to = '127.0.0.1:3306'
mysql_user_to = 'root'
mysql_passwd_to = 'anywhere'

db_from = torndb.Connection(mysql_host_from, "pig_doctor1", user=mysql_user_from, password=mysql_passwd_from)
db_to = torndb.Connection(mysql_host_to, "pig_doctor2", user=mysql_user_to, password=mysql_passwd_to)


def delete_all_relations():
    db_to.execute('truncate doctor_event_relations')

def insert_pig_event_relation():
    pigEvents = db_from.query('select * from `doctor_pig_events` where status = 1 and (rel_pig_event_id is not null or rel_group_event_id is not null)')
    for pigEvent in pigEvents:
        db_to.insert(
            "insert into `doctor_event_relations` (origin_pig_event_id, origin_group_event_id, trigger_pig_event_id, trigger_group_event_id, status)"
            "values (%s, %s, %s, %s, %s)",
            pigEvent.rel_pig_event_id, pigEvent.rel_group_event_id, pigEvent.id, None, 1)
def insert_group_event_relation():
    groupEvents = db_from.query('select * from `doctor_group_events` where status = 1 and (rel_pig_event_id is not null or rel_group_event_id is not null)')
    for groupEvent in groupEvents:
        db_to.insert(
            "insert into `doctor_event_relations` (origin_pig_event_id, origin_group_event_id, trigger_pig_event_id, trigger_group_event_id, status)"
            "values (%s, %s, %s, %s, %s)",
            groupEvent.rel_pig_event_id, groupEvent.rel_group_event_id, None, groupEvent.id, 1)


if __name__ == "__main__":
    delete_all_relations()
    insert_pig_event_relation()
    insert_group_event_relation()
