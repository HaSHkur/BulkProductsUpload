#!/usr/bin/env bash
set -e

awslocal s3 mb s3://my-app-bucket
awslocal dynamodb create-table \
  --table-name Products \
  --attribute-definitions AttributeName=Id,AttributeType=S \
  --key-schema AttributeName=Id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
