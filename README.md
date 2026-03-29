# AzisabaMod

All-in-one mod for Azisaba

## 機能

- Build-Toolのプレビュー表示
- WebSocket経由でのMinecraftサーバーへの接続 (サーバーアドレス欄の`ws://`と`wss://`のサポート)
- デバッグ用オーバーレイ (F3+F6)
  - Build-Toolモード
  - 現在の画面(クラス名)
  - Ping
  - TPS
  - プレイヤーリスト(Tab)の権限表示
- アイテム説明 (F3+F6)
  - `custom_model_data`コンポーネント
  - `item_model`コンポーネント
  - MMID (`MYTHIC_TYPE`タグ)
  - RepairCost
  - Soulbound
- デバッグ用コマンド

## Fabric

### 1.21.11

#### 前提Mod

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [Cloth Config API](https://modrinth.com/mod/cloth-config)

#### あったほうがいいMod

- [Mod Menu](https://modrinth.com/mod/modmenu)

### 手順

1. Mod設定にAPIキーを入れる(APIキーはアジ鯖内で`/apikey`をすることで入手可能)
