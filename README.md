# BDSTW | Team System

![模組 Logo](https://github.com/Stevebell-sp/BDSTW-TeamSystem/blob/main/src/main/resources/logo.png?raw=true)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge&logo=minecraft) ![Forge Version](https://img.shields.io/badge/Forge-47.3.0+-blue?style=for-the-badge&logo=forge) ![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg?style=for-the-badge)

**一個為 Minecraft 伺服器設計，用於整合原版隊伍、計分板與反作弊的後端模組。**

此模組專為需要自動化管理與監控的場景而設計。它能自動偵測玩家在原版隊伍系統中的變化，並在後台觸發預設指令來與其他插件（如權限管理）進行深度整合。

---

## ✨ 主要功能

- **外部插件整合**：自動偵測玩家加入或離開原版隊伍的事件，並在後台**模擬玩家**執行預設的指令。這可用於與權限插件 (如 LuckPerms) 或其他隊伍插件進行無縫同步。
- **進階反作弊系統**：
    - **禁用 F3 除錯畫面**：可透過設定檔，禁止非管理員玩家使用 F3 除錯畫面。
    - **偵測與懲罰**：自動偵測玩家使用 F3+B 的行為。首次使用會發出全服警告，連續使用三次將被處決兩次，並自動執行指令將其標記在計分板上。
- **自動化擊殺計數**：內建擊殺計分板（預設不在 TAB 列表顯示），分數會永久保留直到伺服器重啟。
- **強大的隨機指令**：複製原版快照的 `/random` 指令功能，可用於獲取隨機數、公開擲骰，並支援可重現、可重置的隨機序列。
- **指令方塊友善**：`/random` 指令的結果可以被指令方塊捕捉。

---

## 🔨 安裝與設定

### 安裝

1. 確認你已安裝 Minecraft Forge `1.20.1-47.3.0` 或更高版本。
2. 將本模組的 `.jar` 檔案放入伺服器的 `mods` 資料夾中。
3. 啟動伺服器，模組將會自動載入。

### 🔧 設定

模組的設定檔位於伺服器根目錄的 `config/bdstw_teamsystem-server.toml`。你可以用任何文字編輯器開啟它來修改以下設定：

- `disableF3ForNonOps`：是否禁用非管理員玩家的 F3 除錯畫面 (預設: true)。

### 🔌 整合設定

本模組的外部指令整合功能是透過在程式碼中寫死的方式實現的。如果您需要修改觸發的指令（例如，從 `/groups` 改為 `/lp`），您需要手動修改 `src/main/java/org/bdstw/teamsystem/event/ServerEvents.java` 檔案並重新編譯模組。

預設的指令設定如下：
- **加入隊伍時**: `groups leave (孤狼)`，接著執行對應隊伍的 `groups join ...` 指令。
- **離開隊伍時**: `groups leave (孤狼)`。

---

## 🎮 指令用法

### 管理員指令

這些指令僅限 **OP (權限等級 2 以上)** 或 **指令方塊** 使用。

| 指令 | 功能 |
| :--- | :--- |
| `/random value <範圍>` | 從指定範圍（例如 `1..100`）中取得一個隨機整數。 |
| `/random roll <範圍>` | 公開擲骰，並將結果顯示給所有玩家。 |
| `/random value <範圍> [序列ID]` | 從一個可重現的隨機序列中取得下一個數值。 |
| `/randomsequence reset <序列ID>` | 重置指定的隨機序列，使其從頭開始。 |

---

## 👤 作者

- **BDSTW 小誠**

## 📜 授權條款 (License)

本模組採用 **BSD 3-Clause License** 進行授權。

Copyright (c) 2025, BDSTW 小誠
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
