#!/bin/bash

# generate certificates
openssl req -newkey rsa:2048 -new -nodes -x509 -days 365 -keyout /tmp/key.pem -out /tmp/cert.pem -subj "/C=IN/ST=State/L=Locality/O=MyCompany/OU=IT/CN=example.com"

# import certificate into ACM
CERT_ARN=$(aws acm import-certificate --certificate fileb:///tmp/cert.pem --private-key fileb:///tmp/key.pem --tags Key=Name,Value=ALB-CA --query 'CertificateArn' --output text)
export CERT_ARN

echo "Installed certificates into ACM"