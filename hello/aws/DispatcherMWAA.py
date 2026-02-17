import boto3
import requests
import json
import base64

# Configuration
MWAA_ENV_NAME = "risk-mwaa-environment"
DAG_NAME = "process_risk_parquets"

mwaa_client = boto3.client('mwaa')

def lambda_handler(event, context):
    # 1. Extract file info from SQS/SNS
    # Example assumes SQS record
    body = json.loads(event['Records'][0]['body'])
    s3_key = body['Records'][0]['s3']['object']['key']

    # 2. Get a CLI Token (Valid for 60 seconds)
    auth_token = mwaa_client.create_cli_token(Name=MWAA_ENV_NAME)

    # 3. Construct the Airflow CLI command
    # We pass the file path via --conf so the DAG knows which file to process
    command = f"dags trigger {DAG_NAME} --conf '{{\"file_key\":\"{s3_key}\"}}'"

    # 4. Make the POST request to the MWAA Web Server
    mwaa_url = f"https://{auth_token['WebServerHostname']}/aws_mwaa/cli"
    headers = {
        'Authorization': f'Bearer {auth_token["CliToken"]}',
        'Content-Type': 'text/plain'
    }

    response = requests.post(mwaa_url, data=command, headers=headers)

    # 5. Check if it worked
    if response.status_code == 200:
        return {"status": "success", "mwaa_response": response.json()}
    else:
        raise Exception(f"Failed to trigger DAG: {response.text}")