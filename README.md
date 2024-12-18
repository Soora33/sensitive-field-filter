# sensitive-field-filter

🛡️ 一个轻量级的Java字段过滤工具，只需两个注解即可优雅地控制接口返回字段。

> A lightweight Java field filtering tool that elegantly controls API response fields with just two annotations.

你是否在写接口的时候，是否因为不想将一些敏感数据返回，又或者某个字段的返回结果根本用不上，从而不得不新写一个实体类来包装数据而烦恼。

现在你可以通过 `sensitive-field-filter` ，通过配置 2 个注解来完成这个操作！

## ✨ 特性

- 🚀 极简使用：仅需通过 `@SftFilter` 搭配 `@SftObjectFilter` 或 `@SftResponseFilter` 即可完成字段过滤
- 🎯 精确控制：可以精确控制需要过滤的字段和替换值
- 🔌 开箱即用：支持 Spring Boot 自动配置
- 🛠 灵活配置：支持对象级别和自定义响应级别的过滤控制
- 💡 使用简单：无需修改现有代码结构
- 🎨 优雅处理：基于 Spring AOP，对业务代码无侵入

## 🎯 适用场景

- 敏感信息过滤（如：密码、密钥等）
- 数据字段筛选（如：内部字段过滤）
- 接口数据裁剪（如：不同场景返回不同字段）

## 🚀 快速开始

项目目前正在内测，很快将发布正式版！目前可先使用测试版

### 添加依赖
在项目中添加依赖：
```xml
<dependency>
    <groupId>io.github.soora33</groupId>
    <artifactId>sft</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

### 配置注解

> @SftFilter：配置在实体类上需要过滤的字段
> 
> 可配置项：
> 
> value：过滤后的字段值，默认为 null
```java
public @interface SftFilter {
    String value() default "null";
}
```

> @SftObjectFilter： 配置在方法上，适用于直接返回对象，配置后会对该方法的返回值按照 `SftFilter`配置的字段进行过滤
> 
> 可配置项：
>
> entity：方法的返回值类型
> 
> preserveField：是否需要保留字段，默认为 true
```java
public @interface SftObjectFilter {
    Class<?> entity();
    boolean preserveField() default true;
}
```

>@SftResponseFilter：配置在方法上，适用于封装格式对象，配置后会对该方法的返回值按照 `SftFilter`配置的字段进行过滤（默认获取封装对象中 `data` 中的对象）
> 
> 可配置项：
>
> entity：方法的返回值类型
>
> key：封装数据体中存储数据的字段名，默认为 data
>
> preserveField：是否需要保留字段，默认为 true
```java
public @interface SftResponseFilter {
    Class<?> entity();
    String key() default "data";
    boolean preserveField() default true;
}
```

#### ⚠️⚠️⚠️ 注意：如果配置 `preserveField` 为 false，则会将返回值中的实体类对象转为 `LinkedHashMap`。因为去除过滤字段的实现方式是通过将非过滤字段加入到 Map 内实现的。如果对业务有影响，请不要使用！！！


### 🛸开始使用

#### 2.1 在实体类上配置 @SftFilter
```java
public class User {
    private String id;
    @SftFilter // 默认将该字段值设为 null
    private String name;
    @SftFilter(value = "Nah") // 修改默认值为 Nah
    private String email;
}
```
#### 2.2 使用场景示例
下面四个场景分别会展示四种情况，按顺序分别是：<br>
返回 `User` 对象，保留过滤字段 <br>
返回 `User` 对象，不保留过滤字段 <br>
返回 `AjaxResult` 封装格式对象，保留过滤字段 <br>
返回 `AjaxResult` 封装格式对象，不保留过滤字段 <br>

当然也可以对集合类型进行处理，例如: <br>
返回 `List<User>` 对象 <br>
返回 `AjaxResult` 对象，封装的数据类型为 `List<User>` <br>
这里碍于篇幅就不对 List 类型的数据做细致演示，使用方法都一样，如：

```java
@SftObjectFilter(entity = Person.class)
public List<Person> getUserList() {
    Person azki = new Person("1", "azki", "azki@email.com");
    Person nayuta = new Person("2", "nayuta", "nayuta@email.com");
    ArrayList<Person> list = Lists.newArrayList(azki, nayuta);
    return list;
}

