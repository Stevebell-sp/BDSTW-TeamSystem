# BDSTW | Team System

![模組 Logo](https://github.com/Stevebell-sp/BDSTW-TeamSystem/blob/main/src/main/resources/logo.png?raw=true)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge&logo=minecraft) ![Forge Version](https://img.shields.io/badge/Forge-47.3.0+-blue?style=for-the-badge&logo=forge) ![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg?style=for-the-badge)

**一個為 Minecraft 伺服器設計，由管理員驅動的強大隊伍與隨機性控制模組。**

此模組專為需要由伺服器管理員或地圖製作者完全控制隊伍分配與隨機事件的場景而設計，例如：小遊戲、團隊競賽或特殊活動。

---

## ✨ 主要功能

- **管理員全權控制**：只有 OP 或指令方塊可以創建、解散隊伍以及管理成員。
- **預設隊伍**：伺服器啟動時會自動建立「藍隊」、「紅隊」、「綠隊」、「白隊」，方便快速分隊。
- **自動化擊殺計數**：內建顯示於 TAB 列表的擊殺計分板，並會在伺服器重啟或玩家重登時自動歸零。
- **強大的隨機指令**：複製原版快照的 `/random` 指令功能，可用於獲取隨機數、公開擲骰，並支援可重現、可重置的隨機序列。
- **指令方塊友善**：所有管理員指令都支援目標選擇器 (`@p`, `@a`, `@r`)，並且 `/random` 指令的結果可以被指令方塊捕捉。

---

## 🔨 安裝與設定

### 安裝

1. 確認你已安裝 Minecraft Forge `1.20.1-47.3.0` 或更高版本。
2. 將本模組的 `.jar` 檔案放入伺服器的 `mods` 資料夾中。
3. 啟動伺服器，模組將會自動載入。

### 🔧 設定

模組的設定檔位於伺服器根目錄的 `config/bdstw_teamsystem-server.toml`。你可以用任何文字編輯器開啟它來修改以下設定：

- `maxTeamSize`：一個隊伍的最大玩家數量 (預設: 8)。

---

## 🎮 指令用法

### 一般玩家指令

| 指令 | 功能 | 備註 | 
 | ----- | ----- | ----- | 
| `/team list` | 列出伺服器上所有的隊伍及其狀態。 | 對所有玩家開放。 | 
| `/team info` | 查看你目前所在隊伍的詳細資訊。 | 僅限已在隊伍中的玩家使用。 | 

### 管理員指令

這些指令僅限 **OP (權限等級 2 以上)** 或 **指令方塊** 使用。

#### `/teamadmin` 指令

| 指令 | 功能 | 
 | ----- | ----- | 
| `/teamadmin create <隊伍名>` | 創建一個新的隊伍。 | 
| `/teamadmin disband <隊伍名>` | 強制解散一個指定的隊伍 (無法解散預設隊伍)。 | 
| `/teamadmin forcejoin <玩家> <隊伍名>` | 強制將一名玩家加入指定的隊伍。 | 
| `/teamadmin removeplayer <玩家們>` | 強制將一名或多名玩家從他們目前的隊伍中移除。 | 
| `/teamadmin randomjoin <玩家們>` | 強制將一名或多名玩家隨機分配到一個預設隊伍中。 | 
| `/teamadmin resetkills <玩家們>` | 重置一名或多名玩家的擊殺計數。 | 

#### `/random` 與 `/randomsequence` 指令

| 指令 | 功能 | 
 | ----- | ----- | 
| `/random value <範圍>` | 從指定範圍（例如 `1..100`）中取得一個隨機整數。 | 
| `/random roll <範圍>` | 公開擲骰，並將結果顯示給所有玩家。 | 
| `/random value <範圍> [序列ID]` | 從一個可重現的隨機序列中取得下一個數值。 |
| `/randomsequence reset <序列ID>` | 重置指定的隨機序列，使其從頭開始。 |

---

### 🧱 指令方塊應用

#### 捕捉 `/random` 的結果

`/random value` 指令會將其產生的隨機數作為「成功值」回傳。您可以利用 `/execute store` 指令來捕捉這個值，並將其存入計分板。

**範例：取得一個 1 到 100 的隨機數，並存入玩家的分數中**

1.  **第一步：創建一個計分板目標**
    ```
    /scoreboard objectives add my_random_num dummy
    ```

2.  **第二步：執行指令**
    在一個指令方塊中，輸入以下指令：
    ```
    /execute store result score @p my_random_num run random value 1..100
    ```
    當這個指令方塊被觸發時，系統會執行 `random value 1..100`，並將得到的數字（例如 42）儲存到最近玩家（`@p`）在 `my_random_num` 這個計分板上的分數。

---

## 👤 作者

- **BDSTW 小誠**

## 📜 授權條款 (License)

本模組採用 **BSD 3-Clause License** 進行授權。
