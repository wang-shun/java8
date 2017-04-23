#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 重新生成 猪群的关闭事件， 防止推演猪群track错误

import torndb

from datetime import datetime

mysql_host_from = '127.0.0.1:3306'
mysql_user_from = 'root'
mysql_passwd_from = 'anywhere'

mysql_host_to = '127.0.0.1:3306'
mysql_user_to = 'root'
mysql_passwd_to = 'anywhere'

db_from = torndb.Connection(mysql_host_from, "pig_doctor1", user=mysql_user_from, password=mysql_passwd_from)
db_to = torndb.Connection(mysql_host_to, "pig_doctor2", user=mysql_user_to, password=mysql_passwd_to)


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


def all_group_ids():
    ids = db_from.query('select distinct group_id from doctor_group_events where type = 10')
    return [r.group_id for r in ids]

def create_group_close(group_ids):
    for group_id in group_ids:
        group_event = db_from.get('select * from doctor_group_events where type = 10 and group_id = %s limit 1', group_id)
        db_from.execute("update doctor_group_events set status = -1 where type = 10 and group_id = %s", group_id)
        print group_event
        db_from.insert("INSERT INTO doctor_group_events"
                     "(org_id,org_name,farm_id,farm_name,group_id,group_code,event_at,`type`,`name`,`desc`,barn_id,barn_name,pig_type,quantity,weight,avg_weight,base_weight,avg_day_age,is_auto,change_type_id,price,amount,over_price,trans_group_type,in_type,other_barn_id,other_barn_type,rel_group_event_id,rel_pig_event_id,out_id,status,event_source,remark,extra,created_at,creator_id,creator_name,updated_at,updator_id,updator_name) "
                     "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                     group_event.org_id, group_event.org_name, group_event.farm_id, group_event.farm_name,
                     group_event.group_id, group_event.group_code, group_event.event_at, group_event.type, group_event.name,
                     group_event.desc, group_event.barn_id, group_event.barn_name, group_event.pig_type,
                     group_event.quantity, group_event.weight, group_event.avg_weight, group_event.base_weight,
                     group_event.avg_day_age, group_event.is_auto, group_event.change_type_id, group_event.price,
                     group_event.amount, group_event.over_price, group_event.trans_group_type, group_event.in_type,
                     group_event.other_barn_id, group_event.other_barn_type, group_event.rel_group_event_id,
                     group_event.rel_pig_event_id, group_event.out_id, group_event.status, 4, group_event.remark,
                     group_event.extra, group_event.created_at, group_event.creator_id, group_event.creator_name,
                     group_event.updated_at, group_event.updator_id, group_event.updator_name)


if __name__ == "__main__":
    group_ids = all_group_ids()
    print group_ids
    create_group_close(group_ids)

