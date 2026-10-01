CREATE TABLE sys_coding_rules(
                                 rule_id bigint not null primary key auto_increment comment'ID',
                                 rule_title varchar(200) not null comment'编码标题',
                                 rule_code varchar(200) not null unique comment'规则编码',
                                 rule_desc varchar(500) comment'规则说明',
                                 prefix varchar(200) comment'前缀',
                                 date_str int default(0) comment'是否使用日期字符串，0 不使用，1使用',
                                 serial_number bigint default(1) comment'当前序列号',
                                 number_length int default(6) comment'序列号长度',
                                 creat_time datetime default(now()) comment'创建时间',
                                 create_by varchar(200) default'' comment'创建者',
                                 update_time datetime default(now()) comment'修改时间',
                                 update_by varchar(200) default'' comment'修改者',
                                 status int default(1) comment'状态，1启用，2废弃',
                                 del_flag int default(0) comment'删除标记，0未删除，1删除'
);