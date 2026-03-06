# skylark-xiaolvxingqiu-boot

小绿星球后端服务（Spring Boot）。

## 技术栈

- Spring Boot 2.6.13
- Java 8
- Spring Web
- Bean Validation
- Lombok

## 启动

```bash
mvn spring-boot:run
```

默认端口：`8086`

健康检查：

```text
GET /api/health
```

## 已实现接口（第一版）

### 花园

- `GET /api/garden`
- `PUT /api/garden`
- `GET /api/gardens`
- `POST /api/gardens`
- `GET /api/gardens/{gardenId}`
- `PUT /api/gardens/{gardenId}`
- `DELETE /api/gardens/{gardenId}`
- `PUT /api/gardens/{gardenId}/default`

### 个人资料

- `GET /api/profile`
- `PUT /api/profile`

### 绿植

- `GET /api/plants?filter=all|healthy|abnormal|todo|favorite`
- `GET /api/plants/{id}`
- `POST /api/plants`
- `PUT /api/plants/{id}`
- `DELETE /api/plants/{id}`

### 养护

- `GET /api/care/tasks`
- `POST /api/care/tasks/{taskId}/complete`
- `GET /api/care/activities?date=yyyy-MM-dd`
- `GET /api/care/activities?month=yyyy-MM`

### 反馈

- `POST /api/feedback`
- `GET /api/feedback`

## 说明

- 当前为可联调的内存实现（不依赖数据库）
- 后续可在保持接口不变的前提下替换为 MySQL/JPA/MyBatis 持久化
- 用户身份由外部 `permission` 体系提供，本服务不落地用户表
- 联调阶段可通过 `app.auth.use-mock-user=true` 固定用户，默认 `10001`
- 设计用表结构见 `src/main/resources/db/schema/garden.sql`
