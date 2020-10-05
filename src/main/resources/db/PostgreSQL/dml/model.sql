--global Configuration

--snowflake specific
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sfurl', E'xxxxxx.snowflakecomputing.com', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sfAccount', E'xxxx', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sfUser', E'xxxxxxx', E'ssm');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sfPassword', E'xxxxx', E'ssm');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sfRole', 'DEVELOPER', E'static');

--kubernetes specific
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'eksClusterName', E'eks-dm-dev', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'applicationIamRole', E'arn:aws:iam::xxxxxxxxxxxxx', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'namespace', E'dataengineering', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'appBucketName', E'app-config', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'sparkContainerImage', E'xxxxxxxxxxxx', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'persistentVolumeEnabled', E'false', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'podServiceAccount', E'spark', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'backoffLimit', E'3', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'spark.kubernetes.executor.request.cores', E'350m', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'spark.executor.instances', E'2', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'spark.executor.memory', E'512m', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'spark.executor.cores', E'2', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'jobWaitToFinish', E'7200', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'spark.sql.shuffle.partitions', E'60', E'static');


--databrics specific
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'min_workers', E'1', E'static');
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'password_token', E'xxxxxxxx', E'ssm');


--metrics configuration
INSERT INTO global_config("config_key", "config_value", "config_type") VALUES (E'pushMetricsToPrometheus', E'true', E'static');
INSERT INTO global_config("config_key", "config_value", "config_type") VALUES (E'pushGatewayEndPoint', E'127.0.0.1:9091', E'static');

--general
INSERT INTO global_config ("config_key", "config_value", "config_type") VALUES (E'hashedColumn', E'ROW_HASH', E'static');
