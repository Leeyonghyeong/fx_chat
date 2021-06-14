package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// �Ѹ��� Ŭ���̾�Ʈ�� ����ϰ� ���ִ� Ŭ����
public class Client {
	
	// ������ �־���� Ŭ���̾�Ʈ�� ��Ʈ��ũ�󿡼� ����� �� ����.
	Socket socket;
	
	// ������ ����
	public Client(Socket socket) {
		this.socket = socket;
		
		// �ݺ������� Ŭ���̾�Ʈ�κ��� �޽����� ���޹��� �� �ֵ��� receive() �Լ��� ����.
		receive();
	}
	
	// Ŭ���̾�Ʈ�κ��� �޽����� ���޹޴� �޼ҵ�
	public void receive() {
		
		// �۾� ������ Runnable �������̽� or Callable �������̽��� ������ Ŭ������ �۾���û�� �ڵ带 ������ �۾��� ���� �� ����
		// ���� �������� Runnable�� run() �޼���� ���ϰ��� ����, Callable�� call() �޼���� ���� ���� ����
		
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				
				try {
					// �ݺ������� Ŭ���̾�Ʈ���� ������ ���� �� �ֵ��� while�� ����
					while(true) {
						// � ������ ���� ���� �� �ֵ��� InputStream ��ü ���
						InputStream is = socket.getInputStream();
						
						// ���۸� ����ؼ� �ѹ��� 512byte���� ���� �� �ֵ��� ����
						byte[] buffer = new byte[512];
						
						// �޼����� ũ��
						int length = is.read(buffer);
						while(length == -1) throw new IOException();
						
						// ������ ������ �� Ŭ���̾�Ʈ�� �ּ�����, �������� �̸����� ���
						System.out.println("[�޽��� ���� ����]"
											 + socket.getRemoteSocketAddress()
											 + " : "
											 + Thread.currentThread().getName());
						
						// ���޹��� ���� �ѱ۵� ���� �� �� �ֵ��� UTF-8 ����
						String message = new String(buffer, 0, length, "UTF-8");
						
						// ���޹��� �޽����� �ٸ� Ŭ���̾�Ʈ�鿡�� ���� �� �ֵ��� ����� ��
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				} catch(Exception e) {

					try {
						System.out.println(" [�޽��� ���� ����]" 
											+ socket.getRemoteSocketAddress()
											+ " : "
											+ Thread.currentThread().getName());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		// �����Լ��� �ִ� ������Ǯ�� submit�� ����
		// �� ������Ǯ�� ������� �ϳ��� �����带 ���
		Main.threadPool.submit(thread);
	}
	
	// Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�
	public void send(String message) {
		
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
					OutputStream os = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					// ���ۿ� ��� ������ �������� Ŭ���̾�Ʈ���� ����
					os.write(buffer);
					os.flush();
					
				} catch (Exception e) {
					
					try {
						
						System.out.println("[�޽��� �ۼ��� ����]" 
											+ socket.getRemoteSocketAddress()
											+ " : "
											+ Thread.currentThread().getName());
						
						Main.clients.remove(Client.this);
						socket.close();
						
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		Main.threadPool.submit(thread);
	}
}
