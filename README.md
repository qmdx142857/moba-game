
Java 游戏服务器四服（网关/大厅/匹配/战斗）最小可运行骨架
SpringBoot + Netty + Redis + Protobuf

Client ──TCP──▶ Gateway(9001) ──TCP──▶ 大厅(9000) ──TCP+REDIS──▶ 匹配(9003) ──TCP+REDIS──▶ 战斗(9002)


Gateway：无状态，只透传 + 限流 + 加密
Lobby：有状态，登录、好友、商城、任务
Match：无状态，Redis 收请求，发结果
Battle：有状态，60Hz 帧同步，房间生命周期

eg. 未测试，只实现框架思路