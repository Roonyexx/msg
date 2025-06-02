import socket
import json
import time

def send_json_messages(host='127.0.0.1', port=5001):
    messages = [
        {"type": "auth", "user": "alice", "password": "secret"},
        {"type": "ping"},
        {"type": "message", "text": "Hello, server!"},
        {"type": "logout"}
    ]

    try:
        with socket.create_connection((host, port)) as sock:
            for msg in messages:
                data = json.dumps(msg) + '\n'  
                sock.sendall(data.encode('utf-8'))
                print(f"> Sent: {data.strip()}")
                #time.sleep(0.5)  

    except Exception as e:
        print(f"Connection failed: {e}")

if __name__ == "__main__":
    send_json_messages()
