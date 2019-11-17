package node;

public enum Operation {
	ADD("add"), 
	SUB("sub"),
	READ("get");
	
	private final String operationName;
	
	private Operation(String operationName) {
		this.operationName = operationName;
    }
	
	public static boolean contains(String s){
		for(Operation o : Operation.values()){
			if(o.operationName.equals(s))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(){		
		return operationName;
	}
}
