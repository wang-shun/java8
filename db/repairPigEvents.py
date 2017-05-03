#!/usr/bin/env python
# -*- coding: utf-8 -*-

import torndb
import json
import datetime


mysql_host_from = '127.0.0.1'
mysql_user_from = "root"
mysql_password_from = "anywhere"

db_from = torndb.Connection(host=mysql_host_from, database="pig_doctor", user=mysql_user_from,
                            password=mysql_password_from)

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

def chg_barn_events_pagination(last_id):
    pig_events = db_from.query("select * from doctor_pig_events where type in (1, 10,12,14) and id > %s order by id asc limit 1000", last_id)
    return pig_events

def handle_chg_barn_event(pig_event):
    print pig_event.extra
    if pig_event.extra is not None:
        extra = json.loads(pig_event.extra)
        if extra.get("chgLocationToBarnId") is not None and extra.get("chgLocationToBarnName") is None:
            barn = db_from.get("select * from doctor_barns where id = %s", extra.get("chgLocationToBarnId"))
            extra.update({"chgLocationToBarnName" : barn.name})
            db_from.execute("update doctor_pig_events set extra = %s where id = %s",
                            json.dumps(extra, ensure_ascii=False, default=json_serial), pig_event.id)

def update_chg_barn():
    last_id = 0
    while True:
        pss = chg_barn_events_pagination(last_id)
        for ps in pss:
            handle_chg_barn_event(ps)
        count = len(pss)
        if count == 0:
            return
        if count < 1000:
            return
        last_id = pss[count - 1].id


###更新猪事件doctor_pig_events.barn_type
def update_barn_type():
    print 'update barn type'
    db_from.execute("update doctor_pig_events a , doctor_barns b set a.barn_type = b.pig_type where b.id = a.barn_id")

if __name__ == "__main__":
    #更新历史转舍事件转入猪舍名
    update_chg_barn()
    #更新猪事件barnType
    update_barn_type()

