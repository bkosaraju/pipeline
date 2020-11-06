SELECT
	job_id jobId
	,job_execution_status jobExecutionStatus
	,max(id) over (partition BY job_id ORDER BY job_execution_start_timestamp desc ) AS maxExecutionId
	,job_order_timestamp jobOrderTimestamp
   from
   job_execution where
   job_id = :jobId
   UNION ALL
   SELECT :jobId jobId,
   'FIRST_RUN' jobExecutionStatus,
   0 maxExecutionId,
   current_timestamp jobOrderTimestamp
   LIMIT 1