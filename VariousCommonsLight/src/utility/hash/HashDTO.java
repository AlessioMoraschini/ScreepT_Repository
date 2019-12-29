package utility.hash;

public class HashDTO {

	private byte[] salt;
	private byte[] hashed;
	
	public HashDTO() {
		salt = new byte[0];
		hashed = new byte[0];
	}
	
	public HashDTO(byte[] salt, byte[] hash) {
		this.salt = salt;
		this.hashed = hash;
	}
	
	public byte[] getSalt() {
		return salt;
	}
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
	public byte[] getHashed() {
		return hashed;
	}
	public void setHashed(byte[] hashed) {
		this.hashed = hashed;
	}
}
