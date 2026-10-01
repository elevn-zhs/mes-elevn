import request from '@/utils/request.js'


export function chat(message){
    if(!message){
        // 如果用户没有输入任何问题，直接回应
        return {"code":200,"msg":"ok",data:`<div style="text-align:left">你好，我是你的AI助手;请问你有什么需要帮助的吗？</div>`}
    }
    // 请求后端的chat接口的时候需要携带conversationId；
    // chat的接口中conversationId是后端发过来的；
    // 后端发送过来的conversationId，我们会存储在localStorage中（客户端的存储空间key-value形式）；
    // 这里直接从localStorage中获取即可
    let conversationId = localStorage.getItem("conversationId");
    if(!conversationId){
        conversationId = ''; // 如果没有获取到就设置为空字符串
    }
    // 请求后端
    return  request({
        method:"get",
        url:"/ai/chat",
        params:{message:message,conversationId:conversationId}
    });
}


export function stream(message){
    if(!message){
        // 如果用户没有输入任何问题，直接回应
        return {"code":200,"msg":"ok",data:`<div style="text-align:left">你好，我是你的AI助手;请问你有什么需要帮助的吗？</div>`}
    }
    // 请求后端的chat接口的时候需要携带conversationId；
    // chat的接口中conversationId是后端发过来的；
    // 后端发送过来的conversationId，我们会存储在localStorage中（客户端的存储空间key-value形式）；
    // 这里直接从localStorage中获取即可
    let conversationId = localStorage.getItem("conversationId");
    if(!conversationId){
        conversationId = ''; // 如果没有获取到就设置为空字符串
    }
    // 请求后端
    return  request({
        method:"get",
        url:"/ai/stream",
        params:{message:message,conversationId:conversationId}
    });
}
