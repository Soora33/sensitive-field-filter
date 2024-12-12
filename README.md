# sensitive-field-filter

🛡️ 一个轻量级的Java字段过滤工具，只需两个注解即可优雅地控制接口返回字段。

> A lightweight Java field filtering tool that elegantly controls API response fields with just two annotations.

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

1. 添加依赖
2. 配置注解
3. 开始使用

## 📃 常见问题
 ### 同类中，主方法内调用被@SftObjectFilter/@SftResponseFilter修饰的子方法，过滤失效
   > 出现原因：Spring AOP 的代理机制问题，当在一个类中调用另一个被注解修饰的方法，这个注解不会生效。因为 Spring AOP 基于代理实现，而内部调用绕过了代理 

   > 解决方案：使用     Object object = ((YourController) AopContext.currentProxy()).selectUser2("s").get("data");
