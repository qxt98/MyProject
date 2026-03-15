# UML 建模说明

本系统采用统一建模语言（UML）描述需求、结构与流程，保证需求与实现一致。

## 1. 用例图（Use Case Diagram）

- **角色**：系统管理员、采购人员、审核人员、医生、护士、患者（或访客）。
- **用例示例**：
  - 药品信息：维护药品基础信息（增删改查）。
  - 库存：入库、出库、退库、查询库存与流水。
  - 采购审批：提交采购申请、审批/驳回。
  - 处方审核：提交处方、审核、查看风险提示。
  - 用药指导：输入症状/用药、获取用药建议。
  - 系统管理：用户与角色管理、权限配置、操作日志、系统参数。
- 用例图应体现各角色与上述用例之间的关联（Actor–Use Case）。

## 2. 类图（Class Diagram）

- **核心实体类**（示例）：
  - Drug（药品）、DrugCategory（药品类别）、Supplier（供应商）。
  - Stock（库存）、StockIn（入库）、StockOut（出库）、StockReturn（退库）。
  - PurchaseRequest（采购申请）、PurchaseApproval（审批记录）。
  - Prescription（处方）、PrescriptionItem（处方明细）、PrescriptionReview（处方审核）。
  - User（用户）、Role（角色）、Permission（权限）、OperationLog（操作日志）。
- 类图应标出主要属性、方法及类之间关系（关联、聚合、继承等）。

## 3. 顺序图（Sequence Diagram）

- **典型流程**：
  - 药品入库：用户 → 前端 → 入库接口 → 库存服务 → 数据库；返回结果与库存更新。
  - 采购审批：提交申请 → 创建申请 → 通知审批人 → 审批操作 → 更新状态与库存/采购单。
  - 处方审核：提交处方 → 校验药品与剂量 → 配伍检查 → 生成风险提示 → 审核结论。
- 顺序图按时间顺序画出参与者（用户、前端、Controller、Service、Repository、DB）之间的消息调用。

## 4. 其他可选模型

- **活动图**：采购审批、处方审核等流程的状态与分支。
- **状态图**：采购申请、处方单等的状态流转（如草稿、待审批、已通过、已驳回）。
- **部署图**：浏览器 + 前端静态资源、Spring Boot 应用、MySQL 的部署节点与连接关系。

## 5. 工具与产出

- 可使用 PlantUML、Draw.io、StarUML 等绘制，产出放在 `docs/uml/` 下（如 `usecase.puml`、`class.puml`、`sequence-*.puml`），便于版本管理与答辩展示。
