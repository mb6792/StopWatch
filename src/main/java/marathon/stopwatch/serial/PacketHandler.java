package marathon.stopwatch.serial;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import marathon.stopwatch.data.DataWriter;
import marathon.stopwatch.logging.Logger;


public class PacketHandler {
	
	enum PacketType {START, STOP, DATA};
	
	private final int BYTE_TIMEOUT = 50;

	AtomicBoolean timeout = new AtomicBoolean(false);
	ByteReceptionTimer brt;
	
	Packet currentPacket;
	
	public PacketHandler(){
		
		brt = new ByteReceptionTimer();
		brt.start();
	}
	
	public boolean handlePacket(Packet packet){
				
		String packetString = new StringBuffer(packet.toString()).reverse().toString();
		String[] packetStringArray = packetString.trim().split("  ");
		
		int hour = 0;
		int minute = 0;
		int second = 0;
		int millisecond = 0;
		
		if(packet.getType() == PacketType.DATA){
			hour = Integer.valueOf(packetStringArray[4]) - 20;
			minute = Integer.valueOf(packetStringArray[3]);
			second = Integer.valueOf(packetStringArray[2]);
			millisecond = Integer.valueOf(packetStringArray[1]);
			
			String timeString = String.format("%02d:%02d:%02d:%02d0", hour, minute, second, millisecond);
			
			DataWriter.storeData(timeString);
			
		}else if(packet.getType() == PacketType.START){
			System.out.println("START PACKET");
		}else if(packet.getType() == PacketType.STOP){
			System.out.println("STOP PACKET");
		}
				
		return true;
	}
	
	public boolean handlePacketByte(byte packetByte){
		
		//HAVEN'T RECEIVED BYTE IN 100MS
		if(timeout.get()){
			
			if(packetByte == 1){
				this.currentPacket = new Packet(PacketType.DATA);
				this.currentPacket.addByte(packetByte);
			}else if((packetByte & 0x0F) == 0){
				this.currentPacket = new Packet(PacketType.START);
				this.currentPacket.addByte(packetByte);
			}else if((packetByte & 0x0F) == 5){
				this.currentPacket = new Packet(PacketType.STOP);
				this.currentPacket.addByte(packetByte);
			}else {
				//System.out.println(this.currentPacket);
				System.out.println("ERROR: RECEIVED " + packetByte + " AFTER MORE THAN 50ms IDLE!!");
			}
			
			timeout.set(false);
		
		//HAVE RECEIVED ANOTHER BYTE IN THE LAST 50MS
		}else{
			
			if(!this.currentPacket.isHandled()){
				this.currentPacket.addByte(packetByte);
			}else{
				
				if(packetByte == 0){
					this.currentPacket = new Packet(PacketType.DATA);
					this.currentPacket.addByte(packetByte);
				}else if((packetByte & 0x0F) == 0){
					this.currentPacket = new Packet(PacketType.START);
					this.currentPacket.addByte(packetByte);
				}else if((packetByte & 0x0F) == 5){
					this.currentPacket = new Packet(PacketType.STOP);
					this.currentPacket.addByte(packetByte);
				}else{
					System.out.println("ERROR: RECEIVED " + packetByte + " WHEN EXPECTING A NEW PACKET TO START!!");
				}
			}
			
			brt.interrupt();
		}
		
		if(this.currentPacket.isReady()){
			Logger.logEvent(this.currentPacket.toString());
			handlePacket(this.currentPacket);
			this.currentPacket.setAsHandled();
		}
		
		return true;
	}
	
	private class ByteReceptionTimer extends Thread{
		
		@Override
		public void run(){
			while(true){
				if(!timeout.get()){
					try {
						Thread.sleep(BYTE_TIMEOUT);
						currentPacket = new Packet(null);
						timeout.set(true);
					} catch (InterruptedException e) { }
				}
			}
		}		
	}
	
	private class Packet {
		
		private final int DATA_SIZE = 10;
		private final int START_STOP_SIZE = 5;
		
		private PacketType packetType;		
		private ArrayList<Integer> byteList;
		private boolean isHandled;
		
		public Packet(PacketType packetType){
			this.packetType = packetType;
			byteList = new ArrayList<Integer>();
			this.isHandled = false;
		}
		
		public void addByte(byte value){
			this.byteList.add((Integer)(int)value);
		}

		public boolean isReady(){
	
			if(this.packetType == PacketType.DATA){
				if(this.byteList.size() >= DATA_SIZE)
					return true;
				else
					return false;
			}else{
				if(this.byteList.size() >= START_STOP_SIZE)
					return true;
				else
					return false;
			}
		}
		
		public void setAsHandled(){
			this.isHandled = true;
		}
		
		public boolean isHandled(){
			return this.isHandled;
		}
		
		public PacketType getType(){
			return this.packetType;
		}
		
		@Override
		public String toString(){
			String returnString = "";
			
			for(Integer b:this.byteList){
				byte i = (byte)(int) (b & 0x00FF);
				returnString += String.format("%02X ", i) + " "; 
			}
			
			return returnString;
		}
	}
}
