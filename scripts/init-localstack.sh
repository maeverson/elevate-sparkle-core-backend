#!/bin/bash

# Script to initialize AWS resources in LocalStack
# Run this after docker-compose up to set up SQS queues

echo "🚀 Initializing LocalStack resources..."

# Wait for LocalStack to be ready
echo "⏳ Waiting for LocalStack..."
until curl -s http://localhost:4566/_localstack/health | grep -q "\"sqs\": \"available\""; do
    sleep 2
done

echo "✅ LocalStack is ready!"

# Configure AWS CLI for LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Create SQS queue
echo "📬 Creating SQS queue 'order-events'..."
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name order-events \
    --attributes VisibilityTimeout=30,MessageRetentionPeriod=345600

# Get queue URL
QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url --queue-name order-events --output text)

echo "✅ SQS queue created: $QUEUE_URL"

# List queues
echo "📋 Listing all queues:"
aws --endpoint-url=http://localhost:4566 sqs list-queues

echo ""
echo "🎉 LocalStack initialization complete!"
echo ""
echo "You can now send messages to the queue using:"
echo "aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url $QUEUE_URL --message-body 'test'"
