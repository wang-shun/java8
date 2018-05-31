import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class demo {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        System.out.println("服务正在运行:"+jedis.ping());
        jedis.set("runoobkey","www.runoob.com");
        System.out.println("redis存储的字符串为:"+jedis.get("runoobkey"));

        jedis.lpush("site-list","Runoob");
        jedis.lpush("site-list","Google");
        jedis.lpush("site-list","Taobao");

        List<String> list = jedis.lrange("site-list",0,6);
        for(int i=0;i<list.size();i++){
            System.out.println("列表项为："+list.get(i));
        }

        Set<String> keys = jedis.keys("*");
        Iterator<String> it = keys.iterator();
        while (it.hasNext()){
            String key = it.next();
            System.out.println(key);
        }

        System.out.println(jedis.get("myKey"));
    }
}
