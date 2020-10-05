SELECT
jobId
,taskId
,configKey
,configValue
,configType
,configVersion FROM (
SELECT
jobId
,taskId
,configKey
,configValue
,configType
,configVersion
,Rank () OVER (partition BY configKey ORDER BY priority) rk FROM (
SELECT
		jpc.job_id jobId
		,jpc.task_id taskId
		,jpc.config_key configKey
		,jpc.config_value configValue
		,jpc.config_type configType
		,jpc.config_version configVersion
		,1 priority
FROM meta.job_task_config jpc
INNER JOIN
meta.job_task_order jpo
ON
    jpc.task_id=jpo.task_id AND
    jpc.job_id=jpo.job_id AND
    jpc.config_version=jpo.config_version

WHERE
jpo.job_id= cast( :jobId as SMALLINT) AND
jpo.task_id= cast( :taskId as SMALLINT)
UNION ALL
SELECT
   job_id jobId
   ,cast( :taskId as SMALLINT) as taskId
	,config_key configKey
	,config_value configValue
	,config_type configType
	,1 configVersion
	,2 priority
FROM
meta.job_config
UNION ALL
SELECT
  cast( :jobId as SMALLINT) AS  jobId
   ,cast( :taskId as SMALLINT) taskId
	,config_key configKey
	,config_value configValue
	,config_type configType
	,1 configVersion
	,3 priority
FROM
meta.global_config
UNION ALL
SELECT
  cast( :jobId as SMALLINT) AS  jobId
   ,cast( :taskId as SMALLINT) taskId
	,'jobName' configKey
	,job_name configValue
	,'static' configType
	,1 configVersion
	,3 priority
FROM meta.job jb
WHERE
jb.job_id= cast( :jobId as SMALLINT)
UNION ALL
SELECT
  cast( :jobId as SMALLINT) AS  jobId
   ,cast( :taskId as SMALLINT) taskId
	,'taskName' configKey
	,task_name configValue
	,'static' configType
	,1 configVersion
	,3 priority
FROM meta.task jb
WHERE
jb.task_id= cast( :taskId as SMALLINT)
) config_data ) res WHERE res.rk=1
