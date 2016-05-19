
# 工作流使用说明文档

[TOC]

# 1. 整体架构图
![工作流架构图](http://7xsz2j.com1.z0.glb.clouddn.com/workflow%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

##1.1 流程引擎说明
* `流程引擎`（Workflow Engine）可以实现对`流程定义图`的解析定义、流程的启动、流程的运转等功能，这些操作都是对业务Service所**屏蔽**的，业务Service并不需要关心流程引擎的内部实现。
* 流程引擎向业务Service提供的服务有
	* Workflow Service
	* 事件接口（IHandler） 
	* 节点执行拦截器接口（Interceptor）
	
##1.2 Workflow Service
流程服务对应的是`WorkFlowService`类， 实质是对流程引擎类做了一层封装。
WorkFlowService目前提供三个服务功能：`流程定义相关的Service`，`流程流转相关的Service`，`查询相关的Service`
具体相关的类，如下图：
![WorkFlowService类图](http://7xsz2j.com1.z0.glb.clouddn.com/WorkFlowService%E7%B1%BB%E5%9B%BE.png)
> 注意：可以看到 `查询` 相关的接口比较多，其实每个接口对应一个 `实体类`，每个实体类其实也就是对应数据库的一张表。关于数据表，后面会用一个简单的例子说明。

#2. 简单流程
##2.1 流程定义图
流程定义图一般是以`xml文档`的形式体现，当然后期也会实施图形化界面的配置方案。文档中的内容，最终都会被解析保存到数据库中。（文档目前每个字符只支持小写）
**1、每个流程定义文档有且只能存在一个根节点 `workflow`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 流程开始 -->
<workflow key="simpleFlow" name="简单流程测试">

</workflow>
```
	key属性表示每个流程图的唯一标识，后面流程的一系列操作都要依赖于这个key值。

**2、每个流程文档必须存在 `开始节点` 和 `结束节点` ，且有且只能存在一个。**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 流程开始 -->
<workflow key="simpleFlow" name="简单流程测试">
    <!-- 开始节点 -->
    <start name="开始节点" pointx="50" pointy="100">

    </start>

    <!-- 结束节点 -->
    <end name="结束节点" pointx="650" pointy="100" />
</workflow>
```
	(1) name属性：每个节点的唯一名称，每个文档中的节点name属性值只能存在一次。
	(2) pointx和pointy属性：每个节点相对于x轴和y轴的偏移量，这个为了方便后期绘图，暂时保留。

**3、`事件连线` 和 `任务节点` 的定义**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 流程开始 -->
<workflow key="simpleFlow" name="简单流程测试">
    <!-- 开始节点 -->
    <start name="开始节点" pointx="50" pointy="100">
        <transition name="开始连线" target="任务节点1" />
    </start>

    <!-- 任务节点1 -->
    <task name="任务节点1" assignee="terminus1" pointx="250" pointy="100">
        <transition name="任务连线1" target="任务节点2" />
    </task>

    <!-- 任务节点2 -->
    <task name="任务节点2" assignee="terminus2" pointx="450" pointy="100">
        <transition name="任务连线2" target="结束节点" />
    </task>

    <!-- 结束节点 -->
    <end name="结束节点" pointx="650" pointy="100" />
</workflow>
```
	(1) 每个事件连线节点名称为 "transition"。target属性表示指向下一个节点的name属性。
	(2) 每个任务节点的名称为 "task"。assignee属性表示当前节点的处理人(标识)，在fork/join情况下会使用到，暂时猪场项目使用不到。

**4、上面xml文档图形化体现**
![简单流程定义图](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E5%AE%9A%E4%B9%89%E5%9B%BE.png)

## 2.2 流程部署
流程部署操作需要通过 `FlowDefinitionService` 来完成，具体方法如下：

```java
// 1. 通过classpath文件路径部署
void deploy(String sourceName);
// 2. 通过输入流部署
void deploy(InputStream inputStream);
// 3. 部署的时候指定部署人的id和name
void deploy(String sourceName, Long operatorId, String operatorName);
void deploy(InputStream inputStream, Long operatorId, String operatorName);
```
部署操作：
1、 Spring环境下注入WorkFlowService对象

```java
@Autowired
protected WorkFlowService workFlowService;
```

2、 调用方法

```java
// 部署流程, 多次部署会根据当前key的流程定义，的版本号自增进行区分。
workFlowService.getFlowDefinitionService().deploy("simple/simple_two_task.xml");
```

## 2.3 完成部署表结构说明
当部署成功之后，数据库中会有 `三张` 流程定义相关的表数据发生变化：
`workflow_definitions`，`workflow_definition_nodes`，`workflow_definition_node_events`

**1、workflow_definitions 表定义**

```sql
DROP TABLE IF EXISTS `workflow_definitions`;
CREATE TABLE IF NOT EXISTS `workflow_definitions` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识, 按照版本号区分',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程定义name属性名称',
	`version` BIGINT(20) DEFAULT NULL COMMENT '版本号，用于获取最新的流程定义',
	`resource_name` VARCHAR(128) DEFAULT NULL COMMENT '资源文件的名称(通常指流程定义的xml文件)',
	`resource` text COMMENT '流程定义的资源内容(通常是xml中的内容)',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程定义的状态(1:正常, -1:删除, -2:禁用)',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '发布者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '发布者姓名',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程定义表';
CREATE INDEX idx_flow_definition_key ON workflow_definitions(`key`);
```
完成刚刚部署操作之后，在该表生成的记录如下。
![简单流程部署定义表](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E9%83%A8%E7%BD%B2%E5%AE%9A%E4%B9%89%E8%A1%A8.png)
如果多次部署（提升流程定义的版本号），生成记录如下：
![简单流程部署定义表2](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E9%83%A8%E7%BD%B2%E5%AE%9A%E4%B9%89%E8%A1%A82.png)

**2、workflow_definition_nodes 表定义 (该表用来存放每个节点)**

```sql
DROP TABLE IF EXISTS `workflow_definition_nodes`;
CREATE TABLE IF NOT EXISTS `workflow_definition_nodes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`name` VARCHAR(32) DEFAULT NULL COMMENT '节点name属性名称',
	`node_name` VARCHAR(32) DEFAULT NULL COMMENT '节点标签名称',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '节点类型, 1->开始节点, 2->任务节点, 3->选择节点, 4->并行节点, 5->并行汇聚节点, 10->子流程开始节点, -10->子流程结束节点, -1->结束节点',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`point_x` DOUBLE DEFAULT NULL COMMENT '节点x轴偏移量',
	`point_y` DOUBLE DEFAULT NULL COMMENT '节点y轴偏移量',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程定义节点表';
CREATE INDEX idx_flow_definition_nodes_def_id ON workflow_definition_nodes(flow_definition_id);
```
上面每个字段都和xml文档中定义相一致，xml文档在该表的体现
![简单流程定义节点表](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E5%AE%9A%E4%B9%89%E8%8A%82%E7%82%B9%E8%A1%A8.png)

**3、workflow_definition_node_events 表定义（保存事件连线）**

```sql
DROP TABLE IF EXISTS `workflow_definition_node_events`;
CREATE TABLE IF NOT EXISTS `workflow_definition_node_events`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '连线name属性名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id,冗余',
	`source_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程源节点的id',
	`handler` VARCHAR(128) DEFAULT NULL COMMENT '事件驱动处理类(一般为类标识)',
	`expression` text COMMENT '事件驱动表达式',
	`target_node_id` BIGINT(20) DEFAULT NULL COMMENT '驱动的目标节点id',
	`describe` text COMMENT '连线描述',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程定义节点连线事件表';
CREATE INDEX idx_flow_node_event_def_id ON workflow_definition_node_events(flow_definition_id);
CREATE INDEX idx_flow_node_event_src_id ON workflow_definition_node_events(source_node_id);
CREATE INDEX idx_flow_node_event_handler ON workflow_definition_node_events(handler);
```
	(1) handler 表示执行的事件，下面举例
	(2) expression 驱动表达式，这里主要用在选择节点（decision）上，下面举例
	
简单流程在该表中的体现：
![简单流程定义连线表](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E5%AE%9A%E4%B9%89%E8%BF%9E%E7%BA%BF%E8%A1%A8.png)

## 2.4 启动流程实例
每一个流程实例需要和一个业务id进行相关联，每个业务id只能存在一种类型(key)的流程定义实例。且每次根据 `最新版本` 的流程定义进行启动, 多次启动会抛出异常。

流程实例启动作需要通过 `FlowProcessService` 来完成, 启动流程实例需要执行 `流程定义的key` 和 `业务id` ，方法列表如下：

```java
// 1. 启动流程实例
void startFlowInstance(String flowDefinitionKey, Long businessId);
// 2. 指定全局业务数据
void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData);
// 3. 指定流转数据
void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData);
// 4. 指定执行表达式
void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression);
// 5. 指定操作人id和name
void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression, Long operatorId, String operatorName);
```
	(1) businessData 全局业务数据：执行事件的时候可访问，只读操作
	(2) flowData 流转数据：每个事件产生的流转数据，执行事件的时候可访问，读写操作
	(3) expression 执行表达式：防止start下个节点就是分支节点（decision节点）

启动流程实例会有 `四个表` 发生变化

**1、workflow_process_instances 表会生成对应的实例记录，表定义**

```sql
DROP TABLE IF EXISTS `workflow_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例表';
CREATE INDEX idx_flow_instance_def_id ON workflow_process_instances(flow_definition_id);
CREATE INDEX idx_flow_instance_def_key ON workflow_process_instances(flow_definition_key);
CREATE INDEX idx_flow_instance_busi_id ON workflow_process_instances(business_id);
```
启动简单流程对该表的体现：
![简单流程实例](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E5%AE%9A%E4%B9%89%E5%AE%9E%E4%BE%8B.png)

