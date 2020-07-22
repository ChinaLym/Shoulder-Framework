# shoulder-log-operation 快速上手帮助文档

目录：

[TOC]

# 完整参考：

该文档仅仅是快速文档，完整文档见 [操作日志 DEMO 地址（完整的使用说明）]()

# 功能介绍

记录 `哪个用户` 在 `什么时刻` 用 `哪个终端` 的 `什么操作` ，操作了 `什么类型` 的 `哪个对象` ， `操作结果` 如何， `操作详情` 是怎样的， `关联的上下文` 是什么。

围绕该功能开发了一个可扩展的操作日志框架。

# 快速使用

## 代码编写    

* 在需要记录日志的方法上添加 **@OperationLog(operation = "xxx")** 
* 在方法内使用**OpLogContextHolder.getLog()** 获取操作日志对象进行填充。

- 方法执行完毕后，框架自动将记录一条操作日志。

# 代码示例

[操作日志 DEMO]() 中模拟了开发中各种可能的场景：普通业务、定时任务、异步线程怎、批量操作、性能调优等。

一般来说使用者只需关心 操作动作、被操作对象、操作结果、操作详情。下面举几个简单场景的例子。

```java
/**
 * 操作日志 Demo 
 *	使用 shoulder-log-operation
 *  只需要知道 @OperationLog、OpLogContextHolder 这两个类，就可以入门了~
 *  
 * @author lym
 */
@Serivce
public class UserServiceImpl implements IUserSerivce {
        
    @Autowired
    private IUserDao userDao;

    // ---- 一个注解 + 一行代码 即可记录一条规范的操作日志 ------
    
    /** 业务方法演示（添加用户） */
    @OperationLog(operation = UserActions.ADD)
    public void addUser(UserEntity user){
        // 填充被操作对象信息，包含 ObjectTypes
        OpLogContextHolder.getLog().setOperableObject(user);
        
        userDao.save(user);
    }
    
    /** ------- 定时任务演示。模拟用户信息补充任务。 ------- */
    @Scheduled(cron = "0/20 * * * * *")
    @OperationLog(operation = UserActions.DEMO_TASK)
    public void work() {
        // 1. 从数据库中取出所有需要补偿的记录
        List<UserEntity> users = userDao.xxx();
        // 2. 模拟处理
        List<ValidateResult<UserEntity>> result = DemoUtils.process(users);

        // 3. **填充操作日志，框架自动记录**
        OpLogContextHolder.setOperableObjects(result);//这里 ValidateResult 继承了 OperateRecordDto
    }
   

}
```

----

# 常见问题

#### 术语不懂

#### 常见报错

#### 采集与展示

#### 多语言与翻译

#### 记录操作参数

#### 能力扩展

--------------

# 操作日志框架设计思想与原理

TODO

