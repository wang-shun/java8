#新融农牧CRM接口


####环境
- 测试 [http://doctor-test.xrnm.com](http://doctor-test.xrnm.com)
- 生产 [https://doctor.xrnm.com](https://doctor.xrnm.com)

###编码
若无特殊说明，编码请统一使用 UTF-8

###授权
目前给crm分配的`appKey`= pigDoctorCRM，`appSecret` = 7dI6Pp18SPQySQUz

#### 接口通用参数

接口通用参数是指在调用开放平台时必须传入的参数， 如 `pamapsCall` 、`sign` 等， 除特殊指定接口外任何都会获得形如 `xxxx.miss` 的异常响应

+ pampasCall
   调用公共的方法名
+ appKey
   用户调用开放平台接口时所需的身份验证信息
+ appSecret
    用户调用平台开放接口时需要参与签名运算的密钥
+ timestamp
    (格式为: yyyyMMddHHmmss) 在调用电商平台接口时会额外需要传入App的时间， 当超出调用的时间窗口时候会获得 `invoke.expired` 的错误响应。 除获取用户时间接口

#### 签名方式

所有调用参数(包括pampasCall, 但不包括sign本身), 按照字母序升序排列, 然后调用者再附加上给分配给自己的appSecret, 再做md5签名

示例:
> /api/gateway?appKey=pigDoctorCRM&pampasCall=get.daily.report&date=2016-09-01

1.首先按参数首字母升序排列，得到
> appKey=pigDoctorCRM&date=2016-09-01&pampasCall=get.daily.report

2.再将分配的appSecret附加到参数尾，得到
> appKey=pigDoctorCRM&date=2016-09-01&pampasCall=get.daily.report7dI6Pp18SPQySQUz

3.再计算这段字符串的md5,得到校验码为
> 0c9a4cde47d642b443c285406e2ad311

4.将计算出的校验码添加至请求尾，得到
> appKey=pigDoctorCRM&date=2016-09-01&pampasCall=get.daily.report&sign=0c9a4cde47d642b443c285406e2ad311

5.访问api获取结果
> [http://doctor-test.xrnm.com/api/gateway?appKey=pigDoctorCRM&date=2016-09-01&pampasCall=get.daily.report&sign=0c9a4cde47d642b443c285406e2ad311](http://doctor-test.xrnm.com/api/gateway?appKey=pigDoctorCRM&date=2016-09-01&pampasCall=get.daily.report&sign=0c9a4cde47d642b443c285406e2ad311)


#### 返回结果

返回结果是一个json

调用成功

```
{
  "success": true,      
  "result": "[{\"farmName\":\"融利实业种猪场\",\"sumAt\":1472659200000,\"wean\":{\"count\":305,\"weight\":5.0},\"deliver\":{\"nest\":4,\"live\":49,\"health\":45,\"weak\":4,\"black\":2},\"checkPreg\":{\"positive\":78,\"negative\":0,\"fanqing\":1,\"liuchan\":0},\"mating\":{\"houbei\":0,\"duannai\":0,\"fanqing\":0,\"liuchan\":0},\"liveStock\":{\"group\":0,\"houbeiSow\":521,\"peihuaiSow\":956,\"buruSow\":84,\"konghuaiSow\":0,\"houbeiBoar\":0,\"boar\":16,\"farrow\":1864,\"nursery\":2850,\"fatten\":11212,\"fattenOut\":3113},\"dead\":{\"boar\":0,\"sow\":0,\"farrow\":7,\"nursery\":2,\"fatten\":0},\"sale\":{\"boar\":0.0,\"sow\":0.0,\"nursery\":0.0,\"fatten\":0.0,\"amount\":0.0},\"purchaseAmout\":0.0}]"
}
```
调用失败

```
{
  "success": false,
  "error": "sign.mismatch",
  "errorMessage": "签名错误"
}
```

+ `success`: 调用是否成功，true代表成功
+ `result`: 返回结果，是一个json格式的字符串
+ `error `: 错误代码
+ `errorMessage`: 具体错误信息

通用错误清单    

+ appKey.miss(400): AppKey不能为空
+ appKey.incorrect(400): 非法的AppKey
+ sign.mismatch:(400): 签名未通过
+ timestamp.miss(400): 时间戳不能为空
+ timestamp.incorrect(400): 错误的时间戳格式
+ clientInfo.miss(400): ClientId信息不能为空
+ permission.deny(401): 未获取授权
+ invoke.expired(403): 调用已逾期
+ method.target.not.found (404):  未找到指定方法
+ method.not.allowed (405)：错误的Http请求方法
+ server.internal.error(500):  服务异常

####可调用服务

无特殊说明，请求默认都是GET

1.猪群存栏明细

+ `pampasCall`: get.group.live.stock.detail
+ `date`: 查询日期，格式2016-01-01(yyyy-MM-dd)

2.生产日报表

+ `pampasCall`: get.daily.report
+ `date`: 查询日期，格式2016-01-01(yyyy-MM-dd)

3.生产月报表和指标数据

+ `pampasCall`: get.monthly.report
+ `date`: 查询日期，格式2016-01-01(yyyy-MM-dd)

4.供应商电商产品销售

+ `pampasCall`: get.shop.item.sale
+ `timestamp`: 格式为: 20160320133000(yyyyMMddHHmmss)