**2、workflow_processes 表会记录当前活动的节点**

```sql
DROP TABLE IF EXISTS `workflow_processes`;
CREATE TABLE IF NOT EXISTS `workflow_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留),该值优先于流程定义节点的值',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例的当前活动节点表';
CREATE INDEX idx_flow_process_ins_id ON workflow_processes(flow_instance_id);
```
启动简单流程实例在该表的体现：
![简单流程活动节点1](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E6%B4%BB%E5%8A%A8%E8%8A%82%E7%82%B91.png)

	(1) 由于start节点是流转节点，所以start节点直接执行了。
	(2) 可以看出当前节点为 "任务节点1"

**3、workflow_process_tracks 表会记录节点流转的跟踪，表定义如下：**

```sql
DROP TABLE IF EXISTS `workflow_process_tracks`;
CREATE TABLE IF NOT EXISTS `workflow_process_tracks`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程处理节点跟踪表';
CREATE INDEX idx_flow_process_track_ins_id ON workflow_process_tracks(flow_instance_id);
```
启动简单流程实例在该表的体现：
![简单流程节点跟踪1](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E8%B7%9F%E8%B8%AA1.png)

	(1) 可以看出来当前节点是已经执行完毕的start节点
	(2) 跟踪表的存在是为了能够做到回滚的操作。在不回滚的情况下，跟踪表的内容基本与活动节点历史表一致，历史节点表如下节。在流程实例结束的时候，"track表记录会被清除(暂时不清除)"，历史表保存。
	
**4、workflow_history_processes 活动节点历史表**

```sql
DROP TABLE IF EXISTS `workflow_history_processes`;
CREATE TABLE IF NOT EXISTS `workflow_history_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`describe` text COMMENT '历史节点流转描述',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例的活动节点历史表';
CREATE INDEX idx_flow_process_his_ins_id ON workflow_history_processes(flow_instance_id);
```
启动流程实例在该表的体现：
![简单流程节点历史1](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E5%8E%86%E5%8F%B21.png)

