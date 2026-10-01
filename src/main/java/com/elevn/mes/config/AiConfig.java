package com.elevn.mes.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.elevn.mes.aitools.StorageTools;
import com.elevn.mes.aitools.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
public class AiConfig {



    // 注入提示词模版
    @Value("classpath:prompts/mes-ai-chat-prompt.st")
    private Resource prompt;
    // 当前容器中存在两个ChatModel实例
    // dashScopeChatModel 链接百炼平台
    // openAiChatModel 链接Deepseek
    // 通过@Qualifier("dashScopeChatModel")指定要使用的实例
    @Autowired
    @Qualifier("dashScopeChatModel")
    private ChatModel chatModel;

    @Autowired // 注入DataSource
    private DataSource dataSource;

    // 1. 记忆本体：滑动窗口，最多保留 10 条
    //    不指定 repository 时，默认就是 InMemoryChatMemoryRepository（一个 Map）
    // 配置一个全局的ChatMemory对象
    @Bean
    public ChatMemory chatMemory(){
        // 将ChatMemoryRepository设置为
        JdbcChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder().dataSource(dataSource).build();
        // 消息的最大记忆数量个是10
        return MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).maxMessages(10).build();
    }
    // 注入工具对象
    @Autowired
    private TimeTools timeTools;
    @Autowired
    private StorageTools storageTools;

    //  使用@Bean的形式注入一个ChatClient
    @Bean
    public ChatClient chatClient(){
        // 创建chatClient对象返回
        return ChatClient.builder(chatModel)
                // 使用本地保存的提示词模版
                .defaultSystem(prompt)
                // 记录日志到数据库  滑动窗口
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                        ,new SimpleLoggerAdvisor()
                )
                .defaultTools(timeTools,storageTools)
                .defaultOptions(DashScopeChatOptions.builder()
                        .temperature(0.5).build())
                .build();
    }
}
