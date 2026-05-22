# BreathChain Frontend

Vue 3 + Vite + Pinia + Ant Design Vue + ECharts。

## 启动

```bash
npm install
npm run dev
# 浏览器访问 http://localhost:5173
```

后端默认在 `http://localhost:8080`，Vite 已配置反向代理 `/api -> 8080`。

## 目录约定

```
src/
├── api/         # axios 实例 + 业务接口
├── assets/      # 全局样式
├── components/  # 通用组件
├── layouts/     # 医生端 / 患者端布局
├── router/      # 路由 + 角色守卫
├── store/       # Pinia
└── views/
    ├── doctor/  # Dashboard / TaskManage / Patients
    └── patient/ # Home / Tasks / Training / Rewards / History
```

## 核心页面

- `views/patient/Training.vue` — 呼吸训练核心交互（CSS scale 旋转圆 + 倒计时四阶段）
- `views/doctor/TaskManage.vue` — 任务 CRUD + 发布
- `views/doctor/Dashboard.vue`  — 数据概览（待接 ECharts）
