<template>
<el-card class="messageBox" >
  <template #header>AI助手</template>
  <div v-for="msg in messageList">
    <div class="msg-header" :style="{textAlign: msg.type=='AI'?'left':'right'}" v-if="msg.type=='AI'">
      AI {{msg.time}} :
    </div>
    <div class="msg-header"  :style="{textAlign: msg.type=='USER'?'right':'left'}" v-if="msg.type=='USER'">
      你 {{msg.time}} :
    </div>
    <div v-html="msg.content"  :style="{textAlign: msg.type=='AI'?'left':'right'}" v-if="msg.type=='AI'">
    </div>
    <div  :style="{textAlign: msg.type=='USER'?'right':'left'}" v-if="msg.type=='USER'">
      {{msg.content}}
    </div>
  </div>
</el-card>
  <el-card>
    <el-input v-model="message" type="textarea"></el-input>
    <el-button type="primary" @click="handleSentMesage">发送</el-button>
  </el-card>
</template>
<script setup>
import {ref} from 'vue';
import {chat} from "@/api/ai/ai.js";
const message = ref("");
// 准备一个集合来存储会话的记录
// {type:AI/User,time:xxxx,content:xxxx}
const messageList = ref([]);

const handleSentMesage = async function(){
  // 一旦点击了发送按钮，就将按钮修改为正在加载状态......
  // 添加用户提示词
  messageList.value.push({
    type:"USER",
    content:message.value,
    time:getTime()
  });
  // 发送请求
  let response = await chat(message.value);
  let conversationId = response.msg;
  let content =  response.data;
  // 保存conversationId
  localStorage.setItem("conversationId",conversationId);
  message.value = "";
  // 添加AI相应的消息内容
  messageList.value.push({
    type:"AI",
    content:content,
    time:getTime()
  });
}

const getTime = function(){
  let now = new Date();
  return now.getFullYear() + "-"
  + (now.getMonth() + 1) + "-" +
      now.getDate() + "-"
  + " " +now.getHours() + ":"
  +now.getMinutes() + ":" + now.getSeconds()
}

</script>
<style scoped>
.messageBox{
  height: 700px;
}
.msg-header{
  background-color: #f1f1f1;
}
</style>