## 2.5 执行任务节点
**1、 通过 `FlowProcessService` 获取 `Executor` 对象，API如下**

```java
// 1. 获取任务执行器
Executor getExecutor(String flowDefinitionKey, Long businessId);
// 2. 指定处理人（标识），在 fork/join 需要指定
Executor getExecutor(String flowDefinitionKey, Long businessId, String assignee);
```

**2、执行任务**

```java
// 1. 执行
void execute();
// 2. 如果存在分支decision节点，需要指定表达式
void execute(Map expression);
// 3. 指定流转数据
void execute(String flowData);
void execute(Map expression, String flowData);
// 4. 指定操作人id和name
void execute(Long operatorId, String operatorName);
void execute(Map expression, Long operatorId, String operatorName);
void execute(Map expression, String flowData, Long operatorId, String operatorName);
```

**3、 上述简单流程，执行 `任务节点1` 代码**

```java
// 执行 "任务节点1"
workFlowService.getFlowProcessService()
			.getExecutor(flowDefinitionKey, businessId)
			.execute();
```
此时各表的数据变化如下：
(1) 流程实例表不变

(2) 活动节点表
![简单流程活动节点2](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E6%B4%BB%E5%8A%A8%E8%8A%82%E7%82%B92.png)

	可以看出当前活动的节点是 "任务节点2"

