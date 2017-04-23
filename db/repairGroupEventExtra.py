#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 把doctor_group_event.extra刷成input

import torndb

import json
from datetime import datetime

import sys
reload(sys)
sys.setdefaultencoding('utf8')

__author__ = 'luoys'

mysql_host_from = '127.0.0.1:3306'
mysql_user_from = 'root'
mysql_passwd_from = 'anywhere'

mysql_host_to = '127.0.0.1:3306'
mysql_user_to = 'root'
mysql_passwd_to = 'anywhere'

db_from = torndb.Connection(mysql_host_from, "pig_doctor", user=mysql_user_from, password=mysql_passwd_from)
db_to = torndb.Connection(mysql_host_to, "pig_doctor2", user=mysql_user_to, password=mysql_passwd_to)

# epoch = datetime.utcfromtimestamp(0)
#
#
# def unix_time_millis(dt):
#     return (dt - epoch).total_seconds() * 1000.0



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


def group_events_pagination(last_id):
    return db_from.query('select * from doctor_group_events where id > %s  order by id asc limit 1000', last_id)

def get_default(obj):
    return obj if obj is not None else ""

def handle_doctor_group_event(group_event):
    if group_event.extra is not None:
        # print group_event.extra
        extra = json.loads(group_event.extra)
    extra.update({"eventAt": group_event.event_at.strftime("%Y-%m-%d")})
    extra.update({"isAuto": group_event.is_auto})
    extra.update({"eventType": group_event.type})
    extra.update({"remark": get_default(group_event.remark)})
    extra.update({"creatorId": get_default(group_event.creator_id)})
    extra.update({"creatorName": get_default(group_event.creator_name)})
    extra.update({"relGroupEventId": get_default(group_event.rel_group_event_id)})
    extra.update({"relPigEventId": get_default(group_event.rel_pig_event_id)})
    if group_event.sow_id is not None:
        extra.update({"sowId": group_event.sow_id})
    if group_event.sow_code is not None:
        extra.update({"sowCode": group_event.sow_code})
    sowEvent = False
    if group_event.rel_pig_event_id is not None:
        sowEvent = True
    extra.update({"sowEvent": sowEvent})
    inTypeName = ["","仔猪转入","种猪转商品猪","群间转移", "购买"]
    if group_event.type is 1:
        extra.update({"farmId": group_event.farm_id})
        extra.update({"groupCode": group_event.group_code})
        extra.update({"barnId": group_event.barn_id})
        extra.update({"barnName": group_event.barn_name})
        extra.update({"pigType": group_event.pig_type})

    if group_event.type is 2:
        extra.update({"inType": group_event.in_type})
        if group_event.in_type is not None:
            extra.update({"inTypeName": inTypeName[group_event.in_type]})
        extra.update({"quantity": group_event.quantity})
        extra.update({"avgDayAge": group_event.avg_day_age})
        extra.update({"avgWeight": group_event.avg_weight})
        extra.update({"weight": group_event.weight})
    if group_event.type is 3:
        extra.update({"quantity": group_event.quantity})
        extra.update({"weight": group_event.weight})
    if group_event.type is 4:
        extra.update({"avgWeight": group_event.avg_weight})
        extra.update({"weight": group_event.weight})
    if group_event.type is 6:
        extra.update({"avgWeight": group_event.avg_weight})
    if group_event.type is 9:
        extra.update({"weight": group_event.weight})
    # print extra
    group_event.extra = json.dumps(extra, ensure_ascii=False, default=json_serial)
    # print group_event.extra
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



def repair_doctor_group_events():
    db_to.execute("truncate doctor_group_events")
    last_id = 0
    while True:
        gss = group_events_pagination(last_id)
        for gs in gss:
            handle_doctor_group_event(gs)
        count = len(gss)
        if count == 0:
            return
        if count < 1000:
            return
        last_id = gss[count - 1].id

if __name__ == "__main__":
    repair_doctor_group_events()