@SftResponseFilter(entity = AjaxResult.class)
public AjaxResult getUserList() {
    Person azki = new Person("1", "azki", "azki@email.com");
    Person nayuta = new Person("2", "nayuta", "nayuta@email.com");
    ArrayList<Person> list = Lists.newArrayList(azki, nayuta);
    return AjaxResult.success(list);
}
```


##### 场景一：直接返回对象的过滤
```java
@SftObjectFilter(entity = User.class)
public User getUserToObj() {
    User user = new User("1", "Azki", "Azki@email.com");
    return user;
}
```
* 返回结果：
Person(id=1, name=null, email=Nah)

##### 场景二：直接返回对象的过滤（不保留字段）
⚠️ `preserveField` 设置为 false，会将返回结果转为 Map <br>
⚠️ `preserveField` 设置为 false 同时使用的是 `SftObjectFilter` 注解，那么返回值类型必须为 Object！
```java
@SftObjectFilter(entity = Person.class, preserveField = false)
public Object getUserToObj() {
    User user = new User("1", "Azki", "Azki@email.com");
    return user;
}
```
* 返回结果：
{id=1}

---

下面场景会使用到封装格式对象，封装格式大致分为两种，第一种是以对象的形式，通过 set 字段存储数据，例如：
```java
public class AjaxResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -7126327333321005351L;
    private String msg;
    private Integer code;
    private Object data;

    // get...set...方法

    private static AjaxResult rest(Object object) {
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setMsg("success");
        ajaxResult.setData(object);
        ajaxResult.setCode(200);
        return ajaxResult;
    }

    public static AjaxResult success(Object object) {
        return rest(object);
    }
}
```
第二种则是 Map 的形式存储数据，内部通过 put 的方式存储数据，例如：
```java
public class AjaxResultMap extends HashMap<Object,Object> implements Serializable {
    @Serial
    private static final long serialVersionUID = -7127333321005351L;
    private static String msg;
    private Integer code;
    private Object data;


    private AjaxResultMap(Object object) {
        super.put("msg", "success");
        super.put("data", object);
        super.put("code", 200);
    }

    public static AjaxResultMap success(Object object) {
        return new AjaxResultMap(object);
    }
}
```

无论是哪种方式，`SftResponseFilter` 内部都对其进行了实现，这里以其中一种演示：
请注意， `@SftResponseFilter` 注解默认会获取封装格式内的 `data` 字段或key。
如果你的封装格式存放数据不叫 data，请在注解中的 `key` 中指定。<br> 
例如你的封装格式存放数据叫 `body`,请在注解中将 `key` 指定为 `body`
> @SftResponseFilter(entity = AjaxResult.class, key = "body") 


##### 场景三：封装格式对象的过滤
```java
@SftResponseFilter(entity = AjaxResult.class)
public AjaxResult getUserToRes() {
    User user = new User("1", "Azki", "Azki@email.com");
    return AjaxResult.success(user);
}
```
* 返回结果：
```json
{
  "msg": "success",
  "code": 200,
  "data": {
    "id": "1",
    "name": null,
    "email": "Nah"
  }
}
```

##### 场景四：封装格式对象的过滤（不保留字段）
⚠️ `preserveField` 设置为 false，会将返回结果转为 Map
```java
@SftResponseFilter(entity = AjaxResult.class, preserveField = false)
public AjaxResult getUserToRes() {
    User user = new User("1", "Azki", "Azki@email.com");
    return AjaxResult.success(user);
}
```
* 返回结果：
```json
{
  "msg": "success",
  "code": 200,
  "data": {
    "id": "1"
  }
}
```

## 📃 常见问题
 ### 同类中，主方法内调用被 `@SftObjectFilter` 或 `@SftResponseFilter` 修饰的子方法，过滤失效
   > 出现原因：Spring AOP 的代理机制问题，当在一个类中调用另一个被注解修饰的方法，这个注解不会生效。因为 Spring AOP 基于代理实现，而内部调用绕过了代理 

   > 解决方案：使用下面方式进行同类调用 <br>
   ```java
public void selectPerson() {
    // this.getUser() -> ((YourClass)AopContext.currentProxy()).getUser().method();
    AjaxResult ajaxResultMap = ((UserController) AopContext.currentProxy()).getPerson();
}

@SftResponseFilter(entity = AjaxResult.class)
public AjaxResult getPerson() {
    Person person = new Person("1","person","person@email.com");
    return AjaxResult.success(person);
}
   ```