(3) 节点跟踪表
![简单流程节点跟踪2](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E8%B7%9F%E8%B8%AA2%20.png)

	可以看出 "任务节点1" 成功执行完毕

(4) 历史节点表
![简单流程节点历史2](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E5%8E%86%E5%8F%B22.png)

**5、再次执行上述代码，执行 `任务节点2`**

此时，执行完任务节点2，流程应该处于结束的状态。这个时候，就会在 `流程实例历史表` 中生成记录, 下面是各表记录的变化：

(1) 流程实例表中的记录清除

(2) 无活动节点

(3) 节点跟踪记录
![简单流程节点跟踪3](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E8%B7%9F%E8%B8%AA3.png)

(4) 节点历史表
![简单流程节点历史3](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E8%8A%82%E7%82%B9%E5%8E%86%E5%8F%B23.png)

(5) 流程实例历史表，表结构如下：

```sql
DROP TABLE IF EXISTS `workflow_history_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_history_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态,冗余流程实例表',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`describe` text COMMENT '历史流程描述',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例历史表';
CREATE INDEX idx_flow_instance_his_def_id ON workflow_history_process_instances(flow_definition_id);
CREATE INDEX idx_flow_instance_his_def_key ON workflow_history_process_instances(flow_definition_key);
CREATE INDEX idx_flow_instance_his_busi_id ON workflow_history_process_instances(business_id);
``` 
简单流程完成之后，在该表的体现：
![简单流程实例历史](http://7xsz2j.com1.z0.glb.clouddn.com/%E7%AE%80%E5%8D%95%E6%B5%81%E7%A8%8B%E5%AE%9E%E4%BE%8B%E5%8E%86%E5%8F%B2.png)

# 3. 自定义处理事件
事件的处理，需要业务层类实现 `IHandler接口`。接口定义如下：

```java
public interface IHandler {

    /**
     * 事件执行前置拦截
     * @param execution 执行容器
     */
    void preHandle(Execution execution);

    /**
     * 事件执行
     * @param execution 执行容器
     */
    void handle(Execution execution);

    /**
     * 事件后置拦截
     * @param execution 执行容器
     */
    void afterHandle(Execution execution);
}
```
如果不想处理前后拦截的方法，可以继承 `HandlerAware 类`

```java
public abstract class HandlerAware implements IHandler {
    @Override
    public void preHandle(Execution execution) {

    }
    @Override
    public void afterHandle(Execution execution) {

    }
}
```
## 3.1 定义事件处理类
比如定义了一个事件处理类： `HandlerOne`

```java
@Component
@Slf4j
public class HandlerOne extends HandlerAware {

