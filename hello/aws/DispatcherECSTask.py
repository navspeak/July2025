import boto3
import os

ecs = boto3.client('ecs')

def lambda_handler(event, context):
    # 1. Extract file info from SQS
    # Assuming SQS -> Lambda trigger
    s3_info = event['Records'][0]['body']

    # 2. Trigger the Fargate Task
    response = ecs.run_task(
        cluster='risk-cluster',
        launchType='FARGATE',
        taskDefinition='risk-processor-task:1', # Your Docker image config
        count=1,
        networkConfiguration={
            'awsvpcConfiguration': {
                'subnets': ['subnet-12345', 'subnet-67890'],
                'securityGroups': ['sg-00000'],
                'assignPublicIp': 'ENABLED' # Or DISABLED if using a NAT Gateway
            }
        },
        overrides={
            'containerOverrides': [
                {
                    'name': 'risk-container',
                    'environment': [
                        {
                            'name': 'S3_FILE_PATH',
                            'value': s3_info
                        }
                    ]
                }
            ]
        }
    )

    return {"status": "Task Started", "taskArn": response['tasks'][0]['taskArn']}