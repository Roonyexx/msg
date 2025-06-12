package com.rnxmsg;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class NetworkClient {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    public NetworkClient(String host, int port) throws IOException 
    {
        this.socket = new Socket(host, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(String message) throws IOException 
    {
        byte[] messageBytes = message.getBytes("UTF-8");
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        outputStream.write(buffer.array());
        outputStream.flush();
    }

    public String receiveMessage() throws IOException 
    {
        byte[] lengthBytes = new byte[4];
        inputStream.readFully(lengthBytes);
        // boost отправляет числа в little-endian, java их по умолчанию ожидает в big-endian
        // от того такие некравивые преобразования через буффер
        int length = ByteBuffer.wrap(lengthBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        if (length <= 0) throw new IOException("Invalid message length: " + length);
        byte[] messageBytes = new byte[length];
        inputStream.readFully(messageBytes);
        return new String(messageBytes, "UTF-8");
    }

    public void close() throws IOException 
    {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}