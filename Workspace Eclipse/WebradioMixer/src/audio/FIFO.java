package audio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FIFO {
	private BlockingQueue<byte[]> buffer;
	private int bufferSize;

	public FIFO(int bufferSize) {
		buffer = new ArrayBlockingQueue<byte[]>(bufferSize);
		
		this.bufferSize = bufferSize;
	}

	public void putElement(byte[] value) {
		if (getCurrentBufferSize() == bufferSize) {
			buffer.poll();
		}
		buffer.offer(value);
	}

	public Object[] getBuffer() {
		return buffer.toArray();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public int getCurrentBufferSize() {
		return buffer.toArray().length;
	}

	public void printBuffer() {
		System.out.println("buffer: " + buffer);
	}
}
