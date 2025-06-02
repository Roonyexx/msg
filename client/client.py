import socket
import json
import struct
import time

def send_json_messages(host='127.0.0.1', port=5001):
    messages = [
        {"type": "auth", "user": "alice", "password": "123"},
        {"type": "ping"},
        {"type": "message", "text": "Hello, server!"},
        {"type": "logout"}
    ]

    try:
        with socket.create_connection((host, port)) as sock:
            for msg in messages:
                encoded = json.dumps(msg).encode('utf-8')
                length_prefix = struct.pack('<I', len(encoded))
                sock.sendall(length_prefix + encoded)
                print(f"> Sent: {msg}")
                time.sleep(0)

    except Exception as e:
        print(f"Connection failed: {e}")

if __name__ == "__main__":
    send_json_messages()
