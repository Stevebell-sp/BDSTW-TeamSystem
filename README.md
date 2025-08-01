# BDSTW | Team System

![模組 Logo](https://github.com/Stevebell-sp/BDSTW-TeamSystem/blob/main/src/main/resources/logo.png?raw=true)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge&logo=minecraft) ![Forge Version](https://img.shields.io/badge/Forge-47.3.0+-blue?style=for-the-badge&logo=forge) ![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg?style=for-the-badge)

**一個為 Minecraft 伺服器設計，由管理員驅動的強大隊伍與隨機傳送 (RTP) 模組。**

此模組專為需要由伺服器管理員或地圖製作者完全控制隊伍分配的場景而設計，例如：小遊戲、團隊競賽或特殊活動。一般玩家無法自行創建或加入隊伍，所有隊伍操作皆由 OP 或指令方塊執行。

---

## ✨ 主要功能

- **管理員全權控制**：只有 OP 或指令方塊可以創建、解散隊伍、管理成員以及執行隨機傳送。
- **預設隊伍**：伺服器啟動時會自動建立「藍隊」、「紅隊」、「綠隊」、「白隊」，方便快速分隊。
- **自動化擊殺計數**：內建顯示於 TAB 列表的擊殺計分板，並會在伺服器重啟或玩家重登時自動歸零，確保每場遊戲的公平性。
- **可靠的管理員隨機傳送 (RTP)**：
  - 提供 `/teamadmin rtp` 指令，可傳送指定玩家或整個隊伍。
  - 採用了優化的演算法，大幅提高尋找安全地點的成功率。
- **高度可設定**：所有 RTP 相關的數值（如傳送範圍）都可以在設定檔中調整。
- **指令方塊友善**：所有管理員指令都支援目標選擇器 (`@p`, `@a`, `@r`)，非常適合用於地圖製作。

---

## 🔨 安裝與設定

### 安裝

1. 確認你已安裝 Minecraft Forge `1.20.1-47.3.0` 或更高版本。
2. 將本模組的 `.jar` 檔案放入伺服器的 `mods` 資料夾中。
3. 啟動伺服器，模組將會自動載入。

### 🔧 設定

模組的設定檔位於伺服器根目錄的 `config/bdstw_teamsystem-server.toml`。你可以用任何文字編輯器開啟它來修改以下設定：

- `maxTeamSize`：一個隊伍的最大玩家數量 (預設: 8)。
- `rtpMinRange`：RTP 的最小傳送半徑 (預設: 1000)。
- `rtpMaxRange`：RTP 的最大傳送半徑 (預設: 10000)。
- `rtpCooldown`：RTP 的冷卻時間 (秒) (預設: 300)。 (此設定目前不影響管理員指令)
- `rtpInNether`：是否允許在地獄使用 RTP (預設: true)。
- `rtpInEnd`：是否允許在終界使用 RTP (預設: true)。

---

## 🎮 指令用法

### 一般玩家指令

這些是所有玩家都可以使用的指令，主要用於查詢資訊。

| 指令 | 功能 | 備註 |
| :--- | :--- | :--- |
| `/team list` | 列出伺服器上所有的隊伍及其狀態。 | 對所有玩家開放。 |
| `/team info` | 查看你目前所在隊伍的詳細資訊。 | 僅限已在隊伍中的玩家使用。 |

### 管理員指令 (`/teamadmin`)

這些指令僅限 **OP (權限等級 2 以上)** 或 **指令方塊** 使用。

| 指令 | 功能 |
| :--- | :--- |
| `/teamadmin create <隊伍名>` | 創建一個新的隊伍。 |
| `/teamadmin disband <隊伍名>` | 強制解散一個指定的隊伍 (無法解散預設隊伍)。 |
| `/teamadmin forcejoin <玩家> <隊伍名>` | 強制將一名玩家加入指定的隊伍。 |
| `/teamadmin removeplayer <玩家們>` | 強制將一名或多名玩家 (`@a`, `@p`...) 從他們目前的隊伍中移除。 |
| `/teamadmin randomjoin <玩家們>` | 強制將一名或多名玩家 (`@a`, `@r`...) 隨機分配到一個預設隊伍中。 |
| `/teamadmin rtp player <玩家們> [最大範圍]` | 將一名或多名指定的玩家隨機傳送。 |
| `/teamadmin rtp team <隊伍名> [最大範圍]` | 將指定隊伍的所有成員隨機傳送到同一個地點。 |
| `/teamadmin resetkills <玩家們>` | 重置一名或多名玩家 (`@a`, `@p`...) 的擊殺計數。 |


### 🧱 指令方塊使用範例

本模組的設計非常適合與指令方塊結合，來製作自動化的分隊系統或遊戲機制。

#### 按下按鈕，重置所有玩家的擊殺數
1. 放置一個指令方塊。
2. 在指令方塊中輸入：`teamadmin resetkills @a`
3. 在指令方塊上放置一個按鈕。

#### 將踩到壓力板的玩家加入「紅隊」
1. 放置一個指令方塊。
2. 在指令方塊中輸入：`teamadmin forcejoin @p "紅隊"`
3. 在指令方塊上方放置一個壓力板。

#### 按下按鈕，將所有玩家隨機分配到預設隊伍
1. 放置一個指令方塊。
2. 在指令方塊中輸入：`teamadmin randomjoin @a`
3. 在指令方塊上放置一個按鈕。

#### 按下按鈕，將「藍隊」所有成員隨機傳送
1. 放置一個指令方塊。
2. 在指令方塊中輸入：`teamadmin rtp team "藍隊"`
3. 在指令方塊上放置一個按鈕。

---

## 👤 作者

- **BDSTW 小誠**

## 📜 授權條款

本模組為 **版權所有 (All Rights Reserved)**。

未經作者明確許可，禁止以任何形式複製、修改、散布或重新發布本模組的任何部分。