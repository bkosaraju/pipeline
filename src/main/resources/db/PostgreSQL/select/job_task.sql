select
    jbs.job_id jobId
    ,jbs.job_name jobName
        ,tsks.task_id taskId
    ,tsks.task_name taskName
    ,tsks.task_type taskType
    ,jp.task_seq_id taskSeqId
from meta.job_task_order jp
inner join meta.job jbs
on
    jbs.job_id = jp.job_id
    and jbs.job_status_flag = 1
inner join
meta.task tsks
on
    tsks.task_id = jp.task_id
LEFT OUTER JOIN
	( SELECT pe.task_id
	  ,MAX(pe.task_execution_id) over (partition BY pe.task_id ORDER BY pe.task_execution_timestamp desc)
	 from meta_audit.task_execution pe
	 WHERE pe.job_execution_id = cast(:jobExecutionId as bigint)
	 AND pe.task_execution_status='END' ) jpe
	 on
	 jpe.task_id = jp.task_id
WHERE
	 jpe.task_id IS NULL  AND
    jp.job_task_status_flag = 1 AND
	 jbs.job_id = cast(:jobId as int)
order by taskSeqId