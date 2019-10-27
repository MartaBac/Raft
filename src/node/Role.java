package node;

public enum Role {
	LEADER("Leader"), 
	CANDIDATE("Candidate"),
	FOLLOWER("Follower");
	
	private final String roleName;
	
	private Role(String roleName) {
		this.roleName = roleName;
    }
	
	@Override
	public String toString(){
		
		return roleName;
	}
}