    @Override
    public void handle(Execution execution) {
        log.info("[handler one] -> 执行");
        // ...
        log.info("[handler one] -> 执行结束");
    }
}
```
	(1) 这里可以将 Bean 加 `@Component` 注解交给Spring容器管理，这样也可以方便注入其他服务类。
	(2) 也可以不使用 `@Component` 注解，将 Bean 交给 `流程引擎上下文` 管理，不过这样就需要在xml配置 `全类名`。

## 3.2 在xml中配置事件处理类

```xml
<!-- 任务节点, 这样配置需要使用 @Component注解 -->
<task name="任务节点1" assignee="terminus1" pointx="250" pointy="100">
   <transition name="任务连线1" target="任务节点2" handler="handlerOne" />
</task>
```
或者

```xml
<!-- 任务节点 -->
<task name="任务节点1" assignee="terminus1" pointx="250" pointy="100">
   <transition name="任务连线1" target="结束节点" handler="io.terminus.doctor.workflow.base.handler.HandlerOne" />
</task>
```
## 3.3 Execution对象
通过 `Execution` 对象可以实现的操作

描述|方法
-------|-------
1. 获取当前活动的节点		|`getFlowProcess()`
2. 获取当前流程实例　　　　|   `getFlowInstance()`
3. 获取流程定义key　　　　|   `getFlowDefinitionKey()`
4. 获取业务id　　　　　　 |  `getBusinessId`
5. 获取全局业务数据　　　　| 	`getBusinessData()`
6. 设置和获取节点流转数据　| 	`set/getFlowData`
7. 获取操作人员信息　　　　| 	`getOperatorId()/getOperatorName()`
8. 获取执行表达式　　　　　| 	`Map getExpression();`
9. 其他 ... 				|

# 4. 自定义拦截器
拦截器的定义与事件处理同理，自定义类需要实现 `Interceptor 接口`

```java
public interface Interceptor {

    /**
     * 拦截前置方法
     * @param execution
     */
    void before(Execution execution);

    /**
     * 拦截后置方法
     * @param execution
     */
    void after(Execution execution);
}
```

例子：自带的日志拦截器 `LoggerInterceptor 类`
与处理事件不同是，拦截器自定义类需要使用 `@Component` 注解，需要将Bean交给Spring容器管理。

# 5. 唯一网关(decision节点)
当一个任务执行的下个任务存在多个选择判断的时候，就需要decision节点进行分支。

## 5.1 报销流程讲解
流程图大致如下：
![报销流程图](http://7xsz2j.com1.z0.glb.clouddn.com/%E6%8A%A5%E9%94%80%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

xml文档大致如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 流程开始 -->
<workflow key="decisionFlow" name="报销流程">
    <!-- 开始节点 -->
    <start name="开始节点" pointx="" pointy="">
        <transition name="开始连线" target="任务节点0" />
    </start>
    
    <!-- 填写报销单 -->
    <task name="任务节点0" assignee="" pointx="" pointy="">
        <transition name="任务连线0" target="任务节点1" />
    </task>
    
    <!-- 猴哥审批 -->
    <task name="任务节点1" assignee="猴哥" pointx="" pointy="">
        <transition name="任务连线1" target="选择节点1" />
    </task>
    
    <!-- 选择节点 -->
    <decision name="选择节点1" pointx="" pointy="">
        <transition name="选择连线1" expression="${money} &lt;= 500" target="结束节点" />
        <transition name="选择连线2" expression="500 &lt; ${money}" target="任务节点2" />
    </decision>
    
    <!-- 航哥审批 -->
    <task name="任务节点2" assignee="航哥" pointx="" pointy="">
        <transition name="任务连线2" target="结束节点" />
    </task>
    
    <!-- 结束节点 -->
    <end name="结束节点" pointx="" pointy="" />
</workflow>
```

1、部署流程

2、员工填写报销，生成流程实例。并执行对应的任务
`private String flowDefinitionKey = "decisionFlow";`
`private Long businessId = 1314L;` // 可能是员工编号

```java
// 启动流程实例
String businessData = "{money:800}";
workFlowService.getFlowProcessService()
			.startFlowInstance(flowDefinitionKey, businessId, businessData);
// 执行任务
workFlowService.getFlowProcessService()
			.getExecutor(flowDefinitionKey, businessId)
			.execute();
```

