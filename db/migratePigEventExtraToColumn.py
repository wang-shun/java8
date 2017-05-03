#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 把doctor_pig_events.extra字段中的部分值放到把doctor_pig_events新增的字段中

import torndb
import json
from datetime import datetime

mysql_host_from = "127.0.0.1"
mysql_user_from = "root"
mysql_password_from = "anywhere"

mysql_host_to = "127.0.0.1"
mysql_user_to = "root"
mysql_password_to = "anywhere"

db_from = torndb.Connection(host=mysql_host_from, database="doctor_modify", user=mysql_user_from,
                            password=mysql_password_from)
db_to = torndb.Connection(host=mysql_host_to, database="doctor_modify2", user=mysql_user_to,
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


def pig_event_pagination(last_id):
    return db_from.query('select * from doctor_pig_events where id > %s order by id asc limit 1000',
                         last_id)


def insert_new_pig_event(pig_event):
    if pig_event.extra is not None:
        extra = json.loads(pig_event.extra)
        if pig_event.type == 7:  # 进场
            pig = db_from.get("select * from doctor_pigs where id = %s", pig_event.pig_id)
            pig_event.breed_id = pig.breed_id
            pig_event.breed_name = pig.breed_name
            pig_event.breed_type_id = pig.genetic_id
            pig_event.breed_type_name = pig.genetic_name
            pig_event.source = extra.get("source")
            pig_event.boar_type = pig.boar_type
        if pig_event.type == 9:  # 配种
            pig_event.judge_preg_date = datetime.fromtimestamp(extra.get("judgePregDate") / 1000).strftime(
                "%Y-%m-%d") if extra.has_key("judgePregDate") else None
            pig_event.mating_type = extra.get("matingType")
        if pig_event.type == 11:  # 妊娠检查
            pig_event.basic_id = extra.get("abortionReasonId")
            pig_event.basic_name = extra.get("abortionReasonName")
        if pig_event.type == 17:  # 拼窝
            pig_event.quantity = extra.get("fostersCount")
        if pig_event.type == 19:  # 被拼窝
            pig_event.quantity = extra.get("fostersCount")
        if pig_event.type == 18:  # 仔猪变动
            pig_event.quantity = extra.get("pigletsCount")
            pig_event.weight = extra.get("pigletsWeight")
            pig_event.price = extra.get("pigletsPrice")
            pig_event.customer_id = extra.get("pigletsCustomerId")
            pig_event.customer_name = extra.get("pigletsCustomerName")
            if pig_event.change_type_id is None and extra.get("pigletsChangeType") is not None:
                pig_event.change_type_id = extra.get("pigletsChangeType")
        if pig_event.type == 4:
            pig_event.basic_id = extra.get("diseaseId")
            pig_event.basic_name = extra.get("diseaseName")
        if pig_event.type == 5:
            pig_event.basic_id = extra.get("vaccinationItemId")
            pig_event.basic_name = extra.get("vaccinationItemName")
            pig_event.vaccination_id = extra.get("vaccinationId")
            pig_event.vaccination_name = extra.get("vaccinationName")
    # print pig_event
    db_to.insert(
        "insert into doctor_pig_events (id, org_id, org_name, farm_id, farm_name, pig_id, pig_code, is_auto, event_at, `type`, kind, `name`, `desc`, barn_id, barn_name, barn_type, rel_event_id, rel_group_event_id, rel_pig_event_id, change_type_id, basic_id, basic_name, customer_id, customer_name, vaccination_id, vaccination_name, price, amount, quantity, weight, pig_status_before, pig_status_after, parity, is_impregnation, is_delivery, preg_days, feed_days, preg_check_result, dp_npd, pf_npd, pl_npd, ps_npd, py_npd, pt_npd, jp_npd, npd, group_id, farrow_weight, live_count, health_count, weak_count, mny_count, jx_count, dead_count, black_count, wean_count, wean_avg_weight, current_mating_count, check_date, matting_date, farrowing_date, abortion_date, partwean_date, judge_preg_date, doctor_mate_type, mate_type, boar_code, out_id, status, event_source, extra, source, breed_id, breed_name, breed_type_id, breed_type_name, boar_type, remark, operator_id, operator_name, creator_id, creator_name, updator_id, updator_name, created_at, updated_at) values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        pig_event.id, pig_event.org_id, pig_event.org_name, pig_event.farm_id, pig_event.farm_name, pig_event.pig_id,
        pig_event.pig_code, pig_event.is_auto, pig_event.event_at, pig_event.type, pig_event.kind, pig_event.name,
        pig_event.desc,
        pig_event.barn_id, pig_event.barn_name, pig_event.barn_type, pig_event.rel_event_id, pig_event.rel_group_event_id,
        pig_event.rel_pig_event_id, pig_event.change_type_id, pig_event.basic_id,
        pig_event.basic_name, pig_event.customer_id, pig_event.customer_name, pig_event.vaccination_id,
        pig_event.vaccination_name, pig_event.price, pig_event.amount, pig_event.quantity,
        pig_event.weight, pig_event.pig_status_before, pig_event.pig_status_after, pig_event.parity,
        pig_event.is_impregnation, pig_event.is_delivery, pig_event.preg_days,
        pig_event.feed_days, pig_event.preg_check_result, pig_event.dp_npd, pig_event.pf_npd, pig_event.pl_npd,
        pig_event.ps_npd, pig_event.py_npd, pig_event.pt_npd, pig_event.jp_npd, pig_event.npd, pig_event.group_id,
        pig_event.farrow_weight, pig_event.live_count, pig_event.health_count, pig_event.weak_count,
        pig_event.mny_count, pig_event.jx_count, pig_event.dead_count, pig_event.black_count,
        pig_event.wean_count, pig_event.wean_avg_weight, pig_event.current_mating_count, pig_event.check_date,
        pig_event.matting_date, pig_event.farrowing_date,
        pig_event.abortion_date, pig_event.partwean_date, pig_event.judge_preg_date, pig_event.doctor_mate_type,
        pig_event.mate_type, pig_event.boar_code, pig_event.out_id, pig_event.status,
        pig_event.event_source, pig_event.extra, pig_event.source, pig_event.breed_id, pig_event.breed_name,
        pig_event.breed_type_id, pig_event.breed_type_name, pig_event.boar_type, pig_event.remark,
        pig_event.operator_id, pig_event.operator_name, pig_event.creator_id, pig_event.creator_name,
        pig_event.updator_id, pig_event.updator_name, pig_event.created_at, pig_event.updated_at)

def flat_pig_event():
    db_to.execute("truncate doctor_pig_events")
    last_id = 0
    counts = 0
    while True:
        pig_events = pig_event_pagination(last_id)
        for pig_event in pig_events:
            insert_new_pig_event(pig_event)
        count = len(pig_events)
        counts += count
        if count == 0:
            return counts
        if count < 1000:
            return counts
        last_id = pig_events[count - 1].id

if __name__ == "__main__":
    print "flat event extra start, now:", datetime.now()
    pig_counts = flat_pig_event()
    print 'flush pig events counts', pig_counts
    print "flat event extra end, now:", datetime.now()