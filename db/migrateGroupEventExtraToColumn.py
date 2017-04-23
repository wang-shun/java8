#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 把doctor_group_events.extra字段中的部分值放到doctor_group_events新增的字段中

import torndb
import json
from datetime import datetime

mysql_host_from = "127.0.0.1"
mysql_user_from = "root"
mysql_password_from = "anywhere"

mysql_host_to = "127.0.0.1"
mysql_user_to = "root"
mysql_password_to = "anywhere"

db_from = torndb.Connection(host=mysql_host_from, database="pig_doctor1", user=mysql_user_from,
                            password=mysql_password_from)
db_to = torndb.Connection(host=mysql_host_to, database="pig_doctor2", user=mysql_user_to,
                          password=mysql_password_to)


def json_serial(obj):
    """JSON serializer for objects not serializable by default json code"""

    if isinstance(obj, datetime):
        serial = obj.isoformat()
        return serial
        # return unix_time_millis(obj)
    raise TypeError("Type not serializable")


def memoize(function):
    memo = {}

    def wrapper(*args):
        if args in memo:
            return memo[args]
        else:
            rv = function(*args)
            memo[args] = rv
            return rv

    return wrapper

def group_event_pagination(last_id):
    return db_from.query('select * from doctor_group_events where id > %s order by id asc limit 1000',
                         last_id)


def insert_new_group_event(group_event):
    group_event.operator_id = group_event.creator_id
    group_event.operator_name = group_event.creator_name
    if group_event.extra is not None:
        extra = json.loads(group_event.extra)
        if group_event.rel_pig_event_id is not None:
            pig_event = db_from.get("select * from doctor_pig_events where id = %s", group_event.rel_pig_event_id)
            if pig_event is not None:
                group_event.sow_id = pig_event.pig_id
                group_event.sow_code = pig_event.pig_code
        group_event.customer_id = extra.get("customerId")
        group_event.customer_name = extra.get("customerName")
        if group_event == 7:
            group_event.basic_id = extra.get("diseaseId")
            group_event.basic_name = extra.get("diseaseName")
            group_event.operator_id = extra.get("doctorId") if extra.has_key("doctorId") else group_event.creator_id
            group_event.operator_name = extra.get("doctorName") if extra.has_key(
                "doctorName") else group_event.creator_name
        if group_event == 8:
            group_event.basic_id = extra.get("vaccinItemId")
            group_event.basic_name = extra.get("vaccinItemName")
            group_event.operator_id = extra.get("vaccinStaffId") if extra.has_key(
                "vaccinStaffId") else group_event.creator_id
            group_event.operator_name = extra.get("vaccinStaffName") if extra.has_key(
                "vaccinStaffName") else group_event.creator_name
            group_event.vaccination_id = extra.get("vaccinId")
            group_event.vaccination_name = extra.get("vaccinName")
    # print group_event
    db_to.insert(
        "insert into doctor_group_events (id, org_id, org_name, farm_id, farm_name, group_id, group_code, event_at, `type`, `name`, `desc`, barn_id, barn_name, sow_id, sow_code, pig_type, quantity, weight, avg_weight, base_weight, avg_day_age, is_auto, change_type_id, price, amount, over_price, customer_id, customer_name, basic_id, basic_name, vaccin_result, vaccination_id, vaccination_name, trans_group_type, in_type, other_barn_id, other_barn_type, rel_group_event_id, rel_pig_event_id, out_id, status, event_source, remark, extra, created_at, creator_id, creator_name, operator_id, operator_name, updated_at, updator_id, updator_name) values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        group_event.id, group_event.org_id, group_event.org_name, group_event.farm_id, group_event.farm_name,
        group_event.group_id, group_event.group_code, group_event.event_at, group_event.type, group_event.name,
        group_event.desc, group_event.barn_id, group_event.barn_name, group_event.sow_id, group_event.sow_code,
        group_event.pig_type, group_event.quantity,
        group_event.weight, group_event.avg_weight, group_event.base_weight, group_event.avg_day_age,
        group_event.is_auto, group_event.change_type_id, group_event.price, group_event.amount,
        group_event.over_price,
        group_event.customer_id, group_event.customer_name, group_event.basic_id, group_event.basic_name,
        group_event.vaccin_result, group_event.vaccination_id, group_event.vaccination_name,
        group_event.trans_group_type, group_event.in_type,
        group_event.other_barn_id, group_event.other_barn_type, group_event.rel_group_event_id,
        group_event.rel_pig_event_id, group_event.out_id, group_event.status, group_event.event_source,
        group_event.remark, group_event.extra, group_event.created_at, group_event.creator_id,
        group_event.creator_name, group_event.operator_id, group_event.operator_name,
        group_event.updated_at, group_event.updator_id, group_event.updator_name)


def flat_group_event():
    db_to.execute("truncate doctor_group_events")
    last_id = 0
    counts = 0
    while True:
        group_events = group_event_pagination(last_id)
        for group_event in group_events:
            insert_new_group_event(group_event)
        count = len(group_events)
        counts += count
        if count == 0:
            return counts
        if count < 1000:
            return counts
        last_id = group_events[count - 1].id

if __name__ == "__main__":
    print "flat event extra start, now:", datetime.now()
    group_counts = flat_group_event()
    print 'flush group events counts', group_counts
    print "flat event extra end, now:", datetime.now()