3、猴哥审批

```java
// 获取钱的大小 money
Map expression = new HashMap();
expression.put("money", money); // key对应表达式中的 ${money}

// 执行任务
workFlowService.getFlowProcessService()
			.getExecutor(flowDefinitionKey, businessId)
			.execute(expression);
```

4、航哥审批
如果 `money > 500` 则航哥审批，与猴哥审批类似。
否则流程直接结束。

## 5.2 表达式详解 (基于SpEL)
**1、变量的定义**
还是以money为例，表达式中的变量可以定义为: `${money}` , `#money` , `#{money}`

**2、关系表达式**
支持：`等于(==)` , `不等于(!=)` , `大于(>)` , `大于等于(>=)` , `小于(<)` , `小于等于(<=)` 

**3、逻辑表达式**
支持：`&&` ， `||` ， `!`

**4、正则匹配**
`matches` 如： `${str} matches 'aaa'`

新增支持 `equals` 匹配，如 `${str} equals 'bbb'`

# 6. 并行网关(fork/join)
TODO: 待补充

# 7. 子流程(substart/subend)
TODO: 待补充

# 8. 查询
`FlowQueryService`提供了多个查询接口，每个查询接口都与一个实体类相关联，已经封装好的查询方法是 `get` 开头，为封装的公共查询以 `find` 开头
## 8.1 封装好的查询
### 8.1.1 流程定义 FlowDefinitionQuery
方法|入参|出参|描述
-------|-------|-------|--------
getDefinitions|无|List|获取流程定义列表<br>(状态为normal的)
getDefinitionsByKey|String|List|根据流程定义key获取列表<br>(状态为normal的)
getLatestDefinitionByKey|String|FlowDefinition|根据流程定义的key值获取当前<br>最新版本的流程定义
getDefinitionById|Long|FlowDefinition|根据id查询流程定义

### 8.1.2 流程定义节点 FlowDefinitionNodeQuery
方法|入参|出参|描述
-------|-------|-------|--------
getDefinitionNodes|Long|List|根据流程定义id <br> 获取所有节点
getDefinitionNodesByType|(Long, Integer)|List|根据流程定义id和节点类型 <br> 获取节点列表
getDefinitionNodeByType|(Long, Integer)|FlowDefinitionNode|根据流程定义id和节点类型 <br> 获取唯一节点
getDefinitionNodeByName|(Long, String)|FlowDefinitionNode|根据流程定义id和节点name <br> 获取唯一节点

### 8.1.3 流程定义节点事件连线 FlowDefinitionNodeEventQuery
方法|入参|出参|描述
-------|-------|-------|--------
getNodeEvents|Long|List|根据流程定义id <br> 获取事件连线列表
getNodeEventsBySourceId|(Long, Long)|List|根据流程定义id和源nodeId <br>获取事件连线列表
getNodeEventByST|(Long, Long, Long)|FlowDefinitionNodeEvent|根据流程定义id<br>源nodeId<br>和目标nodeId<br> 获取事件连线列表

### 8.1.4 流程定义实例 FlowInstanceQuery
方法|入参|出参|描述
-------|-------|-------|--------
getFlowInstances|(String, Long)|List|根据流程定义key和业务id<br>获取所有的流程实例(包括子流程)
getExistFlowInstance|(String, Long)|FlowInstance|据流程定义key和业务id<br>查询主流程实例
getExistChildFlowInstance|(String, Long)|List|据流程定义key和业务id<br>查询所有的子流程实例

### 8.1.5 流程流转任务 FlowProcessQuery
方法|入参|出参|描述
-------|-------|-------|--------
getCurrentProcesses|Long|List|根据流程实例id<br>获取当前流程的活动节点(可能存在fork情况多条)
getCurrentProcess|(Long,String)|FlowProcess|根据流程实例id<br>获取当前流程的活动节点, 一个业务人员同时只能办理一个节点

