#!/usr/bin/env python
# -*- coding: utf-8 -*-
# import collections

import torndb

import json
from datetime import datetime

# import sys

__author__ = 'jlchen'

mysql_host_from = '101.201.44.35:3306'
mysql_user_from = 'root'
mysql_passwd_from = 'anywhere'

mysql_host_to = '127.0.0.1:3306'
mysql_user_to = 'root'
mysql_passwd_to = 'anywhere'

db_from = torndb.Connection(mysql_host_from, "pig_doctor", user=mysql_user_from, password=mysql_passwd_from)
db_to = torndb.Connection(mysql_host_to, "pigdoctor", user=mysql_user_to, password=mysql_passwd_to)

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


def all_pig_ids():
    ids = db_from.query('select distinct(pig_id) from doctor_pig_snapshots order by id')
    return [r.pig_id for r in ids]


def migrate_doctor_pig_snapshots(pig_ids):
    db_to.execute("truncate doctor_pig_snapshots")
    for pid in pig_ids:
        pig_snapshots = db_from.query("select * from doctor_pig_snapshots where pig_id=%s order by event_id desc", pid)
        pig = db_from.get("select * from doctor_pigs where id=%s", pid)
        for i, ps in enumerate(pig_snapshots):
            previous_index = i - 1
            next_index = i + 1
            from_event_id = 0
            to_pig_info = ""
            if previous_index >= 0:
                from_event_id = pig_snapshots[previous_index].event_id

            if next_index < len(pig_snapshots):
                to_pig_info = pig_snapshots[next_index].pig_info
            else:
                pig_track = db_from.get("select * from doctor_pig_tracks where pig_id=%s", pid)
                to_pig_info = json.dumps({"doctorPigTrack": pig_track, "pig": pig},
                                         ensure_ascii=False, default=json_serial)

            db_to.insert("INSERT INTO doctor_pig_snapshots"
                         "(id, pig_id,from_event_id,to_event_id,to_pig_info,created_at,updated_at) "
                         "VALUES (%s, %s, %s,%s, %s, %s, %s)",
                         ps.id, ps.pig_id, from_event_id, ps.event_id, to_pig_info,
                         ps.created_at, ps.updated_at)


def group_snapshot_pagination(last_id):
    return db_from.query('select * from doctor_group_snapshots where id > %s order by id asc limit 1000', last_id)


def migrate_doctor_group_snapshots():
    db_to.execute("truncate doctor_group_snapshots")
    last_id = 0
    while True:
        gss = group_snapshot_pagination(last_id)
        for gs in gss:
            db_to.insert("INSERT INTO doctor_group_snapshots"
                         " (id, group_id, from_event_id, to_event_id, to_info, created_at) "
                         " VALUES (%s, %s, %s, %s, %s, %s)",
                         gs.id, gs.to_group_id, gs.from_event_id if gs.from_event_id else 0,
                         gs.to_event_id, gs.to_info, gs.created_at)
        count = len(gss)
        if count == 0:
            return
        if count < 1000:
            return
        last_id = gss[count - 1].id


if __name__ == "__main__":
    pig_ids = all_pig_ids()
    migrate_doctor_pig_snapshots(pig_ids)
    migrate_doctor_group_snapshots()
