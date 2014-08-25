#
#Thu Aug 21 13:45:27 CST 2014
debug=true#日志总开关，控制是否输出过程中log日志


svn_config_comment=#******************svn模块分隔行
svnmonitor_run=true#svn监控开关，控制是否运行关键作者/文件/包的监控
svn_key_pathroot=#工程svn地址
svn_key_username=#svn登录帐号
svn_key_passwd=#svn登录密码，运行后，会被加密保存处理。
svn_key_mail_receivers=#邮件收件人，多个帐号用英文逗号隔开。
svn_startverison=125163#开始监控svn版本号。
svn_period_min=#svn监控间隔时长。
svn_monitor_author=#关键作者，多个作者用英文逗号隔开。凡是关键作者提交的任意修改，均会有邮件通知。
svn_monitor_file=#关键文件，多个文件用英文逗号隔开。用文件名即可，不用带路径。如ScheduleTaskHandler.java,DatabaseHelper.java
svn_monitor_pkg=#关键包路径，多个包路径用英文逗号隔开。如com.package.tools.bitmap
svn_mail_cc_receivers=#邮件抄送人，多个帐号用用英文逗号隔开。
svn_mail_subject=#邮件主题，支持中文显示。但由于properties文件不支持中文保存，因此使用后，此处不能显示中文，不影响正常使用。
svn_mail_com_address=#公司邮箱后缀名称，用于发送给由log信息中提取出的用户名所组成的账户。如@gmail.com


mail_config_comment=#******************邮件模块分隔行
mail_key_host=#发送邮件的邮箱服务器地址
mail_key_user=#发送邮件所需账户
mail_key_passwd=#发送邮件所需账户密码，运行后，会被加密保存处理。
mail_port=#发送邮件所需端口号，默认是25，不用配置，若邮件发送有问题，可另行配置。
mail_validate=#发送邮件是否需要验证，默认是true，不用配置，若邮件发送有问题，可另行配置。
mail_show_from_name=#邮件发送人显示的名称


findbug_comment=#**********************config for findbugs
findbugs_run=false#findbugs开关，控制是否需要运行findbugs处理。
findbugs_key_ant_findbugs=#运行findbugs所需执行的脚本路径及文件，如sh /usr/bin/ant -buildfile build-changed-code-findbugs.xml
findbug_project_workspace=#运行FindBugs所需的本地工程目录。注意，监测到svn修改后的文件，会从svn上checkout下来当前版本的修改文件，然后依赖该目录下的bin目录中，已经编译好的各class文件进行编译。
findbug_mail_subject=#FindBugs检测处理后发送结果报告的邮件标题。
findbug_mail_receivers=#FindBugs检测处理后发送结果报告的邮件收件人。
findbug_mail_ccreivers=#FindBugs检测处理后发送结果报告的邮件抄送人。
findbug_mail_com_address=#FindBugs检测处理后发送结果报告的直接作者的邮件地址后缀，如@gmail.com。则当监测到用户有svn提交后，svn-long只能提取出账户名称huyong，然后才能将Findbugs的结果报告发送到账户huyong@gmail.com的邮箱中。