### 8.1.6 流程流转跟踪 FlowProcessTrackQuery

### 8.1.7 流程实例历史 FlowHistoryInstanceQuery
方法|入参|出参|描述
-------|-------|-------|--------
getFlowHistoryInstances|(String, Long)|List|根据流程定义key和业务id<br>获取流程实例历史

### 8.1.8 流程流转任务历史 FlowHistoryProcessQuery
方法|入参|出参|描述
-------|-------|-------|--------
getHistoryProcess|Long|List|根据流程实例id获取流程任务历史

## 8.2 基本查询
以流程定义查询为例：（其他的查询服务都一样）

```java
// 查询集合
List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition);
List<FlowDefinition> findFlowDefinitions(Map criteria);
// 查询唯一（多个则抛出异常）
FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition);
FlowDefinition findFlowDefinitionSingle(Map criteria);
// 分页查询
Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit);
// 查询数量
long findFlowDefinitionsSize(Map criteria);
```

## 8.3 面向对象式查询
以流程定义查询为例：（其他都一样）

```java
// 指定属性
FlowDefinitionQuery id(Long id);
FlowDefinitionQuery key(String key);
FlowDefinitionQuery version(Long version);
FlowDefinitionQuery status(Integer status);
FlowDefinitionQuery operatorId(Long operatorId);
FlowDefinitionQuery operatorName(String operatorName);
// 指定bean
FlowDefinitionQuery bean(FlowDefinition flowDefinition);
// 指定排序字段（与数据库字段一致）
FlowDefinitionQuery orderBy(String orderBy);
// 是否倒序
FlowDefinitionQuery desc();
FlowDefinitionQuery asc();

Paging<FlowDefinition> paging(Integer offset, Integer limit); // 分页方法
FlowDefinition single();                                      // 唯一值
List<FlowDefinition> list();                                  // 值列表
long size();                                                  // 数量

```

# 9. 其他一些方法

## 9.1 流程定义的删除
`FlowDefinitionService` 提供了删除流程定义的方法

```java
// 不强制删除，如果当前流程定义存在流程实例, 则抛出异常.
void delete(Long flowDefinitionId);

/**
根据流程定义id删除流程定义
*  1. 默认是不强制级联删除, 如果当前定义存在流程实例, 则抛出异常.
*  2. 如果强制级联删除, 则删除流程实例, 以及所有执行的任务和任务追踪/历史
*/
void delete(Long flowDefinitionId, boolean cascade);

// 指定操作人id和name
void delete(Long flowDefinitionId, boolean cascade, Long operatorId, String operatorName);
```

## 9.2 流程实例的终止
`FlowProcessService` 提供了终止流程实例的方法

```java
// 不强制终止流程实例，如果存在正在执行的流程, 抛出异常
void endFlowInstance(String flowDefinitionKey, Long businessId);

/**
是否强制结束, 强制结束会移除所有的正在运行的流程.
isForce 默认为false, 如果存在正在执行的流程, 抛出异常
isForce 为true 强制结束会移除所有的正在运行的流程节点.
*/
void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe);

// 指定操作人id和name
void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe, Long operatorId, String operatorName);
```

## 9.3 回滚流程节点
`FlowProcessService` 提供了回滚流程的方法

```java
// 根据流程定义key和业务id回滚，depth回滚深度默认为1
void rollBack(String flowDefinitionKey, Long businessId);
void rollBack(String flowDefinitionKey, Long businessId, int depth);

// 指定操作人id和name
void rollBack(String flowDefinitionKey, Long businessId, Long operatorId, String operatorName);
void rollBack(String flowDefinitionKey, Long businessId, int depth, Long operatorId, String operatorName);

```
回滚操作暂不支持fork/join和子流程

# 10. 项目例子
## 10.1 doctor-workflow 模块
doctor-workflow 模块下的test包下的一些例子

## 10.2 doctor-event 模块
doctor-event 依赖 doctor-workflow 模块后
doctor-event 模块下的test包下的一个简单例子

# 11. 图形化绘制
TODO: 待补充

