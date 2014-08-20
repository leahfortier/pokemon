package map.entity;

import java.util.regex.Matcher;


public class TriggerEntityData extends EntityData{
	private TriggerEntity entity;
	public String trigger;
	
	public TriggerEntityData(String name, String contents){
		super (name,contents);
		
		entity = null;
		
		Matcher m = variablePattern.matcher(contents);
		while(m.find()){
			switch(m.group(1)){
			case "x":
				x = Integer.parseInt(m.group(2));
				break;
			case "y":
				y = Integer.parseInt(m.group(2));
				break;
			case "trigger":
				trigger = m.group(2);
				break;
			}
		}
	}
	
	public TriggerEntityData (String name, String conditionString, String trigger, int x, int y) {
		super (name, conditionString);
		
		this.trigger = trigger;
		this.x = x;
		this.y = y;
	}
	
	public Entity getEntity(){
		if(entity == null){
			entity = new TriggerEntity(x, y, trigger);
		}
		return entity;
	}
	
	@Override
	public String entityDataAsString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Trigger " +name +"{\n");
		
		if(!condition.getOriginalConditionString().equals(""))
			ret.append("\tcondition: " +condition.getOriginalConditionString()+"\n");
		
		ret.append("\tx: " +x +"\n");
		ret.append("\ty: " +y +"\n");
		ret.append("\ttrigger: "+ trigger+"\n");
		ret.append("}\n");
		return ret.toString();
	}
}
