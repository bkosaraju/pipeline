SELECT
	job_id jobId
	,job_execution_status jobExecutionStatus
	,max(job_execution_id) over (partition BY job_id ORDER BY job_execution_timestamp desc ) AS maxExecutionId
	,job_order_timestamp jobOrderTimestamp
   from
   meta_audit.job_execution where
   job_id = :jobId
   and job_order_timestamp > cast( :jobOrderTimestamp as TIMESTAMP )
   LIMIT 1