from airflow import DAG
from airflow.providers.databricks.operators.databricks import DatabricksSubmitRunOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'risk_team',
    'retries': 2
}

with DAG('risk_parquet_pipeline',
         start_date=days_ago(1),
         schedule_interval=None, # Triggered by Lambda
         default_args=default_args) as dag:

    # Define the Spark Task
    spark_task = DatabricksSubmitRunOperator(
        task_id='process_50gb_risk_file',
        databricks_conn_id='databricks_default',
        new_cluster={
            'spark_version': '13.3.x-scala2.12',
            'node_type_id': 'r5d.xlarge',
            'num_workers': 8, # Horizontal scaling for 50GB file
            'aws_attributes': {'availability': 'SPOT'} # Save 70% cost
        },
        notebook_task={
            'notebook_path': '/Users/admin/risk_calculations',
            'base_parameters': {
                'input_file': "{{ dag_run.conf['file_key'] }}" # Passed from Lambda
            }
        }
    )