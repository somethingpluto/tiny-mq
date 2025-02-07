# TinyMQ
简单的消息队列实现

Topic 集合类型

元素:
- topic
- queuesInfo(队列详情)
- create time(创建信息)

```json
[
  {
    "topic": "order_tpoic",
    "queueList": [
      {
        "id": 1,
        "minOffset": 11,
        "currentOffset": 133,
        "maxOffset": 155
      },
      {}
    ],
    "create_time": 1777777
  }
]

```
