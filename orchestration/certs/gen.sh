#!/bin/bash

# Configuration
KEY_NAME="server.key"
CERT_NAME="server.crt"
PEM_NAME="server.pem"
DAYS_VALID=3650 # 10 years
RSA_BITS=4096

# Check if OpenSSL is installed
if ! command -v openssl &> /dev/null; then
    echo "Error: openssl could not be found. Please install it (sudo apt install openssl)."
    exit 1
fi

echo "--- Self-Signed Certificate Generator for Internal Services ---"

# Prompt for the server's internal IP address or Hostname
# (Defaulting to localhost if strictly local, but usually you want the LAN IP)
read -p "Enter server IP address (e.g., 192.168.1.50) or Domain: " SERVER_SAN
if [ -z "$SERVER_SAN" ]; then
    echo "Error: You must provide an IP or Hostname."
    exit 1
fi

echo "Generating config..."

# Create a temporary OpenSSL config file to handle Subject Alternative Names (SANs)
# This ensures the cert is valid for localhost, 127.0.0.1, AND your specific LAN IP.
cat > openssl_san.cnf <<EOF
[req]
default_bits = $RSA_BITS
prompt = no
default_md = sha256
distinguished_name = dn
x509_extensions = v3_req

[dn]
C = US
ST = Internal
L = Homelab
O = SelfHosted
OU = IT
CN = $SERVER_SAN

[v3_req]
subjectAltName = @alt_names
basicConstraints = CA:TRUE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment, keyCertSign

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
EOF

# 2. Append the specific SAN based on input type
# Regex checks if input looks like an IPv4 address
if [[ "$SERVER_SAN" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "IP.2 = $SERVER_SAN" >> openssl_san.cnf
else
    echo "DNS.2 = $SERVER_SAN" >> openssl_san.cnf
fi

echo "Generating $KEY_NAME and $CERT_NAME..."

# 3. Generate the Key and Certificate
openssl req -new -x509 -nodes -days $DAYS_VALID \
    -config openssl_san.cnf \
    -keyout $KEY_NAME \
    -out $CERT_NAME

# Clean up
rm openssl_san.cnf

# 4. Set permissions
if [ -f "$KEY_NAME" ]; then
    chmod 600 $KEY_NAME
    chmod 644 $CERT_NAME

    cat server.key server.crt > $PEM_NAME

    chmod 644 $PEM_NAME

    echo "-------------------------------------------------------"
    echo "Success! Files generated:"
    echo "1. $KEY_NAME (Keep this private!)"
    echo "2. $CERT_NAME (Public certificate)"
    echo ""
    echo "Certificate is valid for:"
    echo "- localhost"
    echo "- 127.0.0.1"
    echo "- $SERVER_SAN"
    echo "-------------------------------------------------------"
else
    echo "Error: Failed to generate certificates."
    exit 1
fi
