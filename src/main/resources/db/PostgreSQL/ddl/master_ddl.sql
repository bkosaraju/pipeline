create schema if not exists meta;

set schema 'meta';

CREATE TABLE IF NOT EXISTS job (
    job_id      Smallint CONSTRAINT job_id PRIMARY KEY,
    job_name    varchar(40) NOT NULL,
    job_status_flag smallInt DEFAULT 1,
    create_timestamp timestamp NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS task (
    task_id      Smallint CONSTRAINT task_id PRIMARY KEY,
    task_name    varchar(40) NOT NULL,
    task_type     varchar(40) NOT NULL,
    create_timestamp timestamp NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS job_task_order (
    job_id      smallint NOT NULL,
    task_id     Smallint NOT NULL,
    task_seq_id      Smallint NOT NULL,
    job_task_status_flag smallInt DEFAULT 1,
    config_version decimal(10,2) default 1.0,
    PRIMARY KEY(job_id, task_id,config_version)
);


CREATE TABLE IF NOT EXISTS global_config (
	config_key VARCHAR(400) NOT NULL,
	config_value VARCHAR(2000) NULL DEFAULT NULL,
	config_type VARCHAR(30) NULL DEFAULT NULL,
	PRIMARY KEY (config_key)
);

CREATE TABLE  IF NOT EXISTS job_config (
	job_id SMALLINT NULL DEFAULT NULL,
	config_key VARCHAR(400) NOT NULL,
	config_value VARCHAR(2000) NULL DEFAULT NULL,
	config_type VARCHAR(30) NULL DEFAULT NULL,
	PRIMARY KEY (job_id, config_key)
);

CREATE TABLE IF NOT EXISTS job_task_config (
    job_id      smallInt,
    task_id      Smallint,
    config_key   varchar(400),
    config_value varchar(2000),
    config_type varchar(30),
    config_version decimal(10,2) default 1.0,
    PRIMARY KEY (job_id,task_id,config_key,config_version)
);

create schema if not exists meta_audit;

set schema 'meta_audit';


CREATE or replace FUNCTION meta_audit.upd_job_execution(jobId INT,job_execution_status VARCHAR(30))
RETURNS TABLE (
job_execution_id BIGINT,
job_id smallint,
job_execution_status VARCHAR(30),
job_execution_timestamp timestamp,
job_order_timestamp timestamp
)
AS $$
	INSERT INTO job_execution (job_id,job_execution_id,job_execution_status,job_order_timestamp)
	SELECT $1, max(job_execution_id) over (partition BY job_id ORDER BY job_execution_timestamp desc ) AS max_exec_id, $2, job_order_timestamp
   from
   meta_audit.job_execution where
   job_id = $1
   LIMIT 1
	RETURNING job_execution_id,job_id,job_execution_status,job_execution_timestamp,job_order_timestamp;
$$ LANGUAGE SQL;

CREATE TABLE IF NOT EXISTS job_execution (
     job_id SMALLINT,
     job_execution_id BIGSERIAL,
     job_execution_timestamp TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'Australia/Melbourne'),
     job_order_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
     job_execution_status VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS task_execution (
     task_id SMALLINT,
     task_execution_id BIGSERIAL,
     job_execution_id BIGINT,
     task_execution_timestamp TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'Australia/Melbourne'),
     job_order_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
     task_execution_status VARCHAR(30),
     config_version decimal(10,2) DEFAULT 1.0
);

