# 系统管理员 (SYS_ADMIN) 接口文档

**基础路径 (Base URL):** `/api/sys`

## 1. 用户管理模块

### 1.1 分页查询用户列表
- **接口路径:** `GET /users/page`
- **请求参数 (Query):**
  - `current` (Integer, 非必填, 默认 1): 当前页码
  - `size` (Integer, 非必填, 默认 10): 每页数量
  - `keyword` (String, 非必填): 搜索关键字（匹配学号/工号或姓名）
- **响应数据:** `Result<Page<User>>` 包含用户分页数据。

### 1.2 新增/修改用户
- **接口路径:** `POST /users/save`
- **请求体 (JSON):** `User` 对象（包含 `id` 时为修改，不包含时为新增）
- **响应数据:** `Result<String>` 成功提示信息。

### 1.3 删除用户
- **接口路径:** `DELETE /users/{id}`
- **路径参数:** `id` (Long): 用户 ID
- **响应数据:** `Result<String>` 成功提示信息。

### 1.4 发送系统通知
- **接口路径:** `POST /users/notify`
- **请求体 (JSON):**
  ```json
  {
    "userId": 1,
    "title": "通知标题",
    "content": "通知内容"
  }
  ```
- **响应数据:** `Result<String>` 成功提示信息。

---

## 2. 楼宇管理模块

### 2.1 获取所有楼宇列表
- **接口路径:** `GET /buildings/list`
- **请求参数:** 无
- **响应数据:** `Result<List<Building>>` 楼宇列表。

### 2.2 新增/修改楼宇
- **接口路径:** `POST /buildings/save`
- **请求体 (JSON):** `Building` 对象
- **响应数据:** `Result<String>` 成功提示信息。

### 2.3 删除楼宇
- **接口路径:** `DELETE /buildings/{id}`
- **路径参数:** `id` (Long): 楼宇 ID
- **响应数据:** `Result<String>` 成功提示信息。

---

## 3. 权限管理模块 (场地管理员管辖分配)

### 3.1 获取管理员管辖的楼宇
- **接口路径:** `GET /permissions/{adminId}`
- **路径参数:** `adminId` (Long): 场地管理员的用户 ID
- **响应数据:** `Result<List<VenueAdminBuilding>>` 关联记录列表。

### 3.2 分配管辖楼宇
- **接口路径:** `POST /permissions/assign`
- **请求体 (JSON):**
  ```json
  {
    "adminId": 2,
    "buildingIds": [1, 2, 3]
  }
  ```
  *(注：该接口采用全量覆盖策略，会先删除旧的关联记录，再插入新的)*
- **响应数据:** `Result<String>` 成功提示信息。

---

## 4. 公告管理模块

### 4.1 发布公告
- **接口路径:** `POST /announcements/publish`
- **请求体 (JSON):** `Announcement` 对象
- **响应数据:** `Result<String>` 成功提示信息